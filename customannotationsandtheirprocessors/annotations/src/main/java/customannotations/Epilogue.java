package customannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation creates java files that are responsible for printing an epilogue.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Epilogue {
  /**
   * @return Number of lines in the produces epilogue. (Currently not used as intended.) Default is 0.
   */
  int nLines() default 0;
}
