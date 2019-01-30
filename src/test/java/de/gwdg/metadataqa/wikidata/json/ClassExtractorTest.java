package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.json.labelextractor.JenaBasedSparqlClient;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ClassExtractorTest {

  @Test
  public void testSparqlClient() {
    JenaBasedSparqlClient sparqlClient = new JenaBasedSparqlClient();
    Map<String, String> label = sparqlClient.getClasses("Q19007672");

    assertEquals(1, label.size());
    assertTrue(label.containsKey("Q5"));
    assertEquals("human", label.get("Q5"));
  }

  @Test
  public void testExtractor() {

    String entitiesFile = "data/entities-12M.csv";
    ClassExtractor extractor = new ClassExtractor(entitiesFile);
    // System.err.println(new File(entitiesFile).getAbsolutePath());
  }

}
