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
	private String version = "0.96";
	public Logger log = Logger.getLogger("Minecraft");
	
	public MinecraftBotConfiguration config;
	public IRCHandler bot;
	
	// Not instantiating yet because they use config
	private PlayerChatHandler playerListener;
	private EntityHandler entityListener;
	private ServerConsoleHandler serverListener;
	
	public void onEnable() {
		log.info("[MinecraftBot] v" + version + " loaded.");
		PluginManager pm = getServer().getPluginManager();
		
		config = new MinecraftBotConfiguration(this);
		
		if (config.isGood()) {
			playerListener = new PlayerChatHandler(this);
			entityListener = new EntityHandler(this);
			serverListener = new ServerConsoleHandler(this);
			
			pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.SERVER_COMMAND, serverListener, Event.Priority.Monitor, this);
		
			if (!config.bot_nick.equals("MinecraftBot")) // avoid redundancy
				log.info("[MinecraftBot] will now call itself " + config.bot_nick);

			bot = new IRCHandler(this);
			
			// If failed to connect, disable plugin
			if (!bot.connect()) pm.disablePlugin(this);
			// Now only IRCHandler sends info to the logger
		} else {
			pm.disablePlugin(this);
		}
	}
	
	public void onDisable() {
		if (bot != null) {
			bot.disconnect();
			bot.dispose();
		}
		log.info("[MinecraftBot] v" + version + " disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
		String command = cmd.getName().toLowerCase();
		if (command.equals("me")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command.");
				return true;
			}
			
			if (config.event_mc_chat == false) return true; // MC chat turned off in config
			Player player = (Player)sender;
			String message = "* " + player.getDisplayName(); // x03 (IRC color) followed by 6 (purple)
			for (int i=0; i<args.length; i++) message += " " + args[i];
			
			// To IRC
			bot.sendMessage(config.getIRCColor(ColorContext.Me) + message);
			// To Minecraft
			sender.getServer().broadcastMessage(config.getChatColor(ColorContext.Me) + message);
			
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
				else bot.op(config.bot_channel, args[1]);
			}
			else if (subcommand.equals("deop")) {
				if (!permitted(sender, "op")) return true;
				if (showusage) sender.sendMessage("/deop nick");
				else bot.deOp(config.bot_channel, args[1]);
			}
			else if (subcommand.equals("voice")) {
				if (!permitted(sender, "voice")) return true;
				if (showusage) sender.sendMessage("/voice nick");
				else bot.voice(config.bot_channel, args[1]);
			}
			else if (subcommand.equals("devoice")) {
				if (!permitted(sender, "voice")) return true;
				if (showusage) sender.sendMessage("/devoice nick");
				else bot.deVoice(config.bot_channel, args[1]);
			}
			else if (subcommand.equals("kick")) {
				if (!permitted(sender, "kick")) return true;
				if (showusage) sender.sendMessage("/kick nick [reason]");
				else {
					String reason = "";
					for (int i=2;i<args.length;i++) reason += args[i] + " ";
					if (reason.length() > 0) reason = reason.substring(0, reason.length()-1);
					bot.kick(config.bot_channel, args[1], reason);
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
}
