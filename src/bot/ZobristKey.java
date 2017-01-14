package bot;

import java.security.SecureRandom;
import java.util.Random;

import static bot.Util.*;

/**
 * @author Vance Zuo
 */
public class ZobristKey {
  private static final Random RANDOM = new SecureRandom(); // new Random(1095064007);

  private static final long[][] INDEX_KEYS;
  private static final long[] NEXT_MACRO_INDEX_KEY;
  static {
    INDEX_KEYS = new long[BOARD_SIZE][NUM_PLAYERS + 1]; // PLAYER_NONE, PLAYER_1, PLAYER_2
    for (int i = 0; i < INDEX_KEYS.length; i++) {
      for (int j = 1; j < INDEX_KEYS[i].length; j++) { // skip PLAYER_NONE = 0
        INDEX_KEYS[i][j] = RANDOM.nextLong();
      }
    }

    NEXT_MACRO_INDEX_KEY = new long[MACROBOARD_SIZE + 1]; // ANY_MACRO_INDEX, 0 to 8
    for (int i = 0; i < NEXT_MACRO_INDEX_KEY.length; i++) {
      NEXT_MACRO_INDEX_KEY[i] = RANDOM.nextLong();
    }
  }

  private long key;

  public ZobristKey(Game game) {
    initKey(game);
  }

  private void initKey(Game game) {
    // Only need: board, nextMacroInd
    // Redundant: macroboard, currentPlayer, winner
    // Irrelevant: history
    key = 0;
    for (int i = 0; i < BOARD_SIZE; i++) {
      key ^= getIndexKey(i, game.getBoard()[i]);
    }
    key ^= getMacroIndexKey(game.getNextMacroIndex());
  }

  public long getKey() {
    return key;
  }

  public void updateForIndex(int move, int player) {
    key ^= getIndexKey(move, player);
  }

  public void updateForNextMacroInd(int prevNextMacroInd, int currentNextMacroInd) {
    key ^= getMacroIndexKey(prevNextMacroInd);
    key ^= getMacroIndexKey(currentNextMacroInd);
  }

  private long getIndexKey(int index, int player) {
    return INDEX_KEYS[index][player];
  }

  private long getMacroIndexKey(int nextMacroIndex) {
    return NEXT_MACRO_INDEX_KEY[nextMacroIndex + 1];
  }

  @Override
  public String toString() {
    return String.format("ZobristKey(%s)", String.valueOf(key));
  }
}
