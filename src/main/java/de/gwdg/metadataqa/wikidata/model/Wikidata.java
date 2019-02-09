package de.gwdg.metadataqa.wikidata.model;

public interface Wikidata {
  String getId();
  String getType();
  String getLabel();
  String[] asArray();
}
