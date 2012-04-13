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
        
        this.toIRC(Message.applyFormatting(plugin, formatting, message), false);
    }
    
    /**
     * Sends a message directly to the IRC channel.
     * Skips all formatting except the control code translation.
     * @param msg The line to send to IRC
     * @param isAction Is the message an action?
     */
    public void toIRC(String msg, boolean isAction) {
        msg = Formatting.toIRC(msg);
        if (isAction) bot.sendAction(msg);
        else bot.sendMessage(msg);
        plugin.mLRTI++;
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
        
        // Get nick prefix if enabled
        if (config.settingsB(Keys.settings.show_nick_prefixes))
            message.name = bot.getFullNick(message.name);
        
        this.toMinecraft(Message.applyFormatting(plugin, formatting, message));
    }
    /**
     * Sends a message directly to Minecraft chat.
     * Skips all formatting except the control code translation.
     * @param msg The line to send to Minecraft
     */
    private void toMinecraft(String msg) {
        msg = Formatting.toMC(msg);
        plugin.getServer().broadcastMessage(Formatting.toMC(msg));
        plugin.mLRTM++;
    }
}
