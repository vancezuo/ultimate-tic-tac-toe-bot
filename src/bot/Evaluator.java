package bot;

import static bot.Util.PLAYER_1;
import static bot.Util.PLAYER_2;

/**
 * @author Vance Zuo
 */
public class Evaluator {
  public static final int PLAYER_MAX = PLAYER_1;
  public static final int PLAYER_MIN = PLAYER_2;

  public static final int MAX_SCORE = 1 << 30;
  public static final int MIN_SCORE = -MAX_SCORE;

  private Game game;

  public Evaluator(Game game) {
    this.game = game;
  }

  public int score() {
    return 0;
  }

  public boolean hasFinishedGame() {
    return game.isFinished();
  }

  public int getCurrentPlayer() {
    return game.getCurrentPlayer();
  }

  public Iterable<Integer> generateMoves() {
    return game.unsafeGenerateMoves();
  }

  public void doMove(int move) {
    game.unsafeDoMove(move);
  }

  public void undoMove() {
    game.unsafeUndoMove();
  }
}
