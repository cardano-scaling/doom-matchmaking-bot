package fi.sundae.bot.api;

public class MatchRequest {
  private String gameId;
  private String gameTxHash;
  private Competitor competitorOne;
  private Competitor competitorTwo;
  private MatchResult result;

  public MatchRequest() {}

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  public void setPlayerOne(Competitor competitorOne) {
    this.competitorOne = competitorOne;
  }

  public Competitor getPlayerOne() {
    return competitorOne;
  }

  public void setPlayerTwo(Competitor competitorTwo) {
    this.competitorTwo = competitorTwo;
  }

  public MatchResult getResult() {
    return result;
  }

  public void setResult(MatchResult result) {
    this.result = result;
  }

  public Competitor getPlayerTwo() {
    return competitorTwo;
  }

  public void setGameTxHash(String gameTxHash) {
    this.gameTxHash = gameTxHash;
  }

  public String getGameTxHash() {
    return gameTxHash;
  }
}
