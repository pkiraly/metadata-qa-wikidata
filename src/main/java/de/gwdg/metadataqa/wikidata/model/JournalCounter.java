package de.gwdg.metadataqa.wikidata.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalCounter {
  String id;
  String label;
  int properPageNumberCounter = 0;
  int improperPageNumberCounter = 0;
  Map<String, Integer> typeCounter = new HashMap<>();

  public JournalCounter(String id) {
    this.id = id;
  }

  public void addProper() {
    properPageNumberCounter++;
  }

  public void addImproper() {
    improperPageNumberCounter++;
  }

  public void addType(String type) {
    if (!typeCounter.containsKey(type)) {
      typeCounter.put(type, 0);
    }
    typeCounter.put(type, typeCounter.get(type) + 1);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public int getImproperPageNumberCounter() {
    return improperPageNumberCounter;
  }

  public Map<String, Integer> getTypeCounter() {
    return typeCounter;
  }

  public List<String> formatTypeCounters() {
    List<String> entries = new ArrayList<>();
    typeCounter
      .entrySet()
      .forEach((entry) -> entries.add(
        String.format(
          "'%s' (%d)",
          entry.getKey(), entry.getValue()
        )
      ));
    return entries;
  }

  @Override
  public String toString() {
    return label + " (" + id + ")" +
      ": proper=" + properPageNumberCounter +
      ", improper=" + improperPageNumberCounter +
      ", total types=" + typeCounter.size() +
      ", types=" + StringUtils.join(formatTypeCounters(), ", ")
    ;
  }
}
