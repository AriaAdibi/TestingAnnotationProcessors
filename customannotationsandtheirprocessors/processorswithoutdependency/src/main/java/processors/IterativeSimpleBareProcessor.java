package processors;

import customannotations.Epilogue;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;

/**
 * Javadoc for {@code IterativeSimpleBareProcessor} class.
 */
@SupportedAnnotationTypes("customannotations.Epilogue")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class IterativeSimpleBareProcessor extends BareProcessor{
  int finalSum = 0;

  @Override boolean processEach(TypeElement annotation, Element element) throws IOException {
    System.err.println("****Entering IterativeSimpleBareProcessor .... ****");
    Annotation e = element.getAnnotation(Epilogue.class);
    var em = element.getAnnotationMirrors().get(0);
    int nLines = element.getAnnotation(Epilogue.class).nLines();
//    int nLines = element.getAnnotationMirrors().get(0);

    if( nLines <= 0 )
      return false;
    if(nLines > 3) {
      System.err.println("Oh Yee, alas nLines is strange.");
      System.exit(1);
    }

    JavaFileObject jFile = processingEnv.getFiler().createSourceFile("epilogueprinter.IterativeTheEnd" + nLines);
    try (PrintWriter out = new PrintWriter(jFile.openWriter())) {

      if(nLines == 3) {
        finalSum += nLines;
        out.print(
            "package epilogueprinter;\n\n" +
            "import customannotations.Epilogue;\n\n" +
            "public class IterativeTheEnd3 {\n" +
            "   @Epilogue(nLines = 2) \n" +
            "   public void anonfunc() {\n" +
            "   }\n" +
            "}"
        );
      }
      else if(nLines == 2) {
        finalSum += nLines;
        out.print(
            "package epilogueprinter;\n\n" +
            "import customannotations.Epilogue;\n\n" +
            "public class IterativeTheEnd2 {\n" +
            "   @Epilogue(nLines = 1) \n" +
            "   public static void main(String[] args) {\n" +
            "   }\n" +
            "}"
        );

      }
      else if (nLines == 1) {
        out.print(
            "package epilogueprinter;\n\n" +
            "import customannotations.Epilogue;\n\n" +
            "public class IterativeTheEnd1 {\n" +
            "   public static void main(String[] args) {\n" +
            "       System.out.println( \"---- The End ----\" );\n" +
            "   }\n" +
            "}"
        );
        finalSum += nLines;
        System.err.println(finalSum);
      }

    }

    return false;
  }

}
