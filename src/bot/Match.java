package bot;

import static bot.Util.PLAYER_1;

/**
 * @author Vance Zuo
 */
public class Match {
  private EvaluatedGame g1, g2;
  private Searcher p1, p2;

  public Match(Weights w1, Weights w2) {
    g1 = new EvaluatedGame(w1);
    g2 = new EvaluatedGame(w2);
    p1 = new Searcher(g1);
    p2 = new Searcher(g2);
  }

  public boolean manualMove(int move) {
    return g1.doMove(move) & g2.doMove(move);
  }

  public boolean botMove(int searchDepth) {
    int move;
    if (g1.getCurrentPlayer() == PLAYER_1) {
      move = p1.search(searchDepth + 1).getPVMove();
    } else {
      move = p2.search(searchDepth).getPVMove();
    }
    return manualMove(move);
  }

  public int play(int searchDepth) {
    while (!g1.isFinished()) {
      botMove(searchDepth);
    }
    if (!g1.hasWinner()) {
      return 0;
    }
    return g1.getWinner() == PLAYER_1 ? 1 : -1;
  }

  public Game getGame() {
    return g1;
  }
}
