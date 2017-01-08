package bot;

import static bot.Util.PLAYER_1;
import static bot.Util.PLAYER_2;

/**
 * @author Vance Zuo
 */
public class EvaluatedGame extends Game {
  public static final int PLAYER_MAX = PLAYER_1;
  public static final int PLAYER_MIN = PLAYER_2;

  public static final int MAX_SCORE = 1 << 30;
  public static final int MIN_SCORE = -MAX_SCORE;

  public EvaluatedGame() {
    super();
  }

  public EvaluatedGame(EvaluatedGame game) {
    super(game);
  }

  public int score() {
    if (!hasWinner()) {
      return 0;
    }
    return getWinner() == PLAYER_MAX ? MAX_SCORE : MIN_SCORE;
  }

  public Iterable<Integer> unsafeGenerateMoves() {
    return super.unsafeGenerateMoves();
  }

  public void unsafeDoMove(int move) {
    super.unsafeDoMove(move);
  }

  public void unsafeUndoMove() {
    super.unsafeUndoMove();
  }
}
