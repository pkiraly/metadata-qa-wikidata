package de.gwdg.metadataqa.wikidata.json;

import com.sun.media.sound.InvalidDataException;
import de.gwdg.metadataqa.wikidata.model.PageNumberErrorType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PageValidatorTest {

  private PageValidator validator;
  int validCounter = 0;
  int invalidCounter = 0;

  @Before
  public void setUp() throws Exception {
    validator = new PageValidator();
  }

  @Test(expected = InvalidPageNumberException.class)
  public void testInvalidValue() throws InvalidPageNumberException {
    validator.validateUniqFormatEntry("      1 1008.e11-2");
  }

  @Test
  public void testExtractor() throws InvalidPageNumberException {
    validator.validateUniqFormatEntry("      1 10093-10096");
  }

  @Test
  public void testAll() throws InvalidPageNumberException {
    Map<PageNumberErrorType, Integer> counter = new HashMap<>();
    Map<String, Integer> patternCounter = new HashMap<>();

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get("data/uniq-pages.txt"));
      lines.forEach((line) -> {
        try {
          boolean isValid = validator.validateUniqFormatEntry(line);
          if (isValid)
            validCounter++;
          else
            invalidCounter++;
        } catch (InvalidPageNumberException e) {
          invalidCounter++;
          if (!counter.containsKey(e.getType())) {
            counter.put(e.getType(), 0);
          }
          counter.put(e.getType(), counter.get(e.getType()) + 1);
          if (e.getType().equals(PageNumberErrorType.NOT_NUMBERS)) {
            String key = e.getShortened();
            if (!patternCounter.containsKey(key)) {
              patternCounter.put(key, 0);
            }
            patternCounter.put(key, patternCounter.get(key) + 1);
          }
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (BreakException e) {
    } finally {
      if (lines != null)
        lines.close();
    }
    System.out.printf("valid: %d, invalid: %d%n", validCounter, invalidCounter);
    for (Map.Entry entry : counter.entrySet()) {
      System.out.printf("%s: %d%n", entry.getKey(), entry.getValue());
    }
    for (Map.Entry entry : patternCounter.entrySet()) {
      System.out.printf("%s: %d%n", entry.getKey(), entry.getValue());
    }
  }

  @Test
  public void testRomanPattern() {
    Pattern singleRoman = Pattern.compile("^(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", CASE_INSENSITIVE);
    assertTrue(singleRoman.matcher("ix").matches());
    assertTrue(singleRoman.matcher("xxxviii").matches());

    Pattern doubleRoman = Pattern.compile("^(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})(?:–|-)(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", CASE_INSENSITIVE);
    assertTrue(doubleRoman.matcher("ix-xxxviii").matches());
  }

  @Test
  public void testSingleRoman() {
    boolean valid = false;
    try {
      valid = validator.validatePageNumbers("ix", "test");
    } catch (InvalidPageNumberException e) {
      System.err.println(e);
    }
    assertTrue(valid);
  }

  @Test
  public void testRomanRange() {
    boolean valid = false;
    try {
      valid = validator.validatePageNumbers("ix-xxxviii", "test");
    } catch (InvalidPageNumberException e) {
      System.err.println(e);
    }
    assertTrue(valid);
  }

  @Test
  public void testELocation() {
    boolean valid = false;
    try {
      valid = validator.validatePageNumbers("e53453", "Q564954");
    } catch (InvalidPageNumberException e) {
      System.err.println(e);
    }
    assertTrue(valid);
  }

  @Test
  public void testJournalBasedPattern() {
    boolean valid = false;
    InvalidPageNumberException exception = null;
    try {
      valid = validator.validatePageNumbers("e3533", "Q546003");
    } catch (InvalidPageNumberException ex) {
      exception = ex;
    }
    assertNull(exception);
    assertTrue(valid);
  }

  @Test
  public void testSmallerSecond() {
    boolean valid = false;
    InvalidPageNumberException e = null;
    try {
      valid = validator.validatePageNumbers("11954-73", "Q564954");
    } catch (InvalidPageNumberException ex) {
      e = ex;
    }
    assertEquals(PageNumberErrorType.SHORTER_SECOND_NUMBER, e.getType());
    assertFalse(valid);
  }
}
