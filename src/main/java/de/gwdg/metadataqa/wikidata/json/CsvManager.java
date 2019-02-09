package de.gwdg.metadataqa.wikidata.json;

import com.opencsv.CSVReader;
import de.gwdg.metadataqa.wikidata.model.Wikidata;
import de.gwdg.metadataqa.wikidata.model.WikidataEntity;
import de.gwdg.metadataqa.wikidata.model.WikidataProperty;
import de.gwdg.metadataqa.wikidata.model.WikidataType;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CsvManager {

  public Map<String, Wikidata> readCsv(String csvFile, WikidataType type) {
    Map<String, Wikidata> list = new HashMap<>();
    CSVReader reader = null;
    int lineNumber = 0;
    String[] line = null;
    try {
      reader = new CSVReader(new FileReader(csvFile));
      while ((line = reader.readNext()) != null) {
        if (type.equals(WikidataType.PROPERTIES)) {
          WikidataProperty property;
          if (line.length == 2) {
            property = new WikidataProperty(line[0], line[1]);
          } else {
            property = new WikidataProperty(line[0], line[1], line[2]);
          }
          list.put(property.getId(), property);
        } else if (type.equals(WikidataType.ENTITIES)) {
          WikidataEntity entity = new WikidataEntity(line[0], line[1]);
          list.put(entity.getId(), entity);
        }
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
    System.err.println(type.name() + " " + lineNumber);
    return list;
  }


}
