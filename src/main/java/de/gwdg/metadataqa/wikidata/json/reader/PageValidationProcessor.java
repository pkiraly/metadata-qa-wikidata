package de.gwdg.metadataqa.wikidata.json.reader;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.gwdg.metadataqa.wikidata.json.CsvManager;
import de.gwdg.metadataqa.wikidata.json.InvalidPageNumberException;
import de.gwdg.metadataqa.wikidata.json.PageValidator;
import de.gwdg.metadataqa.wikidata.json.Utils;
import de.gwdg.metadataqa.wikidata.model.JournalCounter;
import de.gwdg.metadataqa.wikidata.model.WikidataType;
import net.minidev.json.JSONArray;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageValidationProcessor implements LineProcessor {

  private final String entitiesFile;
  private String outputFile;
  private PageValidator validator = new PageValidator();
  private Map<String, JournalCounter> journalCounter = new HashMap<>();
  private boolean report = false;
  private CsvManager csvManager = new CsvManager();
  private Map<String, String> entities;
  private int recordCounter = 0;
  private DecimalFormat myFormatter = new DecimalFormat("###,###.###");
  private List<String> journalsToDebug = Arrays.asList(); // "Q546003"

  public PageValidationProcessor(String entitiesFile) {
    this.entitiesFile = entitiesFile;
  }

  @Override
  public void setOutputFileName(String fileName) {
    this.outputFile = fileName;
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

    Object pageObject = Utils.getPath(context, "$.claims.P304");
    if (pageObject == null) {
      return;
    }

    String id = context.read("$.id");
    JSONArray journals = (JSONArray) Utils.getPath(context, "$.claims.P1433");
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
          validator.validatePageNumbers(page, journal);
          countProperPageNumber(journal);
        } catch (InvalidPageNumberException e) {
          if (id == null) {
            id = context.read("$.id");
          }
          if (journal != null && journalsToDebug.contains(journal)) {
            System.err.printf("%s) %s (%s): %s%n", id, journal, e.getType(), page);
          }
          if (journal != null) {
            countImproperPageNumber(journal, e);
            if ((report)) { // || journal.equals("Q546003"))
              // && !e.getType().equals(PageNumberErrorType.SHORTER_SECOND_NUMBER)) {
              System.err.printf(
                "%s/%s -- invalid page number: %s (%s)%n",
                id, journal, page, e.getType()
              );
            }
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

  private void countImproperPageNumber(String journal, InvalidPageNumberException execption) {
    if (!journalCounter.containsKey(journal)) {
      journalCounter.put(journal, new JournalCounter(journal));
    }
    JournalCounter journalCounter = this.journalCounter.get(journal);
    journalCounter.addImproper();
    journalCounter.addPageNumberPattern(execption.getShortened());
    journalCounter.addPageErrorType(execption.getType());
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
      writer.write(JournalCounter.csvHeader() + "\n");
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
              writer.write(counter.toCsv() + "\n");
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
