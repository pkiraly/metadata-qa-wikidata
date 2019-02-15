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

  private Map<String, Integer> entitiesFilter = null;

  public <T> Map<String, T> readCsv(String csvFile, WikidataType type, Class<T> clazz) {
    Map<String, T> list = new HashMap<>();
    CSVReader reader = null;

    int lineNumber = 0;

    String[] line = null;
    try {
      reader = new CSVReader(new FileReader(csvFile));
      boolean doFilter = entitiesFilter != null && !entitiesFilter.isEmpty();
      String id;
      String label;
      while ((line = reader.readNext()) != null) {
        id = line[0];
        label = line[1];
        if (type.equals(WikidataType.PROPERTIES)) {
          WikidataProperty property;
          if (line.length == 2) {
            property = new WikidataProperty(id, label);
          } else {
            property = new WikidataProperty(id, label, label);
          }
          list.put(property.getId(), (T)property);
        } else if (type.equals(WikidataType.ENTITIES)) {
          if (label.contains("\n")) {
            System.err.printf("%s%n%s%n%n", id, label);
          }
          if (!doFilter || entitiesFilter.containsKey(id)) {
            if (clazz.equals(Wikidata.class)) {
              WikidataEntity entity = new WikidataEntity(id, label);
              list.put(entity.getId(), (T) entity);
            } else {
              list.put(id, (T)label);
            }
          }
        } else if (type.equals(WikidataType.ENTITIES_COUNT)) {
          list.put(id, (T)label);
        }
        lineNumber++;
        if (lineNumber % 100000 == 0)
          System.err.printf("%,d / %,d - %s%n", list.size(), lineNumber, id);
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

  public void setEntitiesFilter(Map<String, Integer> entitiesCounter) {
    this.entitiesFilter = entitiesCounter;
  }
}
