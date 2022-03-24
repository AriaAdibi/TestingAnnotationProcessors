/**
 * Helper utilities for writing annotation processors. This work is heavily influenced by Google's
 * <a href="https://github.com/google/auto"> Auto.Common </a> collection.
 *
 * @author Aria Adibi
 */
module processorutilities {
  requires static com.google.auto.service;
  requires static java.compiler;
  requires static com.google.common;
  requires static lombok;

  requires static annotations;
  requires static com.squareup.javapoet;

//  provides javax.annotation.processing.Processor with processors.PoeticIterativeEpilogueProcessor;
}
