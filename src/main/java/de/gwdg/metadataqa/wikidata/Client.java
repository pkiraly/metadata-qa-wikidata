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
import de.gwdg.metadataqa.wikidata.json.reader.PageValidationProcessor;
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

    System.err.println(parameters.toString());

    String entityFile = parameters.getEntityFile();
    final Command command = parameters.getCommand();

    System.err.println("command: " + command);

    if (command != null) {
      switch (command) {
        case ENTITY_CLASS_RESOLUTION:
          resolveEntityClasses(entityFile);
          break;

        case ENTITY_CLASS_RESOLUTION_MULTITHREAD:
          resolveEntityClassesWithMultithread(entityFile);
          break;

        case ENTITY_RESOLUTION_FROM_LIST:
          resolveEntitiesFromList(entityFile);
          break;

        case ENTITY_RESOLUTION:
        case TRANSFORMATION:
        case EXTRACT_LABELS_FROM_FILE:
          processEntities(parameters, command);
          break;

        default:
          break;
      }
    }

    long duration = (System.currentTimeMillis() - start);
    System.err.println("duration: " + Utils.formatDuration(duration));
  }

  private static void resolveEntitiesFromList(String entitiesFile) {

    MultithreadClassExtractor extractor = new MultithreadClassExtractor(entitiesFile, 10);
    extractor.resolveAllLabels();
    extractor.saveEntities(entitiesFile);
  }

  private static void processEntities(CliParameters parameters,
                                      Command command) {
    final LineProcessor lineProcessor;
    if (command.equals(Command.ENTITY_RESOLUTION)) {
      lineProcessor = new EntityResolver(
        parameters.getPropertyFile(), parameters.getEntityFile(),
        parameters.getEntityBootstrapFile()
      );
      ((EntityResolver) lineProcessor).setSkipResolution(parameters.skipResolution());
      ((EntityResolver) lineProcessor).setNewEntitiesFile(parameters.getNewEntityFile());
      ((EntityResolver) lineProcessor).initialize(!parameters.skipResolution());
    } else if (command.equals(Command.TRANSFORMATION)) {
      lineProcessor = new JsonTransformer(
        parameters.getPropertyFile(), parameters.getEntityFile()
      );
    } else if (command.equals(Command.EXTRACT_LABELS_FROM_FILE)) {
      lineProcessor = new FileBasedLabelExtractor();
      lineProcessor.setOutputFileName(parameters.getOutputFile());
    } else if (command.equals(Command.PAGE_VALIDATION)) {
      lineProcessor = new PageValidationProcessor(parameters.getOutputFile(), parameters.getEntityFile());
    } else {
      lineProcessor = null;
    }
    lineProcessor.setOutputFileName(parameters.getOutputFile());

    iterateOverLines(
      parameters.getOutputFile(),
      lineProcessor,
      parameters.getFirstRecordToProcess(),
      parameters.getLastRecordToProcess()
    );
  }

  private static void iterateOverLines(String inputFileName,
                                       LineProcessor lineProcessor,
                                       int firstRecordToProcess,
                                       int lastRecordToProcess) {

    Stream<String> lines = null;
    try {
      lines = Files.lines(Paths.get(inputFileName));
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
