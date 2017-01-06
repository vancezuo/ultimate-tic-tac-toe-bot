package bot;

import theaigames.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

import static bot.Util.*;

/**
 * @author Vance Zuo
 */
public class Bot {
  private static final int BUFFER_TIME = 250;

  private final Scanner input;

  private Field field;
  private Game game;
  private Evaluator eval;
  private Searcher searcher;

  public Bot() {
    input = new Scanner(System.in);
  }

  private void reset() {
    field = new Field();
    game = new Game();
    eval = new Evaluator(game);
    searcher = new Searcher(eval);
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
            eval.doMove(move);
            System.err.println("opponent move: " + move);
            System.err.println(game.getFormattedBoardString());
          } else {
            System.err.println("opponent move: ERROR");
          }
        }
      } else if (parts[0].equals("action") && parts[1].equals("move")) { // own move
        System.err.println("processing: " + line);
        int timeLeft = parseInt(parts[2], 500) - BUFFER_TIME;
        int movesLeft = getMovesLeft();
        int move;
        try {
          move = think(timeLeft / movesLeft);
        } catch (Exception e) {
          move = generateRandomMove();
          System.err.println(e.getMessage());
        }
        System.out.println("place_move " + col(move) + " " + row(move));
        if (move != -1) {
          eval.doMove(move);
          System.err.printf("own move: %s (%s, %s)\n", move, row(move), col(move));
          System.err.println(game.getFormattedBoardString());
        } else {
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

  private int getMovesLeft() {
    return MAX_MOVES - field.getMoveNr() + 1;
  }

  public int think(int time) throws ExecutionException, TimeoutException, InterruptedException {
    if (eval.hasFinishedGame())
      throw new IllegalStateException("Game over. No legal moves.");

    System.err.println("called think(" + time + ")");

    ExecutorService executor = Executors.newSingleThreadExecutor();
    long timeStart = System.currentTimeMillis();
    long timeEnd = timeStart + time;
    long nodesStart = searcher.getNodes();
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
          throw e;
        }
        break;
      } catch (ExecutionException e) {
        executor.shutdown();
        throw e;
      } finally {
        future.cancel(true);
      }
      printSearchResult(result, depth, System.currentTimeMillis() - timeStart,
          searcher.getNodes() - nodesStart);
      if (result.isProvenResult())
        break;
    }
    executor.shutdown();

    return result.getPVMove();
  }

  private void printSearchResultHeader() {
    System.err.println("Depth\tTime\tNodes\tScore\tVariation");
  }

  private void printSearchResult(Searcher.Result result, int depth, long time, long nodes) {
    System.err.printf("%d\t", depth);
    System.err.printf("%.3f\t", time / 1000.0);
    System.err.printf("%d\t", nodes);
    if (result.isProvenResult()) {
      String resultStr;
      int distance;
      if (result.getScore() != 0) {
        resultStr = "win";
        distance = Math.abs(Math.abs(result.getScore()) - Evaluator.MAX_SCORE);
      } else {
        resultStr = "draw";
        distance = getMovesLeft();
      }
      System.err.printf("%s-%d\t", resultStr, distance);
    } else {
      System.err.printf("%d\t", result.getScore());
    }
    for (int move : result.getPV()) {
      System.err.print(row(move) + "," + col(move) + " ");
    }
    System.err.println();
  }

  private int generateRandomMove() {
    Random rand = new Random();

    List<Integer> moves = new ArrayList<>();
    for (int move : game.generateMoves()) {
      moves.add(move);
    }

    int i = 0;
    for (int move : moves) {
      if (rand.nextInt(moves.size() - i) == 0)
        return move;
      i++;
    }

    return -1; // should not reach this point
  }
}
