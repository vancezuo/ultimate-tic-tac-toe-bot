package bot;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Vance Zuo
 */
public class Weights implements Serializable {
  public static final int NUM_PARAMETERS = 25;

  public static final int[] SINGLE_LINE_LARGE_BOARD = {100, 100, 100};
  public static final int[] DOUBLE_LINE_LARGE_BOARD = {1000, 1000, 1000};
  public static final int[][] SINGLE_LINE_SMALL_BOARD = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
  public static final int[][] DOUBLE_LINE_SMALL_BOARD = {
      {10, 10, 10}, {10, 10, 10}, {10, 10, 10}};
  public static final int ANY_NEXT_MACRO_IND = 50;

  public final int[] macroboardOneInRow;
  public final int[] macroboardTwoInRow;
  public final int[][] microboardOneInRow;
  public final int[][] microboardTwoInRow;
  public final int anyNextMacroInd;

  public Weights(
      int[] macroboardOneInRow,
      int[] macroboardTwoInRow,
      int[][] microboardOneInRow,
      int[][] microboardTwoInRow,
      int anyNextMacroInd) {
    this.macroboardOneInRow = macroboardOneInRow;
    this.macroboardTwoInRow = macroboardTwoInRow;
    this.microboardOneInRow = microboardOneInRow;
    this.microboardTwoInRow = microboardTwoInRow;
    this.anyNextMacroInd = anyNextMacroInd;
  }

  public Weights() {
    this(
        SINGLE_LINE_LARGE_BOARD,
        DOUBLE_LINE_LARGE_BOARD,
        SINGLE_LINE_SMALL_BOARD,
        DOUBLE_LINE_SMALL_BOARD,
        ANY_NEXT_MACRO_IND);
  }

  public static Weights randomWeights() {
    int[] weights = randomWeightsVector(NUM_PARAMETERS);
    return weightsVectorToWeights(weights);
  }

  public static Weights weightsVectorToWeights(int[] weights) {
    int k = 0;
    int[] singleLineLargeBoard = new int[3];
    for (int i = 0; i < singleLineLargeBoard.length; i++) {
      singleLineLargeBoard[i] = weights[k++];
    }
    int[] doubleLineLargeBoard = new int[3];
    for (int i = 0; i < doubleLineLargeBoard.length; i++) {
      doubleLineLargeBoard[i] = weights[k++];
    }
    int[][] singleLineSmallBoard = new int[3][3];
    for (int i = 0; i < singleLineSmallBoard.length; i++) {
      for (int j = 0; j < singleLineSmallBoard[0].length; j++) {
        singleLineSmallBoard[i][j] = weights[k++];
      }
    }
    int[][] doubleLineSmallBoard = new int[3][3];
    for (int i = 0; i < doubleLineSmallBoard.length; i++) {
      for (int j = 0; j < doubleLineSmallBoard[0].length; j++) {
        doubleLineSmallBoard[i][j] = weights[k++];
      }
    }
    int anyMacroboardIndex = weights[k];

    return new Weights(
        singleLineLargeBoard,
        doubleLineLargeBoard,
        singleLineSmallBoard,
        doubleLineSmallBoard,
        anyMacroboardIndex);
  }

  public static int[] randomWeightsVector(int length) {
    Random random = new SecureRandom();
    double[] relativeWeights = new double[length];
    double totalSquared = 0;
    for (int i = 0; i < relativeWeights.length; i++) { // generate abs(gaussian(0, 1)) values
      relativeWeights[i] = Math.abs(random.nextGaussian());
      totalSquared += relativeWeights[i] * relativeWeights[i];
    }
    double magnitude = Math.sqrt(totalSquared);
    for (int i = 0; i < relativeWeights.length; i++) { // normalize
      relativeWeights[i] /= magnitude;
    }
    int[] weights = new int[relativeWeights.length];
    for (int i = 0; i < weights.length; i++) { // scale to range (-10000, 10000)
      weights[i] = (int) Math.round(relativeWeights[i] * 10000);
    }
    return weights;
  }

  @Override
  public String toString() {
    return String.format("Weights(%s=%s, %s=%s, %s=%s, %s=%s, %s=%s)",
        "macroboardOneInRow", Arrays.toString(macroboardOneInRow),
        "macroboardTwoInRow", Arrays.toString(macroboardTwoInRow),
        "microboardOneInRow", Arrays.deepToString(microboardOneInRow),
        "microboardTwoInRow", Arrays.deepToString(microboardTwoInRow),
        "anyNextMacroInd", anyNextMacroInd);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Weights)) {
      return false;
    }
    Weights other = (Weights) obj;
    return Arrays.equals(macroboardOneInRow, other.macroboardOneInRow)
        && Arrays.equals(macroboardTwoInRow, other.macroboardTwoInRow)
        && Arrays.equals(microboardOneInRow, other.microboardOneInRow)
        && Arrays.equals(microboardTwoInRow, other.microboardTwoInRow)
        && anyNextMacroInd == other.anyNextMacroInd;
  }
}
