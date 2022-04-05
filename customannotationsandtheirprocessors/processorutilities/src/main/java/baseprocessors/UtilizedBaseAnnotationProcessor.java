package baseprocessors;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * {@inheritDoc}
 *
 * <h3>Kapteyn Team</h3>
 *
 * <p>This base annotation processor also includes frequently used methods for
 * annotation processing within Kapteyn Team.
 *
 * @author Aria Adibi
 * <p>For the original authors please refere to
 * <a href="https://github.com/google/auto"> Auto.Common </a> collection.
 */
public abstract class UtilizedBaseAnnotationProcessor extends BaseAnnotationProcessor {

  /* ********************************************************************* */
  /* JavaPoet (processor dependant) utilities **************************** */
  /* ********************************************************************* */

  /**
   * Returns an {@link AnnotationSpec} for {@link Generated} annotation containing
   * the name of the generating processor, and the date and time of the creation.
   *
   * @return Returns an {@link AnnotationSpec} for {@link Generated} annotation
   * containing the name of the generating processor, and the date and time of the
   * creation.
   */
  public AnnotationSpec getDefaultGeneratedAnnotationSpec() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", this.getClass().getCanonicalName())
        .addMember("date", "$S", LocalDateTime.now())
        .build();
  }

  /**
   * Creates a java file in {@code packageName} directory and writes the content
   * of the given {@link TypeSpec} in it.
   *
   * @param packageName the package name of new java file.
   * @param typeSpec    the content of the new file given as {@linkplain TypeSpec}
   */
  public void javaFileWriteTo(String packageName, TypeSpec typeSpec) {
    JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

    try {
      javaFile.writeTo(filer);
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "Annotation processor " + this.getClass().toString() + "'s javaFileWriteTo() failed unexpectedly:: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /* ********************************************************************* */
  /* Common processor dependent utilities ******************************** */
  /* ********************************************************************* */
  
  /**
   * Returns the value (possibly default value) of the {@code memberName} member
   * element of annotation {@code annotationMirror}, and {@linkplain Optional#empty()}
   * if no such value/member element exists.
   *
   * @param memberName       the name of the member element of {@code annotationMirror}
   * @param annotationMirror the investigated {@linkplain AnnotationMirror}
   */
  public <T> T getMemberValueOfAnnotation(AnnotationMirror annotationMirror, String memberName) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> annotEltVals = eltUtils.getElementValuesWithDefaults(annotationMirror);

    //noinspection unchecked
    return (T) annotEltVals.entrySet().stream()
        .filter(entry -> entry.getKey().getSimpleName().toString().equals(memberName))
        .findAny()
        .orElseThrow()
        .getValue().getValue();
  }

}
