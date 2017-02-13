package tuning;

import bot.Weights;

import java.util.Arrays;

/**
 * @author Vance Zuo
 */
public class BotEntry {
  public static final int DEFAULT_ELO = 2000;

  int id;
  double elo;
  double peakElo;
  double avgOppElo;
  int games;
  Weights weights;

  public BotEntry(
      int id,
      double elo,
      double peakElo,
      double avgOppElo,
      int games,
      Weights weights) {
    this.id = id;
    this.elo = elo;
    this.peakElo = peakElo;
    this.avgOppElo = avgOppElo;
    this.games = games;
    this.weights = weights;
  }

  public int getId() {
    return id;
  }

  public double getElo() {
    return elo;
  }

  public double getPeakElo() {
    return peakElo;
  }

  public int getGames() {
    return games;
  }

  public String toRowString() {
    return String.join(Storage.DELIMITER, Arrays.asList(
        Integer.toString(id),
        Double.toString(elo),
        Double.toString(peakElo),
        Double.toString(avgOppElo),
        Integer.toString(games),
        weights.serializeString()));
  }

  public static BotEntry fromRowString(String s) {
    String[] columns = s.split(Storage.DELIMITER);
    return new BotEntry(
        Integer.parseInt(columns[0]),
        Double.parseDouble(columns[1]),
        Double.parseDouble(columns[2]),
        Double.parseDouble(columns[3]),
        Integer.parseInt(columns[4]),
        Weights.deserializeString(columns[5]));
  }

  public static String getRowHeader() {
    return String.join(
        Storage.DELIMITER,
        Arrays.asList("Id", "Elo", "Peak Elo", "Games", "Weights"));
  }
}
