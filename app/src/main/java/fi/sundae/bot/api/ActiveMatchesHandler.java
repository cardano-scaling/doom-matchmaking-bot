package fi.sundae.bot.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.sundae.bot.tournament.Match;
import fi.sundae.bot.tournament.Matchmaker;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class ActiveMatchesHandler implements Handler {

  private final Matchmaker MATCHMAKER;

  public ActiveMatchesHandler(Matchmaker matchmaker) {
    this.MATCHMAKER = matchmaker;
  }

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    Gson gson = new GsonBuilder().registerTypeAdapter(Match.class, new Match.MatchSerializer()).create();
    ctx.json(gson.toJson(MATCHMAKER.getActiveMatches()));
  }
}
