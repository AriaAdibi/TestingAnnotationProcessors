package processors;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;

@SupportedAnnotationTypes("customannotations.Epilogue")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
//@AutoService(Processor.class)
public class SimpleGCommonProcessor extends BasicAnnotationProcessor {
  @Override
  protected Iterable<? extends Step> steps() {
    return ImmutableSet.of(
        new Step() {
          @Override
          public ImmutableSet<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
            JavaFileObject jFile = null;
            try {
              jFile = processingEnv.getFiler().createSourceFile("epilogueprinter.GoogleCommonTheEnd");
            } catch (IOException e) {
              e.printStackTrace();
            }

            try (PrintWriter out = new PrintWriter(jFile.openWriter())) {

              out.print(
                  "package epilogueprinter;\n\n" +
                  "public class GoogleCommonTheEnd {\n" +
                  "   public static void main(String[] args) {\n" +
                  "       System.out.println( \"---- The End ----\" );\n" +
                  "   }\n" +
                  "}"
              );

            } catch (IOException e) {
              e.printStackTrace();
            }
            return ImmutableSet.of();
          }

          @Override
          public ImmutableSet<String> annotations() {return ImmutableSet.of("customannotations.Epilogue");
          }
        });
  }
}
