package bot;

/**
 * @author Vance Zuo
 */
public class TranspositionTable {
  public static class Entry {
    public static class Type {
      public static final byte EMPTY = 0, PV_NODE = 1, CUT_NODE = 2, ALL_NODE = 3;
      public static final String[] NAMES = new String[] {
          "EMPTY", "PV_NODE", "CUT_NODE", "ALL_NODE"
      };
    }

    public long key;
    public byte type;
    public byte depth;
    public byte move;
    public int score;
    public boolean proof;
    public byte moveNum;

    @Override
    public String toString() {
      return String.format(
          "TranspositionTable.Entry(%s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s)",
          "key", key,
          "type", Type.NAMES[type],
          "depth", depth,
          "move", move,
          "score", score,
          "moveNum", moveNum);
    }
  }

  public static class Stats {
    public int inserts, creates, replaces, gets, hits;

    @Override
    public String toString() {
      return String.format(
          "TranspositionTable.Stats(%s=%s, %s=%s, %s=%s, %s=%s, %s=%s)",
          "inserts", inserts,
          "creates", creates,
          "replaces", replaces,
          "gets", gets,
          "hits", hits);
    }
  }

  private static final int DEFAULT_LOG_SIZE = 18;

  private final Entry[] table;
  private Stats stats;

  private byte moveNum;

  public TranspositionTable(int logSize) {
    table = new Entry[1 << logSize];
    for (int i = 0; i < table.length; i++) {
      table[i] = new Entry();
    }
    stats = new Stats();
  }

  public TranspositionTable() {
    this(DEFAULT_LOG_SIZE);
  }

  public Entry[] getTable() {
    return table;
  }

  public Stats getStats() {
    return stats;
  }

  public void setMoveNumberCutoff(int moveNumCutoff) {
    this.moveNum = (byte) moveNumCutoff;
  }

  public void resetStats() {
    stats = new Stats();
  }

  public double estimateLoad() {
    double occupied = 0.0;
    int sampleSize = 1000;
    for (int i = 0; i < Math.min(sampleSize, table.length); i++) {
      if (table[i].type != Entry.Type.EMPTY) {
        occupied++;
      }
    }
    return occupied / sampleSize;
  }

  public boolean insert(
      long key, byte type, int depth, int move, int score, boolean proof, int moveNum) {
    stats.inserts++;

    Entry entry = table[keyToIndex(key)];
    if (entry.type != Entry.Type.EMPTY) {
      // Depth and move number replacement strategy
//      if (entry.depth > depth && entry.moveNum >= this.moveNum) {
//        return false;
//      }
      stats.replaces++;
    } else {
      stats.creates++;
    }

    entry.key = key;
    entry.type = type;
    entry.depth = (byte) depth;
    entry.move = (byte) move;
    entry.score = score;
    entry.moveNum = (byte) moveNum;
    entry.proof = proof;

    return true;
  }

  public Entry get(long key) {
    stats.gets++;

    Entry entry = table[keyToIndex(key)];
    if (entry.key != key) {
      return null;
    }

    stats.hits++;
    return entry;
  }

  private int keyToIndex(long key) {
    return (int)(key & (table.length - 1));
  }

}
