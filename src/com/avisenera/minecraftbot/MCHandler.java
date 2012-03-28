package com.avisenera.minecraftbot;

import com.avisenera.minecraftbot.Relayer.EventType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * This class handles every event coming from Minecraft that needs to be looked at.
 * It possibly sends these events to IRC.
 */
public class MCHandler implements Listener {
    private MinecraftBot plugin;
    
    // Config values
    private boolean event_mc_server;
    private boolean event_mc_chat;
    private boolean event_mc_me;
    private boolean event_mc_join;
    private boolean event_mc_leave;
    private boolean event_mc_kick;
    private boolean event_mc_death;
    
    public MCHandler(MinecraftBot instance, MinecraftBotConfiguration config) {
        plugin = instance;
        
        event_mc_server = config.event_mc_server;
        event_mc_chat = config.event_mc_chat;
        event_mc_me = config.event_mc_me;
        event_mc_join = config.event_mc_join;
        event_mc_leave = config.event_mc_leave;
        event_mc_kick = config.event_mc_kick;
        event_mc_death = config.event_mc_death;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        if (event_mc_server == false) return;
        String check = event.getCommand().toLowerCase();
        
        if(check.startsWith("say ")) {
            String msg = event.getCommand().split("\\s+", 2)[1];
            plugin.send.chatToIRC("*Console", msg);
        }
        
        // Plugins like Essentials and CommandBook have a "broadcast"
        // command which is similar to the console /say.
        else if(check.startsWith("broadcast ")) {
            String msg = event.getCommand().split("\\s+", 2)[1];
            plugin.send.chatToIRC("*Broadcast", msg);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // Third person /me
        if (event.getMessage().toLowerCase().startsWith("/me")) {
            if (event_mc_me == false) return;
            if (!event.getPlayer().hasPermission("minecraftbot.me")) return;

            try {
                // cuts off space after /me
                plugin.send.actionToIRC(event.getPlayer().getDisplayName(), event.getMessage().substring(4));
            }
            catch (IndexOutOfBoundsException e) {
                // ignore blank messages
            }
            
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        if (event_mc_chat == false) return;
        plugin.send.chatToIRC(event.getPlayer().getDisplayName(), event.getMessage());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event_mc_join == false) return;
        plugin.send.eventToIRC(EventType.Event, event.getPlayer().getDisplayName(), "joined the game");
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event_mc_leave == false) return;
        plugin.send.eventToIRC(EventType.Event, event.getPlayer().getDisplayName(), "left the game");
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        if (event_mc_kick == false) return;
        plugin.send.eventToIRC(EventType.Kick, event.getPlayer().getDisplayName(),
                "was kicked from the game: " + event.getReason());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event_mc_death == false) return;
        
        if (event instanceof PlayerDeathEvent) {
            PlayerDeathEvent death = (PlayerDeathEvent)event;
            
            // Get death message. Split it apart to re-add the death color after the name
            String deathmessage[] = death.getDeathMessage().split("\\s+", 2);
            
            plugin.send.eventToIRC(EventType.Death, deathmessage[0], deathmessage[1]);
        }
    }
}
