package me.rafa652.minecraftbot;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class IRCHandler extends PircBot {
	private MinecraftBot plugin;
	public String nick;
	public int port;
	public String server;
	public String serverpass;
	public String channel;
	public String key;
	public String nickpass;
	
	public boolean doNotReconnect = false;
	
	// PlayerChatHandler lists which colors to use
	final ChatColor ce = ChatColor.DARK_AQUA; // color for event
	final ChatColor ca = ChatColor.DARK_PURPLE; // color for action
	
	public IRCHandler(MinecraftBot instance) {
		this.plugin = instance;
		
		// Get data from plugin
		nick = plugin.nick;
		port = plugin.port;
		server = plugin.server;
		serverpass = plugin.serverpass;
		channel = plugin.channel;
		key = plugin.key;
		nickpass = plugin.nickpass;
		
		super.setName(nick);
		super.setLogin("MinecraftBot");
		super.setAutoNickChange(true);
	}
	
	public boolean connect() {
		// Attempts to connect. Returns false on failure.
		int attempt = 0;
		int retry = 50; // Times to attempt connecting
		
		// Limitations with bukkit and/or myself have forced
		// me to just have the plugin connect again and again.
		// No waiting between reconnects.
		// If anyone looking at this code knows how to have it wait
		// and wants to add that, go right ahead.
		
		while (retry > 0) {
			try {
				attempt++;
				sendlog("Connecting to " + server + "... (Attempt " + attempt + ")", 0);
				if (serverpass.isEmpty())
					super.connect(server, port);
				else
					super.connect(server, port, serverpass);
				sendlog("Connected to server.", 0);
			} catch (Exception e) {
				sendlog(e.getMessage(), 1);
				retry--;
				// wait 5 seconds here
				continue;
			}
			
			checkNick();
			
			if (key.isEmpty()) super.joinChannel(channel);
			else super.joinChannel(channel, key);

			return true;
		}
		sendlog("Failed to connect after " + attempt + " attempts. Giving up.", 2);
		return false;
	}
	public void onDisconnect() {
		// Attempts to reconnect. On failure, disables the plugin...
		if (doNotReconnect) {
			// ...unless we actually want to stay disconnected.
			sendlog("Disconnected.", 0);
			return;
		}
		sendlog("Disconnected. Attempting to reconnect...", 1);
		if (!connect()) {
			sendlog("Failed to reconnect.", 2);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
	}
	private void checkNick() {
		// Check to see whether this was the given nick.
		// If yes, identify. If not, ghost. Or... just don't do anything if no nickpass exists.
		if (nickpass.isEmpty()) {
			sendlog("The nick \"" + nick + "\" appears to be taken. The bot is now known as " + super.getNick(), 1);
			nick = super.getNick();
			return;
		}
		if (nick.equals(super.getNick())) {
			super.identify(nickpass);
			return;
		}
		
		sendlog("Nick is taken. Attempting to reclaim...", 0);
		super.sendMessage("NickServ", "ghost " + nick + " " + nickpass);
		// wait 3 seconds here
		
		super.changeNick(nick);
		// wait 2 seconds here
		if (!nick.equals(super.getNick())) {
			sendlog("Failed to reclaim nick. This bot is now known as" + super.getNick(), 1);
			nick = super.getNick();
			return;
		}
	}
	
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		// Rejoin channel on kick
		if (!recipientNick.equals(super.getNick())) return;
		
		// wait 3 seconds here
		if (key.isEmpty()) super.joinChannel(channel);
		else super.joinChannel(channel, key);
	}

	public void onMessage(String channel, String sender, String login, String hostnick, String message) {
		if (!isCommand(sender, message))
			plugin.getServer().broadcastMessage("<#" + sender + "> " + message);
	}
	public void onAction(String sender, String login, String hostnick, String target, String action) {
		plugin.getServer().broadcastMessage(ca + "* #" + sender + " " + action);
	}
	public void onJoin(String channel, String sender, String login, String hostnick) {
		plugin.getServer().broadcastMessage(ce + "* #" + sender + " joined " + channel);
	}
	public void onNickChange(String oldNick, String login, String hostnick, String newNick) {
		plugin.getServer().broadcastMessage(ce + "* #" + oldNick + " is now known as #" + newNick);
	}
	public void onPart(String channel, String sender, String login, String hostnick) {
		// Can't pass the leave reason to here because PircBot doesn't support it.
		plugin.getServer().broadcastMessage(ce + "* #" + sender + " left " + channel);
	}
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostnick, String reason) {
		String message = "";
		if (!reason.isEmpty()) message = ": " + reason;
		plugin.getServer().broadcastMessage(ce + "* #" + sourceNick + " quit IRC" + message);
	}
	
	public void sendMessage(String message) {
		// We already know what the target channel is.
		super.sendMessage(channel, message);
	}
	
	private void sendlog(String message, int type) {
		// To avoid retyping the whole thing every time.
		// 0: info, 1: warning, 2: severe
		if (type == 0) plugin.log.info("[" + nick + "] " + message);
		if (type == 1) plugin.log.warning("[" + nick + "] " + message);
		if (type == 2) plugin.log.severe("[" + nick + "] " + message);
	}
	
	private boolean isCommand(String sender, String message) {
		if (message.toLowerCase().startsWith("!players")) {
			Player players[] = plugin.getServer().getOnlinePlayers();
			String output = "There are " + players.length + " connected:";
			for (int i=0; i<players.length; i++)
				output += " " + players[i].getDisplayName();
			super.sendMessage(channel, output);
			plugin.getServer().broadcastMessage(ce + "* #" + sender + "asked who's playing");
			return true;
		}
		return false;
	}
	
	public String userlist() {
		// User list on a string
		User list[] = super.getUsers(channel);
		String nicks = ce + "";
		
		// In case this is used in a large channel; to prevent flooding the player's screen with names
		if (list.length <= 25)
			for (int i=0; i<list.length; i++) nicks += " " + list[i].getNick();
		else
			nicks += " Too many to list!";
		
		return nicks;
	}
}
