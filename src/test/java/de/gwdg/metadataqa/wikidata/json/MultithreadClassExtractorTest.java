package de.gwdg.metadataqa.wikidata.json;

import org.junit.Test;

public class MultithreadClassExtractorTest {

  @Test
  public void testExtractor() {
    String entitiesFile = "src/test/resources/entities.csv";
    MultithreadClassExtractor extractor = new MultithreadClassExtractor(entitiesFile, 10);
    extractor.resolveAll();
    extractor.saveEntities("src/test/resources/entities-with-classes2.csv");
  }

}
