package de.gwdg.metadataqa.wikidata.json;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.gwdg.metadataqa.wikidata.json.labelextractor.JenaBasedSparqlClient;
import de.gwdg.metadataqa.wikidata.model.WikidataEntity;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MultithreadClassExtractor {

  private final String inputFileName;
  private Map<String, WikidataEntity> entities = new HashMap<>();
  private final JenaBasedSparqlClient sparqlClient;
  private AtomicInteger newEntitiesCount = new AtomicInteger();
  private long duration = 0;
  private int autoSaveTreshold = 1000;
  private int numberOfThreads;

  public MultithreadClassExtractor(String inputFileName, int numberOfThreads) {
    this.inputFileName = inputFileName;
    this.numberOfThreads = numberOfThreads;
    readCsv(inputFileName);
    sparqlClient = new JenaBasedSparqlClient();
  }

  public void resolveAll() {
    long start = System.currentTimeMillis();
    // long startSaving = 0;
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
    for (WikidataEntity entity : entities.values()) {
      if (entity.getClasses() == null || entity.getClasses().isEmpty()) {
        Runnable task = () -> {
          entity.setClasses(sparqlClient.getClasses(entity.getId()));
          int i = newEntitiesCount.incrementAndGet();
          if (autoSaveTreshold != 0
            && i > 0
            && i % autoSaveTreshold == 0) {
            long startSaving = System.currentTimeMillis();
            saveEntities(inputFileName);
            System.err.printf("saved so far %d records, took %d ms%n", i, (System.currentTimeMillis() - startSaving));
          }
        };
        executor.submit(task);
      }
    }
    executor.shutdown();

    while (!executor.isTerminated()) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    duration += (System.currentTimeMillis() - start);
  }

  public Map<String, WikidataEntity> getEntities() {
    return entities;
  }

  private void readCsv(String csvFile) {
    CSVReader reader = null;
    int lineNumber = 0;
    String[] line = null;
    try {
      reader = new CSVReader(new FileReader(csvFile));
      while ((line = reader.readNext()) != null) {
        if (line[0].equals("id"))
          continue;

        WikidataEntity entity = new WikidataEntity(line[0], line[1]);
        if (line.length == 3)
          entity.setClasses(entity.deserializeClasses(line[2]));

        entities.put(line[0], entity);
        lineNumber++;
      }
    } catch (IOException e) {
      System.err.println("line number: " + lineNumber);
      System.err.println("last good line: " + StringUtils.join(line, " // "));
      e.printStackTrace();
      throw new IllegalArgumentException("Wrong CSV file: " + csvFile);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Wrong CSV file: " + csvFile);
    }
    System.err.println(lineNumber);
  }

  public void saveEntities(String entitiesFile) {
    if (newEntitiesCount.get() == 0)
      return;

    System.err.printf(
      "save entities: %d new/%d total (entities import took %d ms)\n",
      newEntitiesCount.get(),
      entities.size(),
      duration
    );
    duration = 0;
    FileWriter writer = null;
    try {
      writer = new FileWriter(entitiesFile);
      //using custom delimiter and quote character
      CSVWriter csvWriter = new CSVWriter(writer);

      List<String[]> data = entitiesToStringArray();

      csvWriter.writeAll(data);
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<String[]> entitiesToStringArray() {
    List<String[]> records = new ArrayList<String[]>();

    // adding header record
    records.add(new String[]{"id", "label", "classes"});

    for (Map.Entry<String, WikidataEntity> entry : entities.entrySet()) {
      records.add(new String[]{
        entry.getKey(),
        entry.getValue().getLabel(),
        entry.getValue().serializeClasses()
      });
    }

    return records;
  }


}
