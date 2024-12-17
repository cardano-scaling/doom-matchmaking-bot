package fi.sundae.bot.tournament;

import fi.sundae.bot.api.MatchResult;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class MatchResultEmbed {
  private String playerOne;
  private String playerTwo;
  private String nodeId;
  private String gameId;
  private int playerOneKills, playerTwoKills;
  private MatchResult result;

  public MatchResultEmbed(MessageEmbed embed) {
    Color embedColor = embed.getColor();
    if (Color.RED.equals(embedColor)) {
      String title = Objects.requireNonNull(embed.getTitle()).toLowerCase();
      if (title.contains("disagreement")) result = MatchResult.DISAGREEMENT;
      else if (title.contains("disconnect")) result = MatchResult.DISCONNECT;
      else if (title.contains("timed out")) result = MatchResult.TIMEOUT;

      String description = Objects.requireNonNull(embed.getDescription());
      String regex = "<@(\\d+)>";

      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(description);
      List<String> discordIds;
      discordIds = new ArrayList<>();
      while (matcher.find()) {
        discordIds.add(matcher.group(1));
      }
      playerOne = discordIds.get(0);
      playerTwo = discordIds.get(1);
    } else result = MatchResult.FINISHED;

    List<MessageEmbed.Field> fields = embed.getFields();
    for (MessageEmbed.Field field : fields) {
      if ("Player 1".equals(field.getName())) {
        playerOne = Objects.requireNonNull(field.getValue()).replace("<@", "").replace(">", "");
      } else if ("Player 2".equals(field.getName())) {
        playerTwo = Objects.requireNonNull(field.getValue()).replace("<@", "").replace(">", "");
      } else if ("Node ID".equals(field.getName())) {
        nodeId = Objects.requireNonNull(field.getValue()).replace("`", "");
      } else if ("Game ID".equals(field.getName())) {
        gameId = Objects.requireNonNull(field.getValue()).replace("`", "");
      } else if ("Kill Counts".equals(field.getName())) {
        String regex = ":(\\s*\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(Objects.requireNonNull(field.getValue()));

        List<Integer> numbers = new ArrayList<>();
        while (matcher.find()) {
          int number = Integer.parseInt(matcher.group(1).trim());
          numbers.add(number);
        }

        playerOneKills = numbers.get(0);
        playerTwoKills = numbers.get(1);
      }
    }
  }

  public String getPlayerOne() {
    return playerOne;
  }

  public String getPlayerTwo() {
    return playerTwo;
  }

  public String getNodeId() {
    return nodeId;
  }

  public String getGameId() {
    return gameId;
  }

  public int getPlayerOneKills() {
    return playerOneKills;
  }

  public int getPlayerTwoKills() {
    return playerTwoKills;
  }

  public MatchResult getResult() {
    return result;
  }
}
