package de.gwdg.metadataqa.wikidata.json;

import java.util.List;
import java.util.Map;

public interface LabelExtractor {
  String getLabel(String entityId);
  Map<String, String> getLabels(List<String> entityIds);
}
