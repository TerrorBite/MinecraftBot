/*
 * The point of this is to make it easier to load the configuration
 * and store the values into the right areas. This object loads the
 * configuration once, then it can be used by other objects. Those
 * other objects can get whichever configuration values they need.
 * Better than making a confusing mess in MinecraftBot's onEnable().
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
		
		config.options().copyDefaults(true);
		
		// Loading all config values
		
		bot_server = config.getString("server.server");
		bot_port = config.getInt("server.port");
		bot_serverpass = config.getString("server.password");
		bot_channel = config.getString("channel.channel");
		bot_key = config.getString("channel.key");
		bot_nick = config.getString("bot.nick");
		bot_nickpass = config.getString("bot.nickpass");
		
		event_mc_server = config.getBoolean("event.mc.server");
		event_mc_chat = config.getBoolean("event.mc.chat");
		event_mc_join = config.getBoolean("event.mc.join");
		event_mc_leave = config.getBoolean("event.mc.leave");
		event_mc_kick = config.getBoolean("event.mc.kick");
		event_mc_death = config.getBoolean("event.mc.death");
		
		event_irc_chat = config.getBoolean("event.irc.chat");
		event_irc_join = config.getBoolean("event.irc.join");
		event_irc_part = config.getBoolean("event.irc.part");
		event_irc_quit = config.getBoolean("event.irc.quit");
		event_irc_kick = config.getBoolean("event.irc.kick");
		event_irc_nick = config.getBoolean("event.irc.nick");
		event_irc_mode = config.getBoolean("event.irc.mode");
		event_irc_topic = config.getBoolean("event.irc.topic");
		
		// save defaults from included config.yml
		plugin.saveConfig();
		
		try {
			setColors(
					config.getString("color.me"),
					config.getString("color.event"),
					config.getString("color.kick"),
					config.getString("color.death"));
		} catch (Exception e) {
			
		// Now checking to see if the config's right
			plugin.log.severe("[MinecraftBot] Could not load the color configuration properly.");
			plugin.log.severe("[MinecraftBot] Are some colors missing or misspelled?");
			success = false;
		}
		
		if (bot_nick == null || bot_nick.isEmpty()) {
			plugin.log.severe("[MinecraftBot] Bot name is missing in the configuration.");
			success = false;
		}
		if (bot_server == null || bot_server.isEmpty()) {
			plugin.log.severe("[MinecraftBot] The server to connect to is not defined in the configuration.");
			success = false;
		}
		if (bot_port > 65535 || bot_port < 0) {
			plugin.log.severe("[MinecraftBot] An invalid port number was specified in the configuration.");
			success = false;
		}
		if (bot_channel == null || bot_channel.isEmpty()) {
			plugin.log.severe("[MinecraftBot] The channel to join is not defined in the configuration.");
			success = false;
		}
		else { if (!bot_channel.startsWith("#")) bot_channel = "#" + bot_channel; }
		
	}
	public boolean isGood() {
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
		setColors(ColorContext.Me, Color.valueOf(me.toLowerCase()));
		setColors(ColorContext.Event, Color.valueOf(event.toLowerCase()));
		setColors(ColorContext.Kick, Color.valueOf(kick.toLowerCase()));
		setColors(ColorContext.Death, Color.valueOf(death.toLowerCase()));
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
			i = 0; m = ChatColor.WHITE; break;
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
	
	public String getIRCColor(ColorContext context) {
		String color = "\u0003"; // IRC color code
		switch (context) {
		case Me:
			return color + color_irc_me;
		case Event:
			return color + color_irc_event;
		case Kick:
			return color + color_irc_kick;
		case Death:
			return color + color_irc_death;
		}
		return color + "01";
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
