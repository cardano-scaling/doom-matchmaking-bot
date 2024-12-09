package fi.sundae.bot;

import fi.sundae.bot.api.API;
import fi.sundae.bot.tournament.Matchmaker;
import fi.sundae.bot.tournament.Player;
import fi.sundae.bot.tournament.QualifierRepository;
import java.io.IOException;
import java.util.List;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener extends ListenerAdapter {
  private final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
  private final Matchmaker MATCHMAKER;
  private final QualifierRepository QUALIFIER_REPOSITORY = new QualifierRepository();
  private final String QUALIFIED_ROLE_ID = "1315181000287846441";
  private final String FAN_ROLE_ID = "1315181766667010101";

  public Listener(Matchmaker matchmaker) {
    this.MATCHMAKER = matchmaker;
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {
    LOGGER.info("Bot ready! Initializing API...");
    new API(this.MATCHMAKER, event.getJDA());
  }

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    LOGGER.info("guild member joined!");
    List<Player> qualifiedUsers;
    try {
      qualifiedUsers = QUALIFIER_REPOSITORY.getQualifiers();
    } catch (IOException e) {
      LOGGER.warn("Failed to validate user {}, failing open", event.getMember().getId());
      return;
    }

    Role role =
        event
            .getGuild()
            .getRoleById(
                qualifiedUsers.stream()
                        .anyMatch(
                            player ->
                                event
                                    .getMember()
                                    .getId()
                                    .equals(player.getLinkedDiscordAccount().getId()))
                    ? QUALIFIED_ROLE_ID
                    : FAN_ROLE_ID);
    if (role == null) {
      return;
    }

    event.getGuild().addRoleToMember(event.getMember().getUser(), role).queue();
    LOGGER.info("Gave role {} to {}", role.getId(), event.getMember().getId());
  }
}
