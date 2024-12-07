package fi.sundae.bot.tournament;

import java.awt.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Match {
  private final String PLAYER_ONE;
  private final String PLAYER_TWO;
  private final Region REGION;

  public Match(String playerOne, String playerTwo, Region region) {
    this.PLAYER_ONE = playerOne;
    this.PLAYER_TWO = playerTwo;
    this.REGION = region;
  }

  public String toMessage() {
    return String.format("Hey <@%s> and <@%s>, get ready to FIGHT!", PLAYER_ONE, PLAYER_TWO);
  }

  public MessageEmbed toMessageEmbed() {
    return new EmbedBuilder()
        .setColor(Color.GREEN)
        .setTitle("New Match!")
        .setDescription(
            """
             A new match up has been created!

             Player 1, please create a new game in the specified region and share the link with Player 2.""")
        .addField("Player 1", String.format("<@%s>", PLAYER_ONE), false)
        .addField("Player 2", String.format("<@%s>", PLAYER_TWO), false)
        .addField("Region", REGION.getPrettyName(), false)
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
}
