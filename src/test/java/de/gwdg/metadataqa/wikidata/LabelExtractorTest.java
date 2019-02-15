package de.gwdg.metadataqa.wikidata;

import de.gwdg.metadataqa.wikidata.json.reader.FileBasedLabelExtractor;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class LabelExtractorTest {

  @Test
  public void test() throws ParseException {
    String jsonString = "{\"id\":\"Q45877\",\"labels\":{\"sh\":\"Jagodići\",\"sr\":\"Јагодићи\",\"fr\":\"Jagodići\"}}";

    FileBasedLabelExtractor processor = new FileBasedLabelExtractor();
    processor.read(jsonString);
  }
}
