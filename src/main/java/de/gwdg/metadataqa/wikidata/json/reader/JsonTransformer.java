package de.gwdg.metadataqa.wikidata.json.reader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.Iterator;

public class JsonTransformer extends Reader {

  private String id = "id";

  public JsonTransformer(String propertiesFile, String entitiesFile) {
    super(propertiesFile, entitiesFile);
  }

  public void read(String jsonString) {
    read(jsonString, true);
  }

  public void read(String jsonString, boolean processable) {
    recordCounter++;

    if (recordCounter % 100000 == 0)
      System.err.println(recordCounter);

    if (!processable)
      return;

    try {
      Object obj = parser.parse(jsonString);
      JSONObject input = (JSONObject) obj;
      JSONObject output = process(true, input);
      out.println(output.toJSONString());
    } catch (ParseException e) {
      e.printStackTrace();
    }

    if (newEntitiesCount >= 1000) {
      saveEntities();
      newEntitiesCount = 0;
    }
  }

  private JSONObject process(boolean isRoot, JSONObject input) {
    JSONObject record = new JSONObject();
    for (Object keyObj : input.keySet()) {
      String key = (String) keyObj;
      Object value = input.get(keyObj);
      if (isRoot && key.equals(id)) {
        record.put(key, value);
      }

      boolean processChild = !isRoot || key.equals("claims");
      String resolvedProperty = resolveProperty(key);
      // System.err.printf("%s - is root: %s, processChild: %s%n", key, isRoot, processChild);

      if (processChild) {
        if (value instanceof String) {
          record.put(resolvedProperty, resolveValue((String)value));
        } else if (value instanceof Long) {
          record.put(resolvedProperty, value);
        } else if (value instanceof Double) {
          record.put(resolvedProperty, value);
        } else if (value instanceof JSONArray) {
          record.put(resolvedProperty, process((JSONArray)value));
        } else if (value instanceof JSONObject) {
          record.put(resolvedProperty, process(false, (JSONObject)value));
        } else {
          System.err.println("unknown type [in object]: " + value.getClass().getSimpleName());
        }
      }
    }
    return record;
  }

  private JSONArray process(JSONArray jsonArray) {
    JSONArray output = new JSONArray();
    Iterator it = jsonArray.iterator();
    int i = 0;
    if (it.hasNext()) {
      Object item = it.next();

      if (item instanceof String) {
        output.add(resolveValue((String)item));
      } else if (item instanceof Long) {
        output.add(item);
      } else if (item instanceof Double) {
        output.add(item);
      } else if (item instanceof JSONArray) {
        output.add(process((JSONArray)item));
      } else if (item instanceof JSONObject) {
        output.add(process(true, (JSONObject)item));
      } else {
        System.err.println("unknown type [array]: " + item.getClass().getSimpleName());
      }
      i++;
    }
    return output;
  }
}
