module processorswithoutdependency {
  requires annotations; //Needed to be non-static, since methods like element.getAnnotation() are runtime core reflection
  requires static com.squareup.javapoet;

  requires static java.compiler;
  provides javax.annotation.processing.Processor with processors.IterativeSimpleBareProcessor;
}
