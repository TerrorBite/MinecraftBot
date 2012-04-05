package com.avisenera.minecraftbot;

import org.bukkit.event.Listener;

public class PlayerListener implements Listener {
    private MinecraftBot plugin;
    private Configuration config;
    
    public PlayerListener(MinecraftBot instance, Configuration cfg) {
        plugin = instance;
        config = cfg;
    }
}
