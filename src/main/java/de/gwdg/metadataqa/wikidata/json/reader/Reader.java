package de.gwdg.metadataqa.wikidata.json.reader;

import com.opencsv.CSVWriter;
import de.gwdg.metadataqa.wikidata.json.CsvManager;
import de.gwdg.metadataqa.wikidata.json.Utils;
import de.gwdg.metadataqa.wikidata.json.labelextractor.JenaBasedSparqlClient;
import de.gwdg.metadataqa.wikidata.json.labelextractor.LabelExtractor;
import de.gwdg.metadataqa.wikidata.json.labelextractor.WdClient;
import de.gwdg.metadataqa.wikidata.model.Wikidata;
import de.gwdg.metadataqa.wikidata.model.WikidataEntity;
import de.gwdg.metadataqa.wikidata.model.WikidataProperty;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class Reader implements LineProcessor {

  private static final Pattern ENTITY_ID_PATTERN = Pattern.compile("^Q\\d+$");
  private static final Pattern PROPERTY_ID_PATTERN = Pattern.compile("^P\\d+$");

  private static final String API_URL_PATTERN =
    "https://www.wikidata.org/wiki/Special:EntityData/%s.json";
  public static final Charset CHARSET = Charset.forName("UTF-8");

  private final String propertiesFile;
  private final String entitiesFile;
  private String newEntitiesFile;
  private String entitiesBootstrapFile;

  protected Map<String, Boolean> unknownProperties = new HashMap<>();
  protected static int recordCounter = 0;
  protected static JSONParser parser = new JSONParser();

  protected Class T = String.class;
  protected Map<String, Wikidata> properties = new HashMap<>();
  protected Map<String, String> entities = new HashMap<>();
  protected Map<String, Integer> entitiesCounter = new HashMap<>();

  protected int newEntitiesCount = 0;
  protected Map<String, String> newEntities = new HashMap<>();

  // private LabelExtractor extractor = new JenaBasedSparqlClient();
  protected LabelExtractor extractor = new WdClient();
  private long duration = 0;

  protected PrintWriter out = null;
  protected CsvManager csvManager = new CsvManager();

  public Reader(String propertiesFile, String entitiesFile) {
    this.propertiesFile = propertiesFile;
    this.entitiesFile = entitiesFile;
    initialize();
  }

  public Reader(String propertiesFile, String entitiesFile, String entitiesBootstrapFile) {
    this.propertiesFile = propertiesFile;
    this.entitiesFile = entitiesFile;
    this.entitiesBootstrapFile = entitiesBootstrapFile;
  }

  public void initialize() {
    initialize(true);
  }

  public void initialize(boolean readEntities) {
    properties = csvManager.readCsv(propertiesFile, WikidataType.PROPERTIES, Wikidata.class);
    System.err.println("properties: " + properties.size());

    if (StringUtils.isNotBlank(entitiesBootstrapFile)) {
      entitiesCounter = csvManager.readCsv(entitiesBootstrapFile, WikidataType.ENTITIES_COUNT, Integer.class);
      csvManager.setEntitiesFilter(entitiesCounter);
    }
    if (readEntities) {
      entities = csvManager.readCsv(entitiesFile, WikidataType.ENTITIES, T);
      System.err.println("entities: " + entities.size());
    }
  }

  abstract public void read(String jsonString);

  protected String resolveProperty(String propertyId) {
    if (properties.containsKey(propertyId)) {
      return properties.get(propertyId).getLabel();
    } else {
      if (!unknownProperties.containsKey(propertyId)) {
        if (!propertyId.equals("claims") && !propertyId.equals("type")) {
          unknownProperties.put(propertyId, true);
          if (PROPERTY_ID_PATTERN.matcher(propertyId).matches()) {
            System.err.printf("Unknow property: %s%n", propertyId);
            Map<String, String> labels = extractor.getLabels(Arrays.asList(propertyId));
            for (Map.Entry<String, String> label : labels.entrySet()) {
              if (!properties.containsKey(label.getKey())) {
                properties.put(label.getKey(), new WikidataProperty(label.getKey(), label.getValue()));
              } else {
                System.err.printf("Property is already there %s %s%n", label.getKey(), label.getValue());
              }
            }
          }
        }
      }
    }
    return propertyId;
  }

  protected String resolveValue(String value, boolean skipResolution) {
    if (skipResolution) {
      countValues(value);
      return null;
    } else {
      return resolveValue(value);
    }
  }

  protected void countValues(String value) {
    String entityId = value;
    if (ENTITY_ID_PATTERN.matcher(entityId).matches()) {
      if (!entitiesCounter.containsKey(entityId)) {
        entitiesCounter.put(entityId, 0);
      }
      entitiesCounter.put(entityId, entitiesCounter.get(entityId) + 1);
    }
  }

  protected String resolveValue(String value) {
    String resolvedValue = value;
    if (value instanceof String) {
      long start = System.currentTimeMillis();
      String entityId = value;
      if (ENTITY_ID_PATTERN.matcher(entityId).matches()) {
        if (!entities.containsKey(entityId)) {
          System.err.println(entityId);
          if (extractor instanceof JenaBasedSparqlClient) {
            String label = extractor.getLabel(entityId);
            // String label = readWd(entityId);
            newEntitiesCount++;
            /*
            if (T.equals(Wikidata.class)) {
              entities.put(entityId, new WikidataEntity(entityId, label));
              resolvedValue = entities.get(entityId).getLabel();
            } else {
            */
            entities.put(entityId, label);
            newEntities.put(entityId, label);
            resolvedValue = label;
            // }
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
                  // entities.put(label.getKey(), new WikidataEntity(label.getKey(), label.getValue()));
                  entities.put(label.getKey(), label.getValue());
                  newEntities.put(label.getKey(), label.getValue());
                } else {
                  System.err.printf("already there %s: '%s' vs '%s'%n",
                    label.getKey(), label.getValue(), entities.get(label.getKey()));
                }
              }
              newEntitiesCount += labels.size();
              extractor.clearOnHold();
              // resolvedValue = entities.get(entityId).getLabel();
              resolvedValue = entities.get(entityId);
            }
          }
        } else {
          // resolvedValue = entities.get(entityId).getLabel();
          resolvedValue = entities.get(entityId);
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

  public int getRecordCounter() {
    return recordCounter;
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
          /*
          entities.put(label.getKey(),
            new WikidataEntity(label.getKey(), label.getValue()));
          */
          entities.put(label.getKey(), label.getValue());
          newEntities.put(label.getKey(), label.getValue());
          i++;
        }
        newEntitiesCount += i;
        ((WdClient) extractor).clearOnHold();
      }
    }
    out.close();
  }

  public void saveState() {
    saveEntities();
    saveProperties();
    saveCounter();
  }

  public void saveCounter() {
    FileWriter writer = null;
    try {
      writer = new FileWriter("entities-count.csv");
      CSVWriter csvWriter = new CSVWriter(writer);
      csvWriter.writeNext(new String[]{"id", "count"});
      for (Map.Entry<String, Integer> entry : entitiesCounter.entrySet()) {
        csvWriter.writeNext(
          new String[]{entry.getKey(), String.valueOf(entry.getValue())},
          false
        );
      }
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void saveEntities() {
    if (newEntitiesCount == 0)
      return;

    System.err.printf(
      "save entities: %d new/%d total (entities import took %.3f s)%n",
      newEntitiesCount,
      entities.size(),
      (duration / 1000.0)
    );

    duration = 0;
    long start = System.currentTimeMillis();
    FileWriter writer = null;
    try {
      String outputFile = StringUtils.isNotBlank(newEntitiesFile) ? newEntitiesFile : entitiesFile;
      writer = new FileWriter(outputFile);
      CSVWriter csvWriter = new CSVWriter(writer);
      csvWriter.writeNext(new String[]{"id", "label"});
      /*
      for (Map.Entry<String, Wikidata> entry : entities.entrySet()) {
        csvWriter.writeNext(entry.getValue().asArray());
      }
      */
      for (Map.Entry<String, String> entry : newEntities.entrySet()) {
        csvWriter.writeNext(new String[]{entry.getKey(), entry.getValue()});
      }

      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.err.printf(
      "save to file took %.3f s)%n",
      ((System.currentTimeMillis() - start) / 1000.0)
    );
  }

  public void saveProperties() {
    if (unknownProperties.size() == 0)
      return;

    System.err.printf(
      "save properties: %d new/%d total (properties import took %d ms)\n",
      unknownProperties.size(),
      properties.size(),
      duration
    );
    for (Map.Entry<String, Boolean> entry : unknownProperties.entrySet()) {
      System.err.println(entry.getKey());
    }
    duration = 0;
    FileWriter writer = null;
    try {
      writer = new FileWriter(propertiesFile);
      CSVWriter csvWriter = new CSVWriter(writer);
      csvWriter.writeNext(new String[]{"id", "label", "datatype"});
      for (Map.Entry<String, Wikidata> entry : properties.entrySet()) {
        csvWriter.writeNext(entry.getValue().asArray());
      }
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<String[]> propertiesToStringArray() {
    List<String[]> records = new ArrayList<String[]>();

    // adding header record
    records.add(new String[]{"id", "label", "datatype"});

    for (Map.Entry<String, Wikidata> entry : properties.entrySet()) {
      records.add(entry.getValue().asArray());
    }

    return records;
  }

  private List<String[]> entitiesToStringArray() {
    List<String[]> records = new ArrayList<String[]>();

    // adding header record
    records.add(new String[]{"id", "label"});

    /*
    for (Map.Entry<String, Wikidata> entry : entities.entrySet()) {
      records.add(entry.getValue().asArray());
    }
    */

    for (Map.Entry<String, String> entry : entities.entrySet()) {
      records.add(new String[]{entry.getKey(), entry.getValue()});
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

  public void setNewEntitiesFile(String newEntitiesFile) {
    this.newEntitiesFile = newEntitiesFile;
  }
}
