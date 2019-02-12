package de.gwdg.metadataqa.wikidata.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EntityResolver extends Reader {

  private static JSONParser parser = new JSONParser();
  private static Map<String, Integer> container = new HashMap<>();
  private static List<String> rootProperties = Arrays.asList("labels", "descriptions", "aliases", "sitelinks");
  private boolean skipResolution = false;

  public EntityResolver(String propertiesFile, String entitiesFile) {
    super(propertiesFile, entitiesFile);
  }

  public void read(String jsonString) {
    recordCounter++;

    if (recordCounter % 100000 == 0)
      System.err.println(recordCounter);
    try {
      processObject("", (JSONObject) parser.parse(jsonString));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    if (unknownProperties.size() >= 1000) {
      saveProperties();
      unknownProperties = new HashMap<>();
    }

    if (newEntitiesCount >= 10000) {
      saveEntities();
      newEntitiesCount = 0;
    }
  }

  private void processObject(String parent, JSONObject jsonObject) {
    for (Object keyObj : jsonObject.keySet()) {
      String key = (String) keyObj;
      if (parent == "" && rootProperties.contains(key)) {
        continue;
      }

      Object value = jsonObject.get(keyObj);
      String resolvedProperty = resolveProperty(key);

      addContainer(key);
      if (value instanceof String) {
        String resolvedValue = resolveValue(value.toString(), skipResolution);
      } else if (value instanceof Long) {
        //
      } else if (value instanceof Double) {
        //
      } else if (value instanceof JSONArray) {
        processArray(resolvedProperty, (JSONArray)value);
      } else if (value instanceof JSONObject) {
        processObject(resolvedProperty, (JSONObject)value);
      } else {
        System.err.println("unknown type [in object]: " + value.getClass().getSimpleName());
      }
    }
  }

  private void processArray(String parent, JSONArray jsonArray) {
    Iterator it = jsonArray.iterator();
    if (it.hasNext()) {
      Object item = it.next();

      if (item instanceof String) {
        resolveValue(item.toString(), skipResolution);
      } else if (item instanceof Long) {
        // addContainer(path, "Long");
      } else if (item instanceof Double) {
        // addContainer(path, "Double");
      } else if (item instanceof JSONArray) {
        processArray(parent, (JSONArray)item);
      } else if (item instanceof JSONObject) {
        processObject(parent, (JSONObject)item);
      } else {
        System.err.println("unknown type [array]: " + item.getClass().getSimpleName());
      }
    }
  }

  protected static void addContainer(String key) {
    String typedKey = key;
    if (!container.containsKey(typedKey)) {
      container.put(typedKey, 0);
    }
    container.put(typedKey, container.get(typedKey) + 1);
  }

  public static Map<String, Integer> getContainer() {
    return container;
  }

  public void setSkipResolution(boolean skipResolution) {
    this.skipResolution = skipResolution;
  }
}
