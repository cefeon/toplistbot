package com.cefeon.toplistbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ToplistCommand implements Command {
    @Override
    public void execute(MessageChannel channel, Message message, ArrayList<String> blackList) {
        blackList.add("");
        List<Message> history = channelHistoryToList(channel, message);
        ArrayList<String> contents = removeBlacklisted(history, blackList);
        Map<String, Integer> sorted = sortByOccurrences(countFrequencies(contents));
        printTopList(sorted, channel);
    }

    private ArrayList<String> removeBlacklisted(List<Message> messageList, ArrayList<String> blackList) {
        ArrayList<String> contents = new ArrayList<>();
        messageList.stream()
                .filter(x -> !(x.getAuthor().isBot()))
                .filter(x -> !(blackList.contains(x.getContentRaw())))
                .forEach(x -> contents.add(x.getContentRaw()));
        return contents;
    }

    private void printTopList(Map<String, Integer> list, MessageChannel channel) {
        StringBuilder builder = new StringBuilder();
        list.forEach((x, y) -> builder.append(x).append(" | ").append("**").append(y).append("**").append("\n"));
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Most popular messages on #" + channel.getName(), null);
        eb.setColor(new Color(140, 0, 100));
        eb.setDescription(builder);
        channel.sendMessage(eb.build()).queue();
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

    private Map<String, Integer> sortByOccurrences(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> x, LinkedHashMap::new));
    }

    private Map<String, Integer> countFrequencies(List<String> list) {
        Map<String, Integer> frequencies = new HashMap<>();
        list.forEach(s -> frequencies.put(s, Collections.frequency(list, s)));
        return frequencies;
    }
}
