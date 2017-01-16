package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import static bot.EvaluatedGame.*;
import static bot.TranspositionTable.Entry.Type.*;
import static bot.Util.*;

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

  private static class WeightedMove {
    Integer move;
    int score;

    public WeightedMove(Integer move, int score) {
      this.move = move;
      this.score = score;
    }
  }

  private final EvaluatedGame masterGame;
  private final TranspositionTable table;

  private long nodes;

  // Used for move ordering
  private final int[] history;
  private int hashMove;

  // Separate game for search do/undo move so that exceptions don't pollute 'master' game
  private EvaluatedGame game;

  private boolean nullMoveAllowed;
  private int baseDepth;

  public Searcher(EvaluatedGame game) {
    this.masterGame = game;
    this.table = new TranspositionTable();

    this.history = new int[BOARD_SIZE];
    this.hashMove = -1;
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
    nullMoveAllowed = true;
    baseDepth = depth;
    table.setMoveNumberCutoff(game.getMoveNumber());
    return search(depth, MIN_SCORE - 1, MAX_SCORE + 1);
  }

  private Result search(int depth, int alpha, int beta) {
    nodes++;

    boolean maxi = game.getCurrentPlayer() == PLAYER_MAX;

    // Check transposition table
    TranspositionTable.Entry ttEntry = table.get(game.getZobristKey());
    if (ttEntry != null) {
      if (ttEntry.depth >= depth) {
        byte type = ttEntry.type;
        int score = ttEntry.score;
        if (type == PV_NODE
            || (type == CUT_NODE && (maxi ? score >= beta : score <= alpha))
            || (type == ALL_NODE && (maxi ? score <= alpha : score >= beta))) {
          int move = ttEntry.move;
          List<Integer> pv = (move != -1) ? Collections.singletonList(move) : null;
          return new Result(score, pv, ttEntry.proof);
        }
      } else {
        hashMove = ttEntry.move;
      }
    }

    // Check win/draw conditions
    if (game.hasWinner()) {
      int score = (game.getWinner() == PLAYER_MAX) ? MAX_SCORE : MIN_SCORE;
      return new Result(score, null, true);
    }
    Iterator<Integer> moves = game.unsafeGenerateMoves().iterator();
    if (!moves.hasNext()) { // draw
      return new Result(DRAW_SCORE, null, true);
    }

    // Evaluate at zero depth
    if (depth <= 0) {
      return new Result(quiescence(alpha, beta), null, false);
    }

    // Null move reduction
//    if (nullMoveAllowed && depth < baseDepth) {
//      int r = (depth + 14) / 5; // min: 3
//      nullMoveAllowed = false;
//      game.doNullMove();
//      Result nullResult = maxi
//          ? search(depth - r - 1, beta - 1, beta)
//          : search(depth - r - 1, alpha, alpha + 1);
//      game.undoNullMove();
//      nullMoveAllowed = true;
//      if (maxi ? nullResult.getScore() >= beta : nullResult.getScore() <= alpha) {
//        depth -= 3;
//        if (depth <= 0) {
//          return new Result(quiescence(alpha, beta), null, false);
//        }
//      }
//    }

    // Recursive search to find best move/score
    List<Integer> pv = new ArrayList<>(depth);
    boolean proof = false;
    byte ttEntryType = ALL_NODE;
    int bestMove = -1;
    boolean searchPv = true; // used for negascout/PVS
    for (int move : generateSortedMoves(moves)) {
      Result result;
      game.unsafeDoMove(move);
      if (searchPv) {
        result = search(depth - 1, alpha, beta);
      } else {
        if (maxi) {
          result = search(depth - 1, alpha, alpha + 1);
          if (result.getScore() > alpha)
            result = search(depth - 1, alpha, beta);
        } else {
          result = search(depth - 1, beta - 1, beta);
          if (result.getScore() < beta)
            result = search(depth - 1, alpha, beta);
        }
      }
      game.unsafeUndoMove();
      if (Thread.currentThread().isInterrupted())
        return new Result(0, null, false);
      int score = result.getScore();
      if (maxi ? score > alpha : score < beta) {
        if (maxi) alpha = score;
        else beta = score;
        bestMove = move;
        proof = result.isProvenResult();
        if (alpha >= beta) {
          ttEntryType = CUT_NODE;
          history[move] += 1 << depth;
          break;
        } else {
          ttEntryType = PV_NODE;
          searchPv = false;
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

  private int quiescence(int alpha, int beta) {
    nodes++;

    boolean maxi = game.getCurrentPlayer() == PLAYER_MAX;

    // Check win/draw conditions
    if (game.hasWinner()) {
      return (game.getWinner() == PLAYER_MAX) ? MAX_SCORE : MIN_SCORE;
    }
    Iterator<Integer> moves = game.unsafeGenerateMoves().iterator();
    if (!moves.hasNext()) { // draw
      return DRAW_SCORE;
    }

    int standPat = game.evaluate();
    if (maxi) {
      if (standPat >= beta)
        return beta;
      if (alpha < standPat)
        alpha = standPat;
    } else {
      if (standPat <= alpha)
        return alpha;
      if (beta > standPat)
        beta = standPat;
    }

    while (moves.hasNext()) {
      int move = moves.next();
      if (!game.unsafeCheckCompletesLine(move)) {
        continue;
      }
      game.unsafeDoMove(move);
      int score = quiescence(alpha, beta);
      game.unsafeUndoMove();

      if (maxi) {
        if (score >= beta)
          return beta;
        if (alpha < score)
          alpha = score;
      } else {
        if (score <= alpha)
          return alpha;
        if (beta > score)
          beta = score;
      }
    }

    return maxi ? alpha : beta;
  }

  public Iterable<Integer> generateSortedMoves(Iterator<Integer> movesIterator) {
    int maxLength = (game.getNextMacroIndex() != ANY_MACRO_INDEX) ? MICROBOARD_SIZE : BOARD_SIZE;

    int[] moves = new int[maxLength];
    int[] scores = new int[maxLength];

    int i = 0;
    while (movesIterator.hasNext()) {
      int move = movesIterator.next();
      moves[i] = move;
      scores[i] = (hashMove == move) ? Integer.MAX_VALUE : history[move];
      i++;
    }

    final int originalLen = i;
    return () -> new Iterator<Integer>() {
      int len = originalLen;

      @Override
      public boolean hasNext() {
        return len > 0;
      }

      @Override
      public Integer next() {
        int bestI = 0;
        for (int i = 1; i < len; i++) {
          if (scores[bestI] < scores[i]) {
            bestI = i;
          }
        }
        len--;
        if (bestI != len) {
          // Swap best with last
          int tempMove, tempScore;
          tempMove = moves[bestI];
          tempScore = scores[bestI];
          moves[bestI] = moves[len];
          scores[bestI] = scores[len];
          moves[len] = tempMove;
          scores[len] = tempScore;
        }
        return moves[len];
      }
    };

//    PriorityQueue<WeightedMove> sortedMoves =
//        new PriorityQueue<>(maxLength, (m1, m2) -> m2.score - m1.score);
//
//    while (movesIterator.hasNext()) {
//      int move = movesIterator.next();
//      int score = (hashMove == move) ? Integer.MAX_VALUE : history[move];
//      sortedMoves.add(new WeightedMove(move, score));
//    }
//
//    return () -> new Iterator<Integer>() {
//      @Override
//      public boolean hasNext() {
//        return !sortedMoves.isEmpty();
//      }
//
//      @Override
//      public Integer next() {
//        return sortedMoves.remove().move;
//      }
//    };
  }

}
