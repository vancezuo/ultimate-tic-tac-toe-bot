package bot;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Vance Zuo
 */
public class Weights {
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

  public int hammingDistance(Weights other) {
    int distance = 0;
    distance += hammingDistance(macroboardOneInRow, other.macroboardOneInRow);
    distance += hammingDistance(macroboardTwoInRow, other.macroboardTwoInRow);
    distance += hammingDistance(microboardOneInRow, other.microboardOneInRow);
    distance += hammingDistance(microboardTwoInRow, other.microboardTwoInRow);
    distance += Math.abs(anyNextMacroInd - other.anyNextMacroInd);
    return distance;
  }

  private static int hammingDistance(int[][] arr1, int[][] arr2) {
    int distance = 0;
    for (int i = 0; i < arr1.length; i++) {
      distance += hammingDistance(arr1[i], arr2[i]);
    }
    return distance;
  }

  private static int hammingDistance(int[] arr1, int[] arr2) {
    int distance = 0;
    for (int i = 0; i < arr1.length; i++) {
      distance += Math.abs(arr1[i] - arr2[i]);
    }
    return distance;
  }

  public Weights average(Weights other, double weight) {
    List<Double> weightsVector = new ArrayList<>();
    average(weightsVector, macroboardOneInRow, other.macroboardOneInRow, weight);
    average(weightsVector, macroboardTwoInRow, other.macroboardTwoInRow, weight);
    average(weightsVector, microboardOneInRow, other.microboardOneInRow, weight);
    average(weightsVector, microboardTwoInRow, other.microboardTwoInRow, weight);
    average(weightsVector, anyNextMacroInd, other.anyNextMacroInd, weight);
    double magnitude = Math.sqrt(weightsVector.stream().mapToDouble(x -> x * x).sum());
    return weightsVectorToWeights(
        weightsVector.stream().mapToInt(x -> (int) Math.round(x / magnitude * 10000)).toArray());
  }

  private static void average(List<Double> result, int[][] arr1, int[][] arr2, double weight) {
    for (int i = 0; i < arr1.length; i++) {
      average(result, arr1[i], arr2[i], weight);
    }
  }

  private static void average(List<Double> result, int[] arr1, int[] arr2, double weight) {
    for (int i = 0; i < arr1.length; i++) {
      average(result, arr1[i], arr2[i], weight);
    }
  }

  private static void average(List<Double> result, int x1, int x2, double weight) {
    result.add(x1 * (1 - weight) + x2 * weight);
  }

  public static Weights randomWeights() {
    int[] weights = randomWeightsVector(NUM_PARAMETERS);
    return weightsVectorToWeights(weights);
  }

  private static Weights weightsVectorToWeights(int[] weights) {
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

  private static int[] randomWeightsVector(int length) {
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

  public String serializeString() {
    StringBuilder sb = new StringBuilder();
    for (int value : macroboardOneInRow) {
      sb.append(Integer.toString(value));
      sb.append(",");
    }
    for (int value : macroboardTwoInRow) {
      sb.append(Integer.toString(value));
      sb.append(",");
    }
    for (int[] x : microboardOneInRow) {
      for (int value : x) {
        sb.append(Integer.toString(value));
        sb.append(",");
      }
    }
    for (int[] x : microboardTwoInRow) {
      for (int value : x) {
        sb.append(Integer.toString(value));
        sb.append(",");
      }
    }
    sb.append(anyNextMacroInd);
    return sb.toString();
  }

  public static Weights deserializeString(String s) {
    int[] weights = Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
    return weightsVectorToWeights(weights);
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
