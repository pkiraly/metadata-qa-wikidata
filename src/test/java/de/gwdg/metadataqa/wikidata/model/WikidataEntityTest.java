package de.gwdg.metadataqa.wikidata.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WikidataEntityTest {

  @Test
  public void serializeSingle() {
    WikidataEntity entity = new WikidataEntity("Q19007672", "Francisco de Robles");
    Map<String, String> instanceOf = new HashMap<>();
    instanceOf.put("Q5", "human");
    entity.setClasses(instanceOf);

    assertEquals("Q5:human", entity.serializeClasses());
  }

  @Test
  public void deserializeSingle() {
    WikidataEntity entity = new WikidataEntity("Q19007672", "Francisco de Robles");
    Map<String, String> instanceOf = entity.deserializeClasses("Q5:human");

    Map<String, String> expected = new HashMap<>();
    expected.put("Q5", "human");


    assertEquals(expected, instanceOf);
  }

  @Test
  public void serializeMultiple() {
    WikidataEntity entity = new WikidataEntity("Q19007672", "Francisco de Robles");
    Map<String, String> instanceOf = new HashMap<>();
    instanceOf.put("Q5", "human");
    instanceOf.put("Q71", "Geneva");
    entity.setClasses(instanceOf);

    assertEquals("Q5:human|Q71:Geneva", entity.serializeClasses());
  }

  @Test
  public void deserializeMultiple() {
    WikidataEntity entity = new WikidataEntity("Q19007672", "Francisco de Robles");
    Map<String, String> instanceOf = entity.deserializeClasses("Q5:human|Q71:Geneva");

    Map<String, String> expected = new HashMap<>();
    expected.put("Q5", "human");
    expected.put("Q71", "Geneva");

    assertEquals(expected, instanceOf);
  }

}
