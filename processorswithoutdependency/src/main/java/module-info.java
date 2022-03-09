module processorswithoutdependency {
  requires annotations;
  requires com.squareup.javapoet;
  requires java.compiler;
  provides javax.annotation.processing.Processor with processors.IterativeSimpleBareProcessor;

  exports processors;
}
