/*
 * Copyright 2014 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package baseprocessors;

import com.google.common.collect.*;
import com.google.common.testing.EquivalenceTester;
import com.google.common.truth.Expect;
import com.google.testing.compile.CompilationRule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;
import static javax.lang.model.type.TypeKind.NONE;
import static javax.lang.model.type.TypeKind.VOID;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

@SuppressWarnings("UnstableApiUsage") //TODO
@RunWith(JUnit4.class)
public class MoreTypesTest {
  @Rule public final CompilationRule compilationRule = new CompilationRule();
  @Rule public final Expect expect = Expect.create();

  @Test
  public void equivalence() {
    Types types = compilationRule.getTypes();
    Elements elements = compilationRule.getElements();
    TypeMirror objectType = elements.getTypeElement(Object.class.getCanonicalName()).asType();
    TypeMirror stringType = elements.getTypeElement(String.class.getCanonicalName()).asType();
    TypeElement mapElement = elements.getTypeElement(Map.class.getCanonicalName());
    TypeElement setElement = elements.getTypeElement(Set.class.getCanonicalName());
    TypeElement enumElement = elements.getTypeElement(Enum.class.getCanonicalName());
    TypeElement container = elements.getTypeElement(Container.class.getCanonicalName());
    TypeElement contained = elements.getTypeElement(Container.Contained.class.getCanonicalName());
    TypeElement funkyBounds = elements.getTypeElement(FunkyBounds.class.getCanonicalName());
    TypeElement funkyBounds2 = elements.getTypeElement(FunkyBounds2.class.getCanonicalName());
    TypeElement funkierBounds = elements.getTypeElement(FunkierBounds.class.getCanonicalName());
    TypeMirror funkyBoundsVar = ((DeclaredType) funkyBounds.asType()).getTypeArguments().get(0);
    TypeMirror funkyBounds2Var = ((DeclaredType) funkyBounds2.asType()).getTypeArguments().get(0);
    TypeMirror funkierBoundsVar = ((DeclaredType) funkierBounds.asType()).getTypeArguments().get(0);
    DeclaredType mapOfObjectToObjectType =
        types.getDeclaredType(mapElement, objectType, objectType);
    TypeMirror mapType = mapElement.asType();
    DeclaredType setOfSetOfObject =
        types.getDeclaredType(setElement, types.getDeclaredType(setElement, objectType));
    DeclaredType setOfSetOfString =
        types.getDeclaredType(setElement, types.getDeclaredType(setElement, stringType));
    DeclaredType setOfSetOfSetOfObject = types.getDeclaredType(setElement, setOfSetOfObject);
    DeclaredType setOfSetOfSetOfString = types.getDeclaredType(setElement, setOfSetOfString);
    WildcardType wildcard = types.getWildcardType(null, null);
    DeclaredType containerOfObject = types.getDeclaredType(container, objectType);
    DeclaredType containerOfString = types.getDeclaredType(container, stringType);
    TypeMirror containedInObject = types.asMemberOf(containerOfObject, contained);
    TypeMirror containedInString = types.asMemberOf(containerOfString, contained);
    EquivalenceTester<TypeMirror> tester =
        EquivalenceTester.<TypeMirror>of(MoreTypes.getTypeEquivalence())
            .addEquivalenceGroup(types.getNullType())
            .addEquivalenceGroup(types.getNoType(NONE))
            .addEquivalenceGroup(types.getNoType(VOID))
            .addEquivalenceGroup(objectType)
            .addEquivalenceGroup(stringType)
            .addEquivalenceGroup(containedInObject)
            .addEquivalenceGroup(containedInString)
            .addEquivalenceGroup(funkyBounds.asType())
            .addEquivalenceGroup(funkyBounds2.asType())
            .addEquivalenceGroup(funkierBounds.asType())
            .addEquivalenceGroup(funkyBoundsVar, funkyBounds2Var)
            .addEquivalenceGroup(funkierBoundsVar)
            // Enum<E extends Enum<E>>
            .addEquivalenceGroup(enumElement.asType())
            // Map<K, V>
            .addEquivalenceGroup(mapType)
            .addEquivalenceGroup(mapOfObjectToObjectType)
            // Map<?, ?>
            .addEquivalenceGroup(types.getDeclaredType(mapElement, wildcard, wildcard))
            // Map
            .addEquivalenceGroup(types.erasure(mapType), types.erasure(mapOfObjectToObjectType))
            .addEquivalenceGroup(types.getDeclaredType(mapElement, objectType, stringType))
            .addEquivalenceGroup(types.getDeclaredType(mapElement, stringType, objectType))
            .addEquivalenceGroup(types.getDeclaredType(mapElement, stringType, stringType))
            .addEquivalenceGroup(setOfSetOfObject)
            .addEquivalenceGroup(setOfSetOfString)
            .addEquivalenceGroup(setOfSetOfSetOfObject)
            .addEquivalenceGroup(setOfSetOfSetOfString)
            .addEquivalenceGroup(wildcard)
            // ? extends Object
            .addEquivalenceGroup(types.getWildcardType(objectType, null))
            // ? extends String
            .addEquivalenceGroup(types.getWildcardType(stringType, null))
            // ? super String
            .addEquivalenceGroup(types.getWildcardType(null, stringType))
            // Map<String, Map<String, Set<Object>>>
            .addEquivalenceGroup(
                types.getDeclaredType(
                    mapElement,
                    stringType,
                    types.getDeclaredType(
                        mapElement, stringType, types.getDeclaredType(setElement, objectType))))
            .addEquivalenceGroup(FAKE_ERROR_TYPE);

    for (TypeKind kind : TypeKind.values()) {
      if (kind.isPrimitive()) {
        PrimitiveType primitiveType = types.getPrimitiveType(kind);
        TypeMirror boxedPrimitiveType = types.boxedClass(primitiveType).asType();
        tester.addEquivalenceGroup(primitiveType, types.unboxedType(boxedPrimitiveType));
        tester.addEquivalenceGroup(boxedPrimitiveType);
        tester.addEquivalenceGroup(types.getArrayType(primitiveType));
        tester.addEquivalenceGroup(types.getArrayType(boxedPrimitiveType));
      }
    }

    ImmutableSet<Class<?>> testClasses =
        ImmutableSet.of(
            ExecutableElementsGroupA.class,
            ExecutableElementsGroupB.class,
            ExecutableElementsGroupC.class,
            ExecutableElementsGroupD.class,
            ExecutableElementsGroupE.class);
    for (Class<?> testClass : testClasses) {
      ImmutableList<TypeMirror> equivalenceGroup =
          FluentIterable.from(
                  elements.getTypeElement(testClass.getCanonicalName()).getEnclosedElements())
              .transform(Element::asType)
              .toList();
      tester.addEquivalenceGroup(equivalenceGroup);
    }

    tester.test();
  }

  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupA {
    ExecutableElementsGroupA() {}

    void a() {}

    public static void b() {}
  }

  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupB {
    ExecutableElementsGroupB(String s) {}

    void a(String s) {}

    public static void b(String s) {}
  }

  @SuppressWarnings({"unused", "RedundantThrows"})
  private static final class ExecutableElementsGroupC {
    ExecutableElementsGroupC() throws Exception {}

    void a() throws Exception {}

    public static void b() throws Exception {}
  }

  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupD {
    ExecutableElementsGroupD() throws RuntimeException {}

    void a() throws RuntimeException {}

    public static void b() throws RuntimeException {}
  }

  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupE {
    <T> ExecutableElementsGroupE() {}

    <T> void a() {}

    public static <T> void b() {}
  }

  @SuppressWarnings("unused")
  private static final class Container<T> {
    @SuppressWarnings("InnerClassMayBeStatic")
    private final class Contained {}
  }

  @SuppressWarnings("unused")
  private static final class FunkyBounds<T extends Number & Comparable<T>> {}

  @SuppressWarnings("unused")
  private static final class FunkyBounds2<T extends Number & Comparable<T>> {}

  @SuppressWarnings("unused")
  private static final class FunkierBounds<T extends Number & Comparable<T> & Cloneable> {}

  @Test
  public void testReferencedTypes() {
    Elements elements = compilationRule.getElements();
    TypeElement testDataElement =
        elements.getTypeElement(ReferencedTypesTestData.class.getCanonicalName());
    ImmutableMap<String, VariableElement> fieldIndex =
        FluentIterable.from(ElementFilter.fieldsIn(testDataElement.getEnclosedElements()))
            .uniqueIndex(input -> input.getSimpleName().toString());

    TypeElement objectElement = elements.getTypeElement(Object.class.getCanonicalName());
    TypeElement stringElement = elements.getTypeElement(String.class.getCanonicalName());
    TypeElement integerElement = elements.getTypeElement(Integer.class.getCanonicalName());
    TypeElement setElement = elements.getTypeElement(Set.class.getCanonicalName());
    TypeElement mapElement = elements.getTypeElement(Map.class.getCanonicalName());
    TypeElement charSequenceElement =
        elements.getTypeElement(CharSequence.class.getCanonicalName());

    assertThat(referencedTypes(fieldIndex, "f1")).containsExactly(objectElement);
    assertThat(referencedTypes(fieldIndex, "f2")).containsExactly(setElement, stringElement);
    assertThat(referencedTypes(fieldIndex, "f3"))
        .containsExactly(mapElement, stringElement, objectElement);
    assertThat(referencedTypes(fieldIndex, "f4")).containsExactly(integerElement);
    assertThat(referencedTypes(fieldIndex, "f5")).containsExactly(setElement);
    assertThat(referencedTypes(fieldIndex, "f6")).containsExactly(setElement, charSequenceElement);
    assertThat(referencedTypes(fieldIndex, "f7"))
        .containsExactly(mapElement, stringElement, setElement, charSequenceElement);
    assertThat(referencedTypes(fieldIndex, "f8")).containsExactly(stringElement);
    assertThat(referencedTypes(fieldIndex, "f9")).containsExactly(stringElement);
    assertThat(referencedTypes(fieldIndex, "f10")).isEmpty();
    assertThat(referencedTypes(fieldIndex, "f11")).isEmpty();
    assertThat(referencedTypes(fieldIndex, "f12")).containsExactly(setElement, stringElement);
  }

  private static ImmutableSet<TypeElement> referencedTypes(
      ImmutableMap<String, VariableElement> fieldIndex, String fieldName) {
    VariableElement field = fieldIndex.get(fieldName);
    requireNonNull(field, fieldName);
    return MoreTypes.referencedTypeElements(field.asType());
  }

  @SuppressWarnings("unused") // types used in compiler tests
  private static final class ReferencedTypesTestData {
    Object f1;
    Set<String> f2;
    Map<String, Object> f3;
    Integer f4;
    Set<?> f5;
    Set<? extends CharSequence> f6;
    Map<String, Set<? extends CharSequence>> f7;
    String[] f8;
    String[][] f9;
    int f10;
    int[] f11;
    Set<? super String> f12;
  }

  private static class Parent<T> {}

  private static class ChildA extends Parent<Number> {}

  private static class ChildB extends Parent<String> {}

  private static class GenericChild<T> extends Parent<T> {}

  private interface InterfaceType {}

  @Test
  public void asElement_throws() {
    TypeMirror javaDotLang = compilationRule.getElements().getPackageElement("java.lang").asType();
    try {
      MoreTypes.asElement(javaDotLang);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void asElement() {
    Elements elements = compilationRule.getElements();
    TypeElement stringElement = elements.getTypeElement("java.lang.String");
    assertThat(MoreTypes.asElement(stringElement.asType())).isEqualTo(stringElement);
    TypeParameterElement setParameterElement =
        Iterables.getOnlyElement(
            compilationRule.getElements().getTypeElement("java.util.Set").getTypeParameters());
    assertThat(MoreTypes.asElement(setParameterElement.asType())).isEqualTo(setParameterElement);
    // we don't test error types because those are very hard to get predictably
  }

  private static class Params<T> {
    @SuppressWarnings("unused")
    T t;

    @SuppressWarnings("unused")
    void add(T t) {}
  }

  private static class NumberParams extends Params<Number> {}

  private static class StringParams extends Params<String> {}

  private static class GenericParams<T> extends Params<T> {}

  private static final ErrorType FAKE_ERROR_TYPE =
      new ErrorType() {
        @Override
        public TypeKind getKind() {
          return TypeKind.ERROR;
        }

        @Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
          return v.visitError(this, p);
        }

        @Override
        public ImmutableList<? extends TypeMirror> getTypeArguments() {
          return ImmutableList.of();
        }

        @Override
        public @Nullable TypeMirror getEnclosingType() {
          return null;
        }

        @Override
        public @Nullable Element asElement() {
          return null;
        }

        @Override
        public <A extends Annotation> A @Nullable [] getAnnotationsByType(Class<A> annotationType) {
          return null;
        }

        @Override
        public <A extends Annotation> @Nullable A getAnnotation(Class<A> annotationType) {
          return null;
        }

        @Override
        @SuppressWarnings("MutableMethodReturnType")
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
          return ImmutableList.of();
        }
      };

  @Test
  public void testIsTypeOf() {
    Types types = compilationRule.getTypes();
    PrimitiveType intType = types.getPrimitiveType(TypeKind.INT);
    TypeMirror integerType = types.boxedClass(intType).asType();
    WildcardType wildcardType = types.getWildcardType(null, null);
    expect.that(MoreTypes.isTypeOf(int.class, intType)).isTrue();
    expect.that(MoreTypes.isTypeOf(Integer.class, integerType)).isTrue();
    expect.that(MoreTypes.isTypeOf(Integer.class, intType)).isFalse();
    expect.that(MoreTypes.isTypeOf(int.class, integerType)).isFalse();
    expect.that(MoreTypes.isTypeOf(Integer.class, FAKE_ERROR_TYPE)).isFalse();
    assertThrows(
        IllegalArgumentException.class, () -> MoreTypes.isTypeOf(Integer.class, wildcardType));
  }

  // The type of every field here is such that casting to it provokes an "unchecked" warning.
  @SuppressWarnings("unused")
  private static class Unchecked<T> {
    private List<String> listOfString;
    private List<? extends CharSequence> listOfExtendsCharSequence;
    private List<? super CharSequence> listOfSuperCharSequence;
    private List<T> listOfT;
    private List<T[]> listOfArrayOfT;
    private T t;
    private T[] arrayOfT;
    private List<T>[] arrayOfListOfT;
    private Map<?, String> mapWildcardToString;
    private Map<String, ?> mapStringToWildcard;
  }

  // The type of every field here is such that casting to it doesn't provoke an "unchecked" warning.
  @SuppressWarnings("unused")
  private static class NotUnchecked {
    private String string;
    private int integer;
    private String[] arrayOfString;
    private int[] arrayOfInt;
    private Thread.State threadStateEnum;
    private List<?> listOfWildcard;
    @SuppressWarnings("TypeParameterExplicitlyExtendsObject")
    private List<? extends Object> listOfWildcardExtendsObject;
    private Map<?, ?> mapWildcardToWildcard;
  }
}
