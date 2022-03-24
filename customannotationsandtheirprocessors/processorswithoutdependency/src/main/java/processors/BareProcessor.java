package processors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

abstract class BareProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    boolean claimAnnotations = false;

    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

      for(Element element : annotatedElements) {
        try {
          claimAnnotations |= processEach(annotation, element);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return claimAnnotations;

  }

  abstract boolean processEach(TypeElement annotation, Element element) throws IOException;

}
