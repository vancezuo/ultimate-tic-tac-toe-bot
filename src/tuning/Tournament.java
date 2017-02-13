package tuning;

import bot.Game;
import bot.Match;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * @author Vance Zuo
 */
public class Tournament {
  public static class RunParameters {
    final int cpus;
    final int randomStartLength;
    final int numRandomStarts;
    final int botSearchDepth;

    public RunParameters(int cpus, int randomStartLength, int numRandomStarts, int botSearchDepth) {
      this.cpus = cpus;
      this.randomStartLength = randomStartLength;
      this.numRandomStarts = numRandomStarts;
      this.botSearchDepth = botSearchDepth;
    }
  }

  Storage storage;
  Set<Integer> botIds;
  Map<Integer, Integer> botScores;

  ExecutorService executor;
  int totalGames;

  public Tournament(Storage storage, Set<Integer> botIds) {
    this.storage = storage;
    this.botIds = botIds;

    this.botScores = new HashMap<>();
    for (Integer botId : botIds) {
      this.botScores.put(botId, 0);
    }
  }

  public Tournament(Set<Integer> botIds) {
    this(new Storage(), botIds);
  }

  public Tournament(
      Storage storage,
      int populationId,
      int limit,
      ToDoubleFunction<BotEntry> ordering) {
    this(
        storage,
        storage.getBotIdsInPopulation(populationId)
            .stream()
            .map(storage::getBotEntry)
            .sorted(Comparator.comparingDouble(ordering).reversed())
            .limit(limit)
            .map(BotEntry::getId)
            .collect(Collectors.toSet()));
  }

  public Tournament(
      int populationId,
      int limit,
      ToDoubleFunction<BotEntry> ordering) {
    this(new Storage(), populationId, limit, ordering);
  }

  public void run(RunParameters params) {
    executor = Executors.newFixedThreadPool(params.cpus);

    System.out.println(String.format("Running new tournament (%s)", LocalDateTime.now()));
    for (Integer botId : botIds) {
      BotEntry botEntry = storage.getBotEntry(botId);
      System.out.println(String.format("Bot %s: %s", botId, botEntry.toRowString()));
    }

    matchAll(params);

    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }

    for (Map.Entry<Integer, Integer> botScore : botScores.entrySet()) {
      BotEntry botEntry = storage.getBotEntry(botScore.getKey());
      System.out.println(String.format(
          "Bot %s: score = %s, weights = %s",
          botScore.getKey(),
          botScore.getValue(),
          botEntry.weights.serializeString()));
    }
  }

  private void matchAll(RunParameters params) {
    List<List<Integer>> randomStarts =
        getRandomStarts(params.numRandomStarts, params.randomStartLength);

    for (int botId1 : botIds) {
      for (int botId2 : botIds) {
        if (botId1 != botId2) {
          for (List<Integer> startingMoves : randomStarts) {
            matchPair(botId1, botId2, params.botSearchDepth, startingMoves);
          }
        }
      }
    }
  }

  private void matchPair(
      int botId1,
      int botId2,
      int searchDepth,
      List<Integer> startingMoves) {
    executor.submit(() -> {
      BotEntry bot1 = storage.getBotEntry(botId1);
      BotEntry bot2 = storage.getBotEntry(botId2);
      Match match = new Match(bot1.weights, bot2.weights);
      for (int move : startingMoves) {
        match.manualMove(move);
      }
      int result = match.play(searchDepth);
      synchronized (this) {
        botScores.put(botId1, botScores.get(botId1) + result);
        botScores.put(botId2, botScores.get(botId2) - result);
        if (++totalGames % 100 == 0) {
          System.out.println(String.format("Ran %s games (%s)", totalGames, LocalDateTime.now()));
        }
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
}
