package de.gwdg.metadataqa.wikidata.json.reader;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.gwdg.metadataqa.wikidata.json.Utils;

import java.text.DecimalFormat;
import java.util.List;

public class AuthorValidationProcessor implements LineProcessor {

  private final String entityFile;
  private String outputFile;
  private int recordCounter = 0;
  private DecimalFormat myFormatter = new DecimalFormat("###,###.###");

  public AuthorValidationProcessor(String entityFile) {
    this.entityFile = entityFile;
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
    String id = context.read("$.id");
    List<String> authorNames = Utils.getPathAsList(context, "$.claims.P2093");
    List<String> authorEntities = Utils.getPathAsList(context, "$.claims.P50");
    if (authorNames != null) {
      if (authorNames.size() == 1) {
        System.err.println(id + ") " + authorNames.get(0));
      }
      System.err.println(id + ") " + authorNames.size());
    }
  }

  @Override
  public int getRecordCounter() {
    return 0;
  }

  @Override
  public void postProcess() {

  }

  @Override
  public void saveState() {

  }
}
