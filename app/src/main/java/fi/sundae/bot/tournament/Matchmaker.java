package fi.sundae.bot.tournament;

import fi.sundae.bot.api.MatchRequest;
import fi.sundae.bot.api.MatchResult;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Matchmaker {
  private final QualifierRepository QUALIFIER_REPOSITORY = new QualifierRepository();
  private final ConcurrentHashMap<Region, List<User>> REGISTERED_USERS;
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
      buildMatch(r, Optional.empty(), Optional.empty()).ifPresent(matches::add);
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
    TextChannel channel = jda.getTextChannelById(CHANNEL_ID);
    if (channel == null) {
      LOGGER.error("NULL channel for ID {}", CHANNEL_ID);
      return;
    }
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
              match.setThread(thread);
            });
  }

  public void endMatch(MatchRequest matchRequest, JDA jda) {
    Optional<Match> maybeMatch =
        ACTIVE_MATCHES.stream()
            .filter(activeMatch -> matchRequest.getGameId().equals(activeMatch.getGameTxHash()))
            .findFirst();

    if (maybeMatch.isEmpty()) {
      ACTIVE_MATCHES.forEach(match -> System.out.printf("Match gameTx: %s\n", match.getGameTxHash()));
      LOGGER.info("Match {} is not in ACTIVE_MATCHES", matchRequest.getGameId());
      return;
    }
    Match match = maybeMatch.get();
    ACTIVE_MATCHES.remove(match);
    match.getThread().delete().queue();

    if (matchRequest.getResult() == MatchResult.TIMEOUT) {
      LOGGER.info("Match {} is over due to timeout", matchRequest.getGameId());
      announceMatchTimeout(match, jda);
      return;
    } else if (matchRequest.getResult() == MatchResult.DISAGREEMENT) {
      LOGGER.info("Match {} is over due to disagreement", matchRequest.getGameId());
      announceMatchDisagreement(match, jda);
      return;
    } else if (matchRequest.getResult() == MatchResult.DISCONNECT) {
      LOGGER.info("Match {} is over due to disconnect", matchRequest.getGameId());
      announceMatchDisconnect(match, jda);
      return;
    }

    Optional<Player> maybePlayerA =
        QUALIFIER_REPOSITORY.getPlayerFromCompetitor(matchRequest.getPlayerOne());
    Optional<Player> maybePlayerB =
        QUALIFIER_REPOSITORY.getPlayerFromCompetitor(matchRequest.getPlayerTwo());
    if (maybePlayerA.isEmpty() || maybePlayerB.isEmpty()) {
      LOGGER.info(
          "At least one competitor is not a qualified player | maybePlayerA.isPresent: {} | maybePlayerB"
              + ".isPresent: {}",
          maybePlayerA.isPresent(),
          maybePlayerB.isPresent());
      return;
    }
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

  private void announceMatchEnd(Match match, JDA jda, int playerOneKills, int playerTwoKills) {
    MessageCreateData msg =
        new MessageCreateBuilder()
            .addEmbeds(match.toEndEmbed(playerOneKills, playerTwoKills))
            .build();
    TextChannel channel = Objects.requireNonNull(jda.getChannelById(TextChannel.class, CHANNEL_ID));
    channel.sendMessage(msg).queue();
  }

  private void announceMatchTimeout(Match match, JDA jda) {
    MessageCreateData msg = new MessageCreateBuilder().addEmbeds(match.toTimeoutEmbed()).build();
    TextChannel channel = Objects.requireNonNull(jda.getChannelById(TextChannel.class, CHANNEL_ID));
    channel.sendMessage(msg).queue();
  }

  private void announceMatchDisagreement(Match match, JDA jda) {
    MessageCreateData msg =
        new MessageCreateBuilder().addEmbeds(match.toDisagreementEmbed()).build();
    TextChannel channel = Objects.requireNonNull(jda.getChannelById(TextChannel.class, CHANNEL_ID));
    channel.sendMessage(msg).queue();
  }

  private void announceMatchDisconnect(Match match, JDA jda) {
    MessageCreateData msg = new MessageCreateBuilder().addEmbeds(match.toDisconnectEmbed()).build();
    TextChannel channel = Objects.requireNonNull(jda.getChannelById(TextChannel.class, CHANNEL_ID));
    channel.sendMessage(msg).queue();
  }

  public Optional<Match> buildMatch(
      Region r, Optional<User> maybePlayerOne, Optional<User> maybePlayerTwo) {
    List<User> users = new ArrayList<>(REGISTERED_USERS.get(r));
    LOGGER.info("building match for {} | Player count: {}", r.getRegionName(), users.size());
    if (users.size() < 2) {
      LOGGER.info("not enough players in {} for a match", r.getRegionName());
      return Optional.empty();
    }

    Collections.shuffle(users);
    User playerOne = maybePlayerOne.orElse(users.get(0));
    User playerTwo = maybePlayerTwo.orElse(users.get(1));

    List<User> registeredUsers = REGISTERED_USERS.get(r);
    registeredUsers.remove(playerOne);
    registeredUsers.remove(playerTwo);

    LOGGER.info(
        "Built match for {} | Player one: {} | Player two: {}",
        r.getRegionName(),
        playerOne,
        playerTwo);

    try {
      Match match = new Match(playerOne.getId(), playerTwo.getId(), r);
      ACTIVE_MATCHES.add(match);

      return Optional.of(match);
    } catch (Exception e) {
      LOGGER.error("Failed to create match", e);
      return Optional.empty();
    }
  }

  public ConcurrentHashMap<Region, List<User>> getUsers() {
    return REGISTERED_USERS;
  }

  public HashMap<Region, List<String>> getUsernames() {
    HashMap<Region, List<String>> userNamesInRegions = new HashMap<>();
    var regions = REGISTERED_USERS.keys();
    Region region = regions.nextElement();
    while (regions.hasMoreElements()) {
      List<String> usernames =
          REGISTERED_USERS.get(region).stream()
              .map(User::getName)
              .collect(Collectors.toCollection(ArrayList::new));
      userNamesInRegions.put(region, usernames);
      region = regions.nextElement();
    }

    return userNamesInRegions;
  }
}
