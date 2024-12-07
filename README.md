# Hydra DOOM Matchmaking Bot
This is a discord bot that facilitates the Hydra DOOM matchmaking for the elimination stage of the Hydra DOOM Tournament.

## Requirements
- Gradle 8.7+
- Java 21

## Discord Setup
In order to run the bot, you must have a bot token. To create a new bot and retrieve the token, head to [discord.com/developers/applications](https://diiscord.com/developers/applications) and click "New Application". Then, navigate to "Bot" on the sidebar and click "Reset Token".

## Expected .env
```
TOKEN=<Discord bot token>
OWNER_ID=<Discord user ID of admin>
CHANNEL_DI=<Discord channel ID of match announcement channel>
```