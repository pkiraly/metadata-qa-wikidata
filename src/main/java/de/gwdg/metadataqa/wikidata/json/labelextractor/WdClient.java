package de.gwdg.metadataqa.wikidata.json.labelextractor;

import de.gwdg.metadataqa.wikidata.json.LabelExtractor;
import de.gwdg.metadataqa.wikidata.json.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WdClient implements LabelExtractor {

  private static final String WD_DATA_CMD_PATTERN = "wd data --props labels '%s'";
  private static final String WD_LABEL_CMD_PATTERN = "wd label --lang en %s";
  private static final Pattern LABEL_OUTPUT = Pattern.compile("^(Q\\d+) +(.*)$");
  private boolean supposeSameOrder = true;
  private final Runtime runtime;
  private Set<String> onHold;

  public static void main(String[] args) {
    WdClient client = new WdClient();
    List<String> entityIds = Arrays.asList("Q18120925", "Q22117436", "Q22117437");
    Map<String, String> labels = client.getLabels(entityIds);
    for (Map.Entry entry : labels.entrySet()) {
      System.err.printf("%s: %s\n", entry.getKey(), entry.getValue());
    }
  }

  public WdClient() {
    runtime = Runtime.getRuntime();
    onHold = new HashSet<>();
  }

  @Override
  public String getLabel(String entityId) {
    String label = entityId;
    try {
      String cmd = String.format(WD_DATA_CMD_PATTERN, entityId);
      Process process = runtime.exec(cmd);
      process.waitFor();
      String json = extractJsonFromWd(process.getInputStream());
      label = Utils.extractEntityLabel(json);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return label;
  }

  @Override
  public Map<String, String> getLabels(List<String> entityIds) {
    Map<String, String> labels = new HashMap<>();
    try {
      String cmd = String.format(WD_LABEL_CMD_PATTERN, StringUtils.join(entityIds, " "));
      // System.err.println(cmd);
      Process process = runtime.exec(cmd);
      process.waitFor();
      labels = extractFromLabelOutput(process.getInputStream(), entityIds);
      if (entityIds.size() != labels.size()) {
        System.err.printf("entities: %d, labels: %d%n", entityIds.size(), labels.size());
      }
      // label = extractEntityLabel(json);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return labels;

  }

  private String extractJsonFromWd(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(
      new InputStreamReader(inputStream));
    StringBuffer json = new StringBuffer();

    while (true) {
      String line = null;
      try {
        if (!((line = bufferedReader.readLine()) != null)) break;
      } catch (IOException e) {
        e.printStackTrace();
      }
      json.append(line);
    }
    try {
      bufferedReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return json.toString();
  }

  private Map<String, String> extractFromLabelOutput(InputStream inputStream,
                                                     List<String> entityIds) {
    BufferedReader bufferedReader = new BufferedReader(
      new InputStreamReader(inputStream));
    StringBuffer json = new StringBuffer();

    Map<String, String> labels = new HashMap<>();
    int lineNumber = 0;
    while (true) {
      try {
        String line = null;
        if (!((line = bufferedReader.readLine()) != null)) break;
        Matcher matcher = LABEL_OUTPUT.matcher(line);
        if (matcher.matches()) {
          String entityId = matcher.group(1);
          String label = matcher.group(2);
          if (entityId.equals(entityIds.get(lineNumber))) {
            labels.put(entityId, label);
          } else if (supposeSameOrder) {
            labels.put(entityIds.get(lineNumber), label);
          }
        } else {
          System.err.println("Unexpected line: " + line);
          if (entityIds.size() == 1) {
            labels.put(entityIds.get(lineNumber), line);
          }
        }
        lineNumber++;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      bufferedReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return labels;
  }

  @Override
  public Set<String> getOnHold() {
    return onHold;
  }

  @Override
  public void clearOnHold() {
    onHold = new HashSet<>();
  }

  @Override
  public void addOnHold(String entityId) {
    onHold.add(entityId);
  }

}
