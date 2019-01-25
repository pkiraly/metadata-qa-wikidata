package de.gwdg.metadataqa.wikidata.json.labelextractor;

import com.jayway.jsonpath.JsonPath;
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
      Process process = runtime.exec(cmd);
      process.waitFor();
      labels = extractFromLabelOutput(process.getInputStream());
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

  private Map<String, String> extractFromLabelOutput(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(
      new InputStreamReader(inputStream));
    StringBuffer json = new StringBuffer();

    Map<String, String> labels = new HashMap<>();
    while (true) {
      try {
        String line = null;
        if (!((line = bufferedReader.readLine()) != null)) break;
        Matcher matcher = LABEL_OUTPUT.matcher(line);
        if (matcher.matches()) {
          labels.put(matcher.group(1), matcher.group(2));
        }
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

  public Set<String> getOnHold() {
    return onHold;
  }

  public void clearOnHold() {
    onHold = new HashSet<>();
  }
}
