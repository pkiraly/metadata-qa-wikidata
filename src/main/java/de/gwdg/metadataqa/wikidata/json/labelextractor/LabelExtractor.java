package de.gwdg.metadataqa.wikidata.json.labelextractor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LabelExtractor {
  String getLabel(String entityId);
  Map<String, String> getLabels(List<String> entityIds);
  Map<String, String> getClasses(String entityId);

  Set<String> getOnHold();
  void addOnHold(String entityId);
  void clearOnHold();

}
