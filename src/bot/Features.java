package bot;

import static bot.EvaluatedGame.PLAYER_MAX;
import static bot.EvaluatedGame.PLAYER_MIN;
import static bot.Util.*;

/**
 * @author Vance Zuo
 */
public class Features {

  private static class Precomputed {
    private static int NUM_BOARDS = 19683; // 3**9

    private static final int[][][] ONE_IN_ROW_TABLE;
    private static final int[][][] TWO_IN_ROW_TABLE;

    static {
      ONE_IN_ROW_TABLE = new int[NUM_BOARDS][NUM_VECTOR_TYPES][NUM_PLAYERS];
      TWO_IN_ROW_TABLE = new int[NUM_BOARDS][NUM_VECTOR_TYPES][NUM_PLAYERS];
      generateBoardsAndComputeLineBoardTables();
    }

    private static void generateBoardsAndComputeLineBoardTables() {
      generateBoardsAndComputeLineBoardTables(new int[MACROBOARD_SIZE], 0);
    }

    private static void generateBoardsAndComputeLineBoardTables(int[] board, int boardIndex) {
      if (boardIndex >= board.length) {
        computeLineBoardTable(board);
        return;
      }
      final int[] players = {PLAYER_NONE, PLAYER_1, PLAYER_2};
      for (int player : players) {
        board[boardIndex] = player;
        generateBoardsAndComputeLineBoardTables(board, boardIndex + 1);
      }
    }

    private static void computeLineBoardTable(int[] board) {
      final int[] players = {PLAYER_MAX, PLAYER_MIN};
      for (int player : players) {
        int tableIndex = boardToTableIndex(board);
        int playerIndex = player - 1;
        int[][] vectors = macroWinVectors();
        int[] vectorTypes = winVectorTypes();
        for (int i = 0; i < vectors.length; i++) {
          int[] vector = vectors[i];
          int vectorType = vectorTypes[i];
          int unopposedInRow = computeUnopposedInRow(player, board, vector);
          switch (unopposedInRow) {
            case 2:
              TWO_IN_ROW_TABLE[tableIndex][vectorType][playerIndex]++;
              // fall through
            case 1:
              ONE_IN_ROW_TABLE[tableIndex][vectorType][playerIndex]++;
          }
        }
      }
    }

    private static int computeUnopposedInRow(int player, int[] board, int[] vector) {
      int unopposedInRow = 0;
      for (int i : vector) {
        if (board[i] == player) {
          unopposedInRow++;
        } else if (board[i] == swapPlayer(player)) {
          return 0;
        }
      }
      return unopposedInRow;
    }

    private static int boardToTableIndex(int[] board) {
      int factor = NUM_PLAYERS + 1;
      int index = 0;
      for (int i = 0; i < board.length; i++) {
        index *= factor;
        index += board[i];
      }
      return index;
    }

    private static int boardToTableIndex(int[] board, int[] indexes) {
      int factor = NUM_PLAYERS + 1;
      int index = 0;
      for (int i : indexes) {
        index *= factor;
        index += board[i];
      }
      return index;
    }
  }

  private static final int CENTER = 0, SIDE = 1, CORNER = 2, NUM_MICROBOARD_TYPES = 3;
  private static final int[] MICROBOARD_TYPES = {
      CORNER, SIDE, CORNER,
      SIDE, CENTER, SIDE,
      CORNER, SIDE, CORNER};

  private final Game game;

  private int[] macroboardOneInRow;
  private int[] macroboardTwoInRow;
  private int[][] microboardOneInRow;
  private int[][] microboardTwoInRow;
  private int anyNextMacroInd;

  public Features(Game game) {
    this.game = game;
    this.macroboardOneInRow = new int[NUM_VECTOR_TYPES];
    this.macroboardTwoInRow = new int[NUM_VECTOR_TYPES];
    this.microboardOneInRow = new int[NUM_MICROBOARD_TYPES][NUM_VECTOR_TYPES];
    this.microboardTwoInRow = new int[NUM_MICROBOARD_TYPES][NUM_VECTOR_TYPES];
  }

  public void computeFeatures() {
    computeMacroboardOneInRow();
    computeMacroboardTwoInRow();
    computeMicroboardOneInRow();
    computeMicroboardTwoInRow();
    computeAnyNextMacroInd();
  }

  private void computeMacroboardOneInRow() {
    int tableIndex = Precomputed.boardToTableIndex(game.getMacroboard());
    for (int vectorType = 0; vectorType < NUM_VECTOR_TYPES; vectorType++) {
      int[] oneInRow = Precomputed.ONE_IN_ROW_TABLE[tableIndex][vectorType];
      macroboardOneInRow[vectorType] = oneInRow[0] - oneInRow[1];
    }
  }

  private void computeMacroboardTwoInRow() {
    int tableIndex = Precomputed.boardToTableIndex(game.getMacroboard());
    for (int vectorType = 0; vectorType < NUM_VECTOR_TYPES; vectorType++) {
      int[] twoInRow = Precomputed.TWO_IN_ROW_TABLE[tableIndex][vectorType];
      macroboardTwoInRow[vectorType] = twoInRow[0] - twoInRow[1];
    }
  }

  private void computeMicroboardOneInRow() {
    for (int i = 0; i < MACROBOARD_SIZE; i++) {
      computeMicroboardOneInRow(i);
    }
  }

  private void computeMicroboardOneInRow(int macroInd) {
    int microboardType = MICROBOARD_TYPES[macroInd];
    int tableIndex = Precomputed.boardToTableIndex(game.getBoard(), indexesInMacroIndex(macroInd));
    for (int vectorType = 0; vectorType < NUM_VECTOR_TYPES; vectorType++) {
      int[] oneInRow = Precomputed.ONE_IN_ROW_TABLE[tableIndex][vectorType];
      microboardOneInRow[microboardType][vectorType] = oneInRow[0] - oneInRow[1];
    }
  }

  private void computeMicroboardTwoInRow() {
    for (int i = 0; i < MACROBOARD_SIZE; i++) {
      computeMicroboardTwoInRow(i);
    }
  }

  private void computeMicroboardTwoInRow(int macroInd) {
    int microboardType = MICROBOARD_TYPES[macroInd];
    int tableIndex = Precomputed.boardToTableIndex(game.getBoard(), indexesInMacroIndex(macroInd));
    for (int vectorType = 0; vectorType < NUM_VECTOR_TYPES; vectorType++) {
      int[] twoInRow = Precomputed.TWO_IN_ROW_TABLE[tableIndex][vectorType];
      microboardTwoInRow[microboardType][vectorType] = twoInRow[0] - twoInRow[1];
    }
  }

  public void computeAnyNextMacroInd() {
    anyNextMacroInd = (game.getNextMacroIndex() == ANY_MACRO_INDEX)
        ? ((game.getCurrentPlayer() == PLAYER_MAX) ? 1 : -1)
        : 0;
  }

  public int score(Weights weights) {
    computeFeatures();

    int score = 0;
    score += dot(macroboardOneInRow, weights.macroboardOneInRow);
    score += dot(macroboardTwoInRow, weights.macroboardTwoInRow);
    score += dot(microboardOneInRow, weights.microboardOneInRow);
    score += dot(microboardTwoInRow, weights.microboardTwoInRow);
    score += anyNextMacroInd * weights.anyNextMacroInd;
    return score;
  }

  private int dot(int[][] a0, int[][] a1) {
    int sum = 0;
    for (int i = 0; i < a0.length; i++) {
      for (int j = 0; j < a0[0].length; j++) {
        sum += a0[i][j] * a1[i][j];
      }
    }
    return sum;
  }

  private int dot(int[] a0, int[] a1) {
    int sum = 0;
    for (int i = 0; i < a0.length; i++) {
      sum += a0[i] * a1[i];
    }
    return sum;
  }
}
