package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.Command;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EntityResolver extends Reader {

  private static JSONParser parser = new JSONParser();
  private static List<String> printableTypes = Arrays.asList("String", "Long", "Double");

  public EntityResolver(Command command, String propertiesFile, String entitiesFile) {
    super(command, propertiesFile, entitiesFile);
  }

  public void read(String jsonString) {
    recordCounter++;

    if (recordCounter % 100000 == 0)
      System.err.println(recordCounter);
    try {
      JSONObject output = processObject(
        new ArrayList<>(),
        (JSONObject) parser.parse(jsonString)
      );
    } catch (ParseException e) {
      e.printStackTrace();
    }

    if (newEntitiesCount >= 1000) {
      saveEntities();
      newEntitiesCount = 0;
    }
  }

  private JSONObject processObject(List<String> path, JSONObject jsonObject) {
    JSONObject record = new JSONObject();
    for (Object keyObj : jsonObject.keySet()) {
      String key = (String) keyObj;
      Object value = jsonObject.get(keyObj);
      String type = value.getClass().getSimpleName();
      String resolvedPath = null;
      if (path.contains("claims")) {// && printableTypes.contains(type)) {
        resolvedPath = resolvePath(path);
        if (printableTypes.contains(type)) {
          final String resolvedValue = resolveValue(value.toString());
          record.put(resolvedPath, resolvedValue);
          // System.err.printf("O: %s/%s: %s\n", resolvedPath, key, resolvedValue);
        }
      }

      if (type.equals("String")) {
        addContainer(path, key, "String");
      } else if (type.equals("Long")) {
        addContainer(path, key, "Long");
      } else if (type.equals("Double")) {
        addContainer(path, key, "Double");
      } else if (type.equals("JSONArray")) {
        processArray(buildPath(path, key), (JSONArray)value);
      } else if (type.equals("JSONObject")) {
        processObject(buildPath(path, key), (JSONObject)value);
      } else {
        System.err.println("unknown type [in object]: " + type);
      }
    }
    return record;
  }

  private void processArray(List<String> path, JSONArray jsonArray) {
    Iterator it = jsonArray.iterator();
    int i = 0;
    if (it.hasNext()) {
      Object item = it.next();
      String type = item.getClass().getSimpleName();
      if (path.contains("claims") && printableTypes.contains(type)) {
        final String resolvedPath = resolvePath(path);
        final String resolvedValue = resolveValue(item.toString());
        // System.err.printf("A: %s/%s: %s\n", resolvedPath, i, resolvedValue);
      }

      if (type.equals("String")) {
        addContainer(path, "0", "String");
      } else if (type.equals("Long")) {
        addContainer(path, "0", "Long");
      } else if (type.equals("Double")) {
        addContainer(path, "0", "Double");
      } else if (type.equals("JSONArray")) {
        processArray(buildPath(path, String.valueOf(i)), (JSONArray)item);
      } else if (type.equals("JSONObject")) {
        processObject(buildPath(path, String.valueOf(i)), (JSONObject)item);
      } else {
        System.err.println("unknown type [array]: " + type);
      }
      i++;
    }
  }

}
