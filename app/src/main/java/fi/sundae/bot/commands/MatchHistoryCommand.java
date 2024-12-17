package fi.sundae.bot.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import fi.sundae.bot.api.MatchResult;
import fi.sundae.bot.api.MatchResultSerializer;
import fi.sundae.bot.tournament.MatchResultEmbed;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchHistoryCommand extends SlashCommand {

  private final String CHANNEL_ID;
  private final String ADMIN_ROLE_ID;
  private final Logger LOGGER = LoggerFactory.getLogger(MatchHistoryCommand.class);

  public MatchHistoryCommand(String channelId, String adminRoleId) {
    this.CHANNEL_ID = channelId;
    this.ADMIN_ROLE_ID = adminRoleId;
    this.name = "match_history";
    this.help = "View parsed match history in JSON";
  }

  @Override
  protected void execute(SlashCommandEvent event) {
    event.deferReply(true).queue();
    if (Objects.requireNonNull(event.getMember()).getRoles().stream()
        .noneMatch(role -> role.getId().equals(ADMIN_ROLE_ID))) {
      event.getHook().editOriginalEmbeds(getNotAllowedEmbed()).queue();
      return;
    }

    LOGGER.info("Fetching match history...");
    TextChannel channel = event.getJDA().getTextChannelById(CHANNEL_ID);
    fetchAllMessages(channel)
        .thenAccept(
            messages -> {
              LOGGER.info("Parsing messages");
              try {
                List<MatchResultEmbed> matchResults = new ArrayList<>();
                for (Message message : messages) {
                  if (!message.getContentRaw().isEmpty() || message.getEmbeds().isEmpty()) {
                    continue;
                  }

                  MessageEmbed embed = message.getEmbeds().get(0);
                  matchResults.add(new MatchResultEmbed(embed));
                  LOGGER.info("Finished parsing message | message ID: {}", message.getId());
                }

                Gson gson =
                    new GsonBuilder()
                        .registerTypeAdapter(MatchResult.class, new MatchResultSerializer())
                        .create();

                LOGGER.info("Building JSON");
                String json = gson.toJson(matchResults);
                String fileName = "results.json";
                try {
                  File file = new File(fileName);
                  try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json);
                  }

                  event
                      .getHook()
                      .editOriginal("See attached for results")
                      .setFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(file))
                      .queue();
                  file.delete();
                } catch (IOException e) {
                  LOGGER.error("Failed to create file", e);
                  event
                      .getHook()
                      .editOriginal("Failed to create file to share here. Check logs for more info")
                      .queue();
                }

              } catch (Exception e) {
                LOGGER.error("Failed parsing messages", e);
                event
                    .getHook()
                    .editOriginal(
                        "There was an error fetching message history. Check the logs for more "
                            + "info")
                    .queue();
              }
            });
  }

  public CompletableFuture<List<Message>> fetchAllMessages(TextChannel channel) {
    CompletableFuture<List<Message>> future = new CompletableFuture<>();
    List<Message> messages = new ArrayList<>();

    LOGGER.info("Fetching all messages");
    fetchMessagesBefore(channel, null, messages, future);
    return future;
  }

  private void fetchMessagesBefore(
      TextChannel channel,
      String lastMessageId,
      List<Message> messages,
      CompletableFuture<List<Message>> future) {
    LOGGER.info("Fetching messages | last message id: {}", lastMessageId);
    int fetchLimit = 100; // limit imposed by Discord
    if (lastMessageId == null) {

      channel
          .getHistory()
          .retrievePast(fetchLimit)
          .queue(
              retrievedMessages -> {
                List<Message> filteredMessages =
                    retrievedMessages.stream()
                        .filter(
                            message -> channel.getJDA().getSelfUser().equals(message.getAuthor()))
                        .collect(Collectors.toCollection(ArrayList::new));
                messages.addAll(filteredMessages);

                if (filteredMessages.size() < fetchLimit) {
                  future.complete(messages);
                } else {
                  String oldestMessageId =
                      retrievedMessages.get(retrievedMessages.size() - 1).getId();
                  fetchMessagesBefore(channel, oldestMessageId, messages, future);
                }
              },
              error -> {
                LOGGER.error("failed to fetch messages", error);
                future.completeExceptionally(error);
              });
    } else {
      channel
          .getHistoryBefore(lastMessageId, fetchLimit)
          .queue(
              history -> {
                List<Message> retrievedMessages = history.getRetrievedHistory();
                messages.addAll(retrievedMessages);

                if (retrievedMessages.size() < fetchLimit) {
                  LOGGER.info("Retrieved all messages. Completing future...");
                  future.complete(messages);
                } else {
                  String oldestMessageId =
                      retrievedMessages.get(retrievedMessages.size() - 1).getId();
                  fetchMessagesBefore(channel, oldestMessageId, messages, future);
                }
              },
              error -> {
                LOGGER.error("failed to fetch messages", error);
                future.completeExceptionally(error);
              });
    }
  }

  private MessageEmbed getNotAllowedEmbed() {
    return new EmbedBuilder()
        .setColor(Color.RED)
        .setTitle("Permission Denied")
        .setDescription("Only a tournament admin can use this command.")
        .build();
  }
}
