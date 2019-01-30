package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.json.labelextractor.JenaBasedSparqlClient;
import de.gwdg.metadataqa.wikidata.model.WikidataEntity;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
    String entitiesFile = "src/test/resources/entities.csv";
    ClassExtractor extractor = new ClassExtractor(entitiesFile);
    extractor.resolveAll();
    extractor.saveEntities("src/test/resources/entities-with-classes.csv");

    List<String> humans = Arrays.asList("Q547086", "Q547097", "Q3264849");
    Map<String, WikidataEntity> entities = extractor.getEntities();
    for (WikidataEntity entity : entities.values()) {
      assertFalse(entity.getClasses().isEmpty());
      assertEquals(1, entity.getClasses().size());
      if (humans.contains(entity.getId())) {
        assertTrue(entity.getClasses().containsKey("Q5"));
      } else if (entity.getId().equals("Q11334178")) {
        assertTrue(entity.getClasses().containsKey("Q134556"));
      } else {
        assertTrue(entity.getClasses().containsKey("Q13442814"));
      }
    }
  }

}
