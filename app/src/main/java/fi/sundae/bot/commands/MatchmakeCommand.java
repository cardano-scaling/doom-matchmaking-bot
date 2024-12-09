package fi.sundae.bot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import fi.sundae.bot.tournament.Match;
import fi.sundae.bot.tournament.Matchmaker;
import fi.sundae.bot.tournament.Region;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class MatchmakeCommand extends SlashCommand {
  private final String ADMIN_ROLE_ID;
  private final Matchmaker MATCHMAKER;

  public MatchmakeCommand(String adminRoleId, Matchmaker matchmaker) {
    this.ADMIN_ROLE_ID = adminRoleId;
    this.MATCHMAKER = matchmaker;
    this.name = "matchmake";
    this.help = "Force matchmaking for a region or for all regions";
    this.options = getCommandOptions();
  }

  @Override
  protected void execute(SlashCommandEvent event) {
    event.deferReply(true).queue();
    if (Objects.requireNonNull(event.getMember()).getRoles().stream()
        .noneMatch(role -> role.getId().equals(ADMIN_ROLE_ID))) {
      event.getHook().editOriginalEmbeds(getNotAllowedEmbed()).queue();
      return;
    }

    Optional<Region> maybeRegion =
        event.getOption("region") == null
            ? Optional.empty()
            : Optional.ofNullable(
                Region.fromRegionName(
                    Objects.requireNonNull(event.getOption("region")).getAsString()));

    maybeRegion.ifPresentOrElse(
        region -> {
          Optional<Match> maybeMatch = MATCHMAKER.buildMatch(region);
          maybeMatch.ifPresent(
              match -> MATCHMAKER.announceMatchesStart(List.of(match), event.getJDA()));
          event
              .getHook()
              .editOriginal(
                  String.format(
                      "Created a match if applicable for region %s", region.getPrettyName()))
              .queue();
        },
        () -> {
          List<Match> matches = MATCHMAKER.buildAllMatches();
          MATCHMAKER.announceMatchesStart(matches, event.getJDA());
          event.getHook().editOriginal("Created a match if applicable in every region").queue();
        });
  }

  private MessageEmbed getNotAllowedEmbed() {
    return new EmbedBuilder()
        .setColor(Color.RED)
        .setTitle("Permission Denied")
        .setDescription("Only a tournament " + "admin can use this " + "command.")
        .build();
  }

  private List<OptionData> getCommandOptions() {
    OptionData option =
        new OptionData(
            OptionType.STRING,
            "region",
            "Specify the region in which you'd like to " + "play a match");

    for (Region r : Region.values()) {
      option.addChoice(r.getPrettyName(), r.getRegionName());
    }

    return List.of(option);
  }
}
