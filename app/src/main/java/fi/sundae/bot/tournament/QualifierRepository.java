package fi.sundae.bot.tournament;

import com.google.gson.*;
import fi.sundae.bot.api.Competitor;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QualifierRepository {
  private final Logger LOGGER = LoggerFactory.getLogger(QualifierRepository.class);

  public List<Player> getQualifiers() throws IOException {
    Optional<String> maybeBody = fetchQualifiers();
    if (maybeBody.isEmpty()) {
      throw new IOException("Failed to fetch qualifier data");
    }

    return parseAndFilterQualifiersResponseBody(maybeBody.get());
  }

  public Optional<Player> getPlayerFromCompetitor(Competitor competitor) {
    return fetchPlayer(competitor.getPkh());
  }

  private List<Player> parseAndFilterQualifiersResponseBody(String body) {
    List<Player> players = new ArrayList<>();
    Gson gson = new Gson();
    try {
      JsonArray json = JsonParser.parseString(body).getAsJsonArray();
      for (JsonElement elem : json) {
        try {
          Player player = gson.fromJson(elem, Player.class);
          if (player.getLinkedDiscordAccount() == null) {
            continue;
          }
          players.add(player);
        } catch (IllegalStateException e) {
          LOGGER.info("Invalid JSON formatting for a qualifier: {}", elem, e);
        }
      }
    } catch (IllegalStateException e) {
      LOGGER.info("Invalid JSON formatting", e);
    }

    return players;
  }

  private Optional<String> fetchQualifiers() {
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(
                  new URI(
                      "https://rewardengine.dripdropz.io/api/v1/stats/leaderboard/d93212b3-dbdc-40d0-befd-f90508c6232d/qualifiers"))
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return Optional.of(response.body());
      }

      LOGGER.error("Failed to fetch qualifiers: Invalid status {}", response.statusCode());
      return Optional.empty();
    } catch (URISyntaxException e) {
      LOGGER.error("Failed to fetch qualifiers: Invalid URI syntax", e);
      return Optional.empty();

    } catch (IOException e) {
      LOGGER.error("Failed to fetch qualifiers: IO Exception", e);
      return Optional.empty();
    } catch (InterruptedException e) {
      LOGGER.error("Failed to fetch qualifiers: Interrupted Exception", e);
      return Optional.empty();
    }
  }

  private Optional<Player> fetchPlayer(String reference) {
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(
                  new URI(
                      "https://rewardengine.dripdropz"
                          + ".io/api/v1/auth/info/d93212b3-dbdc-40d0-befd-f90508c6232d/?reference=%s"
                              .formatted(reference)))
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        Gson gson = new Gson();
        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        Player player = gson.fromJson(body.get("account"), Player.class);
        return Optional.of(player);
      }

      LOGGER.info("None 200 status code when fetching player. Status: {}", response.statusCode());
      return Optional.empty();
    } catch (URISyntaxException | IOException | InterruptedException e) {
      LOGGER.error("Failed to fetch player", e);
      return Optional.empty();
    }
  }
}
