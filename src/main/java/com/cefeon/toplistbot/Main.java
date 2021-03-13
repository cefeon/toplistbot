package com.cefeon.toplistbot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;


import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        final String BOT_TOKEN = System.getenv("BOT_TOKEN");
        Bot bot = new Bot(BOT_TOKEN);
        bot.start();
    }
}
