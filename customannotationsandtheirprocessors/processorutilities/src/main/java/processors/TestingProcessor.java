package processors;

import baseprocessors.BaseAnnotationProcessor;
import baseprocessors.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.util.Set;

@SupportedAnnotationTypes("customannotations.TestThis")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class TestingProcessor extends BaseAnnotationProcessor {

  @Override protected void preRoundProcess(RoundEnvironment roundEnv) {}

  @Override protected void postRoundProcess(RoundEnvironment roundEnv) {}

  @Override protected Iterable<? extends ProcessingStep> processingSteps() {
    return ImmutableSet.of(
        new ProcessingStep() {
          @Override public Set<String> annotations() {
            return ImmutableSet.of("customannotations.TestThis");
          }

          @Override public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
            String key = Iterables.getOnlyElement( elementsByAnnotation.keySet() );
            Element element = Iterables.getOnlyElement( elementsByAnnotation.values() );
            PackageElement pkg = MoreElements.getPackage( element );
            return ImmutableSet.of();
          }
        }
    );
  }

}
