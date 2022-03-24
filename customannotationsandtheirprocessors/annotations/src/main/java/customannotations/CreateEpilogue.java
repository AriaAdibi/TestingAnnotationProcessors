package customannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//https://lingojam.com/EnglishtoShakespearean was used for translation to Middle English.

/**
 * This annotation signals that a java file named {@code Epilouge} will be created that contains
 * {@code printEpilogue()} method.
 *
 * <p> The content of the epilogue can be given as the argument or the default epilogue will be created.
 *
 * <p> Revising the epilogue can also be requested. In this case two java files representing the
 * draft versions will also be created.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface CreateEpilogue {
  /**
   * @return {@code true} if revision is requested. In this case two more java files representing
   * the draft versions of the epilogue will also be created. Default is {@code false}.
   */
  boolean shouldBeRevised() default false;

  String theEpilogue() default "Thee bethink I cannot writeth an ending poem.  O' ye, of dram faith\nAlas, thou art right and i shalt beest f'rev'r silenc'd.";
}
