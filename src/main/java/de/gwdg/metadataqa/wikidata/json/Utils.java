package de.gwdg.metadataqa.wikidata.json;

import com.jayway.jsonpath.JsonPath;

public class Utils {
  public static String extractEntityLabel(String json) {
    return JsonPath.read(json, "$.labels.en.value");
  }
}
