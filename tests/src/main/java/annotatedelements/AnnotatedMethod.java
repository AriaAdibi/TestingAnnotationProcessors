package annotatedelements;

import customannotations.Epilogue;

public class AnnotatedMethod {

  @Epilogue(nLines = 3)
  public static void main(String[] args) {
    System.err.println("The journey of a young structure...");
  }

}
