package fi.sundae.bot.api;

public enum MatchResult {
  TIMEOUT("timeout"),
  FINISHED("finished"),
  DISAGREEMENT("disagreement"),
  DISCONNECT("disconnect");

  private final String SERIALIZED_NAME;

  MatchResult(String serializedName) {
    this.SERIALIZED_NAME = serializedName;
  }

  public String getSerializedName() {
    return SERIALIZED_NAME;
  }
}
