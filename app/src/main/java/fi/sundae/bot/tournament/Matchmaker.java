package fi.sundae.bot.tournament;

import fi.sundae.bot.api.MatchRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Matchmaker {

  private final QualifierRepository QUALIFIER_REPOSITORY = new QualifierRepository();
  private final ConcurrentHashMap<Region, List<String>> REGISTERED_USERS;

  private final List<Match> ACTIVE_MATCHES = new ArrayList<>();
  private final String CHANNEL_ID;
  private final Logger LOGGER = LoggerFactory.getLogger(Matchmaker.class);

  public Matchmaker(String channelId) {
    this.REGISTERED_USERS = new ConcurrentHashMap<>();
    for (Region r : Region.values()) {
      REGISTERED_USERS.put(r, new ArrayList<>());
    }
    this.CHANNEL_ID = channelId;
  }

  public List<Match> buildAllMatches() {
    LOGGER.info("building matches for all regions");
    List<Match> matches = new ArrayList<>();
    for (Region r : Region.values()) {
      buildMatch(r).ifPresent(matches::add);
    }

    return matches;
  }

  public void announceMatchesStart(List<Match> matches, JDA jda) {
    for (Match match : matches) {
      announceMatchStart(match, jda);
    }
  }

  public void announceMatchStart(Match match, JDA jda) {
    MessageCreateData msg =
        new MessageCreateBuilder()
            .addContent(match.toMessage())
            .addEmbeds(match.toNewEmbed())
            .build();
    TextChannel channel = Objects.requireNonNull(jda.getChannelById(TextChannel.class, CHANNEL_ID));
    channel.sendMessage(msg).queue();
    channel
        .createThreadChannel("Game ID: " + match.getCode(), true)
        .queue(
            thread -> {
              thread.addThreadMemberById(match.getPlayerOne()).queue();
              thread.addThreadMemberById(match.getPlayerTwo()).queue();
              thread
                  .sendMessageEmbeds(match.toConnectionEmbed())
                  .addActionRow(match.getConnectionButton())
                  .queue();
            });
  }

  public void endMatch(MatchRequest matchRequest, JDA jda) {
    Optional<Match> maybeMatch =
        ACTIVE_MATCHES.stream()
            .filter(activeMatch -> activeMatch.getCode().equals(matchRequest.getGameId()))
            .findFirst();

    if (maybeMatch.isEmpty()) return;
    Match match = maybeMatch.get();
    ACTIVE_MATCHES.remove(match);
    Optional<Player> maybePlayerA =
        QUALIFIER_REPOSITORY.getPlayerFromCompetitor(matchRequest.getPlayerOne());
    Optional<Player> maybePlayerB =
        QUALIFIER_REPOSITORY.getPlayerFromCompetitor(matchRequest.getPlayerTwo());
    if (maybePlayerA.isEmpty() || maybePlayerB.isEmpty()) return;
    Player playerA = maybePlayerA.get();

    if (playerA.getLinkedDiscordAccount().getId().equals(match.getPlayerOne()))
      announceMatchEnd(
          match,
          jda,
          matchRequest.getPlayerOne().getKillCount(),
          matchRequest.getPlayerTwo().getKillCount());
    else
      announceMatchEnd(
          match,
          jda,
          matchRequest.getPlayerTwo().getKillCount(),
          matchRequest.getPlayerOne().getKillCount());
  }

  public void announceMatchEnd(Match match, JDA jda, int playerOneKills, int playerTwoKills) {
    MessageCreateData msg =
        new MessageCreateBuilder()
            .addEmbeds(match.toEndEmbed(playerOneKills, playerTwoKills))
            .build();
    TextChannel channel = Objects.requireNonNull(jda.getChannelById(TextChannel.class, CHANNEL_ID));
    channel.sendMessage(msg).queue();
  }

  public Optional<Match> buildMatch(Region r) {
    List<String> users = new ArrayList<>(REGISTERED_USERS.get(r));
    LOGGER.info("building match for {} | Player count: {}", r.getRegionName(), users.size());
    if (users.size() < 2) {
      LOGGER.info("not enough players in {} for a match", r.getRegionName());
      return Optional.empty();
    }

    Collections.shuffle(users);
    String playerOne = users.get(0);
    String playerTwo = users.get(1);

    List<String> registeredUsers = REGISTERED_USERS.get(r);
    registeredUsers.remove(playerOne);
    registeredUsers.remove(playerTwo);

    LOGGER.info(
        "Built match for {} | Player one: {} | Player two: {}",
        r.getRegionName(),
        playerOne,
        playerTwo);

    try {
      Match match = new Match(playerOne, playerTwo, r);
      ACTIVE_MATCHES.add(match);

      return Optional.of(match);
    } catch (Exception e) {
      LOGGER.error("Failed to create match", e);
      return Optional.empty();
    }
  }

  public ConcurrentHashMap<Region, List<String>> getUsers() {
    return REGISTERED_USERS;
  }
}
