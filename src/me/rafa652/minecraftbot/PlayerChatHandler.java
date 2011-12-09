package me.rafa652.minecraftbot;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerChatHandler extends PlayerListener {
	
	public static MinecraftBot plugin;
	
	// Colors are defined at the top with very short names just so
	// they're easier to find and change if necessary.
	
	// Normal events are 10 (teal, dark aqua), except kick which is 4 (red)
	// Chat messages have no color.
	// For reference, /me is 6 (purple, dark purple)
	
	final String ce = "\u000310"; // color for event (normal)
	final String ck = "\u000304"; // color for kick
	
	public PlayerChatHandler(MinecraftBot instance) {
		plugin = instance;
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		plugin.bot.sendMessage("<" + event.getPlayer().getDisplayName() + "> " + event.getMessage());
	}
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.bot.sendMessage(ce + "* " + event.getPlayer().getDisplayName() + " joined the game");
	}
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.bot.sendMessage(ce + "* " + event.getPlayer().getDisplayName() + " left the game");
	}
	public void onPlayerKick(PlayerKickEvent event) {
		plugin.bot.sendMessage(ck + "*" + event.getPlayer().getDisplayName() + " was kicked from the game: " + event.getReason());
	}
}
