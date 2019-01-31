package de.gwdg.metadataqa.wikidata.model;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniqFormatEntry {

  private Pattern LINE_PATTERN = Pattern.compile("^ +(\\d+) (.*)$");

  private String line;
  private int count;
  private String pageNumbers;

  public UniqFormatEntry(String line) throws InvalidParameterException {
    Matcher matcher = LINE_PATTERN.matcher(line);
    if (matcher.matches()) {
      count = Integer.parseInt(matcher.group(1));
      pageNumbers = matcher.group(2);
    } else {
      throw new InvalidParameterException("Line doesn't match the pattern: " + line);
    }
  }

  public String getLine() {
    return line;
  }

  public int getCount() {
    return count;
  }

  public String getPageNumbers() {
    return pageNumbers;
  }
}
