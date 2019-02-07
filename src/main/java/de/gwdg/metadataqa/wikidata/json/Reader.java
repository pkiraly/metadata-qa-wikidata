package de.gwdg.metadataqa.wikidata.json;

import com.opencsv.CSVWriter;
import de.gwdg.metadataqa.wikidata.Command;
import de.gwdg.metadataqa.wikidata.json.labelextractor.JenaBasedSparqlClient;
import de.gwdg.metadataqa.wikidata.json.labelextractor.WdClient;
import de.gwdg.metadataqa.wikidata.model.Wikidata;
import de.gwdg.metadataqa.wikidata.model.WikidataEntity;
import de.gwdg.metadataqa.wikidata.model.WikidataType;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class Reader {

  private static final Pattern ENTITY_ID_PATTERN = Pattern.compile("^Q\\d+$");
  private static final String API_URL_PATTERN =
    "https://www.wikidata.org/wiki/Special:EntityData/%s.json";
  public static final Charset CHARSET = Charset.forName("UTF-8");
  private final String propertiesFile;
  private final String entitiesFile;

  protected static int recordCounter = 0;
  private static Map<String, Integer> container = new HashMap<>();
  protected static JSONParser parser = new JSONParser();
  protected Map<String, Wikidata> properties = new HashMap<>();
  protected Map<String, Wikidata> entities = new HashMap<>();
  protected int newEntitiesCount = 0;
  // private JenaBasedSparqlClient sparqlClient = new JenaBasedSparqlClient();
  protected LabelExtractor extractor;
  private long duration = 0;

  protected Command command;
  protected PrintWriter out = null;
  private CsvManager csvManager = new CsvManager();

  public Reader(Command command, String propertiesFile, String entitiesFile) {
    this.command = command;
    this.propertiesFile = propertiesFile;
    this.entitiesFile = entitiesFile;
    properties = csvManager.readCsv(propertiesFile, WikidataType.PROPERTIES);
    System.err.println("properties: " + properties.size());
    entities = csvManager.readCsv(entitiesFile, WikidataType.ENTITIES);
    System.err.println("entities: " + entities.size());
    extractor = new WdClient();
    // extractor = new JenaBasedSparqlClient();
  }

  abstract public void read(String jsonString);

  protected String resolveProperty(String propertyId) {
    if (properties.containsKey(propertyId))
      return properties.get(propertyId).getLabel();
    return propertyId;
  }

  protected String resolveValue(String value) {
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
            entities.put(entityId, new WikidataEntity(entityId, label));
            resolvedValue = entities.get(entityId).getLabel();
          } else if (extractor instanceof WdClient) {
            extractor.addOnHold(entityId);
            Set<String> onHold = extractor.getOnHold();
            if (onHold.size() >= 20) {
              Map<String, String> labels = extractor.getLabels(new ArrayList<>(onHold));
              for (Map.Entry<String, String> label : labels.entrySet()) {
                if (label.equals(entityId)) {
                  System.err.println("THIS IS THE TRIGGER");
                }
                if (!entities.containsKey(label.getKey())) {
                  entities.put(label.getKey(), new WikidataEntity(label.getKey(), label.getValue()));
                } else {
                  System.err.printf("already there %s: '%s' vs '%s'%n",
                    label.getKey(), label.getValue(), entities.get(label.getKey()));
                }
              }
              newEntitiesCount += labels.size();
              extractor.clearOnHold();
              resolvedValue = entities.get(entityId).getLabel();
            }
          }
        } else {
          resolvedValue = entities.get(entityId).getLabel();
        }
      }
      duration += (System.currentTimeMillis() - start);
    }
    return resolvedValue;
  }

  protected static List<String> buildPath(List<String> path, String key) {
    List<String> path2 = new ArrayList<>(path);
    path2.add(key);
    return path2;
    // return path.equals("/") ? "/" + key : String.format("%s/%s", path, key);
  }

  protected static void addContainer(List<String> path, String key, String type) {
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

  protected String resolvePath(List<String> path) {
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
        int i = 0;
        for (Map.Entry<String, String> label : extractor.getLabels(new ArrayList<>(onHold)).entrySet()) {
          entities.put(label.getKey(),
            new WikidataEntity(label.getKey(), label.getValue()));
          i++;
        }
        newEntitiesCount += i;
        ((WdClient) extractor).clearOnHold();
      }
    }
    out.close();
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

    for (Map.Entry<String, Wikidata> entry : entities.entrySet()) {
      records.add(new String[]{entry.getKey(), entry.getValue().getLabel()});
    }

    return records;
  }

  public void setOutputFileName(String outputFileName) {
    FileWriter fw = null;
    try {
      File outputFile = new File(outputFileName);
      if (outputFile.exists())
        outputFile.delete();
      fw = new FileWriter(outputFile, true);
      BufferedWriter bw = new BufferedWriter(fw);
      out = new PrintWriter(bw);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
