package fi.sundae.bot.api;

import com.google.gson.Gson;
import fi.sundae.bot.tournament.Matchmaker;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MatchHandler implements Handler {

  private final Logger LOGGER = LoggerFactory.getLogger(MatchHandler.class);
  private final Matchmaker MATCHMAKER;
  private final JDA JDA;

  public MatchHandler(Matchmaker matchmaker, JDA jda) {
    this.MATCHMAKER = matchmaker;
    this.JDA = jda;
  }

  @Override
  public void handle(@NotNull Context ctx) {
    LOGGER.info("received a new request: {}", ctx.body());

    Gson gson = new Gson();
    MatchRequest request = gson.fromJson(ctx.body(), MatchRequest.class);
    MATCHMAKER.endMatch(request, JDA);
  }
}
