package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.Command;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliParameters {
  protected static Options options = new Options();
  protected static final CommandLineParser parser = new DefaultParser();

  static {
    options.addOption("i", "input-file", true, "input JSON dump");
    options.addOption("o", "output-file", true, "output JSON dump");
    options.addOption("p", "property-file", true, "property file");
    options.addOption("e", "entity-file", true, "entity file");
    options.addOption("w", "new-entity-file", true, "new entity file");
    options.addOption("c", "command", true, "command");
    options.addOption("t", "first-record-to-process", true, "first record to process");
    options.addOption("l", "last-record-to-process", true, "first record to process");
    options.addOption("s", "skip-resolution", false, "skip resolution (only collect statistics)");
    options.addOption("b", "entity-bootstrap", true, "entity bootstrap file (list of entities to read in)");
    options.addOption("h", "help", false, "help");
  }

  private String inputFile;
  private String outputFile;
  private String propertyFile;
  private String entityFile;
  private String newEntityFile;
  private String entityBootstrapFile;
  private boolean help = false;
  private Command command;
  private int firstRecordToProcess = -1;
  private int lastRecordToProcess = -1;
  private boolean skipResolution = false;

  public CliParameters(String[] arguments) throws ParseException {
    CommandLine cmd = parser.parse(options, arguments);

    if (cmd.hasOption("command")) {
      try {
        command = Command.valueOf(cmd.getOptionValue("command"));
      } catch (IllegalArgumentException e) {
        throw new ParseException(cmd.getOptionValue("command") + " is not a valid command!");
      }
    }

    if (cmd.hasOption("input-file")) {
      inputFile = cmd.getOptionValue("input-file");
    }

    if (cmd.hasOption("output-file")) {
      outputFile = cmd.getOptionValue("output-file");
    }

    if (cmd.hasOption("property-file")) {
      propertyFile = cmd.getOptionValue("property-file");
    }

    if (cmd.hasOption("entity-file")) {
      entityFile = cmd.getOptionValue("entity-file");
    }

    if (cmd.hasOption("first-record-to-process")) {
      firstRecordToProcess = Integer.parseInt(cmd.getOptionValue("first-record-to-process"));
    }

    if (cmd.hasOption("last-record-to-process")) {
      lastRecordToProcess = Integer.parseInt(cmd.getOptionValue("last-record-to-process"));
    }

    if (cmd.hasOption("entity-bootstrap")) {
      entityBootstrapFile = cmd.getOptionValue("entity-bootstrap");
    }

    if (cmd.hasOption("new-entity-file")) {
      newEntityFile = cmd.getOptionValue("new-entity-file");
    }

    if (cmd.hasOption("help"))
      help = true;

    if (cmd.hasOption("skip-resolution"))
      skipResolution = true;
  }

  public String getInputFile() {
    return inputFile;
  }

  public String getOutputFile() {
    return outputFile;
  }

  public String getPropertyFile() {
    return propertyFile;
  }

  public String getEntityFile() {
    return entityFile;
  }

  public Command getCommand() {
    return command;
  }

  public int getFirstRecordToProcess() {
    return firstRecordToProcess;
  }

  public int getLastRecordToProcess() {
    return lastRecordToProcess;
  }

  public String getEntityBootstrapFile() {
    return entityBootstrapFile;
  }

  public String getNewEntityFile() {
    return newEntityFile;
  }

  public boolean showHelp() {
    return help;
  }

  public static Options getOptions() {
    return options;
  }

  public boolean skipResolution() {
    return skipResolution;
  }

  @Override
  public String toString() {
    return "[parameters]" + "\n" +
      "  command: " + command + "\n" +
      "  input-file: " + inputFile + "\n" +
      "  output-file: " + outputFile + "\n" +
      "  property-file: " + propertyFile + "\n" +
      "  entity-file: " + entityFile + "\n" +
      "  new-entity-file: " + newEntityFile + "\n" +
      "  first-record-to-process: " + firstRecordToProcess + "\n" +
      "  last-record-to-process: " + lastRecordToProcess + "\n" +
      "  skip-resolution: " + skipResolution + "\n" +
      "  entity-bootstrap: " + entityBootstrapFile + "\n" +
      "  help: " + help + "\n";
  }
}