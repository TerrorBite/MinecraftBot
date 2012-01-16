package me.rafa652.minecraftbot;

import org.bukkit.event.server.ServerListener;
import org.bukkit.event.server.ServerCommandEvent;

public class ServerConsoleHandler extends ServerListener {
	public static MinecraftBot plugin; 
	
	// Values from config
	private boolean event_mc_server;
	
	public ServerConsoleHandler(MinecraftBot instance, MinecraftBotConfiguration config) {
		plugin = instance;
		event_mc_server = config.event_mc_server;
	}
	
	@Override
	public void onServerCommand(ServerCommandEvent event) {
		if (event_mc_server == false) return;
		String check = event.getCommand().toLowerCase();
		
		if(check.startsWith("say ")) {
			String msg = event.getCommand().split("\\s+", 2)[1];
			plugin.bot.sendMessage("<*Console> " + msg);
		}
		
		// Plugins like Essentials and CommandBook have a "broadcast"
		// command which is similar to the console /say.
		else if(check.startsWith("broadcast ")) {
			String msg = event.getCommand().split("\\s+", 2)[1];
			plugin.bot.sendMessage("{Broadcast} " + msg);
		}
	}

}