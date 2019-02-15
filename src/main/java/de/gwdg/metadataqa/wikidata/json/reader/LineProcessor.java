package de.gwdg.metadataqa.wikidata.json.reader;

public interface LineProcessor {
  void setOutputFileName(String fileName);
  void read(String line);
  void read(String line, boolean processable);
  int getRecordCounter();
  void postProcess();
  void saveState();
}
