package com.cefeon.toplistbot;

import net.dv8tion.jda.api.EmbedBuilder;
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
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


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
        if (message.getContentRaw().equals("!toplist")) {
            printTopList(channel, message);
        }

       if (message.getContentRaw().startsWith("!blacklist")){
           String[] currentMessage =  message.getContentRaw().split(" ", 2);
           if (currentMessage.length<=1) return;
           blackList.add(currentMessage[1]);
           channel.sendMessage("added word **" + currentMessage[1] + "** to blacklist").queue();
        }
    }

    private void printTopList(MessageChannel channel, Message message){
        List<Message> history = channelHistoryToList(channel, message);
        ArrayList<String> contents = new ArrayList<>();
        history.stream()
                .filter(x->!(x.getAuthor().isBot()))
                .filter(x->!(blackList.contains(x.getContentRaw())))
                .forEach(x->contents.add(x.getContentRaw()));
        countFrequencies(contents);
        Map<String,Integer> sorted = sortByOccurrences(countFrequencies(contents));

        StringBuilder builder = new StringBuilder();
        sorted.forEach((x,y)-> builder.append(x).append(" | ").append("**").append(y).append("**").append("\n"));

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Most popular messages on #"+channel.getName(), null);
        eb.setColor(new Color(140, 0, 100));
        eb.setDescription(builder);
        channel.sendMessage(eb.build()).queue();
    }

    private Map<String, Integer> sortByOccurrences(Map<String, Integer> frequencies){
        return frequencies.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private Map<String, Integer> countFrequencies(List<String> list) {
        Map<String, Integer> frequencies = new HashMap<>();
        list.forEach(s->frequencies.put(s,Collections.frequency(list, s)));
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
