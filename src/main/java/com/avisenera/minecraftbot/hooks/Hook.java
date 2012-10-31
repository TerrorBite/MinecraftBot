package com.avisenera.minecraftbot.hooks;
// Disclaimer: I have no idea what I'm doing. Is this the worst way of doing this? I don't know.

import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.Message;
import java.util.ArrayList;
import org.bukkit.plugin.PluginManager;

/**
 * Used to quickly get data from all existing hooks. There's only one hook right now,
 * but maybe there will be more later and it can sometimes be fun to plan ahead.
 * Sometimes.
 */
public abstract class Hook {
    protected Hook() {}
    
    // List of available variables that are handled by hooks
    public static final String[] available_hooks = {"Faction"};
    
    /**
     * Attempts to return a value from an external plugin.
     * @param line The entire line which probably includes the variables to be replaced
     * @param msg The message data 
     * @return The string with variables replaced by values from hooks
     */
    public static String getVariable(MinecraftBot instance, String line, Message msg) {
        // Later: Figure out how to use all classes in this package without having to manually add them.
        ArrayList<Hook> hooks = new ArrayList<Hook>();
        hooks.add(new Faction());
        
        for (Hook h : hooks) {
            line = h.get(instance.getServer().getPluginManager(), line, msg);
        }
        
        return line;
    }
    
    protected abstract String get(PluginManager pm, String line, Message msg);
}
