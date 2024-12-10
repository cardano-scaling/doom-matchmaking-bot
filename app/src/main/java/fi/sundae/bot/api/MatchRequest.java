package fi.sundae.bot.api;

public class MatchRequest {
  private String gameId;
  private Competitor playerOne;
  private Competitor playerTwo;
  private MatchResult result;

  public MatchRequest() {}

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  public void setPlayerOne(Competitor playerOne) {
    this.playerOne = playerOne;
  }

  public Competitor getPlayerOne() {
    return playerOne;
  }

  public void setPlayerTwo(Competitor playerTwo) {
    this.playerTwo = playerTwo;
  }

  public MatchResult getResult() {
    return result;
  }

  public void setResult(MatchResult result) {
    this.result = result;
  }

  public Competitor getPlayerTwo() {
    return playerTwo;
  }
}
