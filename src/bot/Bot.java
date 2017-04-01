package bot;

import theaigames.Field;

import java.util.Scanner;
import java.util.concurrent.*;

import static bot.EvaluatedGame.PLAYER_MAX;
import static bot.EvaluatedGame.PLAYER_MIN;
import static bot.Util.*;

/**
 * @author Vance Zuo
 */
public class Bot {
  private static final int TIME_BUFFER = 250;
  private static final int TIME_INCREMENT = 500;

  private final Scanner input;

  private Field field;
  private EvaluatedGame game;
  private Searcher searcher;

  public Bot() {
    input = new Scanner(System.in);
  }

  private void reset() {
    field = new Field();
    game = new EvaluatedGame();
    searcher = new Searcher(game);
  }

  public void run() {
    reset();

    while (input.hasNextLine()) {
      String line = input.nextLine();

      if (line.length() == 0) {
        continue;
      }

      String[] parts = line.split(" ");
      if (parts[0].equals("update") && parts[1].equals("game")) { // game state update
        System.err.println("processing: " + line);
        field.parseGameData(parts[2], parts[3]);
        if (parts[2].equals("field")) { // opponent move
          int move = deduceLastMove();
          if (move != -1) {
            game.unsafeDoMove(move);
            System.err.printf("opponent move: %s (%s, %s)\n", move, row(move), col(move));
            System.err.println(game.getFormattedBoardString());
          } else {
            System.err.println("opponent move: ERROR/NONE");
          }
        }
      } else if (parts[0].equals("action") && parts[1].equals("move")) { // own move
        System.err.println("processing: " + line);
        int timeLeft = parseInt(parts[2], TIME_INCREMENT) - TIME_BUFFER;
        int maxMovesLeft = getMaxMovesLeft();
        int thinkTime = Math.min(timeLeft / Math.max(1, maxMovesLeft) + TIME_INCREMENT, timeLeft);
        int move;
        try {
          move = think(thinkTime);
        } catch (Exception e) {
          move = game.generateRandomMove();
          System.err.println("miscellaneous error: " + e);
        }
        if (move != -1) {
          System.out.println("place_move " + col(move) + " " + row(move));
          game.unsafeDoMove(move);
          System.err.printf("own move: %s (%s, %s)\n", move, row(move), col(move));
          System.err.println(game.getFormattedBoardString());
        } else {
          System.out.println("no_moves");
          System.err.println("own move: ERROR");
        }
      } else {
        System.err.println("ignored: " + line);
      }
    }
  }

  private int deduceLastMove() {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        if (game.getBoard()[index(row, col)] != field.getPlayerId(col, row)) {
          // Assume only one move difference
          return index(row, col);
        }
      }
    }
    return -1;
  }

  private int parseInt(String str, int defaultValue) {
    try {
      return Integer.parseInt(str);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  private int getMaxMovesLeft() {
    return field.getAvailableMoves().size();
  }

  public int think(int time) {
    if (game.isFinished())
      throw new IllegalStateException("Game over. No legal moves.");

    System.err.println("called think(" + time + ")");

    searcher.resetNodes();
    searcher.getTable().resetStats();

    ExecutorService executor = Executors.newSingleThreadExecutor();
    long timeStart = System.currentTimeMillis();
    long timeEnd = timeStart + time;
    Searcher.Result result = null;
    printSearchResultHeader();
    for (int depth = 1; ; depth++) {
      Searcher.Task task = new Searcher.Task(searcher, depth);
      Future<Searcher.Result> future = executor.submit(task);
      long timeRemaining = timeEnd - System.currentTimeMillis();
      try {
        result = future.get(timeRemaining, TimeUnit.MILLISECONDS);
      } catch (TimeoutException | InterruptedException e) {
        if (depth <= 1) {
          System.err.println("early timeout/interrupt error");
        }
        break;
      } catch (ExecutionException e) {
        e.printStackTrace();
        break;
      } finally {
        future.cancel(true);
      }
      printSearchResult(
          result,
          searcher.getGame().getCurrentPlayer(),
          depth,
          System.currentTimeMillis() - timeStart,
          searcher.getNodes());
      if (result.isProvenResult())
        break;
    }
    executor.shutdown();

    printTranspositionTableStats(searcher.getTable());

    return result != null ? result.getPVMove() : game.generateRandomMove();
  }

  private void printSearchResultHeader() {
    System.err.println("Depth\tTime\tNodes\tScore\tVariation");
  }

  private void printSearchResult(
      Searcher.Result result,
      int currentPlayer,
      int depth,
      long time,
      long nodes) {
    System.err.printf("%d\t", depth);
    System.err.printf("%.3f\t", time / 1000.0);
    System.err.printf("%d\t", nodes);
    if (result.isProvenResult()) {
      String resultStr;
      if (result.getScore() != 0) {
        int winningPlayer = result.getScore() > 0 ? PLAYER_MAX : PLAYER_MIN;
        resultStr = (winningPlayer == currentPlayer) ? "win" : "loss";
      } else {
        resultStr = "draw";
      }
      System.err.printf("%s\t", resultStr);
    } else {
      System.err.printf("%d\t", result.getScore());
    }
    for (int move : result.getPV()) {
      System.err.print(row(move) + "," + col(move) + " ");
    }
    System.err.println();
  }

  private void printTranspositionTableStats(TranspositionTable table) {
    TranspositionTable.Stats stats = table.getStats();
    double putRate = (double) (stats.creates + stats.replaces) / stats.inserts;
    double hitRate = (double) stats.hits / stats.gets;
    double load = table.estimateLoad();
    System.err.print("Table stats: ");
    System.err.printf("inserts %.2f%%, ", putRate * 100);
    System.err.printf("hits %.2f%%, ", hitRate * 100);
    System.err.printf("load %.2f%%\n", load * 100);
  }
}
