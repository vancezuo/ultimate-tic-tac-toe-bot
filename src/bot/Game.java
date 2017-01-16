package bot;

import theaigames.Field;

import java.util.*;

import static bot.Util.*;

/**
 * @author Vance Zuo
 */
public class Game {
  private int[] board;
  private int[] macroboard;
  private int nextMacroInd;
  private int currentPlayer;
  private int winner;
  private HistoryStack history;

  public Game() {
    board = new int[ROWS * COLS];
    macroboard = new int[MACRO_ROWS * MACRO_COLS];
    history = new HistoryStack(MAX_MOVES);
    reset();
  }

  public Game(Game game) {
    board = Arrays.copyOf(game.board, game.board.length);
    macroboard = Arrays.copyOf(game.macroboard, game.macroboard.length);
    nextMacroInd = game.nextMacroInd;
    currentPlayer = game.currentPlayer;
    winner = game.winner;
    history = new HistoryStack(game.history);
  }

  Game(int[] board,
       int[] macroboard,
       int nextMacroInd,
       int currentPlayer,
       int winner,
       HistoryStack history) {
    this.board = board;
    this.macroboard = macroboard;
    this.nextMacroInd = nextMacroInd;
    this.currentPlayer = currentPlayer;
    this.winner = winner;
    this.history = history;
  }

  public void reset() {
    Arrays.fill(board, PLAYER_NONE);
    Arrays.fill(macroboard, PLAYER_NONE);
    nextMacroInd = ANY_MACRO_INDEX;
    currentPlayer = PLAYER_1;
    winner = PLAYER_NONE;
    history.clear();
  }

  public int[] getBoard() {
    return board;
  }

  public int[] getMacroboard() {
    return macroboard;
  }

  public int getNextMacroIndex() {
    return nextMacroInd;
  }

  public int getCurrentPlayer() {
    return currentPlayer;
  }

  public int getWinner() {
    return winner;
  }

  public HistoryStack getHistory() {
    return history;
  }

  public int getLastMove() {
    return history.peekMove();
  }

  public int getMoveNumber() {
    return history.size();
  }

  public void parseField(Field field) {
    reset();

    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        board[index(row, col)] = field.getPlayerId(col, row);
      }
    }

    int index = 0;
    int numMoveOptions = 0;
    for (int row = 0; row < MACRO_ROWS; row++) {
      for (int col = 0; col < MACRO_COLS; col++) {
        int macroId = field.getMacroId(col, row);
        if (macroId == -1) {
          numMoveOptions++;
          nextMacroInd = numMoveOptions > 1 ? ANY_MACRO_INDEX : index;
          macroId = PLAYER_NONE;
        }
        macroboard[index++] = macroId;
      }
    }

    currentPlayer = field.getMoveNr() % 2 == 0 ? PLAYER_2 : PLAYER_1;

    updateWinner();
  }

  public boolean hasWinner() {
    return winner != PLAYER_NONE;
  }

  public boolean isFinished() {
    return hasWinner() || !unsafeGenerateMoves().iterator().hasNext();
  }

  public boolean isEmpty(int ind) {
    return board[ind] == PLAYER_NONE;
  }

  public boolean canDoMove(int ind) {
    return !hasWinner()
        && ind >= 0
        && ind < BOARD_SIZE
        && isEmpty(ind)
        && (nextMacroInd == ANY_MACRO_INDEX || nextMacroInd == macroIndex(ind));
  }

  public boolean canUndoMove() {
    return !history.empty();
  }

  public boolean unsafeCheckCompletesLine(int ind) {
    board[ind] = currentPlayer;
    boolean completesLine = checkMicroWin(ind);
    board[ind] = PLAYER_NONE;
    return completesLine;
  }

  public boolean checkCompletesLine(int ind) {
    return canDoMove(ind) && unsafeCheckCompletesLine(ind);
  }

  public void unsafeDoMove(int ind) {
    history.push(ind, nextMacroInd);

    board[ind] = currentPlayer;
    if (checkMicroWin(ind)) {
      macroboard[macroIndex(ind)] = currentPlayer;
    }
    nextMacroInd = microIndex(ind);
    if (macroboard[nextMacroInd] != PLAYER_NONE) {
      nextMacroInd = ANY_MACRO_INDEX;
    }
    updateWinner();
    currentPlayer = swapPlayer(currentPlayer);
  }

  public boolean doMove(int ind) {
    if (!canDoMove(ind)) {
      return false;
    }
    unsafeDoMove(ind);
    return true;
  }

  public void doNullMove() {
    history.push(-1, nextMacroInd);

    nextMacroInd = ANY_MACRO_INDEX;
    currentPlayer = swapPlayer(currentPlayer);
  }

  public void unsafeUndoMove() {
    int prevInd = history.peekMove();
    int prevNextMacroInd = history.peekMacroInd();
    history.pop();

    board[prevInd] = PLAYER_NONE;
    macroboard[macroIndex(prevInd)] = PLAYER_NONE;
    nextMacroInd = prevNextMacroInd;
    winner = PLAYER_NONE;
    currentPlayer = swapPlayer(currentPlayer);
  }

  public boolean undoMove() {
    if (!canUndoMove()) {
      return false;
    }
    unsafeUndoMove();
    updateWinner();
    return true;
  }

  public void undoNullMove() {
    int prevNextMacroInd = history.peekMacroInd();
    history.pop();

    nextMacroInd = prevNextMacroInd;
    currentPlayer = swapPlayer(currentPlayer);
  }

  public Iterable<Integer> generateMoves() {
    if (hasWinner()) {
      return Collections.emptyList();
    }
    return unsafeGenerateMoves();
  }

  public Iterable<Integer> unsafeGenerateMoves() {
    if (nextMacroInd == ANY_MACRO_INDEX) {
      return unsafeGenerateMovesEntireBoard();
    } else {
      return unsafeGenerateMovesNextMacroboard();
    }
  }

  public int generateRandomMove() {
    Random rand = new Random();

    List<Integer> moves = new ArrayList<>();
    for (int move : generateMoves()) {
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

  private Iterable<Integer> unsafeGenerateMovesEntireBoard() {
    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          int potentialMove = 0;

          @Override
          public boolean hasNext() {
            while (potentialMove < BOARD_SIZE) {
              if (board[potentialMove] == PLAYER_NONE
                  && macroboard[macroIndex(potentialMove)] == PLAYER_NONE)
                return true;
              potentialMove++;
            }
            return false;
          }

          @Override
          public Integer next() {
            if (!hasNext())
              throw new NoSuchElementException();
            return potentialMove++;
          }
        };
      }
    };
  }

  private Iterable<Integer> unsafeGenerateMovesNextMacroboard() {
    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          int[] potentialMoves = indexesInMacroIndex(nextMacroInd);
          int len = potentialMoves.length;
          int i = 0;

          @Override
          public boolean hasNext() {
            while (i < len) {
              if (board[potentialMoves[i]] == PLAYER_NONE)
                return true;
              i++;
            }
            return false;
          }

          @Override
          public Integer next() {
            if (!hasNext())
              throw new NoSuchElementException();
            return potentialMoves[i++];
          }
        };
      }
    };
  }

  private void updateWinner() {
    winner = checkMacroWin() ? currentPlayer : PLAYER_NONE;
  }

  private boolean checkMicroWin(int ind) {
    return checkWin(microboardTopLeftIndex(ind), board, microWinVectors());
  }

  private boolean checkMacroWin() {
    return checkWin(0, macroboard, macroWinVectors());
  }

  private boolean checkWin(int startInd, int[] b, int[][] winVectors) {
    for (int[] winVector : winVectors) {
      boolean isWin = true;
      for (int delta : winVector) {
        if (b[startInd + delta] != currentPlayer) {
          isWin = false;
          break;
        }
      }
      if (isWin) {
        return true;
      }
    }
    return false;
  }

  public String getFormattedBoardString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < BOARD_SIZE; i++) {
      boolean boardStart = (i == 0);
      boolean rowStart = (i % COLS == 0);
      boolean verticalDivide = (i % MICRO_COLS == 0) && !rowStart;
      boolean horizonatalDivide = (i % (COLS * 3) == 0) && !boardStart;

      if (rowStart && !boardStart) {
        sb.append("\n");
      }
      if (verticalDivide) {
        sb.append("|");
      }
      if (horizonatalDivide) {
        sb.append("---+---+---");
        sb.append("\n");
      }

      sb.append(charForPlayer(board[i]));
    }
    return sb.toString();
  }

  public String getFormattedMacroboardString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < MACROBOARD_SIZE; i++) {
      boolean boardStart = (i == 0);
      boolean rowStart = (i % MACRO_COLS == 0);
      if (rowStart && !boardStart) {
        sb.append("\n");
      }
      sb.append(charForPlayer(macroboard[i]));
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return String.format("Game(%s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s)",
        "board", Arrays.toString(board),
        "macroboard", Arrays.toString(macroboard),
        "nextMacroInd", nextMacroInd,
        "currentPlayer", currentPlayer,
        "winner", winner,
        "history", history);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Game)) {
      return false;
    }
    Game other = (Game) obj;
    return Arrays.equals(board, other.board)
        && Arrays.equals(macroboard, other.macroboard)
        && nextMacroInd == other.nextMacroInd
        && currentPlayer == other.currentPlayer
        && winner == other.winner
        && history.equals(other.history);
  }
}
