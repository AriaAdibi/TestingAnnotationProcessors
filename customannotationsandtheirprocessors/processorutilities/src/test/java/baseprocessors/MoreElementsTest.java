package baseprocessors;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Correspondence;
import com.google.common.truth.Expect;
import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import utils.MoreElements;
import utils.MoreTypes;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class MoreElementsTest {
  @Rule public CompilationRule compilation = new CompilationRule();
  @Rule public Expect expect = Expect.create();

  private Elements eltUtils;
  private PackageElement javaLangPackageElement;
  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  private TypeElement objectElement;
  private TypeElement stringElement;

  @Before
  public void initializeTestElements() {
    this.eltUtils = compilation.getElements();
    this.javaLangPackageElement = eltUtils.getPackageElement("java.lang");
    this.objectElement = eltUtils.getTypeElement(Object.class.getCanonicalName());
    this.stringElement = eltUtils.getTypeElement(String.class.getCanonicalName());
  }

  @Test
  public void getPackage() {
    assertThat(MoreElements.getPackage(stringElement)).isEqualTo(javaLangPackageElement);
    for (Element childElement : stringElement.getEnclosedElements()) {
      assertThat(MoreElements.getPackage(childElement)).isEqualTo(javaLangPackageElement);
    }
    //TODO add test for unnamed package (I already tested it, it works)
  }

  //  @Test
  //  public void getEnclosingType() {
  //    //TODO
  //  }

  //TODO change the name of methods in accordance to changes made in MoreElements; e.g. asType --> asTypeElement
  @Test
  public void asPackage() {
    assertThat(MoreElements.asPackage(javaLangPackageElement)).isEqualTo(javaLangPackageElement);
  }

  @Test
  public void asPackage_illegalArgument() {
    try {
      MoreElements.asPackage(stringElement);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  //  @Test
  //  public void asTypeElement() { //TODO unnecessary, replicated with the other one + also add test for isType
  //    Element typeElement = eltUtils.getTypeElement(String.class.getCanonicalName());
  //    assertTrue(MoreElements.isTypeElement(typeElement));
  //    assertThat(MoreElements.asTypeElement(typeElement)).isEqualTo(typeElement);
  //  }

  @Test
  public void asTypeElement_notATypeElement() { //TODO Unnecessary, at lease modify
    TypeElement typeElement = eltUtils.getTypeElement(String.class.getCanonicalName());
    for (ExecutableElement e : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
      assertFalse(MoreElements.isTypeElement(e));
      try {
        MoreElements.asTypeElement(e);
        fail();
      } catch (IllegalArgumentException expected) {
      }
    }
  }

  @Test
  public void asTypeParameterElement() {
    Element typeParameterElement =
        getOnlyElement(
            compilation
                .getElements()
                .getTypeElement(List.class.getCanonicalName())
                .getTypeParameters());
    assertThat(MoreElements.asTypeParameter(typeParameterElement)).isEqualTo(typeParameterElement);
  }

  @Test
  public void asTypeParameterElement_illegalArgument() {
    try {
      MoreElements.asTypeParameter(javaLangPackageElement);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void asTypeElement() {
    assertThat(MoreElements.asTypeElement(stringElement)).isEqualTo(stringElement);
  }

  @Test
  public void asTypeElement_illegalArgument() {
    assertFalse(MoreElements.isTypeElement(javaLangPackageElement));
    try {
      MoreElements.asTypeElement(javaLangPackageElement);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void asVariable() {
    for (Element variableElement : ElementFilter.fieldsIn(stringElement.getEnclosedElements())) {
      assertThat(MoreElements.asVariable(variableElement)).isEqualTo(variableElement);
    }
  }

  @Test
  public void asVariable_illegalArgument() {
    try {
      MoreElements.asVariable(javaLangPackageElement);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void asExecutable() {
    for (Element methodElement : ElementFilter.methodsIn(stringElement.getEnclosedElements())) {
      assertThat(MoreElements.asExecutable(methodElement)).isEqualTo(methodElement);
    }
    for (Element methodElement :
        ElementFilter.constructorsIn(stringElement.getEnclosedElements())) {
      assertThat(MoreElements.asExecutable(methodElement)).isEqualTo(methodElement);
    }
  }

  @Test
  public void asExecutable_illegalArgument() {
    try {
      MoreElements.asExecutable(javaLangPackageElement);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface InnerAnnotation {
  }


  @Documented
  @InnerAnnotation
  private @interface AnnotatedAnnotation {
  }

  @Test
  public void isAnnotationPresent() {
    TypeElement annotatedAnnotationElement =
        eltUtils.getTypeElement(AnnotatedAnnotation.class.getCanonicalName());

    // Test Class API
    isAnnotationPresentAsserts(
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, Documented.class),
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, InnerAnnotation.class),
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, SuppressWarnings.class));

    // Test String API
    String documentedName = Documented.class.getCanonicalName();
    String innerAnnotationName = InnerAnnotation.class.getCanonicalName();
    String suppressWarningsName = SuppressWarnings.class.getCanonicalName();
    isAnnotationPresentAsserts(
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, documentedName),
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, innerAnnotationName),
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, suppressWarningsName));

    // Test TypeElement API
    TypeElement documentedElement = eltUtils.getTypeElement(documentedName);
    TypeElement innerAnnotationElement = eltUtils.getTypeElement(innerAnnotationName);
    TypeElement suppressWarningsElement = eltUtils.getTypeElement(suppressWarningsName);
    isAnnotationPresentAsserts(
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, documentedElement),
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, innerAnnotationElement),
        MoreElements.isAnnotationPresent(annotatedAnnotationElement, suppressWarningsElement));
  }

  private void isAnnotationPresentAsserts(
      boolean isDocumentedPresent,
      boolean isInnerAnnotationPresent,
      boolean isSuppressWarningsPresent) {
    assertThat(isDocumentedPresent).isTrue();
    assertThat(isInnerAnnotationPresent).isTrue();
    assertThat(isSuppressWarningsPresent).isFalse();
  }

  @Test
  public void getAnnotationMirrorOfType() {
    TypeElement element =
        eltUtils.getTypeElement(AnnotatedAnnotation.class.getCanonicalName());

    // Test Class API
    getAnnotationMirrorAsserts(
        MoreElements.getAnnotationMirrorOfType(element, Documented.class),
        MoreElements.getAnnotationMirrorOfType(element, InnerAnnotation.class),
        MoreElements.getAnnotationMirrorOfType(element, SuppressWarnings.class));

    // Test String API
    String documentedName = Documented.class.getCanonicalName();
    String innerAnnotationName = InnerAnnotation.class.getCanonicalName();
    String suppressWarningsName = SuppressWarnings.class.getCanonicalName();
    getAnnotationMirrorAsserts(
        MoreElements.getAnnotationMirrorOfType(element, documentedName),
        MoreElements.getAnnotationMirrorOfType(element, innerAnnotationName),
        MoreElements.getAnnotationMirrorOfType(element, suppressWarningsName));

    // Test TypeElement API
    TypeElement documentedElement = eltUtils.getTypeElement(documentedName);
    TypeElement innerAnnotationElement = eltUtils.getTypeElement(innerAnnotationName);
    TypeElement suppressWarningsElement = eltUtils.getTypeElement(suppressWarningsName);
    getAnnotationMirrorAsserts(
        MoreElements.getAnnotationMirrorOfType(element, documentedElement),
        MoreElements.getAnnotationMirrorOfType(element, innerAnnotationElement),
        MoreElements.getAnnotationMirrorOfType(element, suppressWarningsElement));
  }

  @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "Guava"})
  private void getAnnotationMirrorAsserts(
      Optional<? extends AnnotationMirror> jDocumented,
      Optional<? extends AnnotationMirror> jInnerAnnotation,
      Optional<? extends AnnotationMirror> jSuppressWarnings) {
    // TODO for now just quick fix
    com.google.common.base.Optional<? extends AnnotationMirror> documented = com.google.common.base.Optional.fromJavaUtil(jDocumented);
    com.google.common.base.Optional<? extends AnnotationMirror> innerAnnotation = com.google.common.base.Optional.fromJavaUtil(jInnerAnnotation);
    com.google.common.base.Optional<? extends AnnotationMirror> suppressWarnings = com.google.common.base.Optional.fromJavaUtil(jSuppressWarnings);
    expect.that(documented).isPresent();
    expect.that(innerAnnotation).isPresent();
    expect.that(suppressWarnings).isAbsent();

    Element annotationElement = documented.get().getAnnotationType().asElement();
    expect.that(MoreElements.isTypeElement(annotationElement)).isTrue();
    expect
        .that(MoreElements.asTypeElement(annotationElement).getQualifiedName().toString())
        .isEqualTo(Documented.class.getCanonicalName());

    annotationElement = innerAnnotation.get().getAnnotationType().asElement();
    expect.that(MoreElements.isTypeElement(annotationElement)).isTrue();
    expect
        .that(MoreElements.asTypeElement(annotationElement).getQualifiedName().toString())
        .isEqualTo(InnerAnnotation.class.getCanonicalName());
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface AnnotatingAnnotation {
  }


  @AnnotatingAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  private @interface AnnotatedAnnotation1 {
  }


  @AnnotatingAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  private @interface AnnotatedAnnotation2 {
  }


  @Retention(RetentionPolicy.RUNTIME)
  private @interface NotAnnotatedAnnotation {
  }


  @AnnotatedAnnotation1
  @NotAnnotatedAnnotation
  @AnnotatedAnnotation2
  private static final class AnnotatedClass {
  }

  @Test
  public void getAnnotatedAnnotations() {
    TypeElement element = eltUtils.getTypeElement(AnnotatedClass.class.getCanonicalName());

    // Test Class API
    getAnnotatedAnnotationsAsserts(
        MoreElements.getAnnotatedAnnotations(element, AnnotatingAnnotation.class));

    // Test String API
    String annotatingAnnotationName = AnnotatingAnnotation.class.getCanonicalName();
    getAnnotatedAnnotationsAsserts(
        MoreElements.getAnnotatedAnnotations(element, annotatingAnnotationName));

    // Test TypeElement API
    TypeElement annotatingAnnotationElement = eltUtils.getTypeElement(annotatingAnnotationName);
    getAnnotatedAnnotationsAsserts(
        MoreElements.getAnnotatedAnnotations(element, annotatingAnnotationElement));
  }

  private void getAnnotatedAnnotationsAsserts(
      ImmutableSet<? extends AnnotationMirror> annotatedAnnotations) {
    assertThat(annotatedAnnotations)
        .comparingElementsUsing(
            Correspondence.transforming(
                (AnnotationMirror a) -> MoreElements.asTypeElement(MoreTypes.asElement(a.getAnnotationType())), "has type"))
        .containsExactly(
            eltUtils.getTypeElement(MoreElementsTest.AnnotatedAnnotation1.class.getCanonicalName()),
            eltUtils.getTypeElement(MoreElementsTest.AnnotatedAnnotation2.class.getCanonicalName()));
  }

}
