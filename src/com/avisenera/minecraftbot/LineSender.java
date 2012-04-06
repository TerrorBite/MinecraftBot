package com.avisenera.minecraftbot;

/**
 * This class is in charge of sending messages one way or the other.
 * It should be the only class here that uses Bukkit's broadcastMessage()
 * and IRCListener's sendMessage().
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
    public void toIRC(Keys.line_to_irc format, Message message) {
        String formatting = config.line_to_irc(format);
        if (formatting.isEmpty()) return; // Empty formatting string - ignore
        
        this.rawToIRC(Message.applyFormatting(formatting, message));
    }
    
    /**
     * Sends a message directly to the IRC channel.
     * Skips all formatting except the control code translation.
     * @param msg The line to send to IRC
     */
    public void rawToIRC(String msg) {
        bot.sendMessage(msg);
    }
    
    /**
     * Sends a message to the IRC channel.
     * It checks the configuration settings and sets up the message accordingly.
     * @param format The formatting line to use
     * @param message Object containing message values
     */
    public void toMinecraft(Keys.line_to_minecraft format, Message message) {
        String formatting = config.line_to_minecraft(format);
        if (formatting.isEmpty()) return; // Empty formatting string - ignore
        
        this.rawToIRC(Message.applyFormatting(formatting, message));
    }
    /**
     * Sends a message directly to Minecraft chat.
     * Skips all formatting except the control code translation.
     * @param msg The line to send to Minecraft
     */
    public void rawToMinecraft(String msg) {
        plugin.getServer().broadcastMessage(Formatting.toMC(msg));
    }
}
