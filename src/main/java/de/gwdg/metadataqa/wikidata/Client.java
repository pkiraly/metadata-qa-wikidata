package de.gwdg.metadataqa.wikidata;

import de.gwdg.metadataqa.wikidata.json.BreakException;
import de.gwdg.metadataqa.wikidata.json.ClassExtractor;
import de.gwdg.metadataqa.wikidata.json.CliParameters;
import de.gwdg.metadataqa.wikidata.json.reader.EntityResolver;
import de.gwdg.metadataqa.wikidata.json.reader.FileBasedLabelExtractor;
import de.gwdg.metadataqa.wikidata.json.reader.JsonTransformer;
import de.gwdg.metadataqa.wikidata.json.MultithreadClassExtractor;
import de.gwdg.metadataqa.wikidata.json.reader.LineProcessor;
import de.gwdg.metadataqa.wikidata.json.Utils;
import org.apache.commons.cli.HelpFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    final Command command = parameters.getCommand();

    int firstRecordToProcess = parameters.getFirstRecordToProcess();
    int lastRecordToProcess = parameters.getLastRecordToProcess();

    System.err.println("command: " + command);

    if (command != null) {
      if (command.equals(Command.ENTITY_CLASS_RESOLUTION)) {
        resolveEntityClasses(entitiesFile);
      } else if (command.equals(Command.ENTITY_CLASS_RESOLUTION_MULTITHREAD)) {
        resolveEntityClassesWithMultithread(entitiesFile);
      } else if (command.equals(Command.ENTITY_RESOLUTION)) {
        resolveEntities(parameters, propertiesFile, entitiesFile, input, command, firstRecordToProcess, lastRecordToProcess);
      } else if (command.equals(Command.ENTITY_RESOLUTION_FROM_LIST)) {
        resolveEntitiesFromList(parameters, entitiesFile, firstRecordToProcess, lastRecordToProcess);
      } else if (command.equals(Command.TRANSFORMATION)) {
        resolveEntities(parameters, propertiesFile, entitiesFile, input, command, firstRecordToProcess, lastRecordToProcess);
        // container.keySet().stream().forEach(
        //   s -> System.out.printf("%s: %d\n", s, container.get(s))
        // );
      } else if (command.equals(Command.EXTRACT_LABELS_FROM_FILE)) {
        extractLabelsFromFile(parameters, input, firstRecordToProcess);
      }
    }

    long duration = (System.currentTimeMillis() - start);
    System.err.println("duration: " + Utils.formatDuration(duration));
  }

  private static void resolveEntitiesFromList(CliParameters parameters,
                                              String entitiesFile,
                                              int firstRecordToProcess,
                                              int lastRecordToProcess) {

    MultithreadClassExtractor extractor = new MultithreadClassExtractor(entitiesFile, 10);
    extractor.resolveAllLabels();
    extractor.saveEntities(entitiesFile);
  }

  private static void resolveEntities(CliParameters parameters,
                                      String propertiesFile,
                                      String entitiesFile,
                                      String input,
                                      Command command,
                                      int firstRecordToProcess, int lastRecordToProcess) {
    final LineProcessor lineProcessor;
    if (command.equals(Command.ENTITY_RESOLUTION)) {
      lineProcessor = new EntityResolver(propertiesFile, entitiesFile, parameters.getEntityBootstrapFile());
      ((EntityResolver) lineProcessor).setSkipResolution(parameters.isSkipResolution());
      ((EntityResolver) lineProcessor).setNewEntitiesFile(parameters.getNewEntityFile());
      ((EntityResolver) lineProcessor).initialize(!parameters.isSkipResolution());
    } else if (command.equals(Command.TRANSFORMATION)) {
      lineProcessor = new JsonTransformer(propertiesFile, entitiesFile);
    } else {
      lineProcessor = null;
    }
    lineProcessor.setOutputFileName(parameters.getOutputFile());

    iterateOverLines(input, lineProcessor, firstRecordToProcess, lastRecordToProcess);
    //  Map<String, Integer> container = lineProcessor.getContainer();
  }

  private static void extractLabelsFromFile(CliParameters parameters,
                                      String input,
                                      int firstRecordToProcess) {
    final LineProcessor lineProcessor = new FileBasedLabelExtractor();
    lineProcessor.setOutputFileName(parameters.getOutputFile());

    iterateOverLines(input, lineProcessor, firstRecordToProcess, -1);
  }

  private static void iterateOverLines(String input,
                                       LineProcessor lineProcessor,
                                       int firstRecordToProcess,
                                       int lastRecordToProcess) {

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get(input));
      lines.forEach(item -> {
        boolean cond1 = firstRecordToProcess == -1 || lineProcessor.getRecordCounter() >= firstRecordToProcess;
        boolean cond2 = lastRecordToProcess == -1 || lineProcessor.getRecordCounter() <= lastRecordToProcess;

        lineProcessor.read(item, (cond1 && cond2));
        if (!cond2)
          throw new BreakException();
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (BreakException e) {
    } finally {
      if (lines != null)
        lines.close();
    }

    lineProcessor.postProcess();
    lineProcessor.saveState();

    System.err.println(lineProcessor.getRecordCounter());
  }

  private static void resolveEntityClasses(String entitiesFile) {
    ClassExtractor extractor = new ClassExtractor(entitiesFile);
    extractor.resolveAll();
    extractor.saveEntities(entitiesFile);
  }

  private static void resolveEntityClassesWithMultithread(String entitiesFile) {
    MultithreadClassExtractor extractor = new MultithreadClassExtractor(entitiesFile, 10);
    extractor.resolveAllClasses();
    extractor.saveEntities(entitiesFile);
  }

  private static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    String message = String.format("java -cp wikidata-0.1.jar %s [options] [file]", Client.class.getCanonicalName());
    formatter.printHelp(message, CliParameters.getOptions());
  }
}
