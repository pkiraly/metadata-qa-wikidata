package de.gwdg.metadataqa.wikidata.json.reader;

import de.gwdg.metadataqa.wikidata.json.BreakException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PageValidationProcessorTest {

  @Test
  public void test() {
    String inputFile = "src/test/resources/10-records-with-page-numbers.json";
    // String inputFile = "/media/kiru/Elements/projects/wikidata/prefix-01";
    // String inputFile = "/media/kiru/Elements/projects/wikidata/wikidata-20190128-publications.ndjson";
    // String entitiesFile = "data/entities-23Mb.csv";
    String entitiesFile = "src/test/resources/entities-for-journal-test.csv";
    String outputFile = "src/test/resources/pages-per-journals-test.txt";

    Path path = Paths.get(outputFile);
    deleteOutput(path);
    assertFalse(Files.exists(path));

    PageValidationProcessor processor = new PageValidationProcessor(outputFile, entitiesFile);
    int processingLimit = 0;

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get(inputFile));
      lines.forEach(item -> {
        processor.read(item);
        if (processingLimit != 0
          && processor.getRecordCounter() == processingLimit)
          throw new BreakException();
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (BreakException e) {
    } finally {
      if (lines != null)
        lines.close();
    }

    processor.postProcess();
    processor.saveState();
    assertTrue(Files.exists(path));
  }

  private void deleteOutput(Path path) {
    if (Files.exists(path)) {
      try {
        Files.delete(path);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
