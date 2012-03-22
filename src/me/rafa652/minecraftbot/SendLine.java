package me.rafa652.minecraftbot;

/**
 * Class used to format lines based on configuration options.
 * When a message needs to be sent one way or the other, this handles it.
 */
public class SendLine {
    public enum EventType { Event, Kick, Death }
    
    private MinecraftBot plugin;
    
    // Default values
    private String format_irc =    "<%name%> %message%"; // to irc
    private String format_irc_me = "* %name% %message%";
    private String format_mc =     "#> <%name%> %message%"; // to minecraft
    private String format_mc_me =  "#> * %name% %message%";
    
    // Config values
    private String irc_cd; // color for death
    private String irc_ce; // color for event
    private String irc_ck; // color for kick
    private String mc_cd;
    private String mc_ce;
    private String mc_ck;
    
    public SendLine(MinecraftBot instance, MinecraftBotConfiguration c) {
        plugin = instance;
        
        // Keeping default values if input is blank
        if (!(c.format_irc == null || c.format_irc.isEmpty())) format_irc = c.format_irc;
        if (!(c.format_irc_me == null || c.format_irc_me.isEmpty())) format_irc_me = c.format_irc_me;
        if (!(c.format_mc == null || c.format_mc.isEmpty())) format_mc = c.format_mc;
        if (!(c.format_mc_me == null || c.format_mc_me.isEmpty())) format_mc_me = c.format_mc_me;
        
        irc_cd = c.color_irc_death;
        irc_ce = c.color_irc_event;
        irc_ck = c.color_irc_kick;
        mc_cd = c.color_mc_death;
        mc_ce = c.color_mc_event;
        mc_ck = c.color_mc_kick;
    }
    
    
    private enum dest {MC, IRC}
    /**
     * Formats the string so it's ready to be sent.
     * @param name String to put in place of %name%
     * @param message String to put in place of %message%
     * @param format The format to put the string
     * @param d Where the message is going, to know how to convert colors
     * @param c Extra color that is used for the line (if it's an event)
     * @return Formatted string with name and message values replaced
     */
    private String formatted(String name, String message, String format, dest d, String c) {
        if (d == dest.MC) {
            name = Color.toMC(name);
            message = Color.toMC(message);
        } else {
            name = Color.toIRC(name);
            message = Color.toIRC(message);
        }
        
        if (c == null) c = "";
        String result = c + format;
        
        // Color conversions add a normal code at the end of the string which
        // removes the color that was inserted at the beginning of the line
        result = result.replace("%name%", name + c);
        result = result.replace("%message%", message + c);
        
        return result;
    }
    
    public void chatToIRC(String name, String message) {
        plugin.bot.sendMessage(formatted(name, message, format_irc, dest.IRC, null));
    }
    
    public void meToIRC(String name, String message) {
        plugin.bot.sendMessage(formatted(name, message, format_irc_me, dest.IRC, null));
    }
    
    public void eventToIRC(EventType type, String name, String message) {
        String c = "";
        if (type == EventType.Event) c = irc_ce;
        if (type == EventType.Death) c = irc_cd;
        if (type == EventType.Kick) c = irc_ck;
        
        plugin.bot.sendMessage(formatted(name, message, format_irc_me, dest.IRC, c));
    }
    
    public void chatToMC(String name, String message) {
        plugin.getServer().broadcastMessage(formatted(name, message, format_mc, dest.MC, null));
    }
    
    public void meToMC(String name, String message) {
        plugin.getServer().broadcastMessage(formatted(name, message, format_mc_me, dest.MC, null));
    }
    
    public void eventToMC(EventType type, String name, String message) {
        String c = "";
        if (type == EventType.Event) c = mc_ce;
        if (type == EventType.Death) c = mc_cd;
        if (type == EventType.Kick) c = mc_ck;
        
        plugin.getServer().broadcastMessage(formatted(name, message, format_mc_me, dest.MC, c));
    }
}
