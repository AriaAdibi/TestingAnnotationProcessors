import processors.SimpleGCommonProcessor;

module processorswithdependency.googlecommon {
  requires auto.common;
  requires static com.google.common;

  requires static java.compiler;
  provides javax.annotation.processing.Processor with SimpleGCommonProcessor ;
}
