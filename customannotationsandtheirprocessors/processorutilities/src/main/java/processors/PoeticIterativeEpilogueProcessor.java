package processors;

import baseprocessors.UtilizedBaseAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.Data;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Map;

@SupportedAnnotationTypes("customannotations.CreateEpilogue")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class PoeticIterativeEpilogueProcessor extends UtilizedBaseAnnotationProcessor {

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

      TypeSpec theDraftClass = TypeSpec.classBuilder(draftName)
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation( getDefaultGeneratedAnnotationSpec() )
          .addMethod(this.printEpilogueMethodBuilder.build())
          .build();

//      if( !isFinal ) {
//        FileObject jfo = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "epilogues", theDraftClass.toString()+".txt");
//        try {
//          jfo.openWriter().write(
//        } catch (IOException e) {
//          System.err.println("Oh, why thee shalt not write?!" + e.getMessage());
//          e.printStackTrace();
//        }
//      }
//      else
        javaFileWriteTo( "epilogues", theDraftClass);

    }

  }

  @Override protected void preRoundProcess(RoundEnvironment roundEnv) { /* No pre-round process is needed. */ }

  @Override protected void postRoundProcess(RoundEnvironment roundEnv) { /* No post-round process is needed. */ }

  @Override
  protected Iterable<? extends ProcessingStep> processingSteps() {
    return ImmutableSet.of(
        new ProcessingStep() {

          @Override
          public ImmutableSet<String> annotations() {
            return ImmutableSet.of("customannotations.CreateEpilogue");
          }

          @Override
          public ImmutableSet<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {

            for (Element element : elementsByAnnotation.values()) {

              /* Get Annotation values */
              boolean shouldBeRevised = false;
              String theEpilogue = null;
              // Method 1:
              //Contrary to what its name suggests AnnotationMirror is an Element
              for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
//                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues(); // This will not return the defaults
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = eltUtils.getElementValuesWithDefaults( annotationMirror );

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
//              CreateEpilogue appliedAnnotation =  element.getAnnotation(CreateEpilogue.class);
//              shouldBeRevised = appliedAnnotation.shouldBeRevised();
//              theEpilogue = appliedAnnotation.theEpilogue();

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

            }

            return ImmutableSet.of();
          }

        }
        );
  }

}
