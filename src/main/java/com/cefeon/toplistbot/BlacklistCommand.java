package com.cefeon.toplistbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;

public class BlacklistCommand implements Command {
    @Override
    public void execute(MessageChannel channel, Message message, ArrayList<String> blackList) {
            String[] currentMessage =  message.getContentRaw().split(" ", 2);
            if (currentMessage.length<=1) return;
            blackList.add(currentMessage[1]);
            channel.sendMessage("added word **" + currentMessage[1] + "** to blacklist").queue();
    }
}
