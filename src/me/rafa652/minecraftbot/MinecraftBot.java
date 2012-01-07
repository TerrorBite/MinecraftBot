package me.rafa652.minecraftbot;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftBot extends JavaPlugin {
	private String version = "0.93";
	public Logger log = Logger.getLogger("Minecraft");
	private PlayerChatHandler playerListener = new PlayerChatHandler(this);
	private EntityHandler entityListener = new EntityHandler(this);
	private ServerConsoleHandler serverListener = new ServerConsoleHandler(this);
	
	// PlayerChatHandler lists which colors to use
	final String ca_i = "\u000306";
	final ChatColor ca_m = ChatColor.DARK_PURPLE;
	
	// Bot and its info
	public IRCHandler bot;
	public String server;
	public int port;
	public String serverpass;
	public String channel;
	public String key;
	public String nick;
	public String nickpass;
	
	public void onEnable() {
		log.info("[MinecraftBot] v" + version + " loaded.");
		PluginManager pm = getServer().getPluginManager();
		
		if (loadConfiguration()) {
			pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.SERVER_COMMAND, serverListener, Event.Priority.Monitor, this);
			
			if (!nick.equals("MinecraftBot")) // avoid redundancy
				log.info("[MinecraftBot] will now call itself " + nick);

			bot = new IRCHandler(this);
			// IRCHandler constructor copies the values from 'this' now.
			// Better than having a very long constructor.
			if (!bot.connect()) pm.disablePlugin(this);
			// Now the bot is now in charge of reporting errors and just about everything else.
		} else {
			log.severe("[MinecraftBot] Failed to load configuration. This plugin is now disabling itself.");
			pm.disablePlugin(this);
		}
	}
	
	public void onDisable() {
		if (bot != null && bot.isConnected()) {
			bot.doNotReconnect = true;
			bot.disconnect();
			bot.dispose();
		}
		log.info("[MinecraftBot] v" + version + " disabled.");
	}
	
	public boolean loadConfiguration() {
		FileConfiguration config = getConfig();
		
		// config.yml is included
		config.options().copyDefaults(true);
		
		server = config.getString("server.server");
		port = config.getInt("server.port");
		serverpass = config.getString("server.password");
		
		channel = config.getString("channel.channel");
		key = config.getString("channel.key");
		
		nick = config.getString("bot.nick");
		nickpass = config.getString("bot.nickpass");
		
		saveConfig();
		
		// Fail if required info is missing
		// While we're at it, say what exactly went wrong.
		boolean status = true;
		if (nick == null || nick.isEmpty()) {
			log.severe("[MinecraftBot] Bot name is missing in the configuration.");
			status = false;
		}
		if (server == null || server.isEmpty()) {
			log.severe("[MinecraftBot] The server to connect to is not defined in the configuration.");
			status = false;
		}
		if (port > 65535 || port < 0) {
			log.severe("[MinecraftBot] An invalid port number was specified in the configuration.");
			status = false;
		}
		if (channel == null || channel.isEmpty()) {
			log.severe("[MinecraftBot] The channel to join is not defined in the configuration.");
			status = false;
		}
		
		if (!channel.startsWith("#")) {
			// Fixing it instead of complaining about it
			channel = "#" + channel;
		}
		
		return status;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
		if (cmd.getName().equalsIgnoreCase("me")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command.");
				return true;
			}
			
			Player player = (Player)sender;
			String message = "* " + player.getDisplayName(); // x03 (IRC color) followed by 6 (purple)
			for (int i=0; i<args.length; i++) message += " " + args[i];
			
			// To IRC
			bot.sendMessage(ca_i + message);
			// To Minecraft
			sender.getServer().broadcastMessage(ca_m + message);
			
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("names")) {
			sender.sendMessage("Users in " + bot.channel + ":" + bot.userlist());
			return true;
		}
		
		// Don't know if this will work without defining it in plugin.yml
		// but it's worth a try
		if (cmd.getName().equalsIgnoreCase("broadcast")) {
			String message = "{Broadcast}";
			for (int i = 0; i < args.length; i++) message += (" " + args[i]);
			this.bot.sendMessage(message);
			return true;
		}
		
		return false;
	}
}
