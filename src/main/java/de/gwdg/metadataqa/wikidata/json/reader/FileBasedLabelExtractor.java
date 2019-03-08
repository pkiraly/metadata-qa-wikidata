package de.gwdg.metadataqa.wikidata.json.reader;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileBasedLabelExtractor implements LineProcessor {

  private JSONParser parser = new JSONParser();
  private CSVWriter csvWriter;
  private final static List<String> keys = Arrays.asList("en", "de", "fr");
  private int recordCounter = 0;

  public FileBasedLabelExtractor() {
  }

  @Override
  public void setOutputFileName(String fileName) {
    FileWriter writer = null;
    try {
      writer = new FileWriter(fileName);
      csvWriter = new CSVWriter(writer);
      csvWriter.writeNext(new String[]{"id", "label"});
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void read(String jsonString) {
    read(jsonString, true);
  }

  @Override
  public void read(String jsonString, boolean processable) {
    recordCounter++;

    if (recordCounter % 100000 == 0)
      System.err.printf("%,d%n", recordCounter);

    if (!processable)
      return;

    DocumentContext context = JsonPath.parse(jsonString);

    String id = context.read("$.id");
    Map labels = context.read("$.labels");
    String value = null;

    if (labels.isEmpty()) {
      value = id;
    } else {
      for (String key : keys) {
        if (labels.containsKey(key)) {
          value = (String) labels.get(key);
          break;
        }
      }
      if (StringUtils.isBlank(value)) {
        for (Object key : labels.keySet()) {
          value = (String) labels.get(key);
          break;
        }
      }

      if (StringUtils.isBlank(value)) {
        System.err.println(jsonString);
        value = id;
      }
    }


    if (csvWriter != null) {
      csvWriter.writeNext(new String[]{id, value});
    } else {
      System.err.println(StringUtils.join(new String[]{id, value}, ","));
    }
  }

  @Override
  public int getRecordCounter() {
    return 0;
  }

  @Override
  public void postProcess() {

  }

  @Override
  public void saveState() {
    try {
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
