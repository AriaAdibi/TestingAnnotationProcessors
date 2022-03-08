package processors;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import customannotations.Epilogue;

import javax.annotation.processing.Generated;
import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.time.LocalDate;

@SupportedAnnotationTypes("customannotations.Epilogue")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class PoeticBareProcessor extends BareProcessor{

  @Override boolean processEach(TypeElement annotation, Element element) {
    System.err.println("*** *Entering PoeticBareProcessor .... ****");
    int nLines = element.getAnnotation(Epilogue.class).nLines();
    if( nLines > 0 )
      return false;

    MethodSpec mainMethod = MethodSpec.methodBuilder("main")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(TypeName.VOID)
        .addParameter(String[].class, "args")
        .addStatement("$T.out.println($S)", System.class,
            "Those blessèd structures, plot and rhyme—\n" +
            "why are they no help to me now\n" +
            "I want to make\n" +
            "something imagined, not recalled?\n" +
            "I hear the noise of my own voice:\n" +
            "The painter's vision is not a lens,\n" +
            "it trembles to caress the light.\n" +
            "But sometimes everything I write \n" +
            "with the threadbare art of my eye\n" +
            "seems a snapshot,\n" +
            "lurid, rapid, garish, grouped,\n" +
            "heightened from life,\n" +
            "yet paralyzed by fact.\n" +
            "All's misalliance.\n" +
            "Yet why not say what happened?\n" +
            "Pray for the grace of accuracy\n" +
            "Vermeer gave to the sun's illumination\n" +
            "stealing like the tide across a map\n" +
            "to his girl solid with yearning.\n" +
            "We are poor passing facts,\n" +
            "warned by that to give\n" +
            "each figure in the photograph\n" +
            "his living name.\n" +
            "---- The End ----"
        )
        .build();

    AnnotationSpec genAnnot = AnnotationSpec.builder(Generated.class)
        .addMember("value", "\"processors.PoeticBareProcessor\"")
        .addMember("date", "\"" + LocalDate.now().toString() + "\"")
        .build();

    TypeSpec PoeticTheEndClass = TypeSpec.classBuilder("PoeticTheEnd")
        .addAnnotation(genAnnot)
        .addModifiers(Modifier.PUBLIC)
        .addMethod(mainMethod)
        .build();

    JavaFile jFile = JavaFile.builder("epilogueprinter", PoeticTheEndClass)
        .build();

    try {
      jFile.writeTo( processingEnv.getFiler() );
    } catch (IOException e) {
      System.err.println( "Oh, why thee resist to write?!" + e.getMessage() );
      e.printStackTrace();
    }

    return false;
  }

}
