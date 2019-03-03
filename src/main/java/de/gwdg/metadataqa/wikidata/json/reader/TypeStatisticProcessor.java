package de.gwdg.metadataqa.wikidata.json.reader;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.gwdg.metadataqa.wikidata.json.CsvManager;
import de.gwdg.metadataqa.wikidata.json.Utils;
import de.gwdg.metadataqa.wikidata.model.JournalCounter;
import de.gwdg.metadataqa.wikidata.model.WikidataType;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeStatisticProcessor implements LineProcessor {
  private int recordCounter = 0;
  private DecimalFormat myFormatter = new DecimalFormat("###,###.###");
  private Map<String, Integer> typeCounter = new HashMap<>();
  private CsvManager csvManager = new CsvManager();
  private final String entityFile;
  private Map<String, String> entities;
  private String outputFileName;

  public TypeStatisticProcessor(String entityFile) {
    this.entityFile = entityFile;
  }

  @Override
  public void setOutputFileName(String fileName) {
    this.outputFileName = fileName;
  }

  @Override
  public void read(String line) {
    read(line, true);
  }

  @Override
  public void read(String line, boolean processable) {
    recordCounter++;
    if (recordCounter % 100000 == 0)
      System.err.println(myFormatter.format(recordCounter));

    if (!processable) {
      return;
    }

    DocumentContext context = JsonPath.parse(line);

    Object typeObject = Utils.getPath(context, "$.claims.P31");
    if (typeObject == null) {
      return;
    }
    if (typeObject instanceof JSONArray) {
      List<String> types = (List<String>) typeObject;
      for (String type : types) {
        Utils.increment(typeCounter, type);
      }
      // Utils.increment(typeCounter, StringUtils.join(types, '&'));
    }
  }

  private void increment(String key) {
    Utils.increment(typeCounter, key);
  }

  @Override
  public int getRecordCounter() {
    return recordCounter;
  }

  @Override
  public void postProcess() {
    if (typeCounter.isEmpty())
      return;

    csvManager.setEntitiesFilter(typeCounter);
    entities = csvManager.readCsv(entityFile, WikidataType.ENTITIES, String.class);
  }

  @Override
  public void saveState() {

    Path path = Paths.get(outputFileName);
    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      typeCounter
        .entrySet()
        .stream()
        // .filter((a) -> a.getImproperPageNumberCounter() > 1)
        .sorted((a, b) ->
          Integer.compare(b.getValue(), a.getValue())
        )
        .forEach(
          (entity) -> {
            String line = String.format(
              "'%s',%s,%d%n",
              entities.get(entity.getKey()), entity.getKey(), entity.getValue()
            );
            try {
              writer.write(line);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
