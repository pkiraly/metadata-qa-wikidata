package de.gwdg.metadataqa.wikidata.json;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.gwdg.metadataqa.wikidata.json.labelextractor.JenaBasedSparqlClient;
import de.gwdg.metadataqa.wikidata.json.labelextractor.WdClient;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Reader {
  private static final Pattern ENTITY_ID_PATTERN = Pattern.compile("^Q\\d+$");
  private static final String API_URL_PATTERN =
    "https://www.wikidata.org/wiki/Special:EntityData/%s.json";
  public static final Charset CHARSET = Charset.forName("UTF-8");
  private final String propertiesFile;
  private final String entitiesFile;

  private enum TYPE {
    PROPERTIES,
    ENTITIES;
  }

  private static int recordCounter = 0;
  private static Map<String, Integer> container = new HashMap<>();
  private static JSONParser parser = new JSONParser();
  private Map<String, WikidataProperty> properties = new HashMap<>();
  private Map<String, String> entities = new HashMap<>();
  private int newEntitiesCount = 0;
  private static List<String> printableTypes = Arrays.asList("String", "Long", "Double");
  // private JenaBasedSparqlClient sparqlClient = new JenaBasedSparqlClient();
  private LabelExtractor extractor;
  private long duration = 0;

  public Reader(String propertiesFile, String entitiesFile) {
    this.propertiesFile = propertiesFile;
    this.entitiesFile = entitiesFile;
    readCsv(propertiesFile, TYPE.PROPERTIES);
    System.err.println("properties: " + properties.size());
    readCsv(entitiesFile, TYPE.ENTITIES);
    System.err.println("entities: " + entities.size());
    extractor = new WdClient();
    // extractor = new JenaBasedSparqlClient();
  }

  public void read(String jsonString) {
    recordCounter++;

    if (recordCounter % 100000 == 0)
      System.err.println(recordCounter);
    try {
      Object obj = parser.parse(jsonString);
      JSONObject input = (JSONObject) obj;
      JSONObject output = process(new ArrayList<>(), input);
      // JSONObject output = process(true, input);
      // System.err.println(output.toJSONString());
    } catch (ParseException e) {
      e.printStackTrace();
    }

    if (newEntitiesCount >= 100) {
      System.err.println("newEntitiesCount: " + newEntitiesCount);
      saveEntities();
      newEntitiesCount = 0;
    }
  }

  private JSONObject process(List<String> path, JSONObject jsonObject) {
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
        process(buildPath(path, key), (JSONArray)value);
      } else if (type.equals("JSONObject")) {
        process(buildPath(path, key), (JSONObject)value);
      } else {
        System.err.println("unknown type [in object]: " + type);
      }
    }
    return record;
  }

  private JSONObject process(boolean isRoot, JSONObject input) {
    JSONObject record = new JSONObject();
    for (Object keyObj : input.keySet()) {
      String key = (String) keyObj;
      Object value = input.get(keyObj);
      String type = value.getClass().getSimpleName();
      boolean processChild = !isRoot || key.equals("claims");
      String resolvedProperty = resolveProperty(key);
      System.err.printf("%s - %s, %s%n", key, isRoot, processChild);

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

  private String resolveProperty(String propertyId) {
    if (properties.containsKey(propertyId))
      return properties.get(propertyId).getLabel();
    return propertyId;
  }

  private JSONArray process(JSONArray jsonArray) {
    JSONArray output = new JSONArray();
    Iterator it = jsonArray.iterator();
    int i = 0;
    if (it.hasNext()) {
      Object item = it.next();
      String type = item.getClass().getSimpleName();

      if (type.equals("String")) {
        output.add(item);
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

  private void process(List<String> path, JSONArray jsonArray) {
    Iterator it = jsonArray.iterator();
    int i = 0;
    if (it.hasNext()) {
      Object item = it.next();
      String type = item.getClass().getSimpleName();
      if (path.contains("claims") && printableTypes.contains(type)) {
        final String resolvedPath = resolvePath(path);
        // final String resolvedValue = resolveValue(item);
        // System.err.printf("A: %s/%s: %s\n", resolvedPath, i, resolvedValue);
      }

      if (type.equals("String")) {
        addContainer(path, "0", "String");
      } else if (type.equals("Long")) {
        addContainer(path, "0", "Long");
      } else if (type.equals("Double")) {
        addContainer(path, "0", "Double");
      } else if (type.equals("JSONArray")) {
        process(buildPath(path, String.valueOf(i)), (JSONArray)item);
      } else if (type.equals("JSONObject")) {
        process(buildPath(path, String.valueOf(i)), (JSONObject)item);
      } else {
        System.err.println("unknown type [array]: " + type);
      }
      i++;
    }
  }

  private String resolveValue(String value) {
    String resolvedValue = value;
    if (value instanceof String) {
      long start = System.currentTimeMillis();
      String entityId = value;
      if (ENTITY_ID_PATTERN.matcher(entityId).matches()) {
        if (!entities.containsKey(entityId)) {
          // System.err.println(entityId);
          if (extractor instanceof JenaBasedSparqlClient) {
            String label = extractor.getLabel(entityId);
            // String label = readWd(entityId);
            newEntitiesCount++;
            entities.put(entityId, label);
            resolvedValue = entities.get(entityId);
          } else if (extractor instanceof WdClient) {
            extractor.addOnHold(entityId);
            Set<String> onHold = extractor.getOnHold();
            if (onHold.size() >= 20) {
              Map<String, String> labels = extractor.getLabels(new ArrayList<>(onHold));
              // System.err.println("trigger: " + entityId);
              // System.err.println("on hold: " + StringUtils.join(extractor.getOnHold(), ", "));
              // System.err.println("labels: " + StringUtils.join(labels.keySet(), ", "));
              for (Map.Entry<String, String> label : labels.entrySet()) {
                if (label.equals(entityId)) {
                  System.err.println("THIS IS THE TRIGGER");
                }
                if (!entities.containsKey(label.getKey())) {
                  entities.put(label.getKey(), label.getValue());
                } else {
                  System.err.printf("already there %s: '%s' vs '%s'%n",
                    label.getKey(), label.getValue(), entities.get(label.getKey()));
                }
              }
              newEntitiesCount += labels.size();
              System.err.printf(
                "<-extractor.getLabels() %d new / %d total%n",
                newEntitiesCount, entities.size()
              );
              extractor.clearOnHold();
              resolvedValue = entities.get(entityId);
            }
          }
        } else {
          resolvedValue = entities.get(entityId);
        }
      }
      duration += (System.currentTimeMillis() - start);
    }
    return resolvedValue;
  }

  private static List<String> buildPath(List<String> path, String key) {
    List<String> path2 = new ArrayList<>(path);
    path2.add(key);
    return path2;
    // return path.equals("/") ? "/" + key : String.format("%s/%s", path, key);
  }

  private static void addContainer(List<String> path, String key, String type) {
    key = StringUtils.join("/", path) + "/" + key;
    String typedKey = String.format("%s (%s)", key, type);
    if (!container.containsKey(typedKey)) {
      container.put(typedKey, 0);
    }
    container.put(typedKey, container.get(typedKey) + 1);
  }

  public static int getRecordCounter() {
    return recordCounter;
  }

  public static Map<String, Integer> getContainer() {
    return container;
  }

  private void readCsv(String csvFile, TYPE type) {
    CSVReader reader = null;
    int lineNumber = 0;
    String[] line = null;
    try {
      reader = new CSVReader(new FileReader(csvFile));
      while ((line = reader.readNext()) != null) {
        if (type.equals(TYPE.PROPERTIES)) {
          WikidataProperty property = new WikidataProperty(line[0], line[1], line[3]);
          properties.put(property.getId(), property);
        } else if (type.equals(TYPE.ENTITIES)) {
          entities.put(line[0], line[1]);
        }
        lineNumber++;
      }
    } catch (IOException e) {
      System.err.println("line number: " + lineNumber);
      System.err.println("last good line: " + StringUtils.join(line, " // "));
      e.printStackTrace();
      throw new IllegalArgumentException("Wrong CSV file: " + csvFile);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Wrong CSV file: " + csvFile);
    }
    System.err.println(type.name() + " " + lineNumber);
  }

  private String resolvePath(List<String> path) {
    String propertyId = path.get(path.size() - 1);
    String label;
    if (propertyId.startsWith("P")) {
      if (properties.containsKey(propertyId)) {
        label = properties.get(propertyId).getLabel();
      } else {
        // resolve property
        label = resolveProperty(propertyId);
      }
      return label;
    }
    return propertyId;
  }

  // alternative way: using Wikidata API
  // https://www.wikidata.org/wiki/Special:EntityData/Q133704.json
  // https://www.mediawiki.org/wiki/Wikibase/EntityData
  private String readApi(String entityId) {
    String label = entityId;
    String url = String.format(API_URL_PATTERN, entityId);
    try {
      String json = readJsonFromUrl(url);
      label = Utils.extractEntityLabel(json);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return label;
  }

  public static String readJsonFromUrl(String url) throws IOException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(
        new InputStreamReader(is, CHARSET));
      return readAll(rd);
    } finally {
      is.close();
    }
  }

  private static String readAll(java.io.Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public void postProcess() {
    if (extractor instanceof WdClient) {
      Set<String> onHold = ((WdClient) extractor).getOnHold();
      if (onHold.size() > 0) {
        Map<String, String> labels = extractor.getLabels(new ArrayList<>(onHold));
        entities.putAll(labels);
        newEntitiesCount += labels.size();
        ((WdClient) extractor).clearOnHold();
      }
    }
  }

  public void saveEntities() {
    if (newEntitiesCount == 0)
      return;

    System.err.printf(
      "save entities: %d new/%d total (entities import took %d ms)\n",
      newEntitiesCount,
      entities.size(),
      duration
    );
    duration = 0;
    FileWriter writer = null;
    try {
      writer = new FileWriter(entitiesFile);
      //using custom delimiter and quote character
      CSVWriter csvWriter = new CSVWriter(writer);

      List<String[]> data = entitiesToStringArray();

      csvWriter.writeAll(data);
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<String[]> entitiesToStringArray() {
    List<String[]> records = new ArrayList<String[]>();

    // adding header record
    records.add(new String[]{"id", "label"});

    for (Map.Entry<String, String> entry : entities.entrySet()) {
      records.add(new String[]{entry.getKey(), entry.getValue()});
    }

    return records;
  }
}
