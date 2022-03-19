import processors.IterativeSimpleBareProcessor;
/**
 * Javadoc for {@code processorswithoutdependency} module.
 */
module processorswithoutdependency {
  requires annotations; // nonstatic ... for element.getAnnotation() core reflection ...
//  requires static com.google.auto.service;
  requires static com.squareup.javapoet;

  requires static java.compiler;
//  provides javax.annotation.processing.Processor with IterativeSimpleBareProcessor;
}
