package annotatedelements;

import customannotations.Epilogue;

/**
 * Javadoc for {@code AnnotatedMethod} class.
 */
public class AnnotatedMethod {

  /**
   * Javadoc the main() method of the application.
   * @param args application arguments.
   */
  @Epilogue(nLines = 3)
  public static void main(String[] args) {
    System.err.println("The journey of a young structure...");
  }

}
