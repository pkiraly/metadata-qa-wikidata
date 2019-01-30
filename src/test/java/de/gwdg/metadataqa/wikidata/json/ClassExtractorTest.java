package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.json.labelextractor.JenaBasedSparqlClient;
import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ClassExtractorTest {

  @Test
  public void test() {
    JenaBasedSparqlClient sparqlClient = new JenaBasedSparqlClient();
    Map<String, String> label = sparqlClient.getClassess("Q19007672");

    assertEquals(1, label.size());
    assertTrue(label.containsKey("Q5"));
    assertEquals("human", label.get("Q5"));
  }

}
