/**
 * This package provides a base annotation processor called {@code KapteynBaseAnnotationProcessor} to help ease use of the annotation processing environment.
 *
 * <p> <em>The contents of this package are highly influenced by Google's <a href="https://github.com/google/auto"> Auto.Common </a> collection.</em>
 *
 * <h3>Classes:</h3>
 * <ul>
 *    <li><strong>SuperficialValidation:</strong> very simple scanner to ensure an Element is valid and free from distortion from upstream compilation errors</li>
 *    <li><strong>BaseAnnotationProcessor/ProcessingStep:</strong> simple types that
 *        <ul>
 *          <li>implement a validating annotation processor</li>
 *          <li>defer invalid elements to later rounds if they cannot be processed</li>
 *          <li>break processor actions into multiple processing steps (which each may handle different annotations)</li>
 *        </ul>
 *    </li>
 *    <li><strong>UtilizedBaseAnnotationProcessor:</strong>
 *      Adds some frequently used processor-environment dependent utility methods to the BaseAnnotationProcessor.
 *    </li>
 * </ul>
 *
 * @author Aria Adibi
 * <p> For the original authors please refere to <a href="https://github.com/google/auto"> Auto.Common </a> collection.
 */
package baseprocessors;

