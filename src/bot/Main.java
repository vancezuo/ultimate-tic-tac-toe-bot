package bot;

import tuning.BotEntry;
import tuning.GeneticAlgorithm;
import tuning.Tournament;

import static bot.Util.index;

/**
 * @author Vance Zuo
 */
public class Main {
  public static void main(String[] args) {
    runBot();
  }

  public static void runBot() {
    new Bot().run();
  }

  public static void runTournament() {
    Tournament t = new Tournament(4, 36, BotEntry::getElo);
    t.run(new Tournament.RunParameters(6, 4, 8, 8));
  }

  public static void runGeneticAlgorithm() {
    GeneticAlgorithm ga = new GeneticAlgorithm();
    ga.prepareNew(new GeneticAlgorithm.PrepareParameters(6, 16));
    ga.prepareContinue();
    ga.run(new GeneticAlgorithm.RunParameters(-1, 0.05, 0.1, 4, 5, 8, 2, 6, 64, 0.125, 0.25));
  }

  public static void runTestMatch() {
    Weights w1 = new Weights();
    Weights w2 = new Weights();
    System.out.println(w1);
    System.out.println(w2);

    Match m = new Match(w1, w2);
    long startTime = System.nanoTime();
    System.out.println(m.play(10));
    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1000000;
    System.out.println(duration);
    System.out.println(m.getGame().getFormattedBoardString());
    System.out.println();
    System.out.println(m.getGame().getFormattedMacroboardString());
  }

  public static void runTestGame() {
    EvaluatedGame game = new EvaluatedGame();

    System.out.println(game.getZobristKey());
    System.out.println(game.doMove(index(4, 4)));

    System.out.println(game.getZobristKey());
    System.out.println(game.undoMove());
    System.out.println(game.getZobristKey());
    System.out.println(game.doMove(index(4, 4)));
    System.out.println(game.getZobristKey());

    System.out.println(game.doMove(index(3, 3)));
    System.out.println(game.doMove(index(1, 1)));
    System.out.println(game.doMove(index(3, 4)));
    System.out.println(game.doMove(index(1, 4)));
    System.out.println(game.doMove(index(3, 5)));
    System.out.println(game.doMove(index(1, 7)));
    System.out.println(game.doMove(index(4, 1)));
    System.out.println(game.doMove(index(3, 8))); // middle
    System.out.println(game.doMove(index(0, 8)));
    System.out.println(game.doMove(index(2, 8)));
    System.out.println(game.doMove(index(6, 6)));
    System.out.println(game.doMove(index(2, 0)));
    System.out.println(game.doMove(index(6, 1)));
    System.out.println(game.doMove(index(2, 4)));
    System.out.println(game.doMove(index(6, 4)));
    System.out.println(game.doMove(index(0, 4)));
    System.out.println(game.doMove(index(0, 0)));
    System.out.println(game.doMove(index(0, 2)));
    System.out.println(game.doMove(index(0, 7)));
    System.out.println(game.doMove(index(0, 6))); // end
    System.out.println(game);
    System.out.println(game.getFormattedBoardString());
    System.out.println(game.getFormattedMacroboardString());
    for (int i = 0; i < 21; i++) {
      System.out.println(game.undoMove());
    }
  }
}
