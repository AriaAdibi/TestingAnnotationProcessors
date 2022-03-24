package baseprocessors;

import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor9;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Utilities related to {@link TypeMirror} instances.
 */
public final class MoreTypes {

  private MoreTypes(){}

  /**
   * Returns an {@link Equivalence} that can be used to compare types. The standard way to compare
   * types is {@link javax.lang.model.util.Types#isSameType Types.isSameType}, but this alternative
   * may be preferred in a number of cases:
   *
   * <ul>
   *    <li>If you don't have an instance of {@code Types}.
   *    <li>If you want a reliable {@code hashCode()} for the types, for example to construct a set
   *        of types using {@link java.util.HashSet} with {@link Equivalence#wrap(Object)}.
   *    <li>If you want distinct type variables to be considered equal if they have the same names
   *        and bounds.
   *    <li>If you want wildcard types to compare equal if they have the same bounds. {@code
   *        Types.isSameType} never considers wildcards equal, even when comparing a type to itself.
   * </ul>
   */
  public static Equivalence<TypeMirror> equivalence() {
    return TypeEquivalence.INSTANCE;
  }

  private static final class TypeEquivalence extends Equivalence<TypeMirror> {
    private static final TypeEquivalence INSTANCE = new TypeEquivalence();

    @Override
    protected boolean doEquivalent(@NonNull TypeMirror a, @NonNull TypeMirror b) {
      return MoreTypes.equal(a, b, ImmutableSet.of());
    }

    @Override
    protected int doHash(@SuppressWarnings("NullableProblems") TypeMirror t) {
      return MoreTypes.hash(t, ImmutableSet.of());
    }

    @Override
    public String toString() {
      return "MoreTypes.equivalence()";
    }
  }

  /* doHash() */

  //TODO rename visiting to visited?

  private static int hashList(List<? extends TypeMirror> mirrors, Set<Element> visiting) {
    int result = HASH_SEED;
    for (TypeMirror mirror : mirrors) {
      result *= HASH_MULTIPLIER;
      result += hash(mirror, visiting);
    }
    return result;
  }

  private static int hash(TypeMirror mirror, Set<Element> visiting) {
    return mirror == null ? 0 : mirror.accept(HashVisitor.INSTANCE, visiting);
  }

  private static final int HASH_SEED = 17;
  private static final int HASH_MULTIPLIER = 31;

  private static final class HashVisitor extends SimpleTypeVisitor9<Integer, Set<Element>> {
    private static final HashVisitor INSTANCE = new HashVisitor();

    int hashKind(@SuppressWarnings("SameParameterValue") int seed, TypeMirror t) {
      return seed * HASH_MULTIPLIER + t.getKind().hashCode();
    }

    @Override
    protected Integer defaultAction(TypeMirror e, Set<Element> visiting) {
      return hashKind(HASH_SEED, e);
    }

    @Override
    public Integer visitArray(ArrayType t, Set<Element> visiting) {
      int result = hashKind(HASH_SEED, t);
      result *= HASH_MULTIPLIER;
      result += t.getComponentType().accept(this, visiting);
      return result;
    }

    @Override
    public Integer visitDeclared(DeclaredType t, Set<Element> visiting) {
      Element element = t.asElement();
      if (visiting.contains(element))
        return 0;

      Set<Element> newVisiting = new HashSet<>(visiting);
      newVisiting.add(element);
      int result = hashKind(HASH_SEED, t);
      result *= HASH_MULTIPLIER;
      result += t.asElement().hashCode();
      result *= HASH_MULTIPLIER;
      result += t.getEnclosingType().accept(this, newVisiting);
      result *= HASH_MULTIPLIER;
      result += hashList(t.getTypeArguments(), newVisiting);
      return result;
    }

    @Override
    public Integer visitExecutable(ExecutableType t, Set<Element> visiting) {
      int result = hashKind(HASH_SEED, t);
      result *= HASH_MULTIPLIER;
      result += hashList(t.getParameterTypes(), visiting);
      result *= HASH_MULTIPLIER;
      result += t.getReturnType().accept(this, visiting);
      result *= HASH_MULTIPLIER;
      result += hashList(t.getThrownTypes(), visiting);
      result *= HASH_MULTIPLIER;
      result += hashList(t.getTypeVariables(), visiting);
      return result;
    }

    @Override
    public Integer visitTypeVariable(TypeVariable t, Set<Element> visiting) {
      int result = hashKind(HASH_SEED, t);
      result *= HASH_MULTIPLIER;
      result += t.getLowerBound().accept(this, visiting);
      TypeParameterElement element = (TypeParameterElement) t.asElement();
      for (TypeMirror bound : element.getBounds()) {
        result *= HASH_MULTIPLIER;
        result += bound.accept(this, visiting);
      }
      return result;
    }

    @Override
    public Integer visitWildcard(WildcardType t, Set<Element> visiting) {
      int result = hashKind(HASH_SEED, t);
      result *= HASH_MULTIPLIER;
      result += (t.getExtendsBound() == null) ? 0 : t.getExtendsBound().accept(this, visiting);
      result *= HASH_MULTIPLIER;
      result += (t.getSuperBound() == null) ? 0 : t.getSuperBound().accept(this, visiting);
      return result;
    }

    @Override
    public Integer visitUnknown(TypeMirror t, Set<Element> visiting) {
      throw new UnsupportedOperationException();
    }
  }

  /* doEquivalence() */

  private static boolean equalLists(List<? extends TypeMirror> a, List<? extends TypeMirror> b, Set<ComparedElements> visiting) {
    int size = a.size(); //TODO cleanup
    if (size != b.size()) {
      return false;
    }

    // Use iterators in case the Lists aren't RandomAccess
    Iterator<? extends TypeMirror> aIterator = a.iterator();
    Iterator<? extends TypeMirror> bIterator = b.iterator();
    while (aIterator.hasNext()) { //TODO change to foreach
      // We checked that the lists have the same size, so we know that bIterator.hasNext() too.
      TypeMirror nextMirrorA = aIterator.next();
      TypeMirror nextMirrorB = bIterator.next();
      if ( !equal(nextMirrorA, nextMirrorB, visiting) )
        return false;
    }

    return true;
  }

  private static boolean equal(TypeMirror a, TypeMirror b, Set<ComparedElements> visiting) {
    if (a == b)
      return true;
    if (a == null || b == null)
      return false;
    // TypeMirror.equals is not guaranteed to return true for types that are equal, but we can
    // assume that if it does return true then the types are equal. This check also avoids getting
    // stuck in infinite recursion when Eclipse decrees that the upper bound of the second K in
    // <K extends Comparable<K>> is a distinct but equal K.
    // The javac implementation of ExecutableType, at least in some versions, does not take thrown
    // exceptions into account in its equals' implementation, so avoid this optimization for
    // ExecutableType.
//    @SuppressWarnings("TypesEquals")
    boolean equal = a.equals(b);
    if (equal && !(a instanceof ExecutableType)) //TODO Change instanceof, change to a.getKind() == TypeKind.Executable
      return true;

    EqualVisitorParam p = new EqualVisitorParam();
    p.type = b;   p.visiting = visiting;
    return a.accept(EqualVisitor.INSTANCE, p);
  }

  // So EQUAL_VISITOR can be a singleton, we maintain visiting state, in particular which types
  // have been seen already, in this object.
  // The logic for handling recursive types like Comparable<T extends Comparable<T>> is very tricky.
  // If we're not careful we'll end up with an infinite recursion. So we record the types that
  // we've already seen during the recursion, and if we see the same pair of types again we just
  // return true provisionally. But "the same pair of types" is itself poorly-defined. We can't
  // just say that it is an equal pair of TypeMirrors, because of course if we knew how to
  // determine that then we wouldn't need the complicated type visitor at all. On the other hand,
  // we can't say that it is an identical pair of TypeMirrors either, because according to the java
  // document there's no guarantee that the TypeMirrors for the two T's in Comparable<T extends Comparable<T>>
  // will be represented by the same object, and indeed with the Eclipse compiler they aren't. We could
  // compare the corresponding Elements, since equality is well-defined there, but that's not enough
  // either, because the Element for Set<Object> is the same as the one for Set<String>. So we
  // approximate by comparing the Elements and, if there are any type arguments, requiring them to
  // be identical. This may not be foolproof either, but it is sufficient for all the cases we've
  // encountered so far.
  private static final class EqualVisitorParam {
    TypeMirror type;
    Set<ComparedElements> visiting;
  }

  private static class ComparedElements {
    final Element a;
    final ImmutableList<TypeMirror> aArguments;
    final Element b;
    final ImmutableList<TypeMirror> bArguments;

    ComparedElements(
        Element a, ImmutableList<TypeMirror> aArguments,
        Element b, ImmutableList<TypeMirror> bArguments) {
      this.a = a; this.aArguments = aArguments;
      this.b = b; this.bArguments = bArguments;
    }

    protected boolean canEqual(Object o) {
      return o instanceof ComparedElements;
    }

    @Override
    public boolean equals(Object o) {
      if ( !(o instanceof ComparedElements) ) return false;
      ComparedElements that = (ComparedElements) o;
      if( !that.canEqual(this) ) return false;

      int nArguments = aArguments.size();
      // The arguments must be the same size, but we check anyway.
      if (!this.a.equals(that.a) || !this.b.equals(that.b) || nArguments != bArguments.size())
        return false;

      for (int i = 0; i < nArguments; i++)
        if (aArguments.get(i) != bArguments.get(i)) //TODO shouldn't this be Types.isSameType()?, and why no "that"
          return false;

      //TODO NO CHECK FOR that??
      return true;
    }

    @Override
    public int hashCode() {
      return a.hashCode() * 31 + b.hashCode();
    }
  }

  /**
   * Returns the type of the innermost enclosing instance, or null if there is none. This is the
   * same as {@link DeclaredType#getEnclosingType()} except that it returns null rather than
   * NoType for a static type. We need this because of
   * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=508222">this bug</a> whereby
   * the Eclipse compiler returns a value for static classes that is not NoType.
   */
  private static TypeMirror enclosingType(DeclaredType t) {
    TypeMirror enclosing = t.getEnclosingType();
    if ( enclosing.getKind() == TypeKind.NONE || t.asElement().getModifiers().contains(Modifier.STATIC) )
      return null;

    return enclosing;
  }

  private static final class EqualVisitor extends SimpleTypeVisitor9<Boolean, EqualVisitorParam> {
    private static final EqualVisitor INSTANCE = new EqualVisitor();

    private Set<ComparedElements> visitingSetPlus(Set<ComparedElements> visiting, Element a, Element b) {
      ImmutableList<TypeMirror> noArguments = ImmutableList.of();
      return visitingSetPlus(visiting, a, noArguments, b, noArguments);
    }

    private Set<ComparedElements> visitingSetPlus(
        Set<ComparedElements> visiting,
        Element a,
        List<? extends TypeMirror> aArguments,
        Element b,
        List<? extends TypeMirror> bArguments) {
      ComparedElements comparedElements =
          new ComparedElements(
              a, ImmutableList.copyOf(aArguments),
              b, ImmutableList.copyOf(bArguments));
      Set<ComparedElements> newVisiting = new HashSet<>(visiting);
      newVisiting.add(comparedElements);
      return newVisiting;
    }

    @Override
    protected Boolean defaultAction(TypeMirror a, EqualVisitorParam p) {
      return a.getKind().equals(p.type.getKind());
    }

    @Override
    public Boolean visitArray(ArrayType a, EqualVisitorParam p) {
      if (p.type.getKind() != TypeKind.ARRAY)
        return false;

      ArrayType b = (ArrayType) p.type;
      return equal(a.getComponentType(), b.getComponentType(), p.visiting);
    }

    @Override
    public Boolean visitDeclared(DeclaredType a, EqualVisitorParam p) {
      if (p.type.getKind() != TypeKind.DECLARED)
        return false;

      DeclaredType b = (DeclaredType) p.type;
      Element aElement = a.asElement();
      Element bElement = b.asElement();
      Set<ComparedElements> newVisiting = visitingSetPlus(p.visiting, aElement, a.getTypeArguments(), bElement, b.getTypeArguments());
      if (newVisiting.equals(p.visiting)) {
        // We're already visiting this pair of elements. //TODO grammar change
        // This can happen for example with Enum in Enum<E extends Enum<E>>. Return a
        // provisional true value since if the Elements are not in fact equal the original
        // visitor of Enum will discover that. We have to check both Elements being compared
        // though to avoid missing the fact that one of the types being compared
        // differs at exactly this point.
        return true;
      }
      return aElement.equals(bElement)
          && equal(enclosingType(a), enclosingType(b), newVisiting)
          && equalLists(a.getTypeArguments(), b.getTypeArguments(), newVisiting);
    }

    @Override
    public Boolean visitError(ErrorType a, EqualVisitorParam p) {
      return a.equals(p.type); //TODO No getKind()?
    }

    @Override
    public Boolean visitExecutable(ExecutableType a, EqualVisitorParam p) {
      if ( p.type.getKind() != TypeKind.EXECUTABLE )
        return false;

      ExecutableType b = (ExecutableType) p.type;
      return equalLists(a.getParameterTypes(), b.getParameterTypes(), p.visiting)
          && equal(a.getReturnType(), b.getReturnType(), p.visiting)
          && equalLists(a.getThrownTypes(), b.getThrownTypes(), p.visiting)
          && equalLists(a.getTypeVariables(), b.getTypeVariables(), p.visiting);
    }

    @Override
    public Boolean visitIntersection(IntersectionType a, EqualVisitorParam p) {
      if (p.type.getKind() != TypeKind.INTERSECTION)
        return false;

      IntersectionType b = (IntersectionType) p.type;
      return equalLists(a.getBounds(), b.getBounds(), p.visiting);
    }

    @Override
    public Boolean visitTypeVariable(TypeVariable a, EqualVisitorParam p) {
      if (p.type.getKind() != TypeKind.TYPEVAR)
        return false;

      TypeVariable b = (TypeVariable) p.type;
      TypeParameterElement aElement = (TypeParameterElement) a.asElement(); //TODO use the visitors for the castings?
      TypeParameterElement bElement = (TypeParameterElement) b.asElement();
      Set<ComparedElements> newVisiting = visitingSetPlus(p.visiting, aElement, bElement);
      if (newVisiting.equals(p.visiting)) {
        // We're already visiting this pair of elements. //TODO grammar change
        // This can happen with our friend Eclipse when looking at <T extends Comparable<T>>.
        // It incorrectly reports the upper bound of T as T itself.
        return true;
      }
      // We use aElement.getBounds() instead of a.getUpperBound() to avoid having to deal with
      // the different way intersection types (like <T extends Number & Comparable<T>>) are
      // represented before and after Java 8. We do have an issue that this code may consider
      // that <T extends Foo & Bar> is different from <T extends Bar & Foo>, but it's very
      // hard to avoid that, and not likely to be much of a problem in practice.
      return equalLists(aElement.getBounds(), bElement.getBounds(), newVisiting)
          && equal(a.getLowerBound(), b.getLowerBound(), newVisiting)
          && a.asElement().getSimpleName().equals(b.asElement().getSimpleName());
    }

    @Override
    public Boolean visitWildcard(WildcardType a, EqualVisitorParam p) {
      if (p.type.getKind() != TypeKind.WILDCARD)
        return false;

      WildcardType b = (WildcardType) p.type;
      return equal(a.getExtendsBound(), b.getExtendsBound(), p.visiting)
          && equal(a.getSuperBound(), b.getSuperBound(), p.visiting);
    }

    @Override
    public Boolean visitUnknown(TypeMirror a, EqualVisitorParam p) {
      throw new UnsupportedOperationException();
    }

  }

  /* ********************************************************************* */
  /* Type/Presence Check ************************************************* */
  /* ********************************************************************* */

  /**
   * Returns true if the raw type underlying the given {@link TypeMirror} represents the same raw
   * type as the given {@link Class} and throws an {@link IllegalArgumentException} if the {@link
   * TypeMirror} does not represent a type that can be referenced by a {@link Class}
   */
  public static boolean isTypeOf(@NonNull final Class<?> clazz, TypeMirror type) {
    return type.accept(new IsTypeOf(clazz), null);
  } //TODO Why not send the class as argument

  private static final class IsTypeOf extends SimpleTypeVisitor9<Boolean, Void> {
    private final Class<?> clazz; //TODO Why class, why not typeMirror

    IsTypeOf(Class<?> clazz) {
      this.clazz = clazz;
    }

    @Override
    protected Boolean defaultAction(TypeMirror type, Void ignored) {
      throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
    }

    @Override
    public Boolean visitNoType(NoType noType, Void p) {
      if (noType.getKind() == TypeKind.VOID)
        return clazz.equals(Void.TYPE);

      throw new IllegalArgumentException(noType + " cannot be represented as a Class<?>.");
    }

    @Override
    public Boolean visitError(ErrorType errorType, Void p) {
      return false;
    }

    @Override
    public Boolean visitPrimitive(PrimitiveType type, Void p) {
      switch (type.getKind()) {
        case BOOLEAN:
          return clazz.equals(Boolean.TYPE);
        case BYTE:
          return clazz.equals(Byte.TYPE);
        case CHAR:
          return clazz.equals(Character.TYPE);
        case DOUBLE:
          return clazz.equals(Double.TYPE);
        case FLOAT:
          return clazz.equals(Float.TYPE);
        case INT:
          return clazz.equals(Integer.TYPE);
        case LONG:
          return clazz.equals(Long.TYPE);
        case SHORT:
          return clazz.equals(Short.TYPE);
        default:
          throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
      }
    }

    @Override
    public Boolean visitArray(ArrayType array, Void p) {
      return clazz.isArray() && isTypeOf(clazz.getComponentType(), array.getComponentType());
    }

    @Override
    public Boolean visitDeclared(DeclaredType type, Void ignored) {
      TypeElement typeElement = MoreElements.asTypeElement(type.asElement());
      return typeElement.getQualifiedName().contentEquals(clazz.getCanonicalName());
    }
  }

  /* Used only in testing */

  /**
   * Returns true if the raw type underlying the given {@link TypeMirror} represents a type that can
   * be referenced by a {@link Class}. If this returns true, then {@link #isTypeOf} is guaranteed to
   * not throw.
   */
  public static boolean isClassType(TypeMirror type) {
    return type.accept(IsClassTypeVisitor.INSTANCE, null);
  }

  private static final class IsClassTypeVisitor extends SimpleTypeVisitor9<Boolean, Void> {
    private static final IsClassTypeVisitor INSTANCE = new IsClassTypeVisitor();

    @Override
    protected Boolean defaultAction(TypeMirror type, Void ignored) {
      return false;
    }

    @Override
    public Boolean visitNoType(NoType noType, Void p) {
      return noType.getKind() == TypeKind.VOID;
    }

    @Override
    public Boolean visitPrimitive(PrimitiveType type, Void p) {
      return true;
    }

    @Override
    public Boolean visitArray(ArrayType array, Void p) {
      return true;
    }

    @Override
    public Boolean visitDeclared(DeclaredType type, Void ignored) {
      return MoreElements.isTypeElement(type.asElement());
    }

  }

  /**
   * Returns the set of {@linkplain TypeElement types} that are referenced by the given {@link
   * TypeMirror}.
   */
  public static ImmutableSet<TypeElement> referencedTypeElements(@NonNull TypeMirror type) {
    ImmutableSet.Builder<TypeElement> elements = ImmutableSet.builder();
    type.accept(ReferencedTypeElementsVisitor.INSTANCE, elements);
    return elements.build();
  }

  private static final class ReferencedTypeElementsVisitor
      extends SimpleTypeVisitor9<Void, ImmutableSet.Builder<TypeElement>> {
    private static final ReferencedTypeElementsVisitor INSTANCE = new ReferencedTypeElementsVisitor();

    @Override
    public Void visitArray(ArrayType t, ImmutableSet.Builder<TypeElement> p) {
      t.getComponentType().accept(this, p);
      return null;
    }

    @Override
    public Void visitDeclared(DeclaredType t, ImmutableSet.Builder<TypeElement> p) {
      p.add(MoreElements.asTypeElement(t.asElement()));
      for (TypeMirror typeArgument : t.getTypeArguments()) {
        typeArgument.accept(this, p);
      }
      return null;
    }

    @Override
    public Void visitTypeVariable(TypeVariable t, ImmutableSet.Builder<TypeElement> p) {
      t.getLowerBound().accept(this, p);
      t.getUpperBound().accept(this, p);
      return null;
    }

    @Override
    public Void visitWildcard(WildcardType t, ImmutableSet.Builder<TypeElement> p) {
      TypeMirror extendsBound = t.getExtendsBound();
      if (extendsBound != null) {
        extendsBound.accept(this, p);
      }
      TypeMirror superBound = t.getSuperBound();
      if (superBound != null) {
        superBound.accept(this, p);
      }
      return null;
    }
  }

  /* ********************************************************************* */
  /* Castings and Conversions ******************************************** */
  /* ********************************************************************* */

  /* Castings */

  /**
   * Returns a {@link PrimitiveType} if the {@link TypeMirror} represents a primitive type or throws
   * an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static PrimitiveType asPrimitiveType(TypeMirror maybePrimitiveType) {
    return maybePrimitiveType.accept(CastingToPrimitiveTypeVisitor.INSTANCE, null);
  }

  /**
   * Returns a {@link ArrayType} if the {@link TypeMirror} represents an array or throws an {@link
   * IllegalArgumentException}.
   */
  public static ArrayType asArray(TypeMirror maybeArrayType) {
    return maybeArrayType.accept(CastingToArrayTypeVisitor.INSTANCE, null);
  }

  /**
   * Returns a {@link DeclaredType} if the {@link TypeMirror} represents a declared type such as a
   * class, interface, union/compound, or enum or throws an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static DeclaredType asDeclared(TypeMirror maybeDeclaredType) {
    return maybeDeclaredType.accept(CastingToDeclaredTypeVisitor.INSTANCE, null);
  }

  /**
   * Returns a {@link ExecutableType} if the {@link TypeMirror} represents an executable type such
   * as a method, constructor, or initializer or throws an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static ExecutableType asExecutable(TypeMirror maybeExecutableType) {
    return maybeExecutableType.accept(CastingToExecutableTypeVisitor.INSTANCE, null);
  }

  /**
   * Returns an {@link IntersectionType} if the {@link TypeMirror} represents an intersection-type
   * or throws an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static IntersectionType asIntersection(TypeMirror maybeIntersectionType) {
    return maybeIntersectionType.accept(CastingToIntersectionTypeVisitor.INSTANCE, null);
  }

  /**
   * Returns a {@link NoType} if the {@link TypeMirror} represents a non-type such as void, or
   * package, etc. or throws an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static NoType asNoType(TypeMirror maybeNoType) {
    return maybeNoType.accept(CastingToNoTypeVisitor.INSTANCE, null);
  }

  /**
   * Returns a {@link NullType} if the {@link TypeMirror} represents the null type or throws an
   * {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static NullType asNullType(TypeMirror maybeNullType) {
    return maybeNullType.accept(CastingToNullTypeVisitor.INSTANCE, null);
  }

  //
  // visitUnionType would go here, but isn't relevant for annotation processors
  //

  /**
   * Returns a {@link TypeVariable} if the {@link TypeMirror} represents a type variable or throws
   * an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static TypeVariable asTypeVariable(TypeMirror maybeTypeVariable) {
    return maybeTypeVariable.accept(CastingToTypeVariableVisitor.INSTANCE, null);
  }

  /**
   * Returns a {@link WildcardType} if the {@link TypeMirror} represents a wildcard type or throws
   * an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static WildcardType asWildcard(TypeMirror maybeWildcardType) {
    return maybeWildcardType.accept(CastingToWildcardTypeVisitor.INSTANCE, null);
  }

  /**
   * Returns a {@link ExecutableType} if the {@link TypeMirror} represents an executable type such
   * as may result from missing code, or bad compiles or throws an {@link IllegalArgumentException}.
   */
  @SuppressWarnings("unused")
  public static ErrorType asError(TypeMirror maybeErrorType) {
    return maybeErrorType.accept(CastingToErrorTypeVisitor.INSTANCE, null);
  }

  private abstract static class CastingTypeVisitor<T> extends SimpleTypeVisitor9<T, Void> {
    private final String label;

    CastingTypeVisitor(String label) {
      this.label = label;
    }

    @Override
    protected T defaultAction(TypeMirror e, Void v) {
      throw new IllegalArgumentException(e + " does not represent a " + label);
    }
  }

  private static final class CastingToPrimitiveTypeVisitor extends CastingTypeVisitor<PrimitiveType> {
    private static final CastingToPrimitiveTypeVisitor INSTANCE = new CastingToPrimitiveTypeVisitor();

    CastingToPrimitiveTypeVisitor() {
      super("primitive type");
    }

    @Override
    public PrimitiveType visitPrimitive(PrimitiveType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToArrayTypeVisitor extends CastingTypeVisitor<ArrayType> {
    private static final CastingToArrayTypeVisitor INSTANCE = new CastingToArrayTypeVisitor();

    CastingToArrayTypeVisitor() {
      super("array");
    }

    @Override
    public ArrayType visitArray(ArrayType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToDeclaredTypeVisitor extends CastingTypeVisitor<DeclaredType> {
    private static final CastingToDeclaredTypeVisitor INSTANCE = new CastingToDeclaredTypeVisitor();

    CastingToDeclaredTypeVisitor() {
      super("declared type");
    }

    @Override
    public DeclaredType visitDeclared(DeclaredType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToExecutableTypeVisitor extends CastingTypeVisitor<ExecutableType> {
    private static final CastingToExecutableTypeVisitor INSTANCE = new CastingToExecutableTypeVisitor();

    CastingToExecutableTypeVisitor() {
      super("executable type");
    }

    @Override
    public ExecutableType visitExecutable(ExecutableType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToIntersectionTypeVisitor extends CastingTypeVisitor<IntersectionType> {
    private static final CastingToIntersectionTypeVisitor INSTANCE = new CastingToIntersectionTypeVisitor();

    CastingToIntersectionTypeVisitor() {
      super("intersection type");
    }

    @Override
    public IntersectionType visitIntersection(IntersectionType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToNoTypeVisitor extends CastingTypeVisitor<NoType> {
    private static final CastingToNoTypeVisitor INSTANCE = new CastingToNoTypeVisitor();

    CastingToNoTypeVisitor() {
      super("non-type");
    }

    @Override
    public NoType visitNoType(NoType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToNullTypeVisitor extends CastingTypeVisitor<NullType> {
    private static final CastingToNullTypeVisitor INSTANCE = new CastingToNullTypeVisitor();

    CastingToNullTypeVisitor() {
      super("null");
    }

    @Override
    public NullType visitNull(NullType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToTypeVariableVisitor extends CastingTypeVisitor<TypeVariable> {
    private static final CastingToTypeVariableVisitor INSTANCE = new CastingToTypeVariableVisitor();

    CastingToTypeVariableVisitor() {
      super("type variable");
    }

    @Override
    public TypeVariable visitTypeVariable(TypeVariable type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToWildcardTypeVisitor extends CastingTypeVisitor<WildcardType> {
    private static final CastingToWildcardTypeVisitor INSTANCE = new CastingToWildcardTypeVisitor();

    CastingToWildcardTypeVisitor() {
      super("wildcard type");
    }

    @Override
    public WildcardType visitWildcard(WildcardType type, Void ignore) {
      return type;
    }
  }

  private static final class CastingToErrorTypeVisitor extends CastingTypeVisitor<ErrorType> {
    private static final CastingToErrorTypeVisitor INSTANCE = new CastingToErrorTypeVisitor();

    CastingToErrorTypeVisitor() {
      super("error type");
    }

    @Override
    public ErrorType visitError(ErrorType type, Void ignore) {
      return type;
    }
  }

  /* Conversions */

  /**
   * An alternate implementation of {@link Types#asElement} that does not require a {@link Types}
   * instance with the notable difference that it will throw {@link IllegalArgumentException}
   * instead of returning null if the {@link TypeMirror} can not be converted to an {@link Element}.
   *
   * @throws NullPointerException if {@code typeMirror} is {@code null}
   * @throws IllegalArgumentException if {@code typeMirror} cannot be converted to an {@link Element}
   */
  public static Element asElement(TypeMirror typeMirror) {
    return typeMirror.accept(AsElementVisitor.INSTANCE, null);
  }

  private static final class AsElementVisitor extends SimpleTypeVisitor9<Element, Void> {
    private static final AsElementVisitor INSTANCE = new AsElementVisitor();

    @Override
    protected Element defaultAction(TypeMirror e, Void p) {
      throw new IllegalArgumentException(e + " cannot be converted to an Element");
    }

    @Override
    public Element visitDeclared(DeclaredType t, Void p) {
      return t.asElement();
    }

    @Override
    public Element visitError(ErrorType t, Void p) {
      return t.asElement();
    }

    @Override
    public Element visitTypeVariable(TypeVariable t, Void p) {
      return t.asElement();
    }
  }

}