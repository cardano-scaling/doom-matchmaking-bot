package fi.sundae.bot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import fi.sundae.bot.tournament.Matchmaker;
import fi.sundae.bot.tournament.QualifierRepository;
import fi.sundae.bot.tournament.Region;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RegisterCommand extends SlashCommand {

  private final Matchmaker MATCHMAKER;
  private final QualifierRepository QUALIFIER_REPOSITORY = new QualifierRepository();

  public RegisterCommand(Matchmaker matchmaker) {
    this.MATCHMAKER = matchmaker;
    this.name = "register";
    this.help = "Toggle your availability to play a match in a specified region";
    this.options = getCommandOptions();
  }

  @Override
  protected void execute(SlashCommandEvent event) {
    event.deferReply(true).queue();

    Member member = Objects.requireNonNull(event.getMember());
    List<String> qualifiedUsers;
    try {
      qualifiedUsers = QUALIFIER_REPOSITORY.getQualifiedDiscordAccounts();
    } catch (IOException e) {
      event.getHook().editOriginalEmbeds(getErrorEmbed()).queue();
      return;
    }

    if (qualifiedUsers.contains(member.getId())) {
      event.getHook().editOriginalEmbeds(getPermissionDeniedEmbed()).queue();
      return;
    }

    OptionMapping regionOption = Objects.requireNonNull(event.getOption("region"));
    Region region = Objects.requireNonNull(Region.fromRegionName(regionOption.getAsString()));

    List<String> usersInRegion = MATCHMAKER.getUsers().get(region);

    if (!usersInRegion.contains(member.getId())) {
      usersInRegion.add(member.getId());
      MATCHMAKER.getUsers().put(region, usersInRegion);
      event.getHook().editOriginalEmbeds(getRegisteredEmbed(region.getPrettyName())).queue();
      return;
    }

    usersInRegion.remove(member.getId());
    event.getHook().editOriginalEmbeds(getDeregisteredEmbed(region.getPrettyName())).queue();
  }

  private MessageEmbed getRegisteredEmbed(String region) {
    return new EmbedBuilder()
        .setColor(Color.GREEN)
        .setTitle("Successfully Registered")
        .setDescription(
            """
                You have successfully registered for an elimination match!

                You will be pinged if you are selected for a match. Please be ready and available""")
        .addField("Region", region, true)
        .build();
  }

  private MessageEmbed getDeregisteredEmbed(String region) {
    return new EmbedBuilder()
        .setColor(Color.GREEN)
        .setTitle("Successfully Deregistered")
        .setDescription(
            "You have successfully deregistered for an elimination match. Thank you for playing!")
        .addField("Region", region, true)
        .build();
  }

  private MessageEmbed getPermissionDeniedEmbed() {
    return new EmbedBuilder()
        .setTitle("Permission Denied")
        .setColor(Color.RED)
        .setDescription(
            """
            You must have a connected qualified account in order to register for a match. You do not have a connected qualified account.

            If you believe this is an error, please contact tournament staff.""")
        .build();
  }

  private MessageEmbed getErrorEmbed() {
    return new EmbedBuilder()
        .setTitle("Oops... Something went wrong!")
        .setColor(Color.RED)
        .setDescription(
            "There was an error processing your request. Please try again shortly or contact tournament staff"
                + " for help.")
        .build();
  }

  private List<OptionData> getCommandOptions() {
    OptionData option =
        new OptionData(
                OptionType.STRING,
                "region",
                "Specify the region in which you'd like to " + "play a match")
            .setRequired(true);

    for (Region r : Region.values()) {
      option.addChoice(r.getPrettyName(), r.getRegionName());
    }

    return List.of(option);
  }
}
