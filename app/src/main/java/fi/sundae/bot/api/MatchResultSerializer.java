package fi.sundae.bot.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class MatchResultSerializer implements JsonSerializer<MatchResult> {
  @Override
  public JsonElement serialize(MatchResult src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.getSerializedName());
  }
}
