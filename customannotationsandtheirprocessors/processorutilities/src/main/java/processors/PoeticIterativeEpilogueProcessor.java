package processors;

import baseprocessors.UtilizedBaseAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.*;
import lombok.Data;
import utils.AnnotationMirrors;
import utils.AnnotationValues;
import utils.MoreElements;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Set;

@SupportedAnnotationTypes({"customannotations.ReceiveTheLetter", "customannotations.CreateEpilogue"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class PoeticIterativeEpilogueProcessor extends UtilizedBaseAnnotationProcessor {

  @Override protected void preRoundProcess(RoundEnvironment roundEnv) { /* No pre-round process is needed. */ }

  @Override protected void postRoundProcess(RoundEnvironment roundEnv) { /* No post-round process is needed. */ }

  @Override
  protected Iterable<? extends ProcessingStep> processingSteps() {
    return ImmutableSet.of(

        new ProcessingStep() {
          @Override public Set<String> annotations() {
            return ImmutableSet.of("customannotations.ReceiveTheLetter");
          }

          @Override public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
            AnnotationSpec theEpilogueAnnotationSpec =
                AnnotationSpec.builder(customannotations.CreateEpilogue.class)
                    .addMember("shouldBeRevised", "$L", true)
                    .addMember("theEpilogue", "$S",
                        "Oh, fair maiden, mine own belov'd Java annotation processor...\n" +
                            "Taketh mine own handeth and i shalt writeth thee a poem, f'r this structureth's heart \n" +
                            "hast nay ending and purpose without thee.\n" +
                            "Oh, mine own loveth, thee not breaketh mine own heart and alloweth us ch'rish each oth'r.\n\n" +
                            "Et'rnally youre, Structureth."
                    )
                    .build();

            TypeSpec theLetter = TypeSpec.classBuilder("YongeStructurethLoveLetter")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getDefaultGeneratedAnnotationSpec())
                .addMethod(
                    MethodSpec.methodBuilder("main")
                        .addAnnotation(theEpilogueAnnotationSpec)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(TypeName.VOID)
                        .addParameter(TypeName.get(String[].class), "args")
                        .addStatement("$T.out.println($S)",
                            System.class,
                            "The lett'r of a yonge structureth... \n"
                        )
                        .addStatement("$T.printEpilogue()",
                            ClassName.get("epilogues", "TheEpilogue")
                        )
                        .build()
                )
                .build();

            javaFileWriteTo("letters", theLetter);

            return ImmutableSet.of();
          }
        },

        new ProcessingStep() {
          @Override
          public ImmutableSet<String> annotations() {
            return ImmutableSet.of("customannotations.CreateEpilogue");
          }

          @Override
          public ImmutableSet<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
            for (var elementByAnnotation : elementsByAnnotation.entries()) {

              Element element = elementByAnnotation.getValue();
              @SuppressWarnings("OptionalGetWithoutIsPresent")
              AnnotationMirror createEpiloueAnnotationMirror =
                  MoreElements.getAnnotationMirrorOfType(element, elementByAnnotation.getKey()).get();

              /* Get Annotation values */
              @SuppressWarnings("UnusedAssignment")
              boolean shouldBeRevised = false;
              @SuppressWarnings("UnusedAssignment")
              String theEpilogue = null;

              /* Method 1: Using the added utilities*/
              shouldBeRevised = AnnotationValues.getBoolean(
                  AnnotationMirrors.getAnnotationValue(createEpiloueAnnotationMirror, "shouldBeRevised")
              );
              theEpilogue = AnnotationValues.getString(
                  AnnotationMirrors.getAnnotationValue(createEpiloueAnnotationMirror, "theEpilogue")
              );

              /* Method 2: */
              //              CreateEpilogue appliedAnnotation =  element.getAnnotation(CreateEpilogue.class);
              //              shouldBeRevised = appliedAnnotation.shouldBeRevised();
              //              theEpilogue = appliedAnnotation.theEpilogue();

              /* Method 3: */
              // Safe even for Class<?> types. Method 2 would throw Mirror exception. One can modufy method 2 to catch the exception
              // and the retreive the data from the mirror that is passed by the exception. But seems like a dirty hack.
              //              //Contrary to what its name suggests AnnotationMirror is an Element
              //              for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
              //                //                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues(); // This will not return the defaults
              //                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = eltUtils.getElementValuesWithDefaults(annotationMirror);
              //
              //                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
              //                  String key = entry.getKey().getSimpleName().toString();
              //                  switch (key) {
              //                    case "shouldBeRevised":
              //                      shouldBeRevised = (Boolean) entry.getValue().getValue();
              //                      break;
              //                    case "theEpilogue":
              //                      theEpilogue = (String) entry.getValue().getValue();
              //                      break;
              //                    default:
              //                      throw new IllegalStateException("Unexpected value in Annotation: " + key);
              //                  }
              //                }
              //
              //              }

              writeTheEpilogue(shouldBeRevised, theEpilogue);
            }

            return ImmutableSet.of();
          }
        }

    );
  }

  private void writeTheEpilogue(boolean shouldBeRevised, String theEpilogue) {
    EpilogueDraftState epilogueDraft = new EpilogueDraftState();

    if (!shouldBeRevised) {
      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addStatement("$T.out.println($S)", System.class, theEpilogue);

      epilogueDraft.create(true);
    } else {
      epilogueDraft.create(false);

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        System.err.println("(Draft I) nay matt'r how quite quaint i maketh mine own lett'r i shall nev'r beest yours."
            + "I cannot writeth this lett'r.  I give you the most wondrous mine own loveth");
        e.printStackTrace();
      }

      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addModifiers(Modifier.STATIC);
      epilogueDraft.create(false);

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        System.err.println("(Draft II) nay matt'r how quite quaint i maketh mine own lett'r i shall nev'r beest yours."
            + "I cannot writeth this lett'r.  I give you the most wondrous mine own loveth");
        e.printStackTrace();
      }

      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addModifiers(Modifier.PUBLIC);
      epilogueDraft.create(false);

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        System.err.println("(Draft III) nay matt'r how quite quaint i maketh mine own lett'r i shall nev'r beest yours."
            + "I cannot writeth this lett'r.  I give you the most wondrous mine own loveth");
        e.printStackTrace();
      }

      epilogueDraft.getPrintEpilogueMethodBuilder()
          .addStatement("$T.out.println($S)", System.class, theEpilogue);

      epilogueDraft.create(true);
    }
  }

  @Data
  private final class EpilogueDraftState {

    private final MethodSpec.Builder printEpilogueMethodBuilder;
    private int draftNumber;

    private EpilogueDraftState() {
      this.draftNumber = 1;

      this.printEpilogueMethodBuilder = MethodSpec.methodBuilder("printEpilogue")
          .returns(TypeName.VOID);
    }

    public void create(boolean isFinal) {
      String draftName = isFinal ? "TheEpilogue" : "TheEpilogueDraft" + draftNumber++;

      TypeSpec theDraftClass = TypeSpec.classBuilder(draftName)
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(getDefaultGeneratedAnnotationSpec())
          .addMethod(this.printEpilogueMethodBuilder.build())
          .build();

      javaFileWriteTo("epilogues", theDraftClass);
    }

  }

}
