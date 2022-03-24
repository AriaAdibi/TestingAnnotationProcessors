/**
 * Javadoc for {@code processorswithdependency.googlecommon} module.
 */
module processorswithdependency.googlecommon {
  requires static com.google.auto.service;
  requires static auto.common;
  requires static com.google.common;
  requires static com.squareup.javapoet;
  requires static lombok;

  requires static java.compiler;
//  provides javax.annotation.processing.Processor with PoeticIterativeEpilogueGProcessor ;
}
