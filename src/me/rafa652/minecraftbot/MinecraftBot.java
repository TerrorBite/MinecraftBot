package me.rafa652.minecraftbot;

import java.util.logging.Logger;

import me.rafa652.minecraftbot.MinecraftBotConfiguration.ColorContext;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftBot extends JavaPlugin {
	public final String version = "0.96";
	private Logger log = Logger.getLogger("Minecraft");
	
	public IRCHandler bot;
	
	// Not instantiating yet because they use config
	private PlayerChatHandler playerListener;
	private EntityHandler entityListener;
	private ServerConsoleHandler serverListener;
	
	// Values from config
	private boolean event_mc_chat;
	private String icm; // IRC Color Me
	private ChatColor mcm; // MC Color Me
	
	public void onEnable() {
		log(0, "v" + version + " loaded.");
		PluginManager pm = getServer().getPluginManager();
		
		MinecraftBotConfiguration config = new MinecraftBotConfiguration(this);
		
		// If config works, get everything started
		if (config.isGood()) {
			playerListener = new PlayerChatHandler(this, config);
			entityListener = new EntityHandler(this, config);
			serverListener = new ServerConsoleHandler(this, config);
			
			event_mc_chat = config.event_mc_chat;
			icm = config.getIRCColor(ColorContext.Me);
			mcm = config.getChatColor(ColorContext.Me);
			
			pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.SERVER_COMMAND, serverListener, Event.Priority.Monitor, this);
		
			if (!config.bot_nick.equals("MinecraftBot")) // avoid redundancy
				log(0, "will now call itself " + config.bot_nick);

			bot = new IRCHandler(this, config);
		} else {
			pm.disablePlugin(this);
		}
	}
	
	public void onDisable() {
		if (bot != null) {
			bot.disconnect();
			bot.dispose();
		}
		log(0, "v" + version + " disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
		String command = cmd.getName().toLowerCase();
		if (command.equals("me")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command.");
				return true;
			}
			
			if (event_mc_chat == false) return true; // MC chat turned off in config
			Player player = (Player)sender;
			String message = "* " + player.getDisplayName(); // x03 (IRC color) followed by 6 (purple)
			for (int i=0; i<args.length; i++) message += " " + args[i];
			
			// To IRC
			bot.sendMessage(icm + message);
			// To Minecraft
			sender.getServer().broadcastMessage(mcm + message);
			
			return true;
		}
		if (command.equals("names")) {
			sender.sendMessage(bot.userlist());
			return true;
		}
		
		// Admin IRC commands
		if (command.equals("irc")) {
			// Note: args[0] is what comes after "irc"
			// args[1] is what comes after subcommand
			if (args.length < 1) return false;
			
			boolean showusage = (args.length == 1);
			String subcommand = args[0].toLowerCase();
			
			if (subcommand.equals("op")) {
				if (!permitted(sender, "op")) return true;
				if (showusage) sender.sendMessage("/op nick");
				else bot.op(bot.getChannel(), args[1]);
			}
			else if (subcommand.equals("deop")) {
				if (!permitted(sender, "op")) return true;
				if (showusage) sender.sendMessage("/deop nick");
				else bot.deOp(bot.getChannel(), args[1]);
			}
			else if (subcommand.equals("voice")) {
				if (!permitted(sender, "voice")) return true;
				if (showusage) sender.sendMessage("/voice nick");
				else bot.voice(bot.getChannel(), args[1]);
			}
			else if (subcommand.equals("devoice")) {
				if (!permitted(sender, "voice")) return true;
				if (showusage) sender.sendMessage("/devoice nick");
				else bot.deVoice(bot.getChannel(), args[1]);
			}
			else if (subcommand.equals("kick")) {
				if (!permitted(sender, "kick")) return true;
				if (showusage) sender.sendMessage("/kick nick [reason]");
				else {
					String reason = "";
					for (int i=2;i<args.length;i++) reason += args[i] + " ";
					if (reason.length() > 0) reason = reason.substring(0, reason.length()-1);
					bot.kick(bot.getChannel(), args[1], reason);
				}
			}
			else if (subcommand.equals("connect")) {
				if (!permitted(sender, "manage")) return true;
				if (bot.isConnected()) sender.sendMessage("Already connected to IRC!");
				else bot.connect();
			}
			else if (subcommand.equals("rejoin")) {
				if (!permitted(sender, "manage")) return true;
				bot.joinChannel();
			}
			else if (subcommand.equals("disconnect")) {
				if (!permitted(sender, "manage")) return true;
				if (!bot.isConnected()) sender.sendMessage("Already not connected to IRC!");
				else bot.disconnect();
			}
			else return false; // to show /irc usage
			
			return true;
		}
		
		return false;
	}
	
	private boolean permitted(CommandSender sender, String permission) {
		if (sender instanceof ConsoleCommandSender) return true;
		boolean p = (sender.hasPermission("minecraftbot." + permission));
		
		if (!p) sender.sendMessage(ChatColor.RED + "You are not permitted to use this command.");
		return p;
	}
	
	/**
	 * Sends to logger. Prepends [MinecraftBot] or its current nick to it.
	 * @param type 1 for warning, 2 for severe. Anything else will be info
	 * @param message The message to send to the log
	 */
	public void log(int type, String message) {
		String l;
		if (bot != null && bot.getNick() != null) l = bot.getNick();
		else l = "MinecraftBot";
		
		l = "[" + l + "] " + message;
		
		if (type == 1) log.warning(l);
		else if (type == 2) log.severe(l);
		else log.info(l);
	}
}
