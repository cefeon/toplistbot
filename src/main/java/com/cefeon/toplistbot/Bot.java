package com.cefeon.toplistbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.*;


public class Bot extends ListenerAdapter {
    private final Logger logger = LogManager.getLogger("logger");

    private final String botToken;

    ArrayList<String> blackList = new ArrayList<>();

    public Bot(String botToken) {
        if (botToken == null || botToken.isEmpty()) {
            throw new IllegalArgumentException("BOT_TOKEN environmental variable provided too short.");
        }
        this.botToken = botToken;
    }

    public void start() throws LoginException {
        JDABuilder
                .createLight(botToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot(botToken))
                .setActivity(Activity.playing("Game of bot"))
                .build();
        logger.info("Bot started");
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return; //Avoid infinite bot message loop
        String user = event.getAuthor().getAsMention();
        MessageChannel channel = event.getChannel();
        channel.sendMessage("Test zakonczony sukcesem " + user + "!").queue();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return; //Avoid infinite bot message loop
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();

        CommandFactory commandFactory = new CommandFactory();
        Map<String, Command> commandList = commandFactory.getCommandMap();

        if (commandList.containsKey(message.getContentRaw().split(" ")[0])){
            Command targetCommand = commandFactory.getCommand(message.getContentRaw().split(" ")[0]);
            targetCommand.execute(channel, message, blackList);
        }
    }
}
