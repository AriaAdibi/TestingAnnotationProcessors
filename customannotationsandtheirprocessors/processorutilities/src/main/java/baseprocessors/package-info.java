/**
 * This package provides a set of common utilities to help ease use of the annotation processing environment.
 *
 * <p> <em>The contents of this package are highly influenced by Google's <a href="https://github.com/google/auto"> Auto.Common </a> collection.</em>
 *
 * <h2>Utility classes of note</h2>
 * <ul>
 *    <li><strong>MoreTypes:</strong> utilities and {@link com.google.common.base.Equivalence Equivalence} wrappers for {@link javax.lang.model.type.TypeMirror TypeMirror} and related subtypes</li>
 *    <li><strong>MoreElements:</strong> utilities for {@link javax.lang.model.element.Element Element} and related subtypes</li>
 *    <li><strong>SuperficialValidation:</strong> very simple scanner to ensure an Element is valid and free from distortion from upstream compilation errors</li>
 *    <li><strong>BasicAnnotationProcessor/ProcessingStep:</strong> simple types that
 *        <ul>
 *          <li>implement a validating annotation processor</li>
 *          <li>defer invalid elements until later</li>
 *          <li>break processor actions into multiple processing steps (which each may handle different annotations)</li>
 *        </ul>
 *    </li>
 * </ul>
 *
 * @author Aria Adibi
 * <p> For the original authors please refere to <a href="https://github.com/google/auto"> Auto.Common </a> collection.
 */
package baseprocessors;
