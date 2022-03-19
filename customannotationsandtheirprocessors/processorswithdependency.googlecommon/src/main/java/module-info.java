import processors.SimpleGCommonProcessor;

/**
 * Javadoc for {@code processorswithdependency.googlecommon} module.
 */
module processorswithdependency.googlecommon {
  requires auto.common;
  requires static com.google.common;

  requires static java.compiler;
  provides javax.annotation.processing.Processor with SimpleGCommonProcessor ;
}
