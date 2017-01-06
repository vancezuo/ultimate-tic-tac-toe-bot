package bot;

import java.util.Arrays;

/**
 * @author Vance Zuo
 */
public class HistoryStack {
  private int[] moves, macroInds;
  private int size;

  public HistoryStack(int maxSize) {
    moves = new int[maxSize];
    macroInds = new int[maxSize];
    size = 0;
  }

  HistoryStack(int[] moves, int[] macroInds, int size) {
    this.moves = moves;
    this.macroInds = macroInds;
    this.size = size;
  }

  HistoryStack(HistoryStack historyStack) {
    this.moves = Arrays.copyOf(historyStack.moves, historyStack.moves.length);
    this.macroInds = Arrays.copyOf(historyStack.macroInds, historyStack.macroInds.length);
    this.size = historyStack.size;
  }

  public boolean empty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  public void push(int move, int macroInd) {
    moves[size] = move;
    macroInds[size] = macroInd;
    ++size;
  }

  public void pop() {
    --size;
  }

  public int peekMove() {
    return moves[size - 1];
  }

  public int peekMacroInd() {
    return macroInds[size - 1];
  }

  public void clear() {
    size = 0;
  }

  @Override
  public String toString() {
    return String.format("HistoryStack(%s=%s, %s=%s, %s=%s)",
        "moves",
        Arrays.toString(moves),
        "macroInds",
        Arrays.toString(macroInds),
        "size",
        size);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HistoryStack)) {
      return false;
    }
    HistoryStack other = (HistoryStack) obj;
    if (size != other.size) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (moves[i] != other.moves[i] || macroInds[i] != other.macroInds[i]) {
        return false;
      }
    }
    return true;
  }
}
