package fi.sundae.bot.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

  public List<String> getQualifiedDiscordAccounts() throws IOException {
    Optional<String> maybeBody = fetchQualifiers();
    if (maybeBody.isEmpty()) {
      throw new IOException("Failed to fetch qualifier data");
    }

    return parseAndFilterQualifiersResponseBody(maybeBody.get());
  }

  private List<String> parseAndFilterQualifiersResponseBody(String body) {
    List<String> discordIds = new ArrayList<>();
    try {
      JsonArray json = JsonParser.parseString(body).getAsJsonArray();
      for (JsonElement elem : json) {
        try {
          JsonObject user = elem.getAsJsonObject();
          if (user.get("linked_discord_account").isJsonNull()) {
            continue;
          }
          JsonObject discordObject = user.get("linked_discord_account").getAsJsonObject();
          String discordId = discordObject.get("id").getAsString();
          discordIds.add(discordId);
        } catch (IllegalStateException e) {
          LOGGER.info("Invalid JSON formatting for a qualifier: {}", elem, e);
        }
      }
    } catch (IllegalStateException e) {
      LOGGER.info("Invalid JSON formatting", e);
    }

    return discordIds;
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
}
