package com.cefeon.toplistbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;

public interface Command {
    void execute (MessageChannel channel, Message message, ArrayList<String> blackList);
}
