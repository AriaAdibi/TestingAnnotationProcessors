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

import com.google.testing.compile.CompilationRule;
import lombok.Getter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import utils.MoreElements;
import utils.MoreTypes;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.*;

/**
 * Tests {@link MoreTypes#isTypeOf(Class, TypeMirror)}.
 */
@RunWith(JUnit4.class)
public class MoreTypesIsTypeOfTest {

  @Rule public CompilationRule compilationRule = new CompilationRule();

  private Elements eltUtils;
  private Types typeUtils;

  @Before
  public void setUp() {
    this.eltUtils = compilationRule.getElements();
    this.typeUtils = compilationRule.getTypes();
  }

  @Test
  public void isTypeOf_primitiveAndBoxedPrimitiveTypes() {
    @Getter
    class PrimitiveTypeInfo {
      final Class<?> classType;
      final Class<?> boxedClassType;
      final TypeKind typeKind;

      PrimitiveTypeInfo(Class<?> classType, Class<?> boxedClassType, TypeKind typeKind) {
        this.classType = classType;
        this.boxedClassType = boxedClassType;
        this.typeKind = typeKind;
      }
    }
    List<PrimitiveTypeInfo>
        primitivesTypeInfo = List.of(
        new PrimitiveTypeInfo(Byte.TYPE, Byte.class, TypeKind.BYTE),
        new PrimitiveTypeInfo(Short.TYPE, Short.class, TypeKind.SHORT),
        new PrimitiveTypeInfo(Integer.TYPE, Integer.class, TypeKind.INT),
        new PrimitiveTypeInfo(Long.TYPE, Long.class, TypeKind.LONG),
        new PrimitiveTypeInfo(Float.TYPE, Float.class, TypeKind.FLOAT),
        new PrimitiveTypeInfo(Double.TYPE, Double.class, TypeKind.DOUBLE),
        new PrimitiveTypeInfo(Boolean.TYPE, Boolean.class, TypeKind.BOOLEAN),
        new PrimitiveTypeInfo(Character.TYPE, Character.class, TypeKind.CHAR)
    );

    for (int k = 0; k < 2; k++) {
      // k = 0: primitives, k = 1: boxed primitives
      for (int i = 0; i < primitivesTypeInfo.size(); i++) {
        Class<?> clazz =
            (k == 0) ?
                primitivesTypeInfo.get(i).getClassType() :
                primitivesTypeInfo.get(i).getBoxedClassType();

        for (int j = 0; j < primitivesTypeInfo.size(); j++) {
          TypeKind typeKind = primitivesTypeInfo.get(j).getTypeKind();
          TypeMirror typeMirror =
              (k == 0) ?
                  typeUtils.getPrimitiveType(typeKind) :
                  typeUtils.boxedClass(typeUtils.getPrimitiveType(typeKind)).asType();

          String message = "Mirror:\t" + typeMirror.toString() + "\nClass:\t" + clazz.getCanonicalName();
          if (i == j)
            assertWithMessage(message).that(MoreTypes.isTypeOf(clazz, typeMirror)).isTrue();
          else
            assertWithMessage(message).that(MoreTypes.isTypeOf(clazz, typeMirror)).isFalse();
        }

      }

    }
  }

  @Test
  public void isTypeOf_voidAndPseudoVoidTypes() {
    TypeMirror voidType = typeUtils.getNoType(TypeKind.VOID);
    TypeMirror pseudoVoidType = eltUtils.getTypeElement(Void.class.getCanonicalName()).asType();

    assertWithMessage("Mirror:\t" + voidType + "\nClass:\t" + Void.TYPE.getCanonicalName())
        .that(MoreTypes.isTypeOf(Void.TYPE, voidType)).isTrue();
    assertWithMessage("Mirror:\t" + pseudoVoidType + "\nClass:\t" + Void.TYPE.getCanonicalName())
        .that(MoreTypes.isTypeOf(Void.TYPE, pseudoVoidType)).isFalse();


    assertWithMessage("Mirror:\t" + voidType + "\nClass:\t" + Void.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Void.class, voidType)).isFalse();
    assertWithMessage("Mirror:\t" + pseudoVoidType + "\nClass:\t" + Void.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Void.class, pseudoVoidType)).isTrue();
  }

  @Test
  public void isTypeOf_arrayType() {
    TypeMirror type = typeUtils.getArrayType(typeElementFor(String.class).asType());
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + String[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(String[].class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + Integer[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Integer[].class, type)).isFalse();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + int[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(int[].class, type)).isFalse();

    type = typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.INT));
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + String[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(String[].class, type)).isFalse();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + Integer[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Integer[].class, type)).isFalse();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + int[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(int[].class, type)).isTrue();
  }


  @Test
  // ArrayList is a list by its father implementing List (checking interface and direct ancestry)
  public void isTypeOf_listLineage() {
    TypeMirror type = typeUtils.getDeclaredType(
        typeElementFor(ArrayList.class),
        typeElementFor(String.class).asType()
    );
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + ArrayList.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(ArrayList.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + List.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(List.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + String.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(String.class, type)).isFalse();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + LinkedList.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(LinkedList.class, type)).isFalse();

    type = typeUtils.getArrayType(type); // ArrayList<String>[]
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + ArrayList[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(ArrayList[].class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + List[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(List[].class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + LinkedList[].class.getCanonicalName())
        .that(MoreTypes.isTypeOf(LinkedList[].class, type)).isFalse();
  }

  @Test
  // NavigableMap implements SortedMap and SortedMap implements Map (checking interface ancestry)
  public void isTypeOf_mapLineage() {
    TypeMirror type = typeElementFor(SortedMap.class).asType();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + SortedMap.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(SortedMap.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + Map.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Map.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + NavigableMap.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(NavigableMap.class, type)).isFalse();

    //Testing ancestor that is not father
    type = typeElementFor(NavigableMap.class).asType();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + Map.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Map.class, type)).isTrue();
  }

  @Test
  public void isTypeOf_wildcardCapture() {
    TypeMirror type = typeUtils.getWildcardType(typeElementFor(SortedMap.class).asType(), null); // ? extends SortedMap
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + SortedMap.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(SortedMap.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + Map.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Map.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + NavigableMap.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(NavigableMap.class, type)).isFalse();
  }

  private interface TestType {
    @SuppressWarnings("unused") <T extends SortedMap<Number, String>> T method0();

    @SuppressWarnings("unused") <RANDOM_ACCESS_LIST extends List<?> & RandomAccess> void method1(RANDOM_ACCESS_LIST randomAccessList);
  }

  @Test
  public void isTypeOf_declaredType() {
    assertTrue(MoreTypes.isClassType(typeElementFor(TestType.class).asType()));
    assertWithMessage("Mirror:\t" + TestType.class.getCanonicalName() + "\nClass:\t" + TestType.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(TestType.class, typeElementFor(TestType.class).asType()))
        .isTrue();
    assertWithMessage("Mirror:\t" + TestType.class.getCanonicalName() + "\nClass:\t" + String.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(String.class, typeElementFor(TestType.class).asType()))
        .isFalse();
  }

  @Test
  public void isTypeOf_typeParameterCapture() {
    assertTrue(MoreTypes.isClassType(typeElementFor(TestType.class).asType()));

    // Getting type parameter
    ExecutableElement executableElement = MoreElements.asExecutable(
        typeElementFor(TestType.class).getEnclosedElements().get(0)
    );
    TypeMirror type = executableElement.getTypeParameters().get(0).asType();

    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + SortedMap.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(SortedMap.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + Map.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(Map.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + NavigableMap.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(NavigableMap.class, type)).isFalse();

    // Getting parameter type and checking for intersection type
    executableElement = MoreElements.asExecutable(
        typeElementFor(TestType.class).getEnclosedElements().get(1)
    );
    type = executableElement.getParameters().get(0).asType();

    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + List.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(List.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + RandomAccess.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(RandomAccess.class, type)).isTrue();
    assertWithMessage("Mirror:\t" + type + "\nClass:\t" + ArrayList.class.getCanonicalName())
        .that(MoreTypes.isTypeOf(ArrayList.class, type)).isFalse();
  }

  @Test
  public void isTypeOf_fail() {
    assertFalse(
        MoreTypes.isClassType(
            typeElementFor(TestType.class).getEnclosedElements().get(0).asType())
    );
    TypeMirror methodType =
        typeElementFor(TestType.class).getEnclosedElements().get(1).asType();
    try {
      MoreTypes.isTypeOf(List.class, methodType);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  // Utility method(s) for this test.
  private TypeElement typeElementFor(Class<?> clazz) {
    return eltUtils.getTypeElement(clazz.getCanonicalName());
  }
}
