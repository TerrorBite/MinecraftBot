/*
 * The point of this is to make it easier to load the configuration
 * and store the values into the right areas. This object loads the
 * configuration once, then it can be used by other objects. Those
 * other objects can get whichever configuration values they need.
 * Better than making a confusing mess in MinecraftBot's onEnable().
 */

package me.rafa652.minecraftbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

public class MinecraftBotConfiguration {
	private MinecraftBot plugin;

// IRC bot values -------------------------------
	public String bot_server;
	public String bot_serverpass;
	public String bot_nick;
	public int bot_port;
	public String bot_channel;
	public String bot_key;
	public String bot_nickpass;
	public String bot_quitmessage;
// Event display values -------------------------
	public boolean event_mc_server;
	public boolean event_mc_chat;
	public boolean event_mc_me;
	public boolean event_mc_join;
	public boolean event_mc_leave;
	public boolean event_mc_kick;
	public boolean event_mc_death;
	public boolean event_mc_opinfo;
	
	public boolean event_irc_chat;
	public boolean event_irc_me;
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
	public MinecraftBotConfiguration(MinecraftBot instance) {
		plugin = instance;
		
		// Set up the file
		if (!checkFile(plugin)) {
			success = false;
			return;
		}
		FileConfiguration config = plugin.getConfig();
		config.options().copyDefaults(true);
		
		// Load all values
		bot_server = config.getString("server.server");
		bot_port = config.getInt("server.port");
		bot_serverpass = config.getString("server.password");
		bot_channel = config.getString("channel.channel");
		bot_key = config.getString("channel.key");
		bot_nick = config.getString("bot.nick");
		bot_nickpass = config.getString("bot.nickpass");
		bot_quitmessage = config.getString("bot.quitmessage");
		
		event_mc_server = config.getBoolean("event.mc.server");
		event_mc_chat = config.getBoolean("event.mc.chat");
		event_mc_me = config.getBoolean("event.mc.me");
		event_mc_join = config.getBoolean("event.mc.join");
		event_mc_leave = config.getBoolean("event.mc.leave");
		event_mc_kick = config.getBoolean("event.mc.kick");
		event_mc_death = config.getBoolean("event.mc.death");
		event_mc_opinfo = config.getBoolean("event.mc.opinfo");
		
		event_irc_chat = config.getBoolean("event.irc.chat");
		event_irc_me = config.getBoolean("event.irc.me");
		event_irc_join = config.getBoolean("event.irc.join");
		event_irc_part = config.getBoolean("event.irc.part");
		event_irc_quit = config.getBoolean("event.irc.quit");
		event_irc_kick = config.getBoolean("event.irc.kick");
		event_irc_nick = config.getBoolean("event.irc.nick");
		event_irc_mode = config.getBoolean("event.irc.mode");
		event_irc_topic = config.getBoolean("event.irc.topic");
		
		
		// Check for errors
		if (bot_nick == null || bot_nick.isEmpty()) {
			plugin.log(2, "Configuration: Bot name is missing.");
			success = false;
		}
		if (bot_server == null || bot_server.isEmpty()) {
			plugin.log(2, "Configuration: The server to connect to is not defined.");
			success = false;
		}
		if (bot_port > 65535 || bot_port < 0) {
			plugin.log(2, "Configuration: An invalid port number was specified.");
			success = false;
		}
		
		
		if (bot_channel == null || bot_channel.isEmpty()) {
			plugin.log(2, "Configuration: The channel to join is not defined.");
			success = false;
		}
		else {
			if (!bot_channel.startsWith("#")) bot_channel = "#" + bot_channel;
		}
		
		try {
			setColors(
					config.getString("color.event"),
					config.getString("color.kick"),
					config.getString("color.death"));
		} catch (Exception e) {
			plugin.log(2, "Could not load the color configuration properly.");
			plugin.log(2, "Are some color options missing or misspelled?");
			success = false;
		}

	}
	public boolean isGood() {
		return success;
	}
	private boolean checkFile(MinecraftBot plugin) {
		// Checks if the config file exists. If not, creates it.
		// Returns false if an error occured.
    	try {
    		if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
    		plugin.getConfig().load(new File(plugin.getDataFolder(), "config.yml"));
    	} catch (FileNotFoundException e) {
			plugin.log(0, "No config file found. Creating a default configuration file.");
			plugin.log(0, "You must edit this file before being able to use this plugin.");
			saveFile(plugin);
			return false;
		} catch (IOException e) {
			plugin.log(2, "IOException while loading config! Check if config.yml or the plugins folder is writable.");
			return false;
		} catch (InvalidConfigurationException e) {
			plugin.log(2, "Configuration is invalid. Double check your syntax. (And remove any tab characters)");
			return false;
		}
		return true;
	}
	private void saveFile(MinecraftBot plugin) {
		try
		{
			File conf = new File(plugin.getDataFolder(), "config.yml");
			
			InputStream is = this.getClass().getResourceAsStream("/config.yml");
			if (!conf.exists())
				conf.createNewFile();
			OutputStream os = new FileOutputStream(conf);
			
			byte[] buf = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0)
				os.write(buf, 0, len);

			is.close();
			os.close();
		} 
		catch (IOException e) 
		{
			plugin.log(2, "Failed to save config.yml - Check the plugin's data directory!");
		} 
		catch (NullPointerException e) 
		{
			plugin.log(2, "Could not find the default config.yml! Is it in the .jar?");
		}
	}
		
	
// Colors ---------------------------------------
	public String color_irc_event;
	public String color_irc_kick;
	public String color_irc_death;
	public String color_mc_event;
	public String color_mc_kick;
	public String color_mc_death;
	
	private void setColors(String event, String kick, String death) throws IllegalArgumentException {
		// This handles the colors from the configuration and sets them to the proper values
		// It should throw an IllegalArgumentException if the color given is not valid.
		Color cevent = Color.valueOf(event.toUpperCase());
		Color ckick = Color.valueOf(kick.toUpperCase());
		Color cdeath = Color.valueOf(death.toUpperCase());
		
		color_irc_event = cevent.irc;
		color_mc_event = cevent.mc;
		color_irc_kick = ckick.irc;
		color_mc_kick = ckick.mc;
		color_irc_death = cdeath.irc;
		color_mc_death = cdeath.mc;
	}
}
