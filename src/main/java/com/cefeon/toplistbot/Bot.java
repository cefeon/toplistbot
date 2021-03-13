package com.cefeon.toplistbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
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

    private void addChatCommand(Message message, MessageChannel channel, String command, String response) {
        if (message.getContentRaw().equals(command)) {
            channel.sendMessage(response).queue();
        }
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
        addChatCommand(message, channel, "!test", "test zakonczony sukcesem");

        List<Message> history = channelHistoryToList(channel, message);
        ArrayList<String> contents = new ArrayList<>();
        history.forEach(x->contents.add(x.getContentRaw()));

        TreeMap<Integer,String> sorted = sortByOccurances(contents);
        sorted.forEach((x,y)->logger.info(x+":"+y));
    }

    private TreeMap<Integer, String> sortByOccurances(List<String> contents){
        Map<Integer,String> frequencies = countFrequencies(contents);
        TreeMap<Integer, String> sorted = new TreeMap<>(frequencies);
        return new TreeMap<>(sorted.descendingMap());
    }

    private Map<Integer, String> countFrequencies(List<String> list) {
        Map<Integer, String> frequencies = new HashMap<>();
        Set<String> set = new HashSet<>(list);
        set.forEach(s->frequencies.put(Collections.frequency(list, s),s));
        return frequencies;
    }

    private List<Message> channelHistoryToList(MessageChannel channel, Message currentMessage) {
        List<Message> history = new ArrayList<>(MessageHistory
                .getHistoryBefore(channel, currentMessage.getId())
                .limit(100)
                .complete()
                .getRetrievedHistory());
        String lastID = history.get(history.size() - 1).getId();

        while (true) {
            List<Message> historyPrev = new ArrayList<>(MessageHistory
                    .getHistoryBefore(channel, lastID)
                    .limit(100)
                    .complete()
                    .getRetrievedHistory());
            if (historyPrev.isEmpty()) break;
            lastID = historyPrev.get(historyPrev.size() - 1).getId();
            history.addAll(historyPrev);
        }

        return history;
    }
}
