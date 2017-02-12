package tuning;

import bot.Weights;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;
import static tuning.BotEntry.DEFAULT_ELO;

/**
 * @author Vance Zuo
 */
public class Storage {
  private static final String BOTS_FILENAME = "bots.txt";
  private static final String POPULATIONS_FILENAME = "populations.txt";

  public static final String DELIMITER = "\t";

  List<BotEntry> botEntries;
  List<PopulationEntry> populationEntries;

  private String botsFilename, populationFilename;

  public Storage() {
    this(BOTS_FILENAME, POPULATIONS_FILENAME);
  }

  public Storage(String botsFilename, String populationFilename) {
    this.botsFilename = botsFilename;
    this.populationFilename = populationFilename;

    load();
  }

  public BotEntry addBotEntry(Weights weights) {
    BotEntry entry = new BotEntry(getNextBotEntryId(), DEFAULT_ELO, DEFAULT_ELO, 0, 0, weights);
    botEntries.add(entry);
    System.err.println("New bot: " + entry.toRowString());
    return entry;
  }

  public BotEntry getBotEntry(int botId) {
    return botEntries.stream().filter(x -> x.id == botId).findFirst().get();
  }

  public int getNextBotEntryId() {
    if (botEntries.isEmpty()) {
      return 0;
    }
    return 1 + botEntries.get(botEntries.size() - 1).id;

  }

  public void addPopulationEntry(PopulationEntry entry) {
    populationEntries.add(entry);
    System.err.println("New population: " + entry.toRowString());
  }

  public PopulationEntry getLastPopulationEntry() {
    return populationEntries.get(populationEntries.size() - 1);
  }

  public int getNextPopulationEntryId() {
    if (populationEntries.isEmpty()) {
      return 0;
    }
    return 1 + getLastPopulationEntry().id;
  }

  public void load() {
    botEntries = parseFile(botsFilename, BotEntry::fromRowString);
    populationEntries = parseFile(populationFilename, PopulationEntry::fromRowString);
    System.err.println("Loaded from: " + Paths.get(".").toAbsolutePath().normalize());
  }

  private <E> List<E> parseFile(String filename, Function<String, E> func) {
    if (filename == null) {
      return new ArrayList<>();
    }
    try {
      return Files.readAllLines(Paths.get(filename))
          .stream()
          .filter(x -> !x.trim().isEmpty())
          .map(func)
          .collect(Collectors.toList());
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  public void save() {
    saveFile(botEntries, botsFilename, BotEntry::toRowString);
    saveFile(populationEntries, populationFilename, PopulationEntry::toRowString);
    System.err.println("Saved to: " + Paths.get(".").toAbsolutePath().normalize());
  }

  private <E> void saveFile(List<E> entries, String filename, Function<E, String> func) {
    if (filename == null) {
      return;
    }
    try {
      Files.write(
          Paths.get(filename),
          entries.stream().map(func).collect(Collectors.toList()),
          CREATE, TRUNCATE_EXISTING, WRITE);
    } catch (IOException e) {
      // ignored
    }
  }
}
