package fi.sundae.bot.tournament;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Match {
  private final String PLAYER_ONE;
  private final String PLAYER_TWO;
  private final Region REGION;
  private final String CODE;
  private ThreadChannel thread;
  private String gameTxHash;

  public Match(String playerOne, String playerTwo, Region region)
      throws URISyntaxException, IOException, InterruptedException {
    this.PLAYER_ONE = playerOne;
    this.PLAYER_TWO = playerTwo;
    this.REGION = region;
    var newGameData = requestNewGame();
    assert newGameData != null;
    this.CODE = newGameData[0];
    this.gameTxHash = newGameData[1];
  }

  private String[] requestNewGame() throws URISyntaxException, IOException, InterruptedException {
    try (HttpClient client = HttpClient.newHttpClient()) {
      String url =
          "https://api.%s.hydra-doom.sundae.fi/elimination".formatted(REGION.getRegionName());
      HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        String gameId = body.get("game_id").getAsString();
        String gameTxHash = body.get("game_tx_hash").getAsString();

        return new String[] {gameId, gameTxHash};
      }

      System.out.printf("Request to %s failed with status code: %s%n", url, response.statusCode());

      return null;
    }
  }

  public String toMessage() {
    return String.format("Hey <@%s> and <@%s>, get ready to FIGHT!", PLAYER_ONE, PLAYER_TWO);
  }

  public MessageEmbed toConnectionEmbed() {
    return new EmbedBuilder()
        .setColor(Color.GREEN)
        .setTitle("Connection Information")
        .setDescription("Your match is ready for you! Click the button below to join the game.")
        .addField("Node ID", "`%s`".formatted(this.CODE), true)
        .addField("Game ID", "`%s`".formatted(this.gameTxHash), true)
        .build();
  }

  public MessageEmbed toTimeoutEmbed() {
    return new EmbedBuilder()
        .setColor(Color.RED)
        .setTitle("Match Timed Out")
        .setDescription(
            """
            The match between <@%s> and <@%s> has failed to complete. Any kills during the match have been discarded
            """
                .formatted(getPlayerOne(), getPlayerTwo()))
        .addField("Node ID", "`%s`".formatted(this.CODE), true)
        .addField("Game ID", "`%s`".formatted(this.gameTxHash), true)
        .build();
  }

  public MessageEmbed toDisagreementEmbed() {
    return new EmbedBuilder()
        .setColor(Color.RED)
        .setTitle("Match Disagreement")
        .setDescription(
            """
                    The match between <@%s> and <@%s> has failed due to a disagreement in state. Any kills during the match have been discarded
                    """
                .formatted(getPlayerOne(), getPlayerTwo()))
        .addField("Game ID", "`%s`".formatted(this.CODE), true)
        .addField("Game ID", "`%s`".formatted(this.gameTxHash), true)
        .build();
  }

  public MessageEmbed toDisconnectEmbed() {
    return new EmbedBuilder()
        .setColor(Color.RED)
        .setTitle("Match Disconnect")
        .setDescription(
            """
                            The match between <@%s> and <@%s> has failed due to a player disconnect. Any kills during the match have been discarded
                            """
                .formatted(getPlayerOne(), getPlayerTwo()))
        .addField("Game ID", "`%s`".formatted(this.CODE), true)
        .addField("Game ID", "`%s`".formatted(this.gameTxHash), true)
        .build();
  }

  public MessageEmbed toEndEmbed(int playerOneKills, int playerTwoKills) {
    String description;
    if (playerOneKills > playerTwoKills)
      description =
          """
                                                      Game over! <@%s> has defeated <@%s>
                                                      """
              .formatted(getPlayerOne(), getPlayerTwo());
    else if (playerTwoKills > playerOneKills)
      description =
          """
                                                            Game over! <@%s> has defeated <@%s>
                                                            """
              .formatted(getPlayerTwo(), getPlayerOne());
    else
      description =
          """
                       Game over! <@%s> and <@%s> are evenly matched and no one won
                       """
              .formatted(getPlayerOne(), getPlayerTwo());

    return new EmbedBuilder()
        .setColor(Color.GREEN)
        .setTitle("Match Over")
        .setDescription(description)
        .addField(
            "Kill Counts",
            """
                                                     <@%s>: `%s`
                                                     <@%s>: `%s`
                                                     """
                .formatted(getPlayerOne(), playerOneKills, getPlayerTwo(), playerTwoKills),
            false)
        .addField("Node ID", "`%s`".formatted(this.CODE), true)
        .addField("Game ID", "`%s`".formatted(this.gameTxHash), true)
        .build();
  }

  public String getJoinLink() {
    return "https://doom.hydra.family/#/join/%s".formatted(this.CODE);
  }

  public Button getConnectionButton() {
    return Button.link(getJoinLink(), "Join Game");
  }

  public MessageEmbed toNewEmbed() {
    return new EmbedBuilder()
        .setColor(Color.GREEN)
        .setTitle("New Match!")
        .setDescription(
            """
             A new match has been created!

             A private thread will be created with the connection information. Please join the game promptly!""")
        .addField("Player 1", String.format("<@%s>", PLAYER_ONE), false)
        .addField("Player 2", String.format("<@%s>", PLAYER_TWO), false)
        .addField("Region", REGION.getPrettyName(), false)
        .addField("Game ID", String.format("`%s`", gameTxHash), false)
        .build();
  }

  public String getPlayerOne() {
    return PLAYER_ONE;
  }

  public String getPlayerTwo() {
    return PLAYER_TWO;
  }

  public Region getRegion() {
    return REGION;
  }

  public String getCode() {
    return CODE;
  }

  public void setThread(ThreadChannel thread) {
    this.thread = thread;
  }

  public ThreadChannel getThread() {
    return this.thread;
  }

  public void setGameTxHash(String gameTxHash) {
    this.gameTxHash = gameTxHash;
  }

  public String getGameTxHash() {
    return gameTxHash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;

    Match other = (Match) obj;
    return other.CODE.equals(this.CODE)
        && other.PLAYER_ONE.equals(this.PLAYER_ONE)
        && other.PLAYER_TWO.equals(this.PLAYER_TWO)
        && other.REGION == this.REGION;
  }

  @Override
  public int hashCode() {
    int hash = CODE.hashCode();
    hash = 31 * hash + PLAYER_ONE.hashCode();
    hash = 31 * hash + PLAYER_TWO.hashCode();
    hash = 31 * hash + REGION.hashCode();

    return hash;
  }
}
