package de.gwdg.metadataqa.wikidata.json;

import org.apache.http.client.protocol.ResponseProcessCookies;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Stream;

import static java.util.logging.LogManager.*;

public class ReaderClient {

  public static void main(String[] args) throws IOException, ParseException {

    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
    java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
    System.getProperties().put("org.apache.commons.logging.simplelog.defaultlog", "fatal");

    org.slf4j.LoggerFactory.getLogger(ResponseProcessCookies.class);
    java.util.logging.Logger.getLogger(ResponseProcessCookies.class.getName()).setLevel(Level.OFF);

    String directory = "/media/kiru/Elements/projects/wikidata/";
    String propertiesFile = "/home/kiru/Documents/phd/wikidata/properties-12M.csv";
    String entitiesFile = "/home/kiru/Documents/phd/wikidata/entities-12M.csv";
    String[] fileNames = new String[]{"wikidata-20171211-publications.ndjson"};
    Reader reader = new Reader(propertiesFile, entitiesFile);

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get(directory + fileNames[0]));
      lines.forEach(item -> {
        reader.read(item);
        // if (reader.getRecordCounter() == 100)
        //   throw new BreakException();
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (BreakException e) {
    } finally {
      if (lines != null)
        lines.close();
    }

    reader.saveEntities();

    System.err.println(reader.getRecordCounter());
    // System.err.println(container);
    Map<String, Integer> container = reader.getContainer();
    // container.keySet().stream().forEach(
    //   s -> System.out.printf("%s: %d\n", s, container.get(s))
    // );
  }


}
