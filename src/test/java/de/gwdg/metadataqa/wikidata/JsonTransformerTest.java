package de.gwdg.metadataqa.wikidata;

import de.gwdg.metadataqa.wikidata.json.BreakException;
import de.gwdg.metadataqa.wikidata.json.reader.JsonTransformer;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class JsonTransformerTest {

  @Test
  public void test() {
    String propertiesFile = "data/properties.csv";
    // String entitiesFile = "data/entities-12M.csv";
    String entitiesFile = "src/test/resources/entities-1K.csv";
    String inputFile = "src/test/resources/10-records.json";
    String outputFile = "src/test/resources/10-records-transformed.json";
    JsonTransformer transformer = new JsonTransformer(propertiesFile, entitiesFile);
    transformer.setOutputFileName(outputFile);
    int processingLimit = 0;

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get(inputFile));
      lines.forEach(item -> {
        transformer.read(item);
        if (processingLimit != 0
          && transformer.getRecordCounter() == processingLimit)
          throw new BreakException();
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (BreakException e) {
    } finally {
      if (lines != null)
        lines.close();
    }

  }
}
