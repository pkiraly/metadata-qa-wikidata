package de.gwdg.metadataqa.wikidata.model;

import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class UniqFormatEntryTest {

  @Test
  public void testProperInput() {
    UniqFormatEntry entry = new UniqFormatEntry("      1 1008.e11-2");
    assertEquals(1, entry.getCount());
    assertEquals("1008.e11-2", entry.getPages());
  }

  @Test(expected = InvalidParameterException.class)
  public void testInproperInput() {
    UniqFormatEntry entry = new UniqFormatEntry("      11008.e11-2");
  }

}