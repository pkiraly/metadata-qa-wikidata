package de.gwdg.metadataqa.wikidata.json;

import com.opencsv.CSVReader;
import de.gwdg.metadataqa.wikidata.model.WikidataEntity;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClassExtractor {

  private final String inputFileName;
  private Map<String, WikidataEntity> entities = new HashMap<>();

  public ClassExtractor(String inputFileName) {
    this.inputFileName = inputFileName;
    readCsv(inputFileName);
  }

  private void readCsv(String csvFile) {
    CSVReader reader = null;
    int lineNumber = 0;
    String[] line = null;
    try {
      reader = new CSVReader(new FileReader(csvFile));
      while ((line = reader.readNext()) != null) {
        WikidataEntity entity = null;
        if (line.length == 2) {
          entity = new WikidataEntity(line[0], line[1]);
        } else {
          entity = new WikidataEntity(line[0], line[1]);
          entity.setClasses(entity.deserializeClasses(line[2]));
        }
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

}
