package com.avisenera.minecraftbot;

public class LineSender {
    private MinecraftBot plugin;
    private Configuration config;
    private IRCListener bot;
    public LineSender(MinecraftBot instance, Configuration cfg, IRCListener irc) {
        plugin = instance;
        config = cfg;
        bot = irc;
    }
}
