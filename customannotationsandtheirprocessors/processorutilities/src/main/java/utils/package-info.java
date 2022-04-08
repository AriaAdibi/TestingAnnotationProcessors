/**
 * This package provides a set of common static utilities to help ease the use of the
 * annotation processing environment.
 *
 * <p> <em>The contents of this package are highly influenced by Google's
 * <a href="https://github.com/google/auto"> Auto.Common </a> collection.</em>
 *
 * <h3>Utility classes</h3>
 * <ul>
 *    <li><strong>MoreTypes:</strong> More utilities in addition to the ones exists in
 *      {@link javax.lang.model.util.Types}, and {@link com.google.common.base.Equivalence Equivalence}
 *      wrappers for {@link javax.lang.model.type.TypeMirror TypeMirror} and related subtypes
 *    </li>
 *    <li><strong>MoreElements:</strong> More utilities in addition to the ones exists in
 *      {@link javax.lang.model.util.Elements}
 *    </li>
 *    <li><strong>AnnotationMirrors:</strong> Provides {@link com.google.common.base.Equivalence} for equivalency
 *      and the corresponding hashing. Also, provides static getter methods for annotation members.
 *    </li>
 *    <li><strong>AnnotationValues:</strong> Provides {@link com.google.common.base.Equivalence} for equivalency
 *      and the corresponding hashing. Also, provides static getter methods for annotation values.
 *    </li>
 *    <li><strong>AnnotationOutput:</strong> Provides string representation of
 *      {@link javax.lang.model.element.AnnotationMirror}s and {@link javax.lang.model.element.AnnotationValue}s
 *      suitable for inclusion in a Java source file as the initializer of a variable of the appropriate type.
 *    </li>
 * </ul>
 *
 * @author Aria Adibi
 * <p> For the original authors please refere to <a href="https://github.com/google/auto"> Auto.Common </a> collection.
 */
package utils;

