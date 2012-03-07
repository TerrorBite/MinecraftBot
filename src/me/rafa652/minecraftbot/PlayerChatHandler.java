package me.rafa652.minecraftbot;

import me.rafa652.minecraftbot.MinecraftBotConfiguration.ColorContext;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerChatHandler implements Listener {
	
	public static MinecraftBot plugin;
	
	// Values from config
	private String ce; // color for event
	private String ck; // color for kick
	private boolean event_mc_chat;
	private boolean event_mc_me;
	private boolean event_mc_join;
	private boolean event_mc_leave;
	private boolean event_mc_kick;
	
	public PlayerChatHandler(MinecraftBot instance, MinecraftBotConfiguration config) {
		plugin = instance;
		
		ce = config.getIRCColor(ColorContext.Event);
		ck = config.getIRCColor(ColorContext.Kick);
		event_mc_chat = config.event_mc_chat;
		event_mc_me = config.event_mc_me;
		event_mc_join = config.event_mc_join;
		event_mc_leave = config.event_mc_leave;
		event_mc_kick = config.event_mc_kick;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled()) return;
		if (event_mc_chat == false) return;
		String playername = Color.toIRC(event.getPlayer().getDisplayName()); // In case of colorful names
		plugin.bot.sendMessage("<" + playername + "> " + event.getMessage());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event_mc_join == false) return;
		String playername = Color.toIRC(event.getPlayer().getDisplayName());
		plugin.bot.sendMessage(ce + "* " + playername + ce + " joined the game");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (event_mc_leave == false) return;
		String playername = Color.toIRC(event.getPlayer().getDisplayName());
		plugin.bot.sendMessage(ce + "* " + playername + ce + " left the game");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) return;
		if (event_mc_kick == false) return;
		String playername = Color.toIRC(event.getPlayer().getDisplayName());
		plugin.bot.sendMessage(ck + "* " + playername + ck + " was kicked from the game: " + event.getReason());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		// Highest priority events are called before Monitor priority events
		// This is Highest because it can modify (cancel) the event
		// Monitor priority events apparently aren't supposed to modify anything
		if (event.isCancelled()) return;
		if (event_mc_me == false) return;
		
		if (!event.getPlayer().hasPermission("minecraftbot.me")) {
			return;
		}
		
		// Third person /me
		if (event.getMessage().toLowerCase().startsWith("/me ")) {
			String c = event.getMessage();
			if (c.length() < 3) {
				event.setCancelled(true);
				return;
			}
			String playername = Color.toIRC(event.getPlayer().getDisplayName());
			String message = "* " + playername;
			message += c.substring(3); // starts at the space after /me
			
			plugin.bot.sendMessage(message); // To IRC
			plugin.getServer().broadcastMessage(message); // To Minecraft
			
			event.setCancelled(true);
		}
	}
	
}
