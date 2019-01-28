package de.gwdg.metadataqa.wikidata.json;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliParameters {
  protected static Options options = new Options();
  protected static final CommandLineParser parser = new DefaultParser();

  public enum Command {
    TRANSFORMATION,
    READ,
    ENTITY_RESOLUTION;
  }

  static {
    options.addOption("i", "input-file", true, "input JSON dump");
    options.addOption("o", "output-file", true, "output JSON dump");
    options.addOption("p", "property-file", true, "property file");
    options.addOption("e", "entity-file", true, "entity file");
    options.addOption("c", "command", true, "command");
    options.addOption("l", "processing-limit", true, "command");
    options.addOption("h", "help", false, "help");
  }

  private String inputFile;
  private String outputFile;
  private String propertyFile;
  private String entityFile;
  private boolean help = false;
  private Command command;
  private int processingLimit = 0;

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

    if (cmd.hasOption("processing-limit")) {
      processingLimit = Integer.parseInt(cmd.getOptionValue("processing-limit"));
    }

    if (cmd.hasOption("help"))
      help = true;
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

  public int getProcessingLimit() {
    return processingLimit;
  }

  public boolean showHelp() {
    return help;
  }

  public static Options getOptions() {
    return options;
  }

  @Override
  public String toString() {
    return "parameters: " + "\n" +
      "command='" + command + '\'' + "\n" +
      "input-file='" + inputFile + '\'' + "\n" +
      "output-file='" + outputFile + '\'' + "\n" +
      "property-file='" + propertyFile + '\'' + "\n" +
      "entity-file='" + entityFile + '\'' + "\n" +
      "processing-limit='" + processingLimit + '\'' + "\n" +
      "help=" + help + "\n";
  }
}