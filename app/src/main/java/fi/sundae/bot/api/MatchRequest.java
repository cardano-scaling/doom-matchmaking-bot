package fi.sundae.bot.api;

public class MatchRequest {
  private String gameId;
  private Competitor competitorOne;
  private Competitor competitorTwo;

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

  public Competitor getPlayerTwo() {
    return competitorTwo;
  }
}
