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
import org.bukkit.configuration.file.FileConfiguration;

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
	
	public boolean event_irc_chat;
	public boolean event_irc_join;
	public boolean event_irc_part;
	public boolean event_irc_quit;
	public boolean event_irc_kick;
	public boolean event_irc_nick;
	public boolean event_irc_mode;
	public boolean event_irc_topic;
	
// Colors need to be accessed using the methods further below.
	
// Configuration --------------------------------
	// Constructor sets success to false if there's an error
	// It uses the plugin's logger to describe the errors.
	private boolean success = true;
	public MinecraftBotConfiguration(MinecraftBot plugin) {
		FileConfiguration config = plugin.getConfig();
		
		// using config.yml
		config.options().copyDefaults(true);
		
		// Loading all config values
		bot_server = config.getString("server.server");
		bot_port = config.getInt("server.port");
		bot_serverpass = config.getString("server.password");
		bot_channel = config.getString("channel.channel");
		bot_key = config.getString("channel.key");
		bot_nick = config.getString("bot.nick");
		bot_nickpass = config.getString("bot.nickpass");
		
		try {
			setColors(
					config.getString("color.me"),
					config.getString("color.event"),
					config.getString("color.kick"),
					config.getString("color.death"));
		} catch (Exception e) {
			plugin.log.severe("[MinecraftBot] Could not load the color configuration properly.");
			success = false;
		}
				
	}
	public boolean goodConfig() {
		return success;
	}
	
// Colors ---------------------------------------
	private int color_irc_me;
	private int color_irc_event;
	private int color_irc_kick;
	private int color_irc_death;
	private ChatColor color_mc_me;
	private ChatColor color_mc_event;
	private ChatColor color_mc_kick;
	private ChatColor color_mc_death;
	
	private enum Color {black, darkblue, green, red,
		purple, yellow, darkaqua, teal, aqua, blue, darkgray, gray, darkgrey, grey, white}
	public enum ColorContext {Me, Event, Kick, Death}
	
	private void setColors(String me, String event, String kick, String death) throws Exception {
		// This handles the colors from the configuration
		// It should throw an IllegalArgumentException if the color is not valid.
		setColors(ColorContext.Me, Color.valueOf(me));
		setColors(ColorContext.Event, Color.valueOf(event));
		setColors(ColorContext.Kick, Color.valueOf(kick));
		setColors(ColorContext.Death, Color.valueOf(death));
	}
	private void setColors(ColorContext context, Color color) {
		// This translates and stores the colors in their right places
		
		// Must initialize these or else this won't compile
		int i = 1;
		ChatColor m = ChatColor.BLACK;
		
		// Color to actual values
		switch (color) {
		case black:
			i = 1; m = ChatColor.BLACK; break;
		case darkblue:
			i = 2; m = ChatColor.DARK_BLUE; break;
		case green:
			i = 3; m = ChatColor.GREEN; break;
		case red:
			i = 4; m = ChatColor.RED; break;
		case purple:
			i = 6; m = ChatColor.DARK_PURPLE; break;
		case yellow:
			i = 8; m = ChatColor.YELLOW; break;
		case darkaqua:
		case teal:
			i = 10; m = ChatColor.DARK_AQUA; break;
		case aqua:
			i = 11; m = ChatColor.AQUA; break;
		case blue:
			i = 12; m = ChatColor.BLUE; break;
		case darkgray:
		case darkgrey:
			i = 14; m = ChatColor.DARK_GRAY; break;
		case gray:
		case grey:
			i = 15; m = ChatColor.GRAY; break;
		case white:
			i = 16; m = ChatColor.WHITE; break;
		}
		
		// Values to their proper places
		switch (context) {
		case Me:
			color_irc_me = i;
			color_mc_me = m;
			break;
		case Event:
			color_irc_event = i;
			color_mc_event = m;
			break;
		case Kick:
			color_irc_kick = i;
			color_mc_kick = m;
		case Death:
			color_irc_death = i;
			color_mc_death = m;
		}
	}
	
	public int getIRCColor(ColorContext context) {
		switch (context) {
		case Me:
			return color_irc_me;
		case Event:
			return color_irc_event;
		case Kick:
			return color_irc_kick;
		case Death:
			return color_irc_death;
		}
		return 1;
	}
	
	public ChatColor getChatColor(ColorContext context) {
		switch (context) {
		case Me:
			return color_mc_me;
		case Event:
			return color_mc_event;
		case Kick:
			return color_mc_kick;
		case Death:
			return color_mc_death;
		}
		return ChatColor.WHITE;
	}
}
