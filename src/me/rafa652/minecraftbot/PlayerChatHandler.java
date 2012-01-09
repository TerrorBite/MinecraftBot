package me.rafa652.minecraftbot;

import me.rafa652.minecraftbot.MinecraftBotConfiguration.ColorContext;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerChatHandler extends PlayerListener {
	
	public static MinecraftBot plugin;
	
	final String ce; // color for event
	final String ck; // color for kick
	
	public PlayerChatHandler(MinecraftBot instance) {
		plugin = instance;
		
		ce = plugin.config.getIRCColor(ColorContext.Event);
		ck = plugin.config.getIRCColor(ColorContext.Kick);
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled()) return;
		if (plugin.config.event_mc_chat == false) return;
		plugin.bot.sendMessage("<" + event.getPlayer().getDisplayName() + "> " + event.getMessage());
	}
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.config.event_mc_join == false) return;
		plugin.bot.sendMessage(ce + "* " + event.getPlayer().getDisplayName() + " joined the game");
	}
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.config.event_mc_leave == false) return;
		plugin.bot.sendMessage(ce + "* " + event.getPlayer().getDisplayName() + " left the game");
	}
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) return;
		if (plugin.config.event_mc_kick == false) return;
		plugin.bot.sendMessage(ck + "* " + event.getPlayer().getDisplayName() + " was kicked from the game: " + event.getReason());
	}
}
