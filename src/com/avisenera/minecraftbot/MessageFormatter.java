package com.avisenera.minecraftbot;

import com.avisenera.minecraftbot.message.IRCMessage;
import com.avisenera.minecraftbot.message.MCMessage;
import com.avisenera.minecraftbot.message.Message;

/**
 * Various methods that turn Message objects into useful Strings.
 */
public class MessageFormatter {
    private MinecraftBot plugin;
    
    public MessageFormatter(MinecraftBot instance) {
        plugin = instance;
    }
    
    /**
     * Formats a message from Minecraft to be displayed in IRC.
     * @param format The formatting string to use
     * @param message The message object containing the formatting variable values
     * @return A string that is ready to be used, or null if the given format is disabled
     */
    public String toIRC(Keys.line_to_irc format, MCMessage message) {
        String formatting = plugin.config.line_to_irc(format);
        if (formatting.isEmpty()) return null;
        
        return Message.applyFormatting(plugin, formatting, message);
    }
    
    public String toMinecraft(Keys.line_to_minecraft format, IRCMessage message) {
        String formatting = plugin.config.line_to_minecraft(format);
        if (formatting.isEmpty()) return null;
        
        return Message.applyFormatting(plugin, formatting, message);
    }
}
