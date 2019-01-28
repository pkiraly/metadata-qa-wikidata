package de.gwdg.metadataqa.wikidata;

import de.gwdg.metadataqa.wikidata.json.BreakException;
import de.gwdg.metadataqa.wikidata.json.CliParameters;
import de.gwdg.metadataqa.wikidata.json.Reader;
import de.gwdg.metadataqa.wikidata.json.Utils;
import org.apache.commons.cli.HelpFormatter;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public class Client {

  public static void main(String[] args) {

    long start = System.currentTimeMillis();
    CliParameters parameters = null;
    try {
      parameters = new CliParameters(args);
    } catch (org.apache.commons.cli.ParseException e) {
      System.out.printf("ERROR: %s%n", e.getLocalizedMessage());
      printHelp();
      System.exit(1);
    }

    if (parameters.showHelp()) {
      printHelp();
      System.exit(1);
    }

    System.out.println(parameters.toString());

    String propertiesFile = parameters.getPropertyFile();
    String entitiesFile = parameters.getEntityFile();
    String input = parameters.getInputFile();
    Reader reader = new Reader(propertiesFile, entitiesFile);
    reader.setOutputFileName(parameters.getOutputFile());
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
    Map<String, Integer> container = reader.getContainer();
    // container.keySet().stream().forEach(
    //   s -> System.out.printf("%s: %d\n", s, container.get(s))
    // );

    long duration = (System.currentTimeMillis() - start);
    System.err.println("duration: " + Utils.formatDuration(duration));
  }

  private static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    String message = String.format("java -cp wikidata-0.1.jar %s [options] [file]", Client.class.getCanonicalName());
    formatter.printHelp(message, CliParameters.getOptions());
  }
}
