package de.gwdg.metadataqa.wikidata.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LabelExtractor {
  String getLabel(String entityId);
  Map<String, String> getLabels(List<String> entityIds);

  Set<String> getOnHold();
  void addOnHold(String entityId);
  void clearOnHold();
}
