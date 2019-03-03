package de.gwdg.metadataqa.wikidata.json.reader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EntityResolver extends Reader {

  private static JSONParser parser = new JSONParser();
  private static Map<String, Integer> propertyCounter = new HashMap<>();
  private static Map<String, Integer> objectCounter = new HashMap<>();
  private static List<String> rootProperties = Arrays.asList("labels", "descriptions", "aliases", "sitelinks");
  private boolean skipResolution = false;
  private DecimalFormat myFormatter = new DecimalFormat("### ### ###");

  public EntityResolver(String propertiesFile, String entitiesFile) {
    super(propertiesFile, entitiesFile);
  }

  public EntityResolver(String propertiesFile, String entitiesFile, String entitiesBootstrapFile) {
    super(propertiesFile, entitiesFile, entitiesBootstrapFile);
  }

  public void read(String jsonString) {
    read(jsonString, true);
  }

  public void read(String jsonString, boolean processable) {
    recordCounter++;

    if (recordCounter % 100000 == 0)
      System.err.println(myFormatter.format(recordCounter));

    if (!processable)
      return;

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

      countProperty(key);
      if (value instanceof String) {
        resolveValue(value.toString(), skipResolution);
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
        // countProperty(path, "Long");
      } else if (item instanceof Double) {
        // countProperty(path, "Double");
      } else if (item instanceof JSONArray) {
        processArray(parent, (JSONArray)item);
      } else if (item instanceof JSONObject) {
        processObject(parent, (JSONObject)item);
      } else {
        System.err.println("unknown type [array]: " + item.getClass().getSimpleName());
      }
    }
  }

  protected static void countProperty(String key) {
    if (!propertyCounter.containsKey(key)) {
      propertyCounter.put(key, 0);
    }
    propertyCounter.put(key, propertyCounter.get(key) + 1);
  }

  protected static void countObject(String key) {
    if (!objectCounter.containsKey(key)) {
      objectCounter.put(key, 0);
    }
    objectCounter.put(key, objectCounter.get(key) + 1);
  }

  public static Map<String, Integer> getPropertyCounter() {
    return propertyCounter;
  }

  public static Map<String, Integer> getObjectCounter() {
    return propertyCounter;
  }

  public void setSkipResolution(boolean skipResolution) {
    this.skipResolution = skipResolution;
  }

  public void saveState() {
    saveEntities();
    saveProperties();
    if (skipResolution) {
      saveEntityCounter();
      savePropertyCounter();
    }
  }

  public void savePropertyCounter() {
    saveCounter(propertyCounter, "properties-count.csv");
  }
}
