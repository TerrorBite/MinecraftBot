package me.rafa652.minecraftbot;

import org.bukkit.event.server.ServerListener;
import org.bukkit.event.server.ServerCommandEvent;

public class ServerConsoleHandler extends ServerListener {
	
	public static MinecraftBot plugin; 
	
	public ServerConsoleHandler(MinecraftBot instance) {
		plugin = instance;
	}
	
	@Override
	public void onServerCommand(ServerCommandEvent event) {
		String check = event.getCommand().toLowerCase();
		if (plugin.config.event_mc_server == false) return;
		
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