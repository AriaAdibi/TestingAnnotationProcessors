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

import com.google.common.testing.EquivalenceTester;
import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import utils.AnnotationMirrors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import java.util.Map;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests {@link AnnotationMirrors}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("UnstableApiUsage") //TODO
public class AnnotationMirrorsTest {
  @Rule public CompilationRule compilationRule = new CompilationRule();

  private Elements eltUtils;

  @Before
  public void setUp() {
    this.eltUtils = compilationRule.getElements();
  }

  @interface SimpleAnnotation {
  }


  @SimpleAnnotation
  static class SimplyAnnotated {
  }


  @SimpleAnnotation
  static class AlsoSimplyAnnotated {
  }


  enum SimpleEnum {
    BLAH,
    FOO
  }


  @interface Outer {
    SimpleEnum value();
  }


  @Outer(SimpleEnum.BLAH)
  static class TestClassBlah {
  }


  @Outer(SimpleEnum.BLAH)
  static class TestClassBlah2 {
  }


  @Outer(SimpleEnum.FOO)
  static class TestClassFoo {
  }


  @interface DefaultingOuter {
    SimpleEnum value() default SimpleEnum.BLAH;
  }


  @DefaultingOuter
  static class TestWithDefaultingOuterDefault {
  }


  @DefaultingOuter(SimpleEnum.BLAH)
  static class TestWithDefaultingOuterBlah {
  }


  @DefaultingOuter(SimpleEnum.FOO)
  static class TestWithDefaultingOuterFoo {
  }


  @interface AnnotatedOuter {
    DefaultingOuter value();
  }


  @AnnotatedOuter(@DefaultingOuter)
  static class TestDefaultNestedAnnotated {
  }


  @AnnotatedOuter(@DefaultingOuter(SimpleEnum.BLAH))
  static class TestBlahNestedAnnotated {
  }


  @AnnotatedOuter(@DefaultingOuter(SimpleEnum.FOO))
  static class TestFooNestedAnnotated {
  }


  @interface OuterWithValueArray {
    DefaultingOuter[] value() default {};
  }


  @OuterWithValueArray
  static class TestValueArrayWithDefault {
  }


  @OuterWithValueArray({})
  static class TestValueArrayWithEmpty {
  }


  @OuterWithValueArray({@DefaultingOuter})
  static class TestValueArrayWithOneDefault {
  }


  @OuterWithValueArray(@DefaultingOuter(SimpleEnum.BLAH))
  static class TestValueArrayWithOneBlah {
  }


  @OuterWithValueArray(@DefaultingOuter(SimpleEnum.FOO))
  static class TestValueArrayWithOneFoo {
  }


  @OuterWithValueArray({@DefaultingOuter(SimpleEnum.FOO), @DefaultingOuter})
  class TestValueArrayWithFooAndDefaultBlah {
  }


  @OuterWithValueArray({@DefaultingOuter(SimpleEnum.FOO), @DefaultingOuter(SimpleEnum.BLAH)})
  class TestValueArrayWithFooBlah {
  }


  @OuterWithValueArray({@DefaultingOuter(SimpleEnum.FOO), @DefaultingOuter(SimpleEnum.BLAH)})
  class TestValueArrayWithFooBlah2 {
  } // Different instances than on TestValueArrayWithFooBlah.


  @OuterWithValueArray({@DefaultingOuter(SimpleEnum.BLAH), @DefaultingOuter(SimpleEnum.FOO)})
  class TestValueArrayWithBlahFoo {
  }

  @Test
  public void testEquivalences() {
    EquivalenceTester<AnnotationMirror> tester =
        EquivalenceTester.of(AnnotationMirrors.equivalence());

    tester.addEquivalenceGroup(
        annotationOn(SimplyAnnotated.class), annotationOn(AlsoSimplyAnnotated.class));

    tester.addEquivalenceGroup(
        annotationOn(TestClassBlah.class), annotationOn(TestClassBlah2.class));

    tester.addEquivalenceGroup(annotationOn(TestClassFoo.class));

    tester.addEquivalenceGroup(
        annotationOn(TestWithDefaultingOuterDefault.class),
        annotationOn(TestWithDefaultingOuterBlah.class));

    tester.addEquivalenceGroup(annotationOn(TestWithDefaultingOuterFoo.class));

    tester.addEquivalenceGroup(
        annotationOn(TestDefaultNestedAnnotated.class),
        annotationOn(TestBlahNestedAnnotated.class));

    tester.addEquivalenceGroup(annotationOn(TestFooNestedAnnotated.class));

    tester.addEquivalenceGroup(
        annotationOn(TestValueArrayWithDefault.class), annotationOn(TestValueArrayWithEmpty.class));

    tester.addEquivalenceGroup(
        annotationOn(TestValueArrayWithOneDefault.class),
        annotationOn(TestValueArrayWithOneBlah.class));

    tester.addEquivalenceGroup(annotationOn(TestValueArrayWithOneFoo.class));

    tester.addEquivalenceGroup(
        annotationOn(TestValueArrayWithFooAndDefaultBlah.class),
        annotationOn(TestValueArrayWithFooBlah.class),
        annotationOn(TestValueArrayWithFooBlah2.class));

    tester.addEquivalenceGroup(annotationOn(TestValueArrayWithBlahFoo.class));

    tester.test();
  }

  @interface Stringy {
    String value() default "default";
  }


  @Stringy
  static class StringyUnset {
  }


  @Stringy("foo")
  static class StringySet {
  }

  @Test
  public void testGetDefaultValuesUnset() {
    assertThat(annotationOn(StringyUnset.class).getElementValues()).isEmpty();
    Iterable<AnnotationValue> values =
        AnnotationMirrors.getAnnotationValuesWithDefaults(annotationOn(StringyUnset.class))
            .values();
    String value =
        getOnlyElement(values)
            .accept(
                new SimpleAnnotationValueVisitor6<String, Void>() {
                  @Override
                  public String visitString(String value, Void ignored) {
                    return value;
                  }
                },
                null);
    assertThat(value).isEqualTo("default");
  }

  @Test
  public void testGetDefaultValuesSet() {
    Iterable<AnnotationValue> values =
        AnnotationMirrors.getAnnotationValuesWithDefaults(annotationOn(StringySet.class)).values();
    String value =
        getOnlyElement(values)
            .accept(
                new SimpleAnnotationValueVisitor6<String, Void>() {
                  @Override
                  public String visitString(String value, Void ignored) {
                    return value;
                  }
                },
                null);
    assertThat(value).isEqualTo("foo");
  }

  @Test
  public void testGetValueEntry() {
    Map.Entry<ExecutableElement, AnnotationValue> elementValue =
        AnnotationMirrors.getAnnotationElementAndValue(annotationOn(TestClassBlah.class), "value");
    assertThat(elementValue.getKey().getSimpleName().toString()).isEqualTo("value");
    assertThat(elementValue.getValue().getValue()).isInstanceOf(VariableElement.class);
    AnnotationValue value =
        AnnotationMirrors.getAnnotationValue(annotationOn(TestClassBlah.class), "value");
    assertThat(value.getValue()).isInstanceOf(VariableElement.class);
  }

  @Test
  public void testGetValueEntryFailure() {
    try {
      AnnotationMirrors.getAnnotationValue(annotationOn(TestClassBlah.class), "a");
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "@baseprocessors.AnnotationMirrorsTest.Outer does not define an element a()");
      return;
    }
    fail("Should have thrown.");
  }

  private AnnotationMirror annotationOn(Class<?> clazz) {
    return getOnlyElement(eltUtils.getTypeElement(clazz.getCanonicalName()).getAnnotationMirrors());
  }

  @Test
  public void toSourceString() {
    assertThat(AnnotationMirrors.toString(annotationOn(AlsoSimplyAnnotated.class)))
        .isEqualTo("@baseprocessors.AnnotationMirrorsTest.SimpleAnnotation");
    assertThat(AnnotationMirrors.toString(annotationOn(SimplyAnnotated.class)))
        .isEqualTo("@baseprocessors.AnnotationMirrorsTest.SimpleAnnotation");
    assertThat(AnnotationMirrors.toString(annotationOn(StringySet.class)))
        .isEqualTo("@baseprocessors.AnnotationMirrorsTest.Stringy(\"foo\")");
    assertThat(AnnotationMirrors.toString(annotationOn(StringyUnset.class)))
        .isEqualTo("@baseprocessors.AnnotationMirrorsTest.Stringy");
    assertThat(AnnotationMirrors.toString(annotationOn(TestBlahNestedAnnotated.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.AnnotatedOuter(@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH))");
    assertThat(AnnotationMirrors.toString(annotationOn(TestClassBlah2.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.Outer(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH)");
    assertThat(AnnotationMirrors.toString(annotationOn(TestClassBlah.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.Outer(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH)");
    assertThat(AnnotationMirrors.toString(annotationOn(TestClassFoo.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.Outer(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO)");
    assertThat(AnnotationMirrors.toString(annotationOn(TestDefaultNestedAnnotated.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.AnnotatedOuter(@baseprocessors.AnnotationMirrorsTest.DefaultingOuter)");
    assertThat(AnnotationMirrors.toString(annotationOn(TestFooNestedAnnotated.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.AnnotatedOuter(@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO))");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithBlahFoo.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray({@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH),"
                + " @baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO)})");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithDefault.class)))
        .isEqualTo("@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithEmpty.class)))
        .isEqualTo("@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray({})");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithFooAndDefaultBlah.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray({@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO),"
                + " @baseprocessors.AnnotationMirrorsTest.DefaultingOuter})");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithFooBlah2.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray({@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO),"
                + " @baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH)})");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithFooBlah.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray({@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO),"
                + " @baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH)})");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithOneBlah.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray(@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH))");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithOneDefault.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray(@baseprocessors.AnnotationMirrorsTest.DefaultingOuter)");
    assertThat(AnnotationMirrors.toString(annotationOn(TestValueArrayWithOneFoo.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.OuterWithValueArray(@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO))");
    assertThat(AnnotationMirrors.toString(annotationOn(TestWithDefaultingOuterBlah.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.BLAH)");
    assertThat(AnnotationMirrors.toString(annotationOn(TestWithDefaultingOuterDefault.class)))
        .isEqualTo("@baseprocessors.AnnotationMirrorsTest.DefaultingOuter");
    assertThat(AnnotationMirrors.toString(annotationOn(TestWithDefaultingOuterFoo.class)))
        .isEqualTo(
            "@baseprocessors.AnnotationMirrorsTest.DefaultingOuter(baseprocessors.AnnotationMirrorsTest.SimpleEnum.FOO)");
  }

}
