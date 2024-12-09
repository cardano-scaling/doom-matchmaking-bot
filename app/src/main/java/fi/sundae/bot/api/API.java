package fi.sundae.bot.api;

import fi.sundae.bot.tournament.Matchmaker;
import io.javalin.Javalin;
import net.dv8tion.jda.api.JDA;

public class API {

  public API(Matchmaker matchmaker, JDA jda) {
    Javalin.create().post("/match", new MatchHandler(matchmaker, jda)).start();
  }
}
