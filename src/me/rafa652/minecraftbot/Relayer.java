package me.rafa652.minecraftbot;

/**
 * Any message going one way or the other goes through this class first.
 * The methods in this class deal with formatting and sending messages to
 * both IRC and Minecraft.
 */

/*
 * Note: Planning to change EventType a bit later to include more than just those three options
 * Removing the color options later as well and adding more formatting options in their place.
 * In the end, the users will have more control over the output.
 */

public class Relayer {
    public enum EventType { Event, Kick, Death }
    
    private MinecraftBot plugin;
    
    // Default values
    private String format_irc =    "<%name%> %message%"; // to irc
    private String format_irc_me = "* %name% %message%";
    private String format_mc =     "#> <%name%> %message%"; // to minecraft
    private String format_mc_me =  "#> * %name% %message%";
    
    // Config values
    private String color_event;
    private String color_kick;
    private String color_death;
    
    public Relayer(MinecraftBot instance, MinecraftBotConfiguration c) {
        plugin = instance;
        
        // Keeping default values if input is blank
        if (!(c.format_irc == null || c.format_irc.isEmpty())) format_irc = c.format_irc;
        if (!(c.format_irc_me == null || c.format_irc_me.isEmpty())) format_irc_me = c.format_irc_me;
        if (!(c.format_mc == null || c.format_mc.isEmpty())) format_mc = c.format_mc;
        if (!(c.format_mc_me == null || c.format_mc_me.isEmpty())) format_mc_me = c.format_mc_me;
        
        color_death = c.color_death;
        color_event = c.color_event;
        color_kick = c.color_kick;
    }
    
    
    private enum dest {MC, IRC}
    /**
     * Formats the string so it's ready to be sent.
     * @param name String to put in place of %name%
     * @param message String to put in place of %message%
     * @param format The format to put the string
     * @param d Where the message is going, to know how to convert colors
     * @param t Event type, determines which color the line is sent in
     * @return Formatted string with name and message values replaced
     */
    private String formatted(String name, String message, String format, dest d, EventType t) {
        String padding;
        if (t == EventType.Death) padding = color_death;
        else if (t == EventType.Event) padding = color_event;
        else if (t == EventType.Kick) padding = color_kick;
        else padding = "";
        
        String result = padding + format;
        
        // Color conversions add a normal code at the end of the string which
        // removes the color that was inserted at the beginning of the line
        result = result.replace("%name%", name + padding);
        result = result.replace("%message%", message + padding);
        
        if (d == dest.IRC) result = Formatting.toIRC(result);
        if (d == dest.MC) result = Formatting.toMC(result);
        
        return result;
    }
    
    public void chatToIRC(String name, String message) {
        plugin.bot.sendMessage(formatted(name, message, format_irc, dest.IRC, null));
    }
    
    public void actionToIRC(String name, String message) {
        plugin.bot.sendMessage(formatted(name, message, format_irc_me, dest.IRC, null));
    }
    
    public void eventToIRC(EventType type, String name, String message) {
        plugin.bot.sendMessage(formatted(name, message, format_irc_me, dest.IRC, type));
    }
    
    public void chatToMC(String name, String message) {
        plugin.getServer().broadcastMessage(formatted(name, message, format_mc, dest.MC, null));
    }
    
    public void actionToMC(String name, String message) {
        plugin.getServer().broadcastMessage(formatted(name, message, format_mc_me, dest.MC, null));
    }
    
    public void eventToMC(EventType type, String name, String message) {
        plugin.getServer().broadcastMessage(formatted(name, message, format_mc_me, dest.MC, type));
    }
}
