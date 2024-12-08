package fi.sundae.bot;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
  public static void main(String[] args) {
    Dotenv env = Dotenv.configure().ignoreIfMissing().load();
    String token = env.get("TOKEN");
    String ownerId = env.get("OWNER_ID");
    String channelId = env.get("CHANNEL_ID");
    String adminRoleId = env.get("ADMIN_ROLE_ID");

    new Bot(token, channelId, ownerId, adminRoleId);
  }
}
