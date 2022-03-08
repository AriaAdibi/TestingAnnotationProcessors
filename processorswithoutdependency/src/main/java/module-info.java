module processorswithoutdependency {
  requires static annotations;
  requires static com.google.auto.service;
  requires static com.squareup.javapoet;
  requires static java.compiler;
  provides javax.annotation.processing.Processor with processors.IterativeSimpleBareProcessor;
}
