package tuning;

import bot.Game;
import bot.Match;
import bot.Weights;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Vance Zuo
 */
public class GeneticAlgorithm {
  public static class PrepareParameters {
    final int numSpecies;
    final int popPerSpecies;

    public PrepareParameters(int numSpecies, int popPerSpecies) {
      this.numSpecies = numSpecies;
      this.popPerSpecies = popPerSpecies;
    }
  }

  public static class RunParameters {
    final int generations;
    final double mutationRate;
    final double mutationWeight;
    final int cpus;
    final int randomStartLength;
    final int numRandomStartsElite;
    final int numRandomStartsAll;
    final int botSearchDepth;
    final int kFactor;
    final double selectionProp;
    final double replacementRate;

    public RunParameters(
        int generations,
        double mutationRate,
        double mutationWeight,
        int cpus,
        int randomStartLength,
        int numRandomStartsElite,
        int numRandomStartsAll, int botSearchDepth,
        int kFactor,
        double selectionProp,
        double replacementRate) {
      this.generations = generations;
      this.mutationRate = mutationRate;
      this.mutationWeight = mutationWeight;
      this.cpus = cpus;
      this.randomStartLength = randomStartLength;
      this.numRandomStartsElite = numRandomStartsElite;
      this.numRandomStartsAll = numRandomStartsAll;
      this.botSearchDepth = botSearchDepth;
      this.kFactor = kFactor;
      this.selectionProp = selectionProp;
      this.replacementRate = replacementRate;
    }
  }

  Storage storage;

  Map<Integer, BotEntry> bots;
  PopulationEntry currentPopulation;

  public GeneticAlgorithm(Storage storage) {
    this.storage = storage;
    this.bots = new HashMap<>();
  }

  public GeneticAlgorithm() {
    this(new Storage());
  }

  public void prepareNew(PrepareParameters params) {
    System.out.println(String.format("Preparing new run (%s)", LocalDateTime.now()));

    int id = storage.getNextPopulationEntryId();
    int generation = 0;

    List<List<Integer>> population = new ArrayList<>();
    for (int i = 0; i < params.numSpecies; i++) {
      List<Integer> botIds = new ArrayList<>();
      for (int j = 0; j < params.popPerSpecies; j++) {
        Weights weights = new Weights();
        if (i != 0 || j != 0) {
          weights = weights.average(Weights.randomWeights(), 0.5);
        }
        BotEntry botEntry = storage.addBotEntry(weights);
        bots.put(botEntry.id, botEntry);
        botIds.add(botEntry.id);
      }
      population.add(botIds);
    }

    currentPopulation = new PopulationEntry(id, generation, population);
    storage.addPopulationEntry(currentPopulation);

    storage.save();
  }

  public void prepareContinue() {
    System.out.println(String.format("Preparing continue run (%s)", LocalDateTime.now()));

    currentPopulation = storage.getLastPopulationEntry();
    for (List<Integer> species : currentPopulation.botIds) {
      for (int botId : species) {
        bots.put(botId, storage.getBotEntry(botId));
      }
    }
  }

  public void run(RunParameters params) {
    for (int i = 0, l = params.generations; l == -1 || i < l; i++) {
      ExecutorService executor = Executors.newFixedThreadPool(params.cpus);
      System.out.println(String.format(
          "Generation %s (%s)",
          currentPopulation.generation,
          LocalDateTime.now()));

      if (currentPopulation.generation == 0) {
        matchAll(params, executor);
      } else {
        matchElite(params, executor);
      }
      executor.shutdown();
      try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }

      IntStream.range(0, currentPopulation.botIds.size())
          .forEach(species -> currentPopulation.botIds.get(species)
              .stream()
              .map(id -> bots.get(id))
              .sorted((o1, o2) -> o1.elo > o2.elo ? -1 : 1)
              .forEach(
                  bot -> System.out.println(String.format(
                      "%s | %s (%.0f): %s",
                      (char) (species + 'A'),
                      bot.id,
                      bot.elo,
                      bot.weights.serializeString()))));

      System.out.println("Average hamming distances: " + calculateAverageHammingDistance());

      currentPopulation = new PopulationEntry(
          currentPopulation.id,
          currentPopulation.generation + 1,
          generateNextPopulation(params));

      storage.addPopulationEntry(currentPopulation);
      storage.save();

      beep();
    }
  }

  private void beep() {
    java.awt.Toolkit.getDefaultToolkit().beep();
  }

  private List<List<Integer>> generateNextPopulation(RunParameters params) {
    List<List<Integer>> nextPopulation = new ArrayList<>();
    for (List<Integer> species : currentPopulation.botIds) {
      int removes = (int) Math.round(params.replacementRate * species.size());
      int keeps = species.size() - removes;
      List<Integer> sortedSpecies = species.stream()
          .map(x -> bots.get(x))
          .sorted(Comparator.comparingDouble(BotEntry::getElo).reversed())
          .map(BotEntry::getId)
          .collect(Collectors.toList());
      List<Integer> nextBotIds = new ArrayList<>(sortedSpecies.subList(0, keeps));
      nextBotIds.addAll(generateOffspring(
          removes,
          sortedSpecies,
          params.selectionProp,
          params.mutationRate,
          params.mutationWeight));
      nextPopulation.add(nextBotIds);
    }
    return nextPopulation;
  }

  private List<Integer> generateOffspring(
      int numOffspring,
      List<Integer> sortedSpecies,
      double selectionProp,
      double mutationRate,
      double mutationWeight) {
    List<Integer> candidates = new ArrayList<>(sortedSpecies);
    List<Integer> offspring = new ArrayList<>();
    int numSelect = (int) Math.max(2, Math.round(selectionProp * sortedSpecies.size()));
    for (int i = 0; i < numOffspring; i++) {
      Collections.shuffle(candidates);
      List<BotEntry> parents = candidates.subList(0, numSelect).stream()
          .map(x -> bots.get(x))
          .sorted(Comparator.comparingDouble(BotEntry::getElo))
          .limit(2)
          .collect(Collectors.toList());
      BotEntry bot1 = parents.get(0), bot2 = parents.get(1);
      double q1 = Math.pow(10, bot1.elo / 400), q2 = Math.pow(10, bot2.elo / 400);
      double crossoverWeight = q1 / (q1 + q2); // expected win probability bot1 vs bot2
      Weights newWeights = bot1.weights.average(bot2.weights, crossoverWeight);
      if (mutationRate < Math.random()) {
        newWeights = newWeights.average(Weights.randomWeights(), mutationWeight);
      }
      BotEntry botEntry = storage.addBotEntry(newWeights);
      bots.put(botEntry.id, botEntry);
      offspring.add(botEntry.id);
    }
    return offspring;
  }

  private void matchAll(RunParameters params, ExecutorService executor) {
    List<List<Integer>> randomStarts =
        getRandomStarts(params.numRandomStartsAll, params.randomStartLength);
    for (List<Integer> species : currentPopulation.botIds) {
      for (int botId : species) {
        matchAll(botId, params.botSearchDepth, params.kFactor, randomStarts, executor);
      }
    }
  }

  private void matchAll(
      int botId,
      int searchDepth,
      int kFactor,
      List<List<Integer>> starts,
      ExecutorService executor) {
    for (List<Integer> species : currentPopulation.botIds) {
      for (int otherBotId : species) {
        for (List<Integer> startingMoves : starts) {
          if (botId != otherBotId) {
            matchPair(botId, otherBotId, searchDepth, kFactor, startingMoves, executor);
          }
        }
      }
    }
  }

  private void matchElite(RunParameters params, ExecutorService executor) {
    List<List<Integer>> randomStarts =
        getRandomStarts(params.numRandomStartsElite, params.randomStartLength);
    List<Integer> elites = new ArrayList<>();
    for (List<Integer> species : currentPopulation.botIds) {
      elites.add(species.stream()
          .map(x -> bots.get(x))
          .max(Comparator.comparingDouble(BotEntry::getElo))
          .get().id);
    }
    for (List<Integer> species : currentPopulation.botIds) {
      for (int botId : species) {
        matchElite(botId, params.botSearchDepth, params.kFactor, elites, randomStarts, executor);
      }
    }
  }

  private void matchElite(
      int botId,
      int searchDepth,
      int kFactor,
      List<Integer> elites,
      List<List<Integer>> starts,
      ExecutorService executor) {
    for (int otherBotId : elites) {
      for (List<Integer> startingMoves : starts) {
        if (botId != otherBotId) {
          matchPair(botId, otherBotId, searchDepth, kFactor, startingMoves, executor);
          matchPair(otherBotId, botId, searchDepth, kFactor, startingMoves, executor);
        }
      }
    }
  }

  private void matchPair(
      int botId,
      int otherBotId,
      int searchDepth,
      int kFactor,
      List<Integer> startingMoves,
      ExecutorService executor) {
    executor.submit(() -> {
      BotEntry bot1 = bots.get(botId);
      BotEntry bot2 = bots.get(otherBotId);
      Match match = new Match(bot1.weights, bot2.weights);
      for (int move : startingMoves) {
        match.manualMove(move);
      }
      int result = match.play(searchDepth);
      synchronized (this) {
        updateElo(bot1, bot2, kFactor, result);
      }
    });
  }

  private List<List<Integer>> getRandomStarts(int numRandomStart, int randomStartLength) {
    List<List<Integer>> randomStarts = new ArrayList<>();
    for (int i = 0; i < numRandomStart; i++) {
      randomStarts.add(getRandomStartMoves(randomStartLength));
    }
    return randomStarts;
  }

  private List<Integer> getRandomStartMoves(int randomStartLength) {
    Game game = new Game();
    List<Integer> randomStartMoves = new ArrayList<>();
    for (int i = 0; i < randomStartLength; i++) {
      int move = game.generateRandomMove();
      randomStartMoves.add(move);
      game.doMove(move);
    }
    return randomStartMoves;
  }

  private void updateElo(BotEntry bot1, BotEntry bot2, int kFactor, int result) {
    double rating1 = bot1.elo;
    double rating2 = bot2.elo;
    double q1 = Math.pow(10, rating1 / 400);
    double q2 = Math.pow(10, rating2 / 400);
    double expectedScore1 = q1 / (q1 + q2);
    double expectedScore2 = q2 / (q1 + q2);
    double actualScore1 = (result + 1) / 2.0; // -1, 0, 1 -> 0, 0.5, 1
    double actualScore2 = (-result + 1) / 2.0; // -1, 0, 1 -> 1, 0.5, 1
    double newRating1 = rating1 + kFactor * (actualScore1 - expectedScore1);
    double newRating2 = rating2 + kFactor * (actualScore2 - expectedScore2);
    bot1.avgOppElo = (bot1.avgOppElo * bot1.games + rating2) / (bot1.games + 1);
    bot2.avgOppElo = (bot2.avgOppElo * bot2.games + rating1) / (bot2.games + 1);
    bot1.games++;
    bot2.games++;
    bot1.elo = newRating1;
    bot2.elo = newRating2;
    if (bot1.peakElo < bot1.elo) {
      bot1.peakElo = bot1.elo;
    }
    if (bot2.peakElo < bot2.elo) {
      bot2.peakElo = bot2.elo;
    }
  }

  private List<Double> calculateAverageHammingDistance() {
    List<Double> averages = new ArrayList<>();
    for (List<Integer> species : currentPopulation.botIds) {
      double totalDistances = 0;
      double numDistances = 0;
      for (int botId : species) {
        for (int otherBotId : species) {
          if (botId != otherBotId) {
            totalDistances +=
                bots.get(botId).weights.hammingDistance(bots.get(otherBotId).weights);
            numDistances++;
          }
        }
      }
      averages.add(totalDistances / numDistances);
    }
    return averages;
  }
}
