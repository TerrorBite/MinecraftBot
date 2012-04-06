package com.avisenera.minecraftbot;

/**
 * Representation of a message to be relayed. Holds values that replace
 * certain words entered in the configuration file.
 */
public class Message {
    public Message() {}
    
    // Assigning empty strings avoids NullPointerExceptions when using applyFormatting
    public String name = "";
    public String message = "";
    public String reason = "";
    public String channel = "";
    public String oldname = "";
    public String mode = "";
    public String topic = "";
    
    /**
     * Given a formatting string, replaces values such as %name% with their actual values.
     * @param formatting The formatting string to use
     * @param msg A Message object containing the values to replace with
     * @return A formatted string with variables replaced with the actual values
     */
    public static String applyFormatting(String formatting, Message msg) {
        String fullmessage;
        
        fullmessage = formatting.replace("%name%", msg.name);
        fullmessage = fullmessage.replace("%message%", msg.message);
        fullmessage = fullmessage.replace("%reason%", msg.reason);
        fullmessage = fullmessage.replace("%channel%", msg.channel);
        fullmessage = fullmessage.replace("%oldname%", msg.oldname);
        fullmessage = fullmessage.replace("%mode%", msg.mode);
        fullmessage = fullmessage.replace("%topic%", msg.topic);
        
        return fullmessage;
    }
}
