package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.model.PageNumberErrorType;
import de.gwdg.metadataqa.wikidata.model.UniqFormatEntry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageValidator {

  private Pattern TWO_NUMBERS_PATTERN = Pattern.compile("^(\\d+)(?:–|-)(\\d+)$");
  private Pattern ONE_NUMBER_PATTERN = Pattern.compile("^(\\d+)$");
  private static Map<String, List<Pattern>> journalBasedPatterns = new HashMap<>();
  static {
    journalBasedPatterns.put("Q546003", Arrays.asList(
      Pattern.compile("^([abcdefghijk]\\d+)(-\\1)?$"),
      Pattern.compile("^\\d+(-\\d+)?; (author reply|discussion) \\d+(-\\d+)?$")
    ));
  }

  public boolean validateUniqFormatEntry(String line) throws InvalidPageNumberException {
    UniqFormatEntry entry = new UniqFormatEntry(line);
    String pageNumbers = entry.getPageNumbers();
    return validatePageNumbers(pageNumbers, null);
  }

  public boolean validatePageNumbers(String pageNumbers, String journal)
      throws InvalidPageNumberException {
    PageNumberErrorType error = null;
    Matcher matcher = TWO_NUMBERS_PATTERN.matcher(pageNumbers);
    String shortened = pageNumbers;
    if (matcher.matches()) {
      String page1 = matcher.group(1);
      String page2 = matcher.group(2);
      if (page2.length() < page1.length()) {
        error = PageNumberErrorType.SHORTER_SECOND_NUMBER;
      } else {
        try {
          int p1 = Integer.parseInt(page1);
          int p2 = Integer.parseInt(page2);
          if (p2 < p1) {
            error = PageNumberErrorType.SMALLER_SECOND_NUMBER;
          }
        } catch (NumberFormatException e) {
          error = PageNumberErrorType.NUMBER_FORMAT;
        }
      }
    } else {
      matcher = ONE_NUMBER_PATTERN.matcher(pageNumbers);
      if (!matcher.matches()) {
        if (journal != null && journalBasedPatterns.containsKey(journal)) {
          boolean found = false;
          for (Pattern pattern : journalBasedPatterns.get(journal)) {
            if (pattern.matcher(pageNumbers).matches()) {
              found = true;
              break;
            }
          }
          if (!found) {
            error = PageNumberErrorType.NOT_FIT_TO_JOURNAL_STANDARD;
            shortened = shortened.replaceAll("\\d+", "N");
          }
        } else {
          error = PageNumberErrorType.NOT_NUMBERS;
          shortened = shortened.replaceAll("\\d+", "N");
          // System.err.printf("<%s>%n", shortened);
        }
      }
    }
    if (error != null) {
      throw new InvalidPageNumberException(pageNumbers, error, shortened);
    }
    return true;
  }

}
