package utils;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor9;
import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Static utility methods pertaining to {@link Element} instances.
 */
public class MoreElements {

  private MoreElements() {
  }

  /* ********************************************************************* */
  /* Type/Presence Check ************************************************* */
  /* ********************************************************************* */

  /**
   * Returns {@code true} iff the given {@link Element} instance is a {@link TypeElement}.
   * Note that an enum type is a kind of class and an annotation type is a kind of interface.
   *
   * <p>This method is functionally equivalent to an {@code instanceof} check, but should
   * always be used over that idiom as instructed in the documentation for {@link Element}.
   *
   * @param element the element to be checked
   * @return {@code true} iff the given {@link Element} instance is a {@link TypeElement}
   * @throws NullPointerException if {@code element} is {@code null}
   */
  public static boolean isTypeElement(Element element) {
    return element.getKind().isClass() || element.getKind().isInterface();
  }

  /**
   * Returns {@code true} iff the given element has an {@link AnnotationMirror} whose {@linkplain
   * AnnotationMirror#getAnnotationType() annotation type} has the same canonical name as that of
   * {@code annotationClass}. This method is a safer alternative to calling {@link
   * Element#getAnnotation} and checking for {@code null} as it avoids any interaction with
   * annotation proxies.
   *
   * @param element         the investigated element for the presence of specified annotation
   * @param annotationClass the annotation passed as {@linkplain Class} whose presence is to be investigated
   * @return {@code true} iff the given element has an {@linkplain AnnotationMirror} whose
   * {@linkplain AnnotationMirror#getAnnotationType() annotation type} has the same
   * canonical name as that of {@code annotationClass}
   */
  public static boolean isAnnotationPresent(Element element, Class<? extends Annotation> annotationClass) {
    return getAnnotationMirrorOfType(element, annotationClass).isPresent();
  }

  /**
   * Returns {@code true} iff the given element has an {@link AnnotationMirror} whose {@linkplain
   * AnnotationMirror#getAnnotationType() annotation type} has the same fully qualified name as that
   * of {@code annotation}. This method is a safer alternative to calling {@link
   * Element#getAnnotation} and checking for {@code null} as it avoids any interaction with
   * annotation proxies.
   *
   * @param element    the investigated element for the presence of specified annotation
   * @param annotation the annotation passed as {@linkplain TypeElement} whose presence is to be investigated
   * @return {@code true} iff the given element has an {@link AnnotationMirror} whose {@linkplain
   * AnnotationMirror#getAnnotationType() annotation type} has the same fully qualified name as that
   * of {@code annotation}
   */
  public static boolean isAnnotationPresent(Element element, TypeElement annotation) {
    return getAnnotationMirrorOfType(element, annotation).isPresent();
  }

  /**
   * Returns {@code true} iff the given element has an {@link AnnotationMirror} whose {@linkplain
   * AnnotationMirror#getAnnotationType() annotation type} has {@code annotationName} as its
   * canonical name. This method is a safer alternative to calling {@link Element#getAnnotation} and
   * checking for {@code null} as it avoids any interaction with annotation proxies.
   *
   * @param element        the investigated element for the presence of specified annotation
   * @param annotationName the annotation passed as {@linkplain String} whose presence is to be investigated
   * @return {@code true} iff the given element has an {@link AnnotationMirror} whose {@linkplain
   * AnnotationMirror#getAnnotationType() annotation type} has {@code annotationName} as its
   * canonical name
   */
  public static boolean isAnnotationPresent(Element element, String annotationName) {
    return getAnnotationMirrorOfType(element, annotationName).isPresent();
  }

  /* ********************************************************************* */
  /* Getters ************************************************************* */
  /* ********************************************************************* */

  /**
   * An alternate implementation of {@link Elements#getPackageOf} that does not require
   * an {@link Elements} instance.
   *
   * @param element the element whose package is inquired
   * @return the package of the element. The package of a package is itself.
   * @throws NullPointerException if {@code element} is (1) {@code null}, or (2) a {@code module}
   */
  public static PackageElement getPackage(Element element) {
    while (element.getKind() != ElementKind.PACKAGE) {
      element = element.getEnclosingElement();
    }
    return MoreElements.asPackage(element);
  }

  /**
   * Rreturns the nearest enclosing {@link TypeElement} to the current element.
   *
   * @param element an element
   * @return the nearest enclosing {@link TypeElement} to the current element
   * @throws IllegalArgumentException if the provided {@link Element} is a {@link PackageElement} or is
   *                                  otherwise not enclosed by a type
   */
  public static TypeElement getEnclosingType(Element element) {
    return element.accept(
        new SimpleElementVisitor9<TypeElement, Void>() {
          @Override
          protected TypeElement defaultAction(Element e, Void p) {
            return e.getEnclosingElement().accept(this, p);
          }

          @Override
          public TypeElement visitType(TypeElement e, Void p) {
            return e;
          }

          @Override
          public TypeElement visitPackage(PackageElement e, Void p) {
            throw new IllegalArgumentException();
          }
        },
        null
    );
  }

  /**
   * Returns an {@link AnnotationMirror} for the annotation of type {@code annotationClass} on
   * {@code element}, or {@link Optional#empty()} if no such annotation exists. This method is a
   * safer alternative to calling {@link Element#getAnnotation} as it avoids any interaction with
   * annotation proxies.
   *
   * @param <A>             a type of {@linkplain Annotation}
   * @param element         the {@linkplain Element} whose annotation is inquired
   * @param annotationClass the annotation of interest passed as {@linkplain Class}
   * @return the {@linkplain Optional} of {@linkplain  AnnotationMirror} for the annotation of
   * type {@code annotationClass} on {@code element}, or {@linkplain Optional#empty()} if
   * no such annotation exists
   */
  public static <A extends Annotation> Optional<? extends AnnotationMirror> getAnnotationMirrorOfType(Element element, Class<A> annotationClass) {
    String annotationName = annotationClass.getCanonicalName();
    return annotationName == null ? Optional.empty() : getAnnotationMirrorOfType(element, annotationName);
  }

  /**
   * Returns an {@link AnnotationMirror} for the annotation of type {@code annotation} on {@code
   * element}, or {@link Optional#empty()} if no such annotation exists. This method is a safer
   * alternative to calling {@link Element#getAnnotation} as it avoids any interaction with
   * annotation proxies.
   *
   * @param element           the {@linkplain Element} whose annotation mirror is inquired
   * @param annotationElement the annotation of interest passed as {@linkplain TypeElement}
   * @return the {@linkplain Optional} of {@linkplain  AnnotationMirror} for the annotation of
   * type {@code annotation} on {@code element}, or {@linkplain Optional#empty()} if
   * no such annotation exists
   */
  public static Optional<? extends AnnotationMirror> getAnnotationMirrorOfType(Element element, TypeElement annotationElement) {
    return getAnnotationMirrorOfType(element, annotationElement.getQualifiedName().toString());
  }

  /**
   * Returns an {@link AnnotationMirror} for the annotation whose type's canonical name is
   * {@code annotationCanonicalName} and reside on {@code element}, or {@link Optional#empty()}
   * if no such annotation exists. This method is a safer alternative to calling
   * {@link Element#getAnnotation} as it avoids any interaction with annotation proxies.
   *
   * @param element                 the {@linkplain Element} whose annotation mirror is inquired
   * @param annotationCanonicalName the canonical name of the annotation of interest
   *                                passed as {@linkplain String}
   * @return the {@linkplain Optional} of {@linkplain  AnnotationMirror} for the annotation
   * of type {@code annotationCanonicalName} on {@code element}, or
   * {@linkplain Optional#empty()} if no such annotation exists
   */
  public static Optional<? extends AnnotationMirror> getAnnotationMirrorOfType(Element element, String annotationCanonicalName) {
    return element.getAnnotationMirrors().stream()
        .filter(annotationMirror -> annotationMirror.getAnnotationType().toString().equals(annotationCanonicalName))
        .findAny();
  }

  /* ********************************************************************* */
  /* Castings ************************************************************ */
  /* ********************************************************************* */

  /**
   * Returns the given {@link Element} instance as {@link PackageElement}.
   *
   * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
   * always be used over that idiom as instructed in the documentation for {@link Element}.
   *
   * @param element the element to be casted to {@linkplain PackageElement}
   * @return the {@linkplain PackageElement} of the given element
   * @throws NullPointerException     if {@code element} is {@code null}
   * @throws IllegalArgumentException if {@code element} isn't a {@link PackageElement}
   */
  public static PackageElement asPackage(Element element) {
    return element.accept(CastingToPackageElementVisitor.INSTANCE, null);
  }

  /**
   * Returns the given {@link Element} instance as {@link TypeElement}.
   *
   * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
   * always be used over that idiom as instructed in the documentation for {@link Element}.
   *
   * @param element the element to be casted to {@linkplain TypeElement}
   * @return the {@linkplain TypeElement} of the given element
   * @throws NullPointerException     if {@code element} is {@code null}
   * @throws IllegalArgumentException if {@code element} isn't a {@link TypeElement}
   */
  public static TypeElement asTypeElement(Element element) {
    return element.accept(CastingToTypeElementVisitor.INSTANCE, null);
  }

  /**
   * Returns the given {@link Element} instance as {@link ExecutableElement}.
   *
   * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
   * always be used over that idiom as instructed in the documentation for {@link Element}.
   *
   * @param element the element to be casted to {@linkplain ExecutableElement}
   * @return the {@linkplain ExecutableElement} of the given element
   * @throws NullPointerException     if {@code element} is {@code null}
   * @throws IllegalArgumentException if {@code element} isn't a {@link ExecutableElement}
   */
  public static ExecutableElement asExecutable(Element element) {
    return element.accept(CastingToExecutableElementVisitor.INSTANCE, null);
  }

  /**
   * Returns the given {@link Element} instance as {@link TypeParameterElement}.
   *
   * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
   * always be used over that idiom as instructed in the documentation for {@link Element}.
   *
   * @param element the element to be casted to {@linkplain TypeParameterElement}
   * @return the {@linkplain TypeParameterElement} of the given element
   * @throws NullPointerException     if {@code element} is {@code null}
   * @throws IllegalArgumentException if {@code element} isn't a {@link TypeParameterElement}
   */
  public static TypeParameterElement asTypeParameter(Element element) {
    return element.accept(CastingToTypeParameterElementVisitor.INSTANCE, null);
  }

  /**
   * Returns the given {@link Element} instance as {@link VariableElement}.
   *
   * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
   * always be used over that idiom as instructed in the documentation for {@link Element}.
   *
   * @param element the element to be casted to {@linkplain VariableElement}
   * @return the {@linkplain VariableElement} of the given element
   * @throws NullPointerException     if {@code element} is {@code null}
   * @throws IllegalArgumentException if {@code element} isn't a {@link VariableElement}
   */
  public static VariableElement asVariable(Element element) {
    return element.accept(CastingToVariableElementVisitor.INSTANCE, null);
  }

  private abstract static class CastingElementVisitor<T> extends SimpleElementVisitor9<T, Void> {
    private final String label;

    CastingElementVisitor(String label) {
      this.label = label;
    }

    @Override
    protected final T defaultAction(Element e, Void ignore) {
      throw new IllegalArgumentException(e + " does not represent a " + label);
    }
  }


  private static final class CastingToPackageElementVisitor extends CastingElementVisitor<PackageElement> {
    private static final CastingToPackageElementVisitor INSTANCE = new CastingToPackageElementVisitor();

    CastingToPackageElementVisitor() {
      super("package element");
    }

    @Override
    public PackageElement visitPackage(PackageElement e, Void ignore) {
      return e;
    }
  }


  private static final class CastingToTypeElementVisitor extends CastingElementVisitor<TypeElement> {
    private static final CastingToTypeElementVisitor INSTANCE = new CastingToTypeElementVisitor();

    CastingToTypeElementVisitor() {
      super("type element");
    }

    @Override
    public TypeElement visitType(TypeElement e, Void ignore) {
      return e;
    }
  }


  private static final class CastingToExecutableElementVisitor extends CastingElementVisitor<ExecutableElement> {
    private static final CastingToExecutableElementVisitor INSTANCE = new CastingToExecutableElementVisitor();

    CastingToExecutableElementVisitor() {
      super("executable element");
    }

    @Override
    public ExecutableElement visitExecutable(ExecutableElement e, Void ignore) {
      return e;
    }
  }


  private static final class CastingToTypeParameterElementVisitor extends CastingElementVisitor<TypeParameterElement> {
    private static final CastingToTypeParameterElementVisitor INSTANCE = new CastingToTypeParameterElementVisitor();

    CastingToTypeParameterElementVisitor() {
      super("type parameter element");
    }

    @Override
    public TypeParameterElement visitTypeParameter(TypeParameterElement e, Void ignore) {
      return e;
    }
  }


  private static final class CastingToVariableElementVisitor extends CastingElementVisitor<VariableElement> {
    private static final CastingToVariableElementVisitor INSTANCE = new CastingToVariableElementVisitor();

    CastingToVariableElementVisitor() {
      super("variable element");
    }

    @Override
    public VariableElement visitVariable(VariableElement e, Void ignore) {
      return e;
    }
  }

}
