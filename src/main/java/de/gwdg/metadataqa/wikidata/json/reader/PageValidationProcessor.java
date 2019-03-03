package de.gwdg.metadataqa.wikidata.json.reader;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.gwdg.metadataqa.wikidata.json.CsvManager;
import de.gwdg.metadataqa.wikidata.json.InvalidPageNumberException;
import de.gwdg.metadataqa.wikidata.json.PageValidator;
import de.gwdg.metadataqa.wikidata.model.JournalCounter;
import de.gwdg.metadataqa.wikidata.model.WikidataType;
import net.minidev.json.JSONArray;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageValidationProcessor implements LineProcessor {

  private final String entitiesFile;
  private final String outputFile;
  private PageValidator validator = new PageValidator();
  private Map<String, JournalCounter> journalCounter = new HashMap<>();
  private boolean report = false;
  private CsvManager csvManager = new CsvManager();
  private Map<String, String> entities;
  private int recordCounter = 0;
  private DecimalFormat myFormatter = new DecimalFormat("###,###.###");

  public PageValidationProcessor(String outputFile, String entitiesFile) {
    this.outputFile = outputFile;
    this.entitiesFile = entitiesFile;
  }

  @Override
  public void setOutputFileName(String fileName) {

  }

  @Override
  public void read(String line) {
    recordCounter++;
    if (recordCounter % 100000 == 0)
      System.err.println(myFormatter.format(recordCounter));

    DocumentContext context = JsonPath.parse(line);

    Object pageObject = getPath(context, "$.claims.P304");
    if (pageObject == null) {
      return;
    }

    String id = context.read("$.id");
    JSONArray journals = (JSONArray) getPath(context, "$.claims.P1433");
    String journal = null;
    if (journals != null && !journals.isEmpty()) {
      journal = (String)journals.get(0);
      /*
      if (journal.equals("Q7104844")) {
        System.err.println(id);
      }
      */
    }
    ;
    if (pageObject instanceof JSONArray) {
      List<String> pages = (List<String>) pageObject;
      for (String page : pages) {
        try {
          validator.validatePageNumbers(page);
          countProperPageNumber(journal);
        } catch (InvalidPageNumberException e) {
          if (id == null) {
            id = context.read("$.id");
          }
          if (journal != null) {
            countImproperPageNumber(journal, e.getShortened());
          }
          if (report) {
            System.err.printf(
              "%s/%s -- invalid page number: %s (%s)%n",
              id, journal, page, e.getType()
            );
          }
        }
      }
    } else {
      System.err.println(id + " -- unknown pageObject type: " + pageObject.getClass());
    }
  }

  private void countProperPageNumber(String journal) {
    if (!journalCounter.containsKey(journal)) {
      journalCounter.put(journal, new JournalCounter(journal));
    }
    JournalCounter c = journalCounter.get(journal);
    c.addProper();
  }

  private void countImproperPageNumber(String journal, String shortened) {
    if (!journalCounter.containsKey(journal)) {
      journalCounter.put(journal, new JournalCounter(journal));
    }
    JournalCounter c = journalCounter.get(journal);
    c.addImproper();
    c.addType(shortened);
  }

  private Object getPath(DocumentContext context, String path) {
    Object value = null;
    try {
      value = context.read(path);
    } catch (PathNotFoundException e) {
      //
    }
    return value;
  }

  @Override
  public void read(String line, boolean processable) {

  }

  @Override
  public int getRecordCounter() {
    return recordCounter;
  }

  @Override
  public void postProcess() {
    if (journalCounter.isEmpty())
      return;

    csvManager.setEntitiesFilter(journalCounter);
    entities = csvManager.readCsv(entitiesFile, WikidataType.ENTITIES, String.class);

    journalCounter
      .values()
      .forEach((x) -> x.setLabel(entities.get(x.getId())));
  }

  @Override
  public void saveState() {
    if (journalCounter.isEmpty())
      return;

    Path path = Paths.get(outputFile);
    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      journalCounter
        .values()
        .stream()
        // .filter((a) -> a.getImproperPageNumberCounter() > 1)
        .sorted((a, b) ->
          Integer.compare(b.getImproperPageNumberCounter(), a.getImproperPageNumberCounter())
        )
        .forEach(
          (counter) -> {
            try {
              writer.write(counter.toString() + "\n");
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
