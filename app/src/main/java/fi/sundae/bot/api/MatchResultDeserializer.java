package fi.sundae.bot.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class MatchResultDeserializer implements JsonDeserializer<MatchResult> {
  @Override
  public MatchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String value = json.getAsString();
    for (MatchResult matchResult : MatchResult.values()) {
      if (matchResult.name().equalsIgnoreCase(value)
          || matchResult.getSerializedName().equals(value)) {
        return matchResult;
      }
    }
    throw new JsonParseException("Unknown MatchResult value: " + value);
  }
}
