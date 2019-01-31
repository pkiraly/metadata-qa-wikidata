package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.model.PageNumberErrorType;
import de.gwdg.metadataqa.wikidata.model.UniqFormatEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageValidator {

  private Pattern TWO_NUMBERS_PATTERN = Pattern.compile("^(\\d+)(?:–|-)(\\d+)$");
  private Pattern ONE_NUMBER_PATTERN = Pattern.compile("^(\\d+)$");

  public boolean validateUniqFormatEntry(String line) throws InvalidPageNumberException {
    UniqFormatEntry entry = new UniqFormatEntry(line);
    String pageNumbers = entry.getPageNumbers();
    return validatePageNumbers(pageNumbers);
  }

  private boolean validatePageNumbers(String pageNumbers) throws InvalidPageNumberException {
    PageNumberErrorType error = null;
    Matcher matcher = TWO_NUMBERS_PATTERN.matcher(pageNumbers);
    String shortened = pageNumbers;
    if (matcher.matches()) {
      String page1 = matcher.group(1);
      String page2 = matcher.group(2);
      if (page2.length() < page1.length()) {
        error = PageNumberErrorType.SHORTER_SECOND_NUMBER;
      } else {
        int p1 = Integer.parseInt(page1);
        int p2 = Integer.parseInt(page2);
        if (p2 < p1) {
          error = PageNumberErrorType.SMALLER_SECOND_NUMBER;
        }
      }
    } else {
      matcher = ONE_NUMBER_PATTERN.matcher(pageNumbers);
      if (!matcher.matches()) {
        error = PageNumberErrorType.NOT_NUMBERS;
        shortened = shortened.replaceAll("\\d+", "N");
        // System.err.printf("<%s>%n", shortened);
      }
    }
    if (error != null) {
      throw new InvalidPageNumberException(pageNumbers, error, shortened);
    }
    return true;
  }

}
