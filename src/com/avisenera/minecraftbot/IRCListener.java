package com.avisenera.minecraftbot;

import org.jibble.pircbot.PircBot;

public class IRCListener extends PircBot implements Runnable {
    private MinecraftBot plugin;
    private Configuration config;
    
    // Always attempt to reconnect on disconnect unless this should stay disconnected.
    boolean autoreconnect = true;
    
    public IRCListener(MinecraftBot instance, Configuration config) {
        plugin = instance;
        this.config = config;
    }
    
    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
