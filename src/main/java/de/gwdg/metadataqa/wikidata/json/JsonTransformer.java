package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.Command;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.Iterator;

public class JsonTransformer extends Reader {

  public JsonTransformer(Command command, String propertiesFile, String entitiesFile) {
    super(command, propertiesFile, entitiesFile);
  }

  public void read(String jsonString) {
    recordCounter++;

    if (recordCounter % 100000 == 0)
      System.err.println(recordCounter);
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
      String type = value.getClass().getSimpleName();
      boolean processChild = !isRoot || key.equals("claims");
      String resolvedProperty = resolveProperty(key);
      // System.err.printf("%s - is root: %s, processChild: %s%n", key, isRoot, processChild);

      if (processChild) {
        if (type.equals("String")) {
          record.put(resolvedProperty, resolveValue((String)value));
        } else if (type.equals("Long")) {
          record.put(resolvedProperty, value);
        } else if (type.equals("Double")) {
          record.put(resolvedProperty, value);
        } else if (type.equals("JSONArray")) {
          record.put(resolvedProperty, process((JSONArray)value));
        } else if (type.equals("JSONObject")) {
          record.put(resolvedProperty, process(false, (JSONObject)value));
        } else {
          System.err.println("unknown type [in object]: " + type);
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
      String type = item.getClass().getSimpleName();

      if (type.equals("String")) {
        output.add(resolveValue((String)item));
      } else if (type.equals("Long")) {
        output.add(item);
      } else if (type.equals("Double")) {
        output.add(item);
      } else if (type.equals("JSONArray")) {
        output.add(process((JSONArray)item));
      } else if (type.equals("JSONObject")) {
        output.add(process(true, (JSONObject)item));
      } else {
        System.err.println("unknown type [array]: " + type);
      }
      i++;
    }
    return output;
  }

}
