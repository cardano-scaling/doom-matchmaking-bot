package fi.sundae.bot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import fi.sundae.bot.commands.MatchmakeCommand;
import fi.sundae.bot.commands.ReadyCommand;
import fi.sundae.bot.tournament.Match;
import fi.sundae.bot.tournament.Matchmaker;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {

  private final Matchmaker MATCHMAKER;

  public Bot(String token, String ownerId, String channelId, String adminRoleId) {
    this.MATCHMAKER = new Matchmaker(channelId);
    JDABuilder jdaBuilder =
        JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);

    CommandClientBuilder commandBuilder =
        new CommandClientBuilder()
            .setActivity(Activity.competing("Hydra DOOM"))
            .setOwnerId(ownerId)
            .setStatus(OnlineStatus.ONLINE)
            .addSlashCommands(
                new ReadyCommand(MATCHMAKER), new MatchmakeCommand(adminRoleId, MATCHMAKER));
    jdaBuilder.addEventListeners(commandBuilder.build(), new Listener(MATCHMAKER));

    JDA jda = jdaBuilder.build();
    scheduleMatchmaking(jda);
  }

  private void scheduleMatchmaking(JDA jda) {
    try (ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()) {
      executor.scheduleAtFixedRate(
          () -> {
            List<Match> matches = MATCHMAKER.buildAllMatches();
            MATCHMAKER.announceMatchesStart(matches, jda);
          },
          0,
          5,
          TimeUnit.MINUTES);
    }
  }
}
