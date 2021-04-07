package com.cefeon.toplistbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;

public class TestCommand implements Command{
    @Override
    public void execute(MessageChannel channel, Message message, ArrayList<String> blackList) {
        channel.sendMessage("test zakonczony sukcesem").queue();
    }
}
