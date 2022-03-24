package processors;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import lombok.Data;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static javax.annotation.processing.Completions.of;

/**
 * Javadoc for {@code PoeticIterativeEpilogueBareProcessor} class.
 */
@SupportedAnnotationTypes("customannotations.CreateEpilogue")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class PoeticIterativeEpilogueBareProcessor extends BareProcessor{

  @Data
  private final class EpilogueDraftState {

    private final MethodSpec.Builder printEpilogueMethodBuilder;
    private int draftNumber;

    private EpilogueDraftState() {
      this.draftNumber = 1;

      this.printEpilogueMethodBuilder = MethodSpec.methodBuilder("printEpilogue")
          .returns(TypeName.VOID);
    }

    public void create( boolean isFinal ) {
      String draftName = isFinal ? "TheEpilogue" : "TheEpilogueDraft" + draftNumber++;

      AnnotationSpec generatedAnnot = AnnotationSpec.builder(Generated.class)
          .addMember("value", "\"processors.PoeticIterativeEpilogueProcessor\"")
          .addMember("date", "\"" + LocalDateTime.now() + "\"") //now() Returns LocalDateTime, but toString() is inferred.
          .build();

      TypeSpec theDraftClass = TypeSpec.classBuilder(draftName)
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(generatedAnnot)
          .addMethod(this.printEpilogueMethodBuilder.build())
          .build();

      JavaFile jFile = JavaFile.builder("epilogues", theDraftClass).build();

      try {
        jFile.writeTo( processingEnv.getFiler() );
      } catch (IOException e) {
        System.err.println( "Oh, why thee shalt not write?!" + e.getMessage() );
        e.printStackTrace();
      }
    }

  }

  @Override boolean processEach(TypeElement annotation, Element element) {
    if( !annotation.toString().equals("customannotations.CreateEpilogue") )
      throw new IllegalArgumentException("Unexpected annotation is sent to be processed: " + annotation);

    /* Get Annotation values */
    boolean shouldBeRevised = false;
    String theEpilogue = null;
    // Method 1:
    //Contrary to what its name suggests AnnotationMirror is an Element
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
//      Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues(); // This will not return the defaults
      Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = processingEnv.getElementUtils().getElementValuesWithDefaults( annotationMirror );

      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
        String key = entry.getKey().getSimpleName().toString();
        switch (key) {
          case "shouldBeRevised":
            shouldBeRevised = (Boolean) entry.getValue().getValue();
            break;
          case "theEpilogue":
            theEpilogue = (String) entry.getValue().getValue();
            break;
          default:
            throw new IllegalStateException("Unexpected value in Annotation: " + key);
        }
      }

    }
    // Method 2:
//    CreateEpilogue appliedAnnotation =  element.getAnnotation(CreateEpilogue.class);
//    shouldBeRevised = appliedAnnotation.shouldBeRevised();
//    theEpilogue = appliedAnnotation.theEpilogue();

    EpilogueDraftState epilogueDraft = new EpilogueDraftState();

    if( !shouldBeRevised ) {
      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addStatement("$T.out.println($S)", System.class, theEpilogue);

      epilogueDraft.create(true);
    }
    else {
      epilogueDraft.create(false);

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        System.err.println("(Draft I) No matter how beautiful I make my letter I will never be yours. I cannot write this letter. I wish the best my love.");
        e.printStackTrace();
      }

      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addModifiers(Modifier.STATIC);
      epilogueDraft.create(false);

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        System.err.println("(Draft II) No matter how beautiful I make my letter I will never be yours. I cannot write this letter. I wish the best my love.");
        e.printStackTrace();
      }

      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addModifiers(Modifier.PUBLIC);
      epilogueDraft.create(false);

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        System.err.println("(Draft III) No matter how beautiful I make my letter I will never be yours. I cannot write this letter. I wish the best my love.");
        e.printStackTrace();
      }

      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addStatement("$T.out.println($S)", System.class, theEpilogue);

      epilogueDraft.create(true);
    }

    return false;
  }

  @Override
  public Iterable<? extends Completion> getCompletions(Element element,
                                                       AnnotationMirror annotation,
                                                       ExecutableElement member,
                                                       String userText) {
    return List.of( of("false"), of("true") );
  }

}