package me.rafa652.minecraftbot;

import me.rafa652.minecraftbot.MinecraftBotConfiguration.ColorContext;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class IRCHandler extends PircBot {
	public static MinecraftBot plugin;
	
	// Defined in the constructor
	private final ChatColor ce; // color for event
	private final ChatColor ck; // color for kick
	private final ChatColor cm; // color for /me
	private String server;
	private String serverpass;
	private String nick;
	private int port;
	private String channel;
	private String key;
	private String nickpass;
	
	public IRCHandler(MinecraftBot instance) {
		plugin = instance;
		
		MinecraftBotConfiguration c = plugin.config;
		ce = c.getChatColor(ColorContext.Event);
		ck = c.getChatColor(ColorContext.Kick);
		cm = c.getChatColor(ColorContext.Me);
		server = c.bot_server;
		serverpass = c.bot_serverpass;
		nick = c.bot_nick;
		port = c.bot_port;
		channel = c.bot_channel;
		key = c.bot_key;
		nickpass = c.bot_nickpass;
		
		super.setName(nick);
		super.setLogin("MinecraftBot");
		super.setAutoNickChange(true);
	}
	
	public boolean connect() {
		// Attempts to connect. Returns false on failure.
		int attempt = 0;
		int retry = 10; // Times to attempt connecting
		
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
			
			joinChannel();

			return true;
		}
		sendlog("Failed to connect after " + attempt + " attempts. Enter /reconnect to try again.", 2);
		return false;
	}
	public void onDisconnect() {
		sendlog("Disconnected.", 0);
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
	public void joinChannel() {
		// Overriding because there's only one channel to join
		if (key.isEmpty()) super.joinChannel(channel);
		else super.joinChannel(channel, key);
	}
	

	public void onMessage(String channel, String sender, String login, String hostnick, String message) {
		if (plugin.config.event_irc_chat == false) return;
		if (isCommand(sender, message)) return; 
		plugin.getServer().broadcastMessage("<#" + sender + "> " + c(message));
	}
	public void onAction(String sender, String login, String hostnick, String target, String action) {
		if (plugin.config.event_irc_chat == false) return;
		plugin.getServer().broadcastMessage(cm + "* #" + sender + " " + c(action));
	}
	public void onJoin(String channel, String sender, String login, String hostnick) {
		if (plugin.config.event_irc_join == false) return;
		plugin.getServer().broadcastMessage(ce + "* #" + sender + " joined " + channel);
	}
	public void onNickChange(String oldNick, String login, String hostnick, String newNick) {
		if (plugin.config.event_irc_nick == false) return;
		plugin.getServer().broadcastMessage(ce + "* #" + oldNick + " is now known as #" + newNick);
	}
	public void onPart(String channel, String sender, String login, String hostnick) {
		if (plugin.config.event_irc_part == false) return;
		// Can't pass the leave reason to here because PircBot doesn't support it.
		plugin.getServer().broadcastMessage(ce + "* #" + sender + " left " + channel);
	}
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostnick, String reason) {
		if (plugin.config.event_irc_quit == false) return;
		String message = "";
		if (!reason.isEmpty()) message = ": " + reason;
		plugin.getServer().broadcastMessage(ce + "* #" + sourceNick + " quit IRC" + c(message));
	}
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		if (recipientNick.equals(super.getNick())) {
			// Self was kicked - attempt to rejoin.
			
			// wait 3 seconds here
			joinChannel();
		}
		
		if (plugin.config.event_irc_kick == false) return;
		String message = "";
		if (!reason.isEmpty()) message = ": " + reason;
		plugin.getServer().broadcastMessage(ck + "* #" + recipientNick + " was kicked by #" + kickerNick + c(message));
	}
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		if (plugin.config.event_irc_topic == false) return;
		if (!changed) return; // Don't want the original topic
		plugin.getServer().broadcastMessage(ce + "* #" + setBy + " changed the topic to: " + c(topic));
	}
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		if (plugin.config.event_irc_mode == false) return;
		plugin.getServer().broadcastMessage("* #" + sourceNick + " gave channel operator status to #" + recipient);
	}
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		if (plugin.config.event_irc_mode == false) return;
		plugin.getServer().broadcastMessage("* #" + sourceNick + " removed channel operator status from #" + recipient);
	}
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		if (plugin.config.event_irc_mode == false) return;
		plugin.getServer().broadcastMessage("* #" + sourceNick + " gave voice to #" + recipient);
	}
	public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		if (plugin.config.event_irc_mode == false) return;
		plugin.getServer().broadcastMessage("* #" + sourceNick + " took voice from #" + recipient);
	}
	private String c(String line) {
		// TODO IRC colors translate to Minecraft colors here
		// Short name to keep lines short
		return line;
	}
	
	
	
	public void sendMessage(String message) {
		// Overriding because we already know what the target channel is.
		super.sendMessage(channel, message);
	}
	public String userlist() {
		// Returns a list of users on IRC.
		User list[] = super.getUsers(channel);
		String nicks = channel + ":";
		
		// In case this is used in a large channel; to prevent flooding the player's screen with names
		if (list.length <= 25)
			for (int i=0; i<list.length; i++) nicks += " " + list[i].getNick();
		else
			nicks += " Too many to list! You will have to look at " + channel + " yourself to see who's on.";
		
		return nicks;
	}
	
	private void sendlog(String message, int type) {
		// To avoid retyping the whole thing every time.
		// 0: info, 1: warning, 2: severe
		if (type == 0) plugin.log.info("[" + nick + "] " + message);
		if (type == 1) plugin.log.warning("[" + nick + "] " + message);
		if (type == 2) plugin.log.severe("[" + nick + "] " + message);
	}
	
	private boolean isCommand(String sender, String message) {
		// Place IRC commands in here. Return true if it was a command.
		// Returning false causes the line to be shown in Minecraft.
		
		// Player list
		if (message.toLowerCase().startsWith("!players")) {
			Player p[] = plugin.getServer().getOnlinePlayers();
			String o;
			int n = p.length;
			o = "There " + (n==1?"is ":"are ") + p + " player" + (n==1?"s":"") + " connected" + (n==0?".":":");
			for (int i=0; i<p.length; i++) o += " " + p[i].getDisplayName();
			super.sendMessage(channel, o);
			if (plugin.config.event_irc_chat)
				plugin.getServer().broadcastMessage(ce + "* #" + sender + " asked for the player list");
			return true;
		}
		
		return false;
	}
}
