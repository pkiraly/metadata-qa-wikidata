package de.gwdg.metadataqa.wikidata.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikidataEntity {
  String id;
  String type;
  String label;
  Map<String, String> classes;

  public WikidataEntity(String id, String label) {
    this.id = id;
    this.type = type;
    this.label = label;
  }

  public void setClasses(Map<String, String> classes) {
    this.classes = classes;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  public String serializeClasses() {
    List<String> entries = new ArrayList<>();
    for (Map.Entry<String, String> entry : classes.entrySet()) {
      entries.add(String.format("%s:%s", entry.getKey(), entry.getValue()));
    }
    return StringUtils.join(entries, "|");
  }

  public Map<String, String> deserializeClasses(String serializedClasses) {
    Map<String, String> entries = new HashMap<>();
    for (String entry : serializedClasses.split("\\|")) {
      String[] parts = entry.split(":", 2);
      entries.put(parts[0], parts[1]);
    }
    return entries;
  }
}
