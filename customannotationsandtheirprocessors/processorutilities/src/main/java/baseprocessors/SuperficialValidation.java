package baseprocessors;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.AbstractElementVisitor9;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import javax.lang.model.util.SimpleTypeVisitor9;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * A utility class that traverses {@link Element} instances and ensures that all type information
 * is present and resolvable.
 */
public final class SuperficialValidation {

  private SuperficialValidation(){}

  /* ********************************************************************* */
  /* Element Validators ************************************************** */
  /* ********************************************************************* */

  /**
   * Returns {@code true} iff all the given elements return true from {@link #validateElement(Element)}.
   *
   * @param elements elements to be superficially validated
   * @return {@code true} iff all the given elements return true from {@link #validateElement(Element)}
   */
  public static boolean validateElements(Iterable<? extends Element> elements) { //TODO maybe merge the singular and plural methods into one later
    return StreamSupport.stream(elements.spliterator(), false)
        .allMatch(SuperficialValidation::validateElement);
  }

  /**
   * Returns {@code true} iff all types referenced by the given element are defined. The exact meaning of
   * this depends on the kind of element. For packages, it means that all annotations on the package
   * are fully defined. For other element kinds, it means that types referenced by the element,
   * anything it contains, and any of its annotations' element are all defined.
   *
   * @param element element to be validated
   * @return {@code true} iff all types referenced by the given element are defined
   */
  public static boolean validateElement(Element element) {
    return element.accept(ELEMENT_VALIDATING_VISITOR, null);
  }

  private static final ElementVisitor<Boolean, Void> ELEMENT_VALIDATING_VISITOR =
      new AbstractElementVisitor9<>() {

        private boolean isValidBaseElement(Element e) {
          return validateType(e.asType())
              && validateAnnotations(e.getAnnotationMirrors())
              && validateElements(e.getEnclosedElements());
        }

        @Override public Boolean visitModule(ModuleElement t, Void unused) {
          return visitUnknown(t, unused); //Ignore Modules
        } //TODO

        @Override
        public Boolean visitPackage(PackageElement e, Void p) {
          // does not validate enclosed elements because it will return types in the package
          return validateAnnotations(e.getAnnotationMirrors());
        }

        @Override
        public Boolean visitType(TypeElement e, Void p) {
          return isValidBaseElement(e)
              && validateElements(e.getTypeParameters())
              && validateTypes(e.getInterfaces())
              && validateType(e.getSuperclass());
        }

        @Override
        public Boolean visitVariable(VariableElement e, Void p) {
          return isValidBaseElement(e);
        }

        @Override
        public Boolean visitExecutable(ExecutableElement e, Void p) {
          AnnotationValue defaultValue = e.getDefaultValue();
          return isValidBaseElement(e)
              && (defaultValue == null || validateAnnotationValue(defaultValue, e.getReturnType()))
              && validateType(e.getReturnType())
              && validateTypes(e.getThrownTypes())
              && validateElements(e.getTypeParameters())
              && validateElements(e.getParameters());
        }

        @Override
        public Boolean visitTypeParameter(TypeParameterElement e, Void p) {
          return isValidBaseElement(e) && validateTypes(e.getBounds());
        }

        @Override
        public Boolean visitUnknown(Element e, Void p) {
          // just assume that unknown elements are OK
          return true;
        }
      };

  /* ********************************************************************* */
  /* Type Validators ***************************************************** */
  /* ********************************************************************* */

  /**
   * Returns {@code true} iff all the given type-mirrors return true from {@link #validateType(TypeMirror)}.
   *
   * @param types type-mirrors to be superficially validated
   * @return {@code true} iff all the given type-mirrors return true from {@link #validateType(TypeMirror)}
   */
  public static boolean validateTypes(Iterable<? extends TypeMirror> types) {
    for (TypeMirror type : types)
      if (!validateType(type))
        return false;
    return true;
  }

  /**
   * Returns {@code true} iff the given type is fully defined. This means that the type itself is
   * defined, as are any types it references, such as any type arguments or type bounds. For an
   * {@link ExecutableType}, the parameter and return types must be fully defined, as must types
   * declared in a {@code throws} clause or in the bounds of any type parameters.
   *
   * @param type the {@linkplain TypeMirror} whose definition is to be validated
   * @return {@code true} iff the given {@linkplain TypeMirror} is fully defined
   */
  public static boolean validateType(TypeMirror type) {
    return type.accept(TYPE_VALIDATING_VISITOR, null);
  }

  /*
   * This visitor does not test type variables specifically, but it seems that that is not actually
   * an issue.  Javac turns the whole type parameter into an error type if it can't figure out the
   * bounds.
   */
  private static final TypeVisitor<Boolean, Void> TYPE_VALIDATING_VISITOR =
      new SimpleTypeVisitor9<>() {
        @Override
        protected Boolean defaultAction(TypeMirror t, Void p) {
          return true;
        }

        @Override
        public Boolean visitArray(ArrayType t, Void p) {
          return validateType(t.getComponentType());
        }

        @Override
        public Boolean visitDeclared(DeclaredType t, Void p) {
          return validateTypes(t.getTypeArguments());
        }

        @Override
        public Boolean visitError(ErrorType t, Void p) {
          return false;
        }

        @Override
        public Boolean visitUnknown(TypeMirror t, Void p) {
          // just make the default choice for unknown types
          return defaultAction(t, p);
        }

        @Override
        public Boolean visitWildcard(WildcardType t, Void p) {
          TypeMirror extendsBound = t.getExtendsBound();
          TypeMirror superBound = t.getSuperBound();
          return (extendsBound == null || validateType(extendsBound))
              && (superBound == null || validateType(superBound));
        }

        @Override
        public Boolean visitExecutable(ExecutableType t, Void p) {
          return validateTypes(t.getParameterTypes())
              && validateType(t.getReturnType())
              && validateTypes(t.getThrownTypes())
              && validateTypes(t.getTypeVariables());
        }
      };

  /* ********************************************************************* */
  /* Annotation Validators *********************************************** */
  /* ********************************************************************* */

  /**
   * Returns {@code true} iff all the given annotation-mirrors return true
   * from {@link #validateAnnotation(AnnotationMirror)}.
   *
   * @param annotationMirrors annotation-mirrors to be superficially validated
   * @return {@code true} iff all the given annotation-mirrors return true from {@link #validateAnnotation(AnnotationMirror)}
   */
  public static boolean validateAnnotations(Iterable<? extends AnnotationMirror> annotationMirrors) {
    for (AnnotationMirror annotationMirror : annotationMirrors)
      if (!validateAnnotation(annotationMirror))
        return false;
    return true;
  }

  /**
   * Returns {@code true} iff the given annotationMirror is fully defined. This means that the type
   * of the annotation itself, as well as any types references, such as any type arguments, type bounds
   * the type of its elements, and the types of the values passed to these elements are all fully defined.
   *
   * @param annotationMirror the {@linkplain AnnotationMirror} whose definition is to be validated
   * @return {@code true} iff the given {@linkplain AnnotationMirror} is fully defined
   */
  public static boolean validateAnnotation(AnnotationMirror annotationMirror) {
    return validateType(annotationMirror.getAnnotationType())
        && validateAnnotationValues(annotationMirror.getElementValues());
  }

  private static boolean validateAnnotationValues(Map<? extends ExecutableElement, ? extends AnnotationValue> valueMap) {
    return valueMap.entrySet().stream()
        .allMatch(
            valueEntry -> {
              TypeMirror expectedType = valueEntry.getKey().getReturnType();
              return validateAnnotationValue(valueEntry.getValue(), expectedType);
            }
            );
  }

  private static boolean validateAnnotationValue(AnnotationValue annotationValue, TypeMirror expectedType) {
    return annotationValue.accept(VALUE_VALIDATING_VISITOR, expectedType);
  }

  private static final AnnotationValueVisitor<Boolean, TypeMirror> VALUE_VALIDATING_VISITOR =
      new SimpleAnnotationValueVisitor9<>() {
        @Override
        protected Boolean defaultAction(Object o, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(o.getClass(), expectedType); //TODO since we know it is AnnotationValue not better to just do getValue()?? WHY NOT USE TYPEELEMENT INSTEAD
        }

        @Override
        public Boolean visitUnknown(AnnotationValue av, TypeMirror expectedType) {
          // just take the default action for the unknown
          return defaultAction(av, expectedType);
        }

        @Override
        public Boolean visitAnnotation(AnnotationMirror a, TypeMirror expectedType) {
          return MoreTypes.getTypeEquivalence().equivalent(a.getAnnotationType(), expectedType)
              && validateAnnotation(a);
        }

        @Override
        public Boolean visitArray(List<? extends AnnotationValue> values, TypeMirror expectedType) {
          if (expectedType.getKind() != TypeKind.ARRAY)
            return false;

          TypeMirror componentType = MoreTypes.asArray(expectedType).getComponentType();
          return values.stream().allMatch(value -> value.accept(this, componentType));
        }

        @Override
        public Boolean visitEnumConstant(VariableElement enumConstant, TypeMirror expectedType) {
          return MoreTypes.getTypeEquivalence().equivalent(enumConstant.asType(), expectedType)
              && validateElement(enumConstant);
        }

        @Override
        public Boolean visitType(TypeMirror type, TypeMirror ignored) {
          // We could check assignability here, but would require a Types instance. Since this
          // isn't really the sort of thing that shows up in a bad AST from upstream compilation
          // we ignore the expected type and just validate the type.  It might be wrong, but
          // it's valid.
          return validateType(type);
        }

        @Override
        public Boolean visitBoolean(boolean b, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Boolean.TYPE, expectedType);
        }

        @Override
        public Boolean visitByte(byte b, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Byte.TYPE, expectedType);
        }

        @Override
        public Boolean visitChar(char c, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Character.TYPE, expectedType);
        }

        @Override
        public Boolean visitDouble(double d, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Double.TYPE, expectedType);
        }

        @Override
        public Boolean visitFloat(float f, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Float.TYPE, expectedType);
        }

        @Override
        public Boolean visitInt(int i, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Integer.TYPE, expectedType);
        }

        @Override
        public Boolean visitLong(long l, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Long.TYPE, expectedType);
        }

        @Override
        public Boolean visitShort(short s, TypeMirror expectedType) {
          return MoreTypes.isTypeOf(Short.TYPE, expectedType);
        }
      };

}
