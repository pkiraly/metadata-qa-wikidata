package de.gwdg.metadataqa.wikidata.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikidataEntity implements Wikidata {
  private String id;
  private String type;
  private String label;
  private String[] asArray;
  private Map<String, String> classes;
  private String serializedClasses;

  public WikidataEntity(String id) {
    this.id = id;
  }

  public WikidataEntity(String id, String label) {
    this.id = id;
    this.type = "";
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

  public void setLabel(String label) {
    this.label = label;
  }

  public Map<String, String> getClasses() {
    return classes;
  }

  public String serializeClasses() {
    if (serializedClasses == null) {
      List<String> entries = new ArrayList<>();
      if (classes != null) {
        for (Map.Entry<String, String> entry : classes.entrySet()) {
          entries.add(String.format("%s:%s", entry.getKey(), entry.getValue()));
        }
      }
      serializedClasses = StringUtils.join(entries, "|");
    }
    return serializedClasses;
  }

  public Map<String, String> deserializeClasses(String serializedClasses) {
    Map<String, String> entries = new HashMap<>();
    if (StringUtils.isNotBlank(serializedClasses)) {
      for (String entry : serializedClasses.split("\\|")) {
        String[] parts = entry.split(":", 2);
        if (parts.length == 2) {
          entries.put(parts[0], parts[1]);
        } else {
          System.err.println("Error: " + entry);
        }
      }
    }
    return entries;
  }

  @Override
  public String[] asArray() {
    // if (asArray == null) {
    //   asArray = new String[]{id, label};
    // }
    return new String[]{id, label};

  }
}
