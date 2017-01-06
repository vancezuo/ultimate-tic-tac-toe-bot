package bot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static bot.Evaluator.PLAYER_MAX;
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

  private final Evaluator eval;

  private long nodes;

  public Searcher(Evaluator eval) {
    this.eval = eval;
  }

  public long getNodes() {
    return nodes;
  }

  public Result search(int depth) {
    return search(depth, Evaluator.MIN_SCORE - 1, Evaluator.MAX_SCORE + 1);
  }

  private Result search(int depth, int alpha, int beta) {
    nodes++;

    if (eval.hasFinishedGame())
      return new Result(eval.score(), null, true);
    if (depth <= 0)
      return new Result(eval.score(), null, false);

    boolean maxi = eval.getCurrentPlayer() == PLAYER_MAX;

    List<Integer> pv = new ArrayList<>(depth);
    boolean proof = false;
    for (int move : eval.generateMoves()) {
      eval.doMove(move);
      Result result = search(depth - 1, alpha, beta);
      eval.undoMove();
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
    if (score == Evaluator.MIN_SCORE + depth - 1) {
      score++;
    } else if (score == Evaluator.MAX_SCORE - depth + 1) {
      score--;
    }

    return new Result(score, pv, proof);
  }
}
