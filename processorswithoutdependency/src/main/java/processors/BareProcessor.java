package processors;

import javax.annotation.processing.*;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static javax.annotation.processing.Completions.of;

abstract class BareProcessor extends AbstractProcessor {
  int roundNum = 0;

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

    System.err.println("This is round " + ++roundNum + "the processing is over? " + roundEnv.processingOver() );

    return claimAnnotations;

  }

  @Override //TODO
  public Iterable<? extends Completion> getCompletions( Element element,
                                                        AnnotationMirror annotation,
                                                        ExecutableElement member,
                                                        String userText) {
    return List.of( of("1"), of("2"), of("5") );
  }

  abstract boolean processEach(TypeElement annotation, Element element) throws IOException;

}
