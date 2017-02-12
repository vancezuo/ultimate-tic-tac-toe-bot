package tuning;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vance Zuo
 */
class PopulationEntry {
  int id;
  int generation;
  List<List<Integer>> botIds;

  public PopulationEntry(
      int id,
      int generation,
      List<List<Integer>> botIds) {
    this.id = id;
    this.generation = generation;
    this.botIds = botIds;
  }

  public String toRowString() {
    return String.join(Storage.DELIMITER, Arrays.asList(
        Integer.toString(id),
        Integer.toString(generation),
        botIds.stream()
            .map(x -> x.stream().map(Object::toString).collect(Collectors.joining(",")))
            .collect(Collectors.joining("/"))));
  }

  public static PopulationEntry fromRowString(String s) {
    String[] columns = s.split(Storage.DELIMITER);
    return new PopulationEntry(
        Integer.parseInt(columns[0]),
        Integer.parseInt(columns[1]),
        Arrays.stream(columns[2].split("/"))
            .map(x -> Arrays.stream(x.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList()))
            .collect(Collectors.toList()));
  }

  public static String getRowHeader() {
    return String.join(Storage.DELIMITER, Arrays.asList("Id", "Generation", "Bot Ids"));
  }
}
