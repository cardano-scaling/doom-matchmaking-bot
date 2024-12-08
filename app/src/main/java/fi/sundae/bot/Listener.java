package fi.sundae.bot;

import fi.sundae.bot.tournament.QualifierRepository;
import java.io.IOException;
import java.util.List;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener extends ListenerAdapter {
  private final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
  private final QualifierRepository QUALIFIER_REPOSITORY = new QualifierRepository();
  private final String QUALIFIED_ROLE_ID = "1315181000287846441";
  private final String FAN_ROLE_ID = "1315181766667010101";

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    LOGGER.info("guild member joined!");
    List<String> qualifiedUsers;
    try {
      qualifiedUsers = QUALIFIER_REPOSITORY.getQualifiedDiscordAccounts();
    } catch (IOException e) {
      LOGGER.warn("Failed to validate user {}, failing open", event.getMember().getId());
      return;
    }

    Role role =
        event
            .getGuild()
            .getRoleById(
                qualifiedUsers.contains(event.getMember().getId())
                    ? QUALIFIED_ROLE_ID
                    : FAN_ROLE_ID);
    if (role == null) {
      LOGGER.warn("role is null {} | QUALIFIED: {} | FAN: {}", qualifiedUsers.contains(event.getMember().getId()),
                  QUALIFIED_ROLE_ID, FAN_ROLE_ID);
      return;
    }

    event.getGuild().addRoleToMember(event.getMember().getUser(), role).queue();

    LOGGER.info("Gave role {} to {}", role.getId(), event.getMember().getId());
  }
}
