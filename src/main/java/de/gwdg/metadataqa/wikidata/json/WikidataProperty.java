package de.gwdg.metadataqa.wikidata.json;

public class WikidataProperty {
  String id;
  String type;
  String label;

  public WikidataProperty(String id, String type, String label) {
    this.id = id;
    this.type = type;
    this.label = label;
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
}
