package de.gwdg.metadataqa.wikidata.json;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;

import java.util.Map;

public class Utils {
  private static final int SECOND = 1000;
  private static final int MINUTE = 60 * SECOND;
  private static final int HOUR = 60 * MINUTE;
  private static final int DAY = 24 * HOUR;

  public static String extractEntityLabel(String json) {
    return JsonPath.read(json, "$.labels.en.value");
  }

  public static String formatDuration(long duration) {

    StringBuffer text = new StringBuffer("");
    if (duration > DAY) {
      text.append(duration / DAY).append(" days ");
      duration %= DAY;
    }
    if (duration > HOUR) {
      text.append(String.format("%02d:", duration / HOUR));
      duration %= HOUR;
    } else {
      text.append("00:");
    }
    if (duration > MINUTE) {
      text.append(String.format("%02d:", duration / MINUTE));
      duration %= MINUTE;
    } else {
      text.append("00:");
    }
    if (duration > SECOND) {
      text.append(String.format("%02d.", duration / SECOND));
      duration %= SECOND;
    } else {
      text.append("00.");
    }
    text.append(duration);
    return text.toString();
  }

  public static Object getPath(DocumentContext context, String path) {
    Object value = null;
    try {
      value = context.read(path);
    } catch (PathNotFoundException e) {
      //
    }
    return value;
  }

  public static String getPathAsString(DocumentContext context, String path) {
    JSONArray items = (JSONArray) Utils.getPath(context, path);
    String item = null;
    if (items != null && !items.isEmpty()) {
      item = (String)items.get(0);
    }
    return item;
  }

  public static void increment(Map<String, Integer> counter, String key) {
    if (!counter.containsKey(key)) {
      counter.put(key, 0);
    }
    counter.put(key, counter.get(key) + 1);
  }

}
