package de.gwdg.metadataqa.wikidata.json;

import de.gwdg.metadataqa.wikidata.model.PageNumberErrorType;

public class InvalidPageNumberException extends RuntimeException {
  PageNumberErrorType type;
  String shortened;

  public InvalidPageNumberException(String message, PageNumberErrorType type) {
    super(message);
    this.type = type;
  }

  public InvalidPageNumberException(String message, PageNumberErrorType type, String shortened) {
    super(message);
    this.type = type;
    this.shortened = shortened;
  }

  public PageNumberErrorType getType() {
    return type;
  }

  public String getShortened() {
    return shortened;
  }
}
