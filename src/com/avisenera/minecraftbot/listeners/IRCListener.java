package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.configuration.Configuration;
import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MinecraftBot;
import org.jibble.pircbot.PircBot;

public class IRCListener extends PircBot implements Runnable {
    private MinecraftBot plugin;
    private Configuration config;
    
    // Always attempt to reconnect on disconnect unless this should stay disconnected.
    public boolean autoreconnect = true;
    
    public IRCListener(MinecraftBot instance, Configuration config) {
        plugin = instance;
        this.config = config;
    }
    
    /**
     * Begins attempting to connect to the server, if it isn't already connected.
     * The rest of the connection routine is in run().
     */
    public synchronized void connect() {
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, this);
    }
    // Used to prevent the connection method from running more than once at a time
    private boolean busyconnecting = false;
    @Override
    public void run() {
        if (busyconnecting) return;
        busyconnecting = true;
        // TODO everything
        busyconnecting = false;
    }

    public void sendMessage(String message) {
        this.sendMessage(config.connection(Keys.connection.channel), message);
    }
    
}
