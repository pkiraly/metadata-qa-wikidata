package de.gwdg.metadataqa.wikidata.json;

import com.sun.media.sound.InvalidDataException;
import de.gwdg.metadataqa.wikidata.model.UniqFormatEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageValidator {

  enum Error {
    NOT_NUMBERS,
    SHORTER_SECOND_NUMBER,
    SMALLER_SECOND_NUMBER;
  }
  private Pattern TWO_NUMBERS_PATTERN = Pattern.compile("^(\\d+)-(\\d+)$");
  private Pattern ONE_NUMBER_PATTERN = Pattern.compile("^(\\d+)$");

  public boolean validateUniqFormatEntry(String line) throws InvalidDataException {
    UniqFormatEntry entry = new UniqFormatEntry(line);
    String pages = entry.getPages();
    return validatePageNumbers(pages);
  }

  private boolean validatePageNumbers(String pages) throws InvalidDataException {
    Error error = null;
    Matcher matcher = TWO_NUMBERS_PATTERN.matcher(pages);
    if (matcher.matches()) {
      String page1 = matcher.group(1);
      String page2 = matcher.group(2);
      if (page2.length() < page1.length()) {
        error = Error.SHORTER_SECOND_NUMBER;
      } else {
        int p1 = Integer.parseInt(page1);
        int p2 = Integer.parseInt(page2);
        if (p2 < p1) {
          error = Error.SMALLER_SECOND_NUMBER;
        }
      }
    } else {
      matcher = ONE_NUMBER_PATTERN.matcher(pages);
      if (!matcher.matches())
        error = Error.NOT_NUMBERS;
    }
    if (error != null) {
      throw new InvalidDataException(error.toString());
    }
    return true;
  }

}
