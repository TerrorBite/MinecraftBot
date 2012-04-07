package com.avisenera.minecraftbot;

import com.avisenera.minecraftbot.listeners.IRCListener;
import com.avisenera.minecraftbot.message.IRCMessage;
import com.avisenera.minecraftbot.message.MCMessage;
import com.avisenera.minecraftbot.message.Message;

/**
 * This class is in charge of sending messages one way or the other.
 * This should be the only class here that uses Bukkit's broadcastMessage() and IRCListener's sendMessage().
 */
public class LineSender {
    private MinecraftBot plugin;
    private Configuration config;
    private IRCListener bot;
    public LineSender(MinecraftBot instance, Configuration cfg, IRCListener irc) {
        plugin = instance;
        config = cfg;
        bot = irc;
    }
    
    /**
     * Sends a message to the IRC channel.
     * It checks the configuration settings and sets up the message accordingly.
     * @param format The formatting line to use
     * @param message Object containing message values
     */
    public void toIRC(Keys.line_to_irc format, MCMessage message) {
        String formatting = config.line_to_irc(format);
        if (formatting.isEmpty()) return; // Empty formatting string - ignore
        
        this.rawToIRC(Message.applyFormatting(plugin, formatting, message), false);
    }
    
    /**
     * Sends a message directly to the IRC channel.
     * Skips all formatting except the control code translation.
     * @param msg The line to send to IRC
     * @param boolean Is the message an action?
     */
    public void rawToIRC(String msg, boolean action) {
        msg = Formatting.toIRC(msg);
        if (action) bot.sendAction(msg);
        else bot.sendMessage(msg);
    }
    
    /**
     * Sends a message to the IRC channel.
     * It checks the configuration settings and sets up the message accordingly.
     * @param format The formatting line to use
     * @param message Object containing message values
     */
    public void toMinecraft(Keys.line_to_minecraft format, IRCMessage message) {
        String formatting = config.line_to_minecraft(format);
        if (formatting.isEmpty()) return; // Empty formatting string - ignore
        
        this.rawToMinecraft(Message.applyFormatting(plugin, formatting, message));
    }
    /**
     * Sends a message directly to Minecraft chat.
     * Skips all formatting except the control code translation.
     * @param msg The line to send to Minecraft
     */
    public void rawToMinecraft(String msg) {
        msg = Formatting.toMC(msg);
        plugin.getServer().broadcastMessage(Formatting.toMC(msg));
    }
}
