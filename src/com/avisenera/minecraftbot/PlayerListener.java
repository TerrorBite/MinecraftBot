package com.avisenera.minecraftbot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

public class PlayerListener implements Listener {
    private MinecraftBot plugin;
    
    public PlayerListener(MinecraftBot instance) {
        plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        String check = event.getCommand().toLowerCase();
        Message msg = new Message();
        
        if(check.startsWith("say ")) {
            msg.message = event.getCommand().split("\\s+", 2)[1];
            plugin.send.toIRC(Keys.line_to_irc.chat, msg);
        }
        
        // Plugins like Essentials and CommandBook have a "broadcast"
        // command which is similar to the console /say.
        else if(check.startsWith("broadcast ")) {
            msg.message = event.getCommand().split("\\s+", 2)[1];
            plugin.send.toIRC(Keys.line_to_irc.chat, msg);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // The command /me can't be registered normally, so it's handled here instead
        if (event.getMessage().toLowerCase().startsWith("/me")) {
            try {
                Message msg = new Message();
                msg.name = event.getPlayer().getDisplayName();
                msg.message = event.getMessage().substring(4); // cuts off space after /me
                plugin.send.toIRC(Keys.line_to_irc.action, msg);
            }
            catch (IndexOutOfBoundsException e) {
                // ignore blank messages
            }
            
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        Message msg = new Message();
        
        msg.name = event.getPlayer().getDisplayName();
        msg.message = event.getMessage();
        plugin.send.toIRC(Keys.line_to_irc.chat, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Message msg = new Message();
        
        msg.name = event.getPlayer().getDisplayName();
        plugin.send.toIRC(Keys.line_to_irc.join, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Message msg = new Message();
        
        msg.name = event.getPlayer().getDisplayName();
        plugin.send.toIRC(Keys.line_to_irc.leave, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        Message msg = new Message();
        msg.name = event.getPlayer().getDisplayName();
        msg.reason = event.getReason();
        
        plugin.send.toIRC(Keys.line_to_irc.kick, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Check if death message is null or blank - if yes, it was cancelled.
        String dm = event.getDeathMessage();
        if (dm == null || dm.isEmpty()) return;
        
        Message msg = new Message();
        msg.message = dm;
        plugin.send.toIRC(Keys.line_to_irc.death, msg);
    }
}
