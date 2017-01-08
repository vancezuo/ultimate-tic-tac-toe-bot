package bot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static bot.EvaluatedGame.PLAYER_MAX;
import static bot.Util.MAX_MOVES;

/**
 * @author Vance Zuo
 */
public class Searcher {
  public static class Task implements Callable<Result> {
    private final Searcher searcher;
    private final int depth;

    public Task(Searcher search, int depth) {
      this.searcher = search;
      this.depth = depth;
    }

    @Override
    public Result call() throws Exception {
      return searcher.search(depth);
    }
  }

  public class Result {
    private final int score;
    private final List<Integer> pv;
    private final boolean proof;

    public Result(int score, List<Integer> pv, boolean proof) {
      this.score = score;
      this.pv = pv;
      this.proof = proof;
    }

    public int getScore() {
      return score;
    }

    public List<Integer> getPV() {
      return pv;
    }

    public int getPVLength() {
      return pv.size();
    }

    public int getPVMove() {
      return pv.get(0);
    }

    public boolean isProvenResult() {
      return proof;
    }
  }

  public class PrincipalVariation {
    private int[] pv;
    private int size;

    public PrincipalVariation() {
      pv = new int[MAX_MOVES];
      size = 0;
    }

    public int get(int i) {
      return pv[i];
    }

    public int size() {
      return size;
    }

    public void add(int move) {
      pv[size++] = move;
    }
  }

  private final EvaluatedGame masterGame;

  // Separate game for search do/undo move so that exceptions don't pollute 'master' game
  private EvaluatedGame game;

  private long nodes;

  public Searcher(EvaluatedGame game) {
    this.masterGame = game;
  }

  public long getNodes() {
    return nodes;
  }

  public Result search(int depth) {
    game = new EvaluatedGame(masterGame);
    return search(depth, EvaluatedGame.MIN_SCORE - 1, EvaluatedGame.MAX_SCORE + 1);
  }

  private Result search(int depth, int alpha, int beta) {
    nodes++;

    if (game.isFinished())
      return new Result(game.score(), null, true);
    if (depth <= 0)
      return new Result(game.score(), null, false);

    boolean maxi = game.getCurrentPlayer() == PLAYER_MAX;

    List<Integer> pv = new ArrayList<>(depth);
    boolean proof = false;
    for (int move : game.unsafeGenerateMoves()) {
      game.unsafeDoMove(move);
      Result result = search(depth - 1, alpha, beta);
      game.unsafeUndoMove();
      if (result == null || Thread.currentThread().isInterrupted())
        return null;
      int score = result.getScore();
      if (maxi ? score > alpha : score < beta) {
        if (maxi) alpha = score; else beta = score;
        proof = result.isProvenResult();
        if (alpha >= beta)
          break;
        pv.clear();
        pv.add(move);
        if (result.getPV() != null)
          pv.addAll(result.getPV());
      }
    }

    int score = maxi ? alpha : beta;
    if (score == EvaluatedGame.MIN_SCORE + depth - 1) {
      score++;
    } else if (score == EvaluatedGame.MAX_SCORE - depth + 1) {
      score--;
    }

    return new Result(score, pv, proof);
  }
}
