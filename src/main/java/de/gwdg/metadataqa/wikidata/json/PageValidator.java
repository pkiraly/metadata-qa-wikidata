package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.model.PageNumberErrorType;
import de.gwdg.metadataqa.wikidata.model.UniqFormatEntry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class PageValidator {

  private final static Pattern PAGE_RANGE_PATTERN = Pattern.compile("^(\\d+)(?:–|-)(\\d+)$");
  private final static Pattern SINGLE_ROMAN_PATTERN = Pattern.compile(
    "^(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", CASE_INSENSITIVE);
  private final static Pattern ROMAN_RANGE_PATTERN = Pattern.compile(
    "^(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})(?:–|-)(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", CASE_INSENSITIVE);
  private final static Pattern ONE_NUMBER_PATTERN = Pattern.compile("^(\\d+)$");
  private final static Pattern E_LOCATION_ID_PATTERN = Pattern.compile("^e(\\d+)$");
  private final static List<String> E_LOCATIONS = Arrays.asList(
    "Q26839533", // BMJ Open
    "Q3359737", // PLOS Neglected Tropical Diseases
    "Q564954", // PLoS ONE
    "Q2635829", // PLOS Computational Biology
    "Q2000010", // PeerJ
    "Q15716652" // Medicine
  );

  private static Map<String, List<Pattern>> journalBasedPatterns = new HashMap<>();
  static {
    // British Medical Journal
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
    Check check = isPageRange(pageNumbers);
    if (!check.isValid()) {
      check = isSingleRoman(pageNumbers);
      if (!check.isValid()) {
        check = isRomanRange(pageNumbers);
        if (!check.isValid()) {
          check = isSingleNumber(pageNumbers);
          if (!check.isValid()) {
            if (journal != null) {
              check = isELocation(pageNumbers, journal);
              if (!check.isValid()) {
                check = hasMetJournalStandard(pageNumbers, journal);
                if (!check.isValid()) {
                  check = new Check(false, PageNumberErrorType.NOT_NUMBERS);
                }
              }
            } else {
              check = new Check(false, PageNumberErrorType.NOT_NUMBERS);
            }
          }
        }
      }
    }

    if (!check.isValid()) {
      String shortened = pageNumbers.replaceAll("\\d+", "N");
      throw new InvalidPageNumberException(pageNumbers, check.getError(), shortened);
    }
    return true;
  }

  private Check hasMetJournalStandard(String pageNumbers, String journal) {
    Check check = new Check(false);
    if (journalBasedPatterns.containsKey(journal)) {
      boolean found = false;
      for (Pattern pattern : journalBasedPatterns.get(journal)) {
        if (pattern.matcher(pageNumbers).matches()) {
          found = true;
          break;
        }
      }
      if (found) {
        check = new Check(true);
      } else {
        check = new Check(false, PageNumberErrorType.UNMET_JOURNAL_STANDARD);
      }
    }
    return check;
  }

  public Check isPageRange(String pageNumbers) {
    Matcher matcher = PAGE_RANGE_PATTERN.matcher(pageNumbers);
    Check isValid;
    if (matcher.matches()) {
      String page1 = matcher.group(1);
      String page2 = matcher.group(2);
      if (page2.length() < page1.length()) {
        isValid = new Check(false, PageNumberErrorType.SHORTER_SECOND_NUMBER);
      } else {
        try {
          int p1 = Integer.parseInt(page1);
          int p2 = Integer.parseInt(page2);
          if (p2 < p1) {
            isValid = new Check(false, PageNumberErrorType.SMALLER_SECOND_NUMBER);
          } else {
            isValid = new Check(true);
          }
        } catch (NumberFormatException e) {
          isValid = new Check(false, PageNumberErrorType.NUMBER_FORMAT);
        }
      }
    } else {
      isValid = new Check(false);
    }
    return isValid;
  }

  public Check isSingleRoman(String pageNumbers) {
    return new Check(SINGLE_ROMAN_PATTERN.matcher(pageNumbers).matches());
  }

  public Check isRomanRange(String pageNumbers) {
    return new Check(ROMAN_RANGE_PATTERN.matcher(pageNumbers).matches());
  }

  public Check isSingleNumber(String pageNumbers) {
    return new Check(ONE_NUMBER_PATTERN.matcher(pageNumbers).matches());
  }

  public Check isELocation(String pageNumbers, String journal) {
    Check check;
    if (E_LOCATIONS.contains(journal)) {
      if (E_LOCATION_ID_PATTERN.matcher(pageNumbers).matches()) {
        check = new Check(true);
      } else {
        check = new Check(false, PageNumberErrorType.NOT_E_LOCATION_ID);
      }
    } else {
      check = new Check(false);
    }
    return check;
  }

  private class Check {
    boolean valid = false;
    PageNumberErrorType error = null;

    public Check() {
    }

    public Check(boolean valid) {
      this.valid = valid;
    }

    public Check(boolean valid, PageNumberErrorType error) {
      this.valid = valid;
      this.error = error;
    }

    public boolean isValid() {
      return valid;
    }

    public PageNumberErrorType getError() {
      return error;
    }
  }
}
