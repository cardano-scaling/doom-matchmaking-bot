package fi.sundae.bot.tournament;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Matchmaker {
  private final ConcurrentHashMap<Region, List<String>> REGISTERED_USERS;
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

  public void announceMatches(List<Match> matches, JDA jda) {
    for (Match match : matches) {
      MessageCreateData msg =
          new MessageCreateBuilder()
              .addContent(match.toMessage())
              .addEmbeds(match.toMessageEmbed())
              .build();
      Objects.requireNonNull(jda.getChannelById(TextChannel.class, CHANNEL_ID))
          .sendMessage(msg)
          .queue();
    }
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

    return Optional.of(new Match(playerOne, playerTwo, r));
  }

  public ConcurrentHashMap<Region, List<String>> getUsers() {
    return REGISTERED_USERS;
  }
}
