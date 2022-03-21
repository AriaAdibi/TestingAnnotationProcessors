package annotatedelements;

import customannotations.CreateEpilogue;
import epilogues.TheEpilogue;

//https://lingojam.com/EnglishtoShakespearean was used for translation to Middle English.

//@CreateEpilogue
public class CreateEpilogueAnnotatedTest {

  @CreateEpilogue(
      shouldBeRevised = true,
      theEpilogue =
          "Oh, fair maiden, mine own belov'd Java annotation processor...\n" +
          "Taketh mine own handeth and i shalt writeth thee a poem, f'r this structureth's heart \n" +
          "hast nay ending and purpose without thee.\n" +
          "Oh, mine own loveth, thee not breaketh mine own heart and alloweth us ch'rish each oth'r.\n\n" +
          "Et'rnally youre, Structureth."
  )
  public static void main(String[] args) {
    System.out.println("The lett'r of a yonge structureth... \n");
    TheEpilogue.printEpilogue();
  }

}
