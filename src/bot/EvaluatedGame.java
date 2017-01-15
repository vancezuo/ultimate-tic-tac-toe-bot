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
  public static final int DRAW_SCORE = 0;

  private ZobristKey key;
  private Weights weights;
  private Features features;

  public EvaluatedGame() {
    this(new Weights());
  }

  public EvaluatedGame(Weights weights) {
    super();
    init(weights);
  }

  public EvaluatedGame(EvaluatedGame game) {
    super(game);
    init(game.weights);
  }

  private void init(Weights weights) {
    this.key = new ZobristKey(this);
    this.weights = weights;
    this.features = new Features(this);
  }

  public long getZobristKey() {
    return key.getKey();
  }

  public int evaluate() {
    return features.score(weights);
  }

  public void unsafeDoMove(int move) {
    int player = getCurrentPlayer();
    int prevNextMacroInd = getNextMacroIndex();

    super.unsafeDoMove(move);

    int currentNextMacroInd = getNextMacroIndex();

    key.updateForIndex(move, player);
    key.updateForNextMacroInd(prevNextMacroInd, currentNextMacroInd);
  }

  public void unsafeUndoMove() {
    int move = getLastMove();
    int currentNextMacroInd = getNextMacroIndex();

    super.unsafeUndoMove();

    int player = getCurrentPlayer();
    int prevNextMacroInd = getNextMacroIndex();

    key.updateForIndex(move, player);
    key.updateForNextMacroInd(currentNextMacroInd, prevNextMacroInd);
  }
}
