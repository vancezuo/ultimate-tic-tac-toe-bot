package bot;

import java.util.stream.IntStream;

/**
 * @author Vance Zuo
 */
public class Util {
  public static final int BOARD_LENGTH = 9, SUB_BOARD_LENGTH = 3;
  public static final int ROWS = 9, COLS = 9;
  public static final int NESTED_ROWS = 3, NESTED_COLS = 3;
  public static final int MACRO_ROWS = 3, MACRO_COLS = 3;
  public static final int MICRO_ROWS = 3, MICRO_COLS = 3;

  public static final int SUB_BOARD_SIZE = 9, MACROBOARD_SIZE = 9, MICROBOARD_SIZE = 9;
  public static final int BOARD_SIZE = 81;
  public static final int MAX_MOVES = 81;

  public static final String P1_NAME = "player1", P2_NAME = "player2";

  public static final int ANY_MACRO_INDEX = -1;

  public static final int PLAYER_NONE = 0, PLAYER_1 = 1, PLAYER_2 = 2;

  private static final int[] ALL_INDEXES = IntStream.range(0, MAX_MOVES).toArray();

  private static final int[] INDEX_TO_ROW = {
      0, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2,
      3, 3, 3, 3, 3, 3, 3, 3, 3,
      4, 4, 4, 4, 4, 4, 4, 4, 4,
      5, 5, 5, 5, 5, 5, 5, 5, 5,
      6, 6, 6, 6, 6, 6, 6, 6, 6,
      7, 7, 7, 7, 7, 7, 7, 7, 7,
      8, 8, 8, 8, 8, 8, 8, 8, 8,
  };

  private static final int[] INDEX_TO_COL = {
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      0, 1, 2, 3, 4, 5, 6, 7, 8,
  };

  private static final int[] INDEX_TO_MACRO_INDEX = {
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      3, 3, 3, 4, 4, 4, 5, 5, 5,
      3, 3, 3, 4, 4, 4, 5, 5, 5,
      3, 3, 3, 4, 4, 4, 5, 5, 5,
      6, 6, 6, 7, 7, 7, 8, 8, 8,
      6, 6, 6, 7, 7, 7, 8, 8, 8,
      6, 6, 6, 7, 7, 7, 8, 8, 8,
  };

  private static final int[] INDEX_TO_MACRO_ROW = {
      0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2,
      2, 2, 2, 2, 2, 2, 2, 2, 2,
      2, 2, 2, 2, 2, 2, 2, 2, 2,
  };

  private static final int[] INDEX_TO_MICRO_INDEX = {
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      3, 4, 5, 3, 4, 5, 3, 4, 5,
      6, 7, 8, 6, 7, 8, 6, 7, 8,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      3, 4, 5, 3, 4, 5, 3, 4, 5,
      6, 7, 8, 6, 7, 8, 6, 7, 8,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      3, 4, 5, 3, 4, 5, 3, 4, 5,
      6, 7, 8, 6, 7, 8, 6, 7, 8,
  };

  private static final int[] INDEX_TO_MICRO_ROW = {
      0, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2,
      0, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2,
      0, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2,
  };

  private static final int[] INDEX_TO_MACRO_COL = {
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
      0, 0, 0, 1, 1, 1, 2, 2, 2,
  };

  private static final int[] INDEX_TO_MICRO_COL = {
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
      0, 1, 2, 0, 1, 2, 0, 1, 2,
  };

  private static final int[] TOP_LEFT_INDEX = {
      0, 0, 0, 3, 3, 3, 6, 6, 6,
      0, 0, 0, 3, 3, 3, 6, 6, 6,
      0, 0, 0, 3, 3, 3, 6, 6, 6,
      27, 27, 27, 30, 30, 30, 33, 33, 33,
      27, 27, 27, 30, 30, 30, 33, 33, 33,
      27, 27, 27, 30, 30, 30, 33, 33, 33,
      54, 54, 54, 57, 57, 57, 60, 60, 60,
      54, 54, 54, 57, 57, 57, 60, 60, 60,
      54, 54, 54, 57, 57, 57, 60, 60, 60,
  };

  private static final int[][] MICRO_WIN_VECTORS = {
      {0, 1, 2}, {9, 10, 11}, {18, 19, 20}, // rows
      {0, 9, 18}, {1, 10, 19}, {2, 11, 20}, // columns
      {0, 10, 20}, // diagonal
      {2, 10, 18}}; // anti-diagonal

  private static final int[][] MACRO_WIN_VECTORS = {
      {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
      {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columns
      {0, 4, 8}, // diagonal
      {2, 4, 6}}; // anti-diagonal

  private static final int[][] MACRO_INDEX_TO_INDEX = {
      {0, 1, 2, 9, 10, 11, 18, 19, 20},
      {3, 4, 5, 12, 13, 14, 21, 22, 23},
      {6, 7, 8, 15, 16, 17, 24, 25, 26},
      {27, 28, 29, 36, 37, 38, 45, 46, 47},
      {30, 31, 32, 39, 40, 41, 48, 49, 50},
      {33, 34, 35, 42, 43, 44, 51, 52, 53},
      {54, 55, 56, 63, 64, 65, 72, 73, 74},
      {57, 58, 59, 66, 67, 68, 75, 76, 77},
      {60, 61, 62, 69, 70, 71, 78, 79, 80}
  };

  public static int swapPlayer(int player) {
    return player ^ 0b11; // 1 -> 2 -> ...
  }

  public static char charForPlayer(int player) {
    switch (player) {
      case PLAYER_1:
        return 'X';
      case PLAYER_2:
        return 'O';
      default:
        return '.';
    }
  }

  public static int row(int ind) {
    return INDEX_TO_ROW[ind];
  }

  public static int col(int ind) {
    return INDEX_TO_COL[ind];
  }

  public static int macroIndex(int ind) {
    return INDEX_TO_MACRO_INDEX[ind];
  }

  public static int microIndex(int ind) {
    return INDEX_TO_MICRO_INDEX[ind];
  }

  public static int macroRow(int ind) {
    return INDEX_TO_MACRO_ROW[ind];
  }

  public static int microRow(int ind) {
    return INDEX_TO_MICRO_ROW[ind];
  }

  public static int macroCol(int ind) {
    return INDEX_TO_MACRO_COL[ind];
  }

  public static int microCol(int ind) {
    return INDEX_TO_MICRO_COL[ind];
  }

  public static int index(int row, int col) {
    return row * COLS + col;
  }

  public static int microboardTopLeftIndex(int ind) {
    return TOP_LEFT_INDEX[ind];
  }

  public static int[][] microWinVectors() {
    return MICRO_WIN_VECTORS;
  }

  public static int[][] macroWinVectors() {
    return MACRO_WIN_VECTORS;
  }

  public static int[] indexesInMacroIndex(int macroInd) {
    return MACRO_INDEX_TO_INDEX[macroInd];
  }

  public static int[] allIndexes() { return ALL_INDEXES; }
}
