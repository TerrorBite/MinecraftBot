/*
 * This makes it easier to get to the configuration options.
 * Instead of at one point loading the configuration file and
 * making sure that all the values have been stored where they
 * need to go, this object loads the values at the beginning
 * and then it can be sent to any other object.
 */

// To do: Add support with EntityHandler, IRCHandler, MinecraftBot, PlayerChatHandler, ServerConsoleHandler

package me.rafa652.minecraftbot;

import org.bukkit.ChatColor;

public class MinecraftBotConfiguration {

// IRC bot values -------------------------------
	public String bot_server;
	public String bot_serverpass;
	public String bot_nick;
	public int bot_port;
	public String bot_channel;
	public String bot_key;
	public String bot_nickpass;
// Event display values -------------------------
	public boolean event_mc_server;
	public boolean event_mc_chat;
	public boolean event_mc_me;
	public boolean event_mc_join;
	public boolean event_mc_leave;
	public boolean event_mc_kick;
	public boolean event_mc_death;
	public boolean event_irc_join;
	public boolean event_irc_part;
	public boolean event_irc_quit;
	public boolean event_irc_kick;
	public boolean event_irc_nick;
// Uncomment the following once they're implemented
//	public boolean event_irc_topic;
//	public boolean event_irc_mode;
	
// Colors need to be accessed using the methods further below.
	
// Configuration --------------------------------
	private String status;
	public MinecraftBotConfiguration(MinecraftBot plugin) {
		// Get config (plugin.getConfig())
		// Load all the values (use setColors for colors)
		// Put results on status - "OK" if all good (MinecraftBot checks this as it starts up)
	}
	public String getStatus() {
		return status;
	}
	
// Colors ---------------------------------------
	private int color_irc_normal;
	private int color_irc_me;
	private int color_irc_event;
	private int color_irc_kick;
	private int color_irc_death;
	private ChatColor color_mc_normal;
	private ChatColor color_mc_me;
	private ChatColor color_mc_event;
	private ChatColor color_mc_kick;
	private ChatColor color_mc_death;
	
	private boolean setColors(String color_normal, String color_me, String color_event, String color_kick, String color_death) {
		// Get public string values and translate
		// them to IRC color numbers and bukkit ChatColors
		
		return false;
	}
	
	// When asking for a color
	public enum Context {Normal, Me, Event, Kick, Kill}
	
	public int getIRCColor(Context context) {
		// Give what's being asked for
		return 1; // eclipse complains too much
	}
	
	public ChatColor getChatColor(Context context) {
		// Give what's being asked for
		return ChatColor.WHITE;
	}
}
