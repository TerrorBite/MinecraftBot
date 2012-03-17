package me.rafa652.minecraftbot;

import org.bukkit.entity.Player;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 * This class handles the connection to the IRC server and all the events that happen on it.
 * It may send some of these events to Minecraft.
 */
public class IRCHandler extends PircBot implements Runnable {
	public static MinecraftBot plugin;
	
	// Values from config
	private final String ce; // color for event
	private final String ck; // color for kick
	
	private boolean event_irc_chat;
	private boolean event_irc_me;
	private boolean event_irc_join;
	private boolean event_irc_part;
	private boolean event_irc_quit;
	private boolean event_irc_kick;
	private boolean event_irc_nick;
	private boolean event_irc_mode;
	private boolean event_irc_topic;
	
	private String server;
	private String serverpass;
	private String nick;
	private int port;
	private String channel;
	private String key;
	private String nickpass;
	
	public boolean attempt_reconnect = true;
	
	public IRCHandler(MinecraftBot instance, MinecraftBotConfiguration config) {
		plugin = instance;
		
		// Load all the config
		ce = config.color_mc_event;
		ck = config.color_mc_kick;
		event_irc_chat = config.event_irc_chat;
		event_irc_me = config.event_irc_me;
		event_irc_join = config.event_irc_join;
		event_irc_part = config.event_irc_part;
		event_irc_quit = config.event_irc_quit;
		event_irc_kick = config.event_irc_kick;
		event_irc_nick = config.event_irc_nick;
		event_irc_mode = config.event_irc_mode;
		event_irc_topic = config.event_irc_topic;
		server = config.bot_server;
		serverpass = config.bot_serverpass;
		nick = config.bot_nick;
		port = config.bot_port;
		channel = config.bot_channel;
		key = config.bot_key;
		nickpass = config.bot_nickpass;
		
		// Set some info
		super.setLogin(nick);
		super.setVersion("MinecraftBot v" + plugin.getDescription().getVersion() + 
				" - https://github.com/Rafa652/MinecraftBot");
		super.setAutoNickChange(true);
	}
	
	public synchronized void connect() {
		// Connect method moved to run()
		
		// Starting immediately				
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, this, 0);
	}
	
	@Override
	public synchronized void run() {		
		// Attempts to connect.
		int attempt = 0;
		int retries = 4; // Times to attempt connecting, minus 1
		
		super.setName(nick);
		
		while (attempt < retries) {
			attempt++;
			try {
				if (isConnected()) break; // If it's already working, stop.
				plugin.log(0, "Connecting to " + server + "... (Attempt " + attempt + ")");
				if (serverpass.isEmpty()) super.connect(server, port);
				else super.connect(server, port, serverpass);
				plugin.log(0, "Connected to server.");
				
				checkNick();
				joinChannel();
				break;
			} catch (NickAlreadyInUseException e) { // ignore this exception
			} catch (Exception e) {
				plugin.log(1, "Failed to connect: " + e.getMessage());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
		}
		if (!isConnected())
			plugin.log(2, "Failed to connect after " + attempt + " attempts. Enter '/irc connect' to try again.");
	}
	private synchronized void checkNick() {
		// ONLY call if it's in a thread.
		
		// Check to see whether this was the given nick.
		// If yes, identify. If not, ghost. Or... just don't do anything if no nickpass exists.
		if (nick.equals(super.getNick())) {
			if (!nickpass.isEmpty()) this.identify(nickpass);
			return;	
		}
		
		if (nickpass.isEmpty()) {
			plugin.log(1, "\"" + nick + "\" appears to be taken. Current nick is now " + super.getNick() + ".");
			return;
		}
		
		// nickpass does exist - going to use it
		
		plugin.log(0, "\"" + nick + "\" is taken. Attempting to reclaim...");
		this.sendMessage("NickServ", "ghost " + nick + " " + nickpass);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.changeNick(nick);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!nick.equals(super.getNick())) {
			plugin.log(1, "Failed to reclaim nick. Current nick is " + super.getNick() + ".");
			return;
		}
	}
	
	public void onDisconnect() {
		if (attempt_reconnect) {
			plugin.log(2, "Disconnected. Will attempt to reconnect.");
			connect();
		}
		else {
			plugin.log(0, "Disconnected.");
			attempt_reconnect = true;
		}
	}
	
	public void joinChannel() {
		if (key.isEmpty()) joinChannel(channel);
		else joinChannel(channel, key);
	}
	

	public void onMessage(String channel, String sender, String login, String hostnick, String message) {
		if (event_irc_chat == false) return;
		if (isCommand(sender, message)) return; 
		plugin.getServer().broadcastMessage("<#" + sender + "> " + Color.toMC(message));
	}
	public void onAction(String sender, String login, String hostnick, String target, String action) {
		if (event_irc_me == false) return;
		plugin.getServer().broadcastMessage("* #" + sender + " " + Color.toMC(action));
	}
	public void onJoin(String channel, String sender, String login, String hostnick) {
		if (event_irc_join == false) return;
		plugin.getServer().broadcastMessage(ce + "* #" + sender + " joined " + channel);
	}
	public void onNickChange(String oldNick, String login, String hostnick, String newNick) {
		if (event_irc_nick == false) return;
		plugin.getServer().broadcastMessage(ce + "* #" + oldNick + " is now known as #" + newNick);
	}
	public void onPart(String channel, String sender, String login, String hostnick) {
		if (event_irc_part == false) return;
		// Can't pass the leave reason to here because PircBot doesn't support it.
		plugin.getServer().broadcastMessage(ce + "* #" + sender + " left " + channel);
	}
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostnick, String reason) {
		if (event_irc_quit == false) return;
		String message = "";
		if (!reason.isEmpty()) message = ": " + reason;
		plugin.getServer().broadcastMessage(ce + "* #" + sourceNick + " quit IRC" + Color.toMC(message));
	}
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		if (recipientNick.equals(super.getNick())) {
			// Self was kicked - attempt to rejoin.
			joinChannel();
		}
		
		if (event_irc_kick == false) return;
		String message = "";
		if (!reason.isEmpty()) message = ": " + reason;
		plugin.getServer().broadcastMessage(ck + "* #" + recipientNick + " was kicked by #" + kickerNick + Color.toMC(message));
	}
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		if (event_irc_topic == false) return;
		if (!changed) return; // Don't want the original topic
		plugin.getServer().broadcastMessage(ce + "* #" + setBy + " changed the topic to: " + Color.toMC(topic));
	}
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		if (event_irc_mode == false) return;
		plugin.getServer().broadcastMessage("* #" + sourceNick + " set mode " + mode);
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
	public String getChannel() {
		return channel;
	}
	
	private boolean isCommand(String sender, String message) {
		// Place IRC commands in here. Return true if it was a command.
		// Returning true causes the line to disappear.
		// Returning false causes the line to be shown in Minecraft.
		
		// Player list
		if (message.toLowerCase().startsWith("!players")) {
			Player p[] = plugin.getServer().getOnlinePlayers();
			String o;
			int n = p.length;
			o = "There " + (n==1?"is ":"are ") + n + " player" + (n==1?"":"s") + " connected" + (n==0?".":":");
			for (int i=0; i<p.length; i++) o += " " + p[i].getDisplayName();
			super.sendMessage(channel, o);
			if (event_irc_chat)
				plugin.getServer().broadcastMessage(ce + "* #" + sender + " asked for the player list");
			return true;
		}
		
		return false;
	}
	
}
