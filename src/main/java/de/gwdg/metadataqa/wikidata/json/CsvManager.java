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

  public <T> Map<String, T> readCsv(String csvFile, WikidataType type, Class<T> clazz) {
    Map<String, T> list = new HashMap<>();
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
          list.put(property.getId(), (T)property);
        } else if (type.equals(WikidataType.ENTITIES)) {
          if (clazz.equals(Wikidata.class)) {
            WikidataEntity entity = new WikidataEntity(line[0], line[1]);
            list.put(entity.getId(), (T) entity);
          } else {
            list.put(line[0], (T)line[1]);
          }
        }
        lineNumber++;
        if (lineNumber % 10000 == 0)
          System.err.printf("%,d%n", lineNumber);
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
