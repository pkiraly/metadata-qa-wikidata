package de.gwdg.metadataqa.wikidata.json;

import org.apache.commons.cli.HelpFormatter;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public class ReaderClient {

  public static void main(String[] args) throws IOException, ParseException {

    long start = System.currentTimeMillis();
    CliParameters parameters = null;
    try {
      parameters = new CliParameters(args);
    } catch (org.apache.commons.cli.ParseException e) {
      printHelp();
      e.printStackTrace();
    }

    if (parameters.showHelp()) {
      printHelp();
      System.exit(1);
    }

    System.out.print(parameters.toString());

    // System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
    // java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
    // System.getProperties().put("org.apache.commons.logging.simplelog.defaultlog", "fatal");

    // org.slf4j.LoggerFactory.getLogger(ResponseProcessCookies.class);
    // java.util.logging.Logger.getLogger(ResponseProcessCookies.class.getName()).setLevel(Level.OFF);

    // String directory = parameters.getInputFile(); //"/media/kiru/Elements/projects/wikidata/";
    String propertiesFile = parameters.getPropertyFile(); // "/home/kiru/Documents/phd/wikidata/properties-12M.csv";
    String entitiesFile = parameters.getEntityFile(); // "/home/kiru/Documents/phd/wikidata/entities-12M.csv";
    // String[] fileNames = new String[]{"wikidata-20171211-publications.ndjson"};
    String input = parameters.getInputFile();
    Reader reader = new Reader(propertiesFile, entitiesFile);
    int processingLimit = 0;

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get(input));
      lines.forEach(item -> {
        reader.read(item);
        if (processingLimit != 0 && reader.getRecordCounter() == processingLimit)
          throw new BreakException();
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (BreakException e) {
    } finally {
      if (lines != null)
        lines.close();
    }

    reader.postProcess();
    reader.saveEntities();

    System.err.println(reader.getRecordCounter());
    // System.err.println(container);
    Map<String, Integer> container = reader.getContainer();
    // container.keySet().stream().forEach(
    //   s -> System.out.printf("%s: %d\n", s, container.get(s))
    // );

    long duration = (System.currentTimeMillis() - start);
    System.err.println("duration: " + Utils.formatDuration(duration));
  }

  private static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    String message = String.format("java -cp metadata-qa-wikidata.jar %s [options] [file]", ReaderClient.class.getCanonicalName());
    formatter.printHelp(message, CliParameters.getOptions());
  }
}
