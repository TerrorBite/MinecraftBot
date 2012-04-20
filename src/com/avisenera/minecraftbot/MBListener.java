package com.avisenera.minecraftbot;

import com.avisenera.minecraftbot.listeners.IRCManager;

/**
 * The MinecraftBot Listener (MBListener) is the way to make the plugin
 * send and receive messages on IRC. Other plugins may add their listeners
 * for their own needs.
 */
public abstract class MBListener {
    private MinecraftBot plugin;
    private IRCManager manager;
    
    // Method only available to MinecraftBot and only used when the listener is added to it
    final void initialize(MinecraftBot instance, IRCManager irc) {
        plugin = instance;
        manager = irc;
    }
    
    /**
     * A line has been received from IRC.
     * @param line The IRC line exactly as it looks when sent to Minecraft
     * @param isAction True if the line received was an action (/me)
     */
    public void onMessage(final String line) {}

    /**
     * Sends a line to the IRC channel
     * @param line The line to send to IRC
     * @param isAction Set to true if it will be an action (/me), otherwise it will send as a regular message
     */
    public final void sendToIRC(String line, boolean isAction) {
        if (line == null) return; // Ignore null string
        if (isAction) manager.sendAction(line);
        else manager.sendMessage(line);
    }
    
    /**
     * Disables this listener. This listener will no longer receive IRC messages from MinecraftBot.
     */
    public final void unregister() {
        plugin.removeListener(this);
    }
}
