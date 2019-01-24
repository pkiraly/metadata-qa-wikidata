package de.gwdg.metadataqa.wikidata.json;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public class ReaderClient {

  public static void main(String[] args) throws IOException, ParseException {

    String directory = "/media/kiru/Elements/projects/wikidata/";
    String propertyFile = "/home/kiru/Documents/phd/wikidata/properties-12M.csv";
    String entitiesFile = "/home/kiru/Documents/phd/wikidata/entities-12M.csv";
    String[] fileNames = new String[]{"wikidata-20171211-publications.ndjson"};
    Reader reader = new Reader(propertyFile);

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get(directory + fileNames[0]));
      lines.forEach(item -> {
        reader.read(item);
        if (reader.getRecordCounter() == 100)
          throw new BreakException();
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (BreakException e) {
    } finally {
      if (lines != null)
        lines.close();
    }

    reader.saveEntities(entitiesFile);

    System.err.println(reader.getRecordCounter());
    // System.err.println(container);
    Map<String, Integer> container = reader.getContainer();
    // container.keySet().stream().forEach(
    //   s -> System.out.printf("%s: %d\n", s, container.get(s))
    // );
  }


}
