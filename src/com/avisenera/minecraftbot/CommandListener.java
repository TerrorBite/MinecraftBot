package com.avisenera.minecraftbot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListener implements CommandExecutor {
    private MinecraftBot plugin;
    private Configuration config;
    public CommandListener(MinecraftBot instance, Configuration cfg) {
        plugin = instance;
        config = cfg;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
