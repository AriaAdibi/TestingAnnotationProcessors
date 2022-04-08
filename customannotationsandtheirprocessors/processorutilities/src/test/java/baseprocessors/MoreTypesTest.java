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
import utils.MoreTypes;

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
    Types typeUtils = compilationRule.getTypes();
    Elements eltUtils = compilationRule.getElements();
    TypeMirror objectType = eltUtils.getTypeElement(Object.class.getCanonicalName()).asType();
    TypeMirror stringType = eltUtils.getTypeElement(String.class.getCanonicalName()).asType();
    TypeElement mapElement = eltUtils.getTypeElement(Map.class.getCanonicalName());
    TypeElement setElement = eltUtils.getTypeElement(Set.class.getCanonicalName());
    TypeElement enumElement = eltUtils.getTypeElement(Enum.class.getCanonicalName());
    TypeElement container = eltUtils.getTypeElement(Container.class.getCanonicalName());
    TypeElement contained = eltUtils.getTypeElement(Container.Contained.class.getCanonicalName());
    TypeElement funkyBounds = eltUtils.getTypeElement(FunkyBounds.class.getCanonicalName());
    TypeElement funkyBounds2 = eltUtils.getTypeElement(FunkyBounds2.class.getCanonicalName());
    TypeElement funkierBounds = eltUtils.getTypeElement(FunkierBounds.class.getCanonicalName());
    TypeMirror funkyBoundsVar = ((DeclaredType) funkyBounds.asType()).getTypeArguments().get(0);
    TypeMirror funkyBounds2Var = ((DeclaredType) funkyBounds2.asType()).getTypeArguments().get(0);
    TypeMirror funkierBoundsVar = ((DeclaredType) funkierBounds.asType()).getTypeArguments().get(0);
    DeclaredType mapOfObjectToObjectType =
        typeUtils.getDeclaredType(mapElement, objectType, objectType);
    TypeMirror mapType = mapElement.asType();
    DeclaredType setOfSetOfObject =
        typeUtils.getDeclaredType(setElement, typeUtils.getDeclaredType(setElement, objectType));
    DeclaredType setOfSetOfString =
        typeUtils.getDeclaredType(setElement, typeUtils.getDeclaredType(setElement, stringType));
    DeclaredType setOfSetOfSetOfObject = typeUtils.getDeclaredType(setElement, setOfSetOfObject);
    DeclaredType setOfSetOfSetOfString = typeUtils.getDeclaredType(setElement, setOfSetOfString);
    WildcardType wildcard = typeUtils.getWildcardType(null, null);
    DeclaredType containerOfObject = typeUtils.getDeclaredType(container, objectType);
    DeclaredType containerOfString = typeUtils.getDeclaredType(container, stringType);
    TypeMirror containedInObject = typeUtils.asMemberOf(containerOfObject, contained);
    TypeMirror containedInString = typeUtils.asMemberOf(containerOfString, contained);
    EquivalenceTester<TypeMirror> tester =
        EquivalenceTester.<TypeMirror>of(MoreTypes.equivalence())
            .addEquivalenceGroup(typeUtils.getNullType())
            .addEquivalenceGroup(typeUtils.getNoType(NONE))
            .addEquivalenceGroup(typeUtils.getNoType(VOID))
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
            .addEquivalenceGroup(typeUtils.getDeclaredType(mapElement, wildcard, wildcard))
            // Map
            .addEquivalenceGroup(typeUtils.erasure(mapType), typeUtils.erasure(mapOfObjectToObjectType))
            .addEquivalenceGroup(typeUtils.getDeclaredType(mapElement, objectType, stringType))
            .addEquivalenceGroup(typeUtils.getDeclaredType(mapElement, stringType, objectType))
            .addEquivalenceGroup(typeUtils.getDeclaredType(mapElement, stringType, stringType))
            .addEquivalenceGroup(setOfSetOfObject)
            .addEquivalenceGroup(setOfSetOfString)
            .addEquivalenceGroup(setOfSetOfSetOfObject)
            .addEquivalenceGroup(setOfSetOfSetOfString)
            .addEquivalenceGroup(wildcard)
            // ? extends Object
            .addEquivalenceGroup(typeUtils.getWildcardType(objectType, null))
            // ? extends String
            .addEquivalenceGroup(typeUtils.getWildcardType(stringType, null))
            // ? super String
            .addEquivalenceGroup(typeUtils.getWildcardType(null, stringType))
            // Map<String, Map<String, Set<Object>>>
            .addEquivalenceGroup(
                typeUtils.getDeclaredType(
                    mapElement,
                    stringType,
                    typeUtils.getDeclaredType(
                        mapElement, stringType, typeUtils.getDeclaredType(setElement, objectType))))
            .addEquivalenceGroup(FAKE_ERROR_TYPE);

    for (TypeKind kind : TypeKind.values()) {
      if (kind.isPrimitive()) {
        PrimitiveType primitiveType = typeUtils.getPrimitiveType(kind);
        TypeMirror boxedPrimitiveType = typeUtils.boxedClass(primitiveType).asType();
        tester.addEquivalenceGroup(primitiveType, typeUtils.unboxedType(boxedPrimitiveType));
        tester.addEquivalenceGroup(boxedPrimitiveType);
        tester.addEquivalenceGroup(typeUtils.getArrayType(primitiveType));
        tester.addEquivalenceGroup(typeUtils.getArrayType(boxedPrimitiveType));
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
                  eltUtils.getTypeElement(testClass.getCanonicalName()).getEnclosedElements())
              .transform(Element::asType)
              .toList();
      tester.addEquivalenceGroup(equivalenceGroup);
    }

    tester.test();
  }

  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupA {
    ExecutableElementsGroupA() {
    }

    void a() {
    }

    public static void b() {
    }
  }


  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupB {
    ExecutableElementsGroupB(String s) {
    }

    void a(String s) {
    }

    public static void b(String s) {
    }
  }


  @SuppressWarnings({"unused", "RedundantThrows"})
  private static final class ExecutableElementsGroupC {
    ExecutableElementsGroupC() throws Exception {
    }

    void a() throws Exception {
    }

    public static void b() throws Exception {
    }
  }


  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupD {
    ExecutableElementsGroupD() throws RuntimeException {
    }

    void a() throws RuntimeException {
    }

    public static void b() throws RuntimeException {
    }
  }


  @SuppressWarnings("unused")
  private static final class ExecutableElementsGroupE {
    <T> ExecutableElementsGroupE() {
    }

    <T> void a() {
    }

    public static <T> void b() {
    }
  }


  @SuppressWarnings("unused")
  private static final class Container<T> {
    @SuppressWarnings("InnerClassMayBeStatic")
    private final class Contained {
    }
  }


  @SuppressWarnings("unused")
  private static final class FunkyBounds<T extends Number & Comparable<T>> {
  }


  @SuppressWarnings("unused")
  private static final class FunkyBounds2<T extends Number & Comparable<T>> {
  }


  @SuppressWarnings("unused")
  private static final class FunkierBounds<T extends Number & Comparable<T> & Cloneable> {
  }

  @Test
  public void testReferencedTypes() {
    Elements eltUtils = compilationRule.getElements();
    TypeElement testDataElement =
        eltUtils.getTypeElement(ReferencedTypesTestData.class.getCanonicalName());
    ImmutableMap<String, VariableElement> fieldIndex =
        FluentIterable.from(ElementFilter.fieldsIn(testDataElement.getEnclosedElements()))
            .uniqueIndex(input -> input.getSimpleName().toString());

    TypeElement objectElement = eltUtils.getTypeElement(Object.class.getCanonicalName());
    TypeElement stringElement = eltUtils.getTypeElement(String.class.getCanonicalName());
    TypeElement integerElement = eltUtils.getTypeElement(Integer.class.getCanonicalName());
    TypeElement setElement = eltUtils.getTypeElement(Set.class.getCanonicalName());
    TypeElement mapElement = eltUtils.getTypeElement(Map.class.getCanonicalName());
    TypeElement charSequenceElement =
        eltUtils.getTypeElement(CharSequence.class.getCanonicalName());

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

  @SuppressWarnings({"unused", "NotNullFieldNotInitialized"}) // typeUtils used in compiler tests
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


  private static class Parent<T> {
  }


  private static class ChildA extends Parent<Number> {
  }


  private static class ChildB extends Parent<String> {
  }


  private static class GenericChild<T> extends Parent<T> {
  }


  private interface InterfaceType {
  }

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
    Elements eltUtils = compilationRule.getElements();
    TypeElement stringElement = eltUtils.getTypeElement("java.lang.String");
    assertThat(MoreTypes.asElement(stringElement.asType())).isEqualTo(stringElement);
    TypeParameterElement setParameterElement =
        Iterables.getOnlyElement(
            compilationRule.getElements().getTypeElement("java.util.Set").getTypeParameters());
    assertThat(MoreTypes.asElement(setParameterElement.asType())).isEqualTo(setParameterElement);
    // we don't test error types because those are very hard to get predictably
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static class Params<T> {
    @SuppressWarnings("unused")
    T t;

    @SuppressWarnings("unused")
    void add(T t) {
    }
  }


  private static class NumberParams extends Params<Number> {
  }


  private static class StringParams extends Params<String> {
  }


  private static class GenericParams<T> extends Params<T> {
  }


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
        public @Nullable <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
          return null;
        }

        @Override
        public @Nullable <A extends Annotation> A getAnnotation(Class<A> annotationType) {
          return null;
        }

        @Override
        @SuppressWarnings("MutableMethodReturnType")
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
          return ImmutableList.of();
        }
      };

  @Test
  public void testIsExactTypeOf() {
    Types typeUtils = compilationRule.getTypes();
    PrimitiveType intType = typeUtils.getPrimitiveType(TypeKind.INT);
    TypeMirror integerType = typeUtils.boxedClass(intType).asType();
    WildcardType wildcardType = typeUtils.getWildcardType(null, null);
    expect.that(MoreTypes.isExactTypeOf(int.class, intType)).isTrue();
    expect.that(MoreTypes.isExactTypeOf(Integer.class, integerType)).isTrue();
    expect.that(MoreTypes.isExactTypeOf(Integer.class, intType)).isFalse();
    expect.that(MoreTypes.isExactTypeOf(int.class, integerType)).isFalse();
    expect.that(MoreTypes.isExactTypeOf(Integer.class, FAKE_ERROR_TYPE)).isFalse();
    assertThrows(
        IllegalArgumentException.class, () -> MoreTypes.isExactTypeOf(Integer.class, wildcardType));
  }

  // The type of every field here is such that casting to it provokes an "unchecked" warning.
  @SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
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
  @SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
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
