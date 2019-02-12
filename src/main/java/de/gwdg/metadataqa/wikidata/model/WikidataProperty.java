package de.gwdg.metadataqa.wikidata.model;

public class WikidataProperty implements Wikidata {
  private String id;
  private String label;
  private String type = "";
  private String[] asArray;

  public WikidataProperty(String id, String label) {
    this.id = id;
    this.label = label;
  }

  public WikidataProperty(String id, String label, String type) {
    this(id, label);
    this.type = type;
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

  @Override
  public String[] asArray() {
    // if (asArray == null) {
    //   asArray = new String[]{id, label, type};
    // }
    return new String[]{id, label, type};
  }
}
