package com.avisenera.minecraftbot.message;

import com.avisenera.minecraftbot.Formatting;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.hooks.Hook;

/**
 * Representation of a message to be relayed. Holds values that replace
 * certain words entered in the configuration file.
 */
public class Message {
    // To distinguish between where the messages are from, two different classes inherit
    // from this class. They are both exactly alike except for the name.
    protected Message() {}
    
    // Internal values
    // Assigning empty strings avoids NullPointerExceptions when using applyFormatting
    public String name = "";
    public String message = "";
    public String reason = "";
    public String channel = "";
    public String kicker = "";
    public String oldname = "";
    public String mode = "";
    public String topic = "";
    
    // External values come from the Hook class and may depend on the internal values
    
    /**
     * Given a formatting string, replaces values such as %name% with their actual values.
     * @param p MinecraftBot instance, used to pass it on to Hook
     * @param formatting The formatting string to use
     * @param msg A Message object containing the values to replace with
     * @return A formatted string with variables replaced with the actual values
     */
    public static String applyFormatting(MinecraftBot p, String formatting, Message msg) {
        // && temprarily becomes U+00FE - Latin Small Letter Thorn
        String ampersand = "\u00FE";
        String mc_control_code = "\u00A7";
        
        formatting = formatting.replaceAll("&&", ampersand);
        formatting = formatting.replaceAll("&", mc_control_code);
        
        // Formatting is reset at the end of each variable to
        // prevent formatting in one variable from spreading to the rest of the line
        String fullmessage;
        fullmessage = formatting.replace("%name%", msg.name);
        fullmessage = fullmessage.replace("%message%", msg.message);
        fullmessage = fullmessage.replace("%reason%", msg.reason);
        fullmessage = fullmessage.replace("%channel%", msg.channel);
        fullmessage = fullmessage.replace("%kicker%", msg.kicker);
        fullmessage = fullmessage.replace("%oldname%", msg.oldname);
        fullmessage = fullmessage.replace("%mode%", msg.mode);
        fullmessage = fullmessage.replace("%topic%", msg.topic);
        
        fullmessage = Hook.getVariable(p, fullmessage, msg);
        
        // Turn special characters back into ampersands
        fullmessage = fullmessage.replaceAll(ampersand, "&");
        
        // Translate formatting codes
        if (msg instanceof IRCMessage) {
            return Formatting.toMC(fullmessage);
        } else {
            return Formatting.toIRC(fullmessage);
        }
    }
}
