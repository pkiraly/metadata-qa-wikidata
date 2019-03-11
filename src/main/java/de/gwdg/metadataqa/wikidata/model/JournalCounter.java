package de.gwdg.metadataqa.wikidata.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JournalCounter {
  String id;
  String label;
  int properPageNumberCounter = 0;
  int improperPageNumberCounter = 0;
  Map<String, Integer> pageNumberPatternCounter = new HashMap<>();
  Map<PageNumberErrorType, Integer> errorTypeCounter = new HashMap<>();

  public JournalCounter(String id) {
    this.id = id;
  }

  public void addProper() {
    properPageNumberCounter++;
  }

  public void addImproper() {
    improperPageNumberCounter++;
  }

  public void addPageNumberPattern(String value) {
    if (!pageNumberPatternCounter.containsKey(value)) {
      pageNumberPatternCounter.put(value, 0);
    }
    pageNumberPatternCounter.put(value, pageNumberPatternCounter.get(value) + 1);
  }

  public void addPageErrorType(PageNumberErrorType value) {
    if (!errorTypeCounter.containsKey(value)) {
      errorTypeCounter.put(value, 0);
    }
    errorTypeCounter.put(value, errorTypeCounter.get(value) + 1);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public int getImproperPageNumberCounter() {
    return improperPageNumberCounter;
  }

  public Map<String, Integer> getPageNumberPatternCounter() {
    return pageNumberPatternCounter;
  }

  public List<String> formatTypeCounters() {
    List<String> entries = new ArrayList<>();
    pageNumberPatternCounter
      .entrySet()
      .stream()
      .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
      .forEach((entry) -> entries.add(
        String.format(
          "'%s' (%d)",
          entry.getKey(), entry.getValue()
        )
      ));
    return entries;
  }

  public static String csvHeader() {
    List<String> fields = new ArrayList<String>(Arrays.asList(
      "label", "id", "properPageNumberCounter", "improperPageNumberCounter"
    ));
    for (PageNumberErrorType type : PageNumberErrorType.values()) {
      fields.add(type.name());
    }
    fields.add("totalTypes");
    fields.add("types");
    return StringUtils.join(fields, ",");
  }

  private Object getErrorTypes() {
    List<Integer> list = new ArrayList<>();
    for (PageNumberErrorType type : PageNumberErrorType.values()) {
      list.add(errorTypeCounter.getOrDefault(type, 0));
    }
    return StringUtils.join(list, ",");
  }

  public String toCsv() {
    return String.format(
      "\"%s\",%s,%d,%d,%s,%d,\"%s\"",
      label, id,
      properPageNumberCounter, improperPageNumberCounter,
      getErrorTypes(),
      pageNumberPatternCounter.size(),
      StringUtils.join(
        formatTypeCounters()
          .stream()
          .limit(10)
          .collect(Collectors.toList()),
        "; "
      )
    );
  }

  @Override
  public String toString() {
    return label + " (" + id + ")" +
      ": proper=" + properPageNumberCounter +
      ", improper=" + improperPageNumberCounter +
      ", total types=" + pageNumberPatternCounter.size() +
      ", types=" + StringUtils.join(formatTypeCounters(), ", ")
    ;
  }
}
