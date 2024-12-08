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

public class Bot {

  private final Matchmaker MATCHMAKER;

  public Bot(String token, String ownerId, String channelId) {
    this.MATCHMAKER = new Matchmaker(channelId);
    JDABuilder jdaBuilder = JDABuilder.createDefault(token);
    CommandClientBuilder commandBuilder =
        new CommandClientBuilder()
            .setActivity(Activity.competing("Hydra DOOM"))
            .setOwnerId(ownerId)
            .setStatus(OnlineStatus.ONLINE)
            .addSlashCommands(
                    new ReadyCommand(MATCHMAKER), new MatchmakeCommand(ownerId, MATCHMAKER));
    jdaBuilder.addEventListeners(commandBuilder.build());

    JDA jda = jdaBuilder.build();
    scheduleMatchmaking(jda);
  }

  private void scheduleMatchmaking(JDA jda) {
    try(ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()) {
      executor.scheduleAtFixedRate(
              () -> {
                List<Match> matches = MATCHMAKER.buildAllMatches();
                MATCHMAKER.announceMatches(matches, jda);
              },
              0,
              10,
              TimeUnit.MINUTES);
    }
  }
}
