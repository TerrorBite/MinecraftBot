package me.rafa652.minecraftbot;

import me.rafa652.minecraftbot.MinecraftBotConfiguration.ColorContext;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerChatHandler extends PlayerListener {
	
	public static MinecraftBot plugin;
	
	// Values from config
	private String ce; // color for event
	private String ck; // color for kick
	private String icm; // IRC Color Me
	private ChatColor mcm; // MC Color Me
	private boolean event_mc_chat;
	private boolean event_mc_join;
	private boolean event_mc_leave;
	private boolean event_mc_kick;
	
	public PlayerChatHandler(MinecraftBot instance, MinecraftBotConfiguration config) {
		plugin = instance;
		
		ce = config.getIRCColor(ColorContext.Event);
		ck = config.getIRCColor(ColorContext.Kick);
		icm = config.getIRCColor(ColorContext.Me);
		mcm = config.getChatColor(ColorContext.Me);
		event_mc_chat = config.event_mc_chat;
		event_mc_join = config.event_mc_join;
		event_mc_leave = config.event_mc_leave;
		event_mc_kick = config.event_mc_kick;
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled()) return;
		if (event_mc_chat == false) return;
		plugin.bot.sendMessage("<" + event.getPlayer().getDisplayName() + "> " + event.getMessage());
	}
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event_mc_join == false) return;
		plugin.bot.sendMessage(ce + "* " + event.getPlayer().getDisplayName() + " joined the game");
	}
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (event_mc_leave == false) return;
		plugin.bot.sendMessage(ce + "* " + event.getPlayer().getDisplayName() + " left the game");
	}
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) return;
		if (event_mc_kick == false) return;
		plugin.bot.sendMessage(ck + "* " + event.getPlayer().getDisplayName() + " was kicked from the game: " + event.getReason());
	}
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		// Highest priority events are called before Monitor priority events
		// This is Highest because it can modify (cancel) the event
		// Monitor priority events apparently aren't supposed to modify anything
		if (event.isCancelled()) return;
		if (event_mc_chat == false) return;
		
		String args[] = event.getMessage().split(" ");
		
		// Third person /me
		if (args.length > 0 && args[0].equalsIgnoreCase("/me")) {
			String message = "* " + event.getPlayer().getDisplayName();
			for (int i=1; i<args.length; i++) message += " " + args[i];
			
			plugin.bot.sendMessage(icm + message); // To IRC
			plugin.getServer().broadcastMessage(mcm + message); // To Minecraft
			
			event.setCancelled(true);
		}
	}
}
