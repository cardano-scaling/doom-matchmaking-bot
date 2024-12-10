package fi.sundae.bot.api;

import fi.sundae.bot.tournament.Matchmaker;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class ReadyPlayersHandler implements Handler {
  private final Matchmaker MATCHMAKER;

  public ReadyPlayersHandler(Matchmaker matchmaker) {
    MATCHMAKER = matchmaker;
  }

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    ctx.json(MATCHMAKER.getUsernames());
  }
}
