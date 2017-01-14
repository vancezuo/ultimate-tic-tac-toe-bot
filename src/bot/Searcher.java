package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static bot.EvaluatedGame.*;
import static bot.TranspositionTable.Entry.Type.*;
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
  private final TranspositionTable table;

  // Separate game for search do/undo move so that exceptions don't pollute 'master' game
  private EvaluatedGame game;

  private long nodes;

  public Searcher(EvaluatedGame game) {
    this.masterGame = game;
    this.table = new TranspositionTable();
  }

  public TranspositionTable getTable() {
    return table;
  }

  public long getNodes() {
    return nodes;
  }

  public void resetNodes() {
    nodes = 0;
  }

  public Result search(int depth) {
    game = new EvaluatedGame(masterGame);
    table.setMoveNumberCutoff(game.getMoveNumber());
    return search(depth, MIN_SCORE - 1, MAX_SCORE + 1);
  }

  private Result search(int depth, int alpha, int beta) {
    nodes++;

    boolean maxi = game.getCurrentPlayer() == PLAYER_MAX;

    // Check transposition table
    TranspositionTable.Entry ttEntry = table.get(game.getZobristKey());
    if (ttEntry != null && ttEntry.depth >= depth) {
      byte type = ttEntry.type;
      int score = ttEntry.score;
      if (type == PV_NODE
          || (type == CUT_NODE && (maxi ? score >= beta : score <= alpha))
          || (type == ALL_NODE && (maxi ? score <= alpha : score >= beta))) {
        int move = ttEntry.move;
        List<Integer> pv = (move != -1) ? Collections.singletonList(move) : null;
        return new Result(score, pv, ttEntry.proof);
      }
    }

    // Check win/draw conditions
    if (game.hasWinner()) {
      int score = (game.getWinner() == PLAYER_MAX) ? MAX_SCORE : MIN_SCORE;
      return new Result(score, null, true);
    }
    Iterable<Integer> moves = game.unsafeGenerateMoves();
    if (!moves.iterator().hasNext()) { // draw
      return new Result(DRAW_SCORE, null, true);
    }

    // Evaluate at zero depth
    if (depth <= 0) {
      return new Result(game.evaluate(), null, false);
    }

    // Recursive search to find best move/score
    List<Integer> pv = new ArrayList<>(depth);
    boolean proof = false;
    byte ttEntryType = ALL_NODE;
    int bestMove = -1;
    for (int move : moves) {
      game.unsafeDoMove(move);
      Result result = search(depth - 1, alpha, beta);
      game.unsafeUndoMove();
      if (result == null || Thread.currentThread().isInterrupted())
        return null;
      int score = result.getScore();
      if (maxi ? score > alpha : score < beta) {
        if (maxi) alpha = score; else beta = score;
        bestMove = move;
        proof = result.isProvenResult();
        if (alpha >= beta) {
          ttEntryType = CUT_NODE;
          break;
        } else {
          ttEntryType = PV_NODE;
        }
        pv.clear();
        pv.add(move);
        if (result.getPV() != null)
          pv.addAll(result.getPV());
      }
    }

    // Adjust win/loss score to reflect depth
    int score = maxi ? alpha : beta;
    if (score == MIN_SCORE + depth - 1) {
      score++;
    } else if (score == MAX_SCORE - depth + 1) {
      score--;
    }

    // Cache to transposition table
    table.insert(
        game.getZobristKey(), ttEntryType, depth, bestMove, score, proof, game.getMoveNumber());

    return new Result(score, pv, proof);
  }
}
