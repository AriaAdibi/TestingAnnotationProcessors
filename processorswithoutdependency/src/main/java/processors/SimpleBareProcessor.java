package processors;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;

@SupportedAnnotationTypes("customannotations.Epilogue")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class SimpleBareProcessor extends BareProcessor{

  @Override boolean processEach(TypeElement annotation, Element element) throws IOException {
    JavaFileObject jFile = processingEnv.getFiler().createSourceFile("epilogueprinter.TheEnd");
    try (PrintWriter out = new PrintWriter(jFile.openWriter())) {

      out.print(
              "package epilogueprinter;\n\n" +
              "public class TheEnd {\n" +
              "    public static void main(String[] args) {\n" +
              "        System.out.println( \"---- The End ----\" );\n" +
              "    }\n" +
              "}"
      );

    }

    return false;
  }

}
