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
import java.util.stream.Stream;

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
          // System.err.println(e.getMessage());
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
          // e.printStackTrace();
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

}
