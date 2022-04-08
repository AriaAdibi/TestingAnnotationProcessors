package baseprocessors;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Generated;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * {@inheritDoc}
 *
 * <h3>Processor dependent utility methods</h3>
 *
 * <p>This base annotation processor includes frequently used
 * processor-environment dependent utility methods for annotation processing.
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
   * Creates a source java file in {@code packageName} directory and writes the content
   * of the given {@link TypeSpec} in it.
   *
   * @param packageName the package name of new java file.
   * @param typeSpec    the content of the new file given as {@linkplain TypeSpec}
   */
  public void javaFileWriteTo(String packageName, TypeSpec typeSpec) {
    JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
    javaFileWriteTo(javaFile);
  }

  /**
   * Receives a {@link JavaFile} and creates a source java file representing
   * it using the processors' {@link javax.annotation.processing.Filer}.
   * Reports if IO problem occurs.
   *
   * @param javaFile the java file to be created.
   */
  public void javaFileWriteTo(JavaFile javaFile) {
    try {
      javaFile.writeTo(filer);
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "(IOExceptione) Annotation processor " +
          this.getClass().toString() + "'s javaFileWriteTo() failed unexpectedly:: " + e.getMessage());
      e.printStackTrace();
    }
  }

}
