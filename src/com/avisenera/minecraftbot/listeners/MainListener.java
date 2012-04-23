package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MBListener;
import com.avisenera.minecraftbot.MetricsLineCount;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.MCMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * This class is the main purpose of this plugin.
 * It relays messages between IRC and Minecraft using the simple API provided by this plugin.
 */
public class MainListener extends MBListener implements Listener {
    private MinecraftBot plugin;
    private MetricsLineCount metrics;
    
    public MainListener(MinecraftBot instance, MetricsLineCount mlc) {
        plugin = instance;
        metrics = mlc;
    }

    @Override
    public void onMessage(String line) {
        // Received IRC message - sending it to the game
        plugin.getServer().broadcastMessage(line);
        metrics.increment();
    }
    
    private void send(Keys.line_to_irc format, MCMessage message) {
        // Sending MC event to IRC
        String line = plugin.getFormatter().toIRC(format, message);
        if (line == null) return;
        this.sendToIRC(line, false);
        metrics.increment();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        String check = event.getCommand().toLowerCase();
        
        if(check.startsWith("say ")) {
            MCMessage msg = new MCMessage();
            msg.message = event.getCommand().split("\\s+", 2)[1];
            send(Keys.line_to_irc.server, msg);
        }
        
        // Plugins like Essentials and CommandBook have a "broadcast"
        // command which is similar to the console /say.
        else if(check.startsWith("broadcast ")) {
            MCMessage msg = new MCMessage();
            msg.message = event.getCommand().split("\\s+", 2)[1];
            send(Keys.line_to_irc.server, msg);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // The command /me can't be registered normally, so it's handled here instead
        if (event.getMessage().toLowerCase().startsWith("/me")) {
            try {
                MCMessage msg = new MCMessage();
                msg.player = event.getPlayer();
                msg.name = event.getPlayer().getDisplayName();
                msg.message = event.getMessage().substring(4); // cuts off space after /me
                send(Keys.line_to_irc.action, msg);
            }
            catch (IndexOutOfBoundsException e) {
                // ignore blank messages
            }
            
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        MCMessage msg = new MCMessage();
        msg.player = event.getPlayer();
        msg.name = event.getPlayer().getDisplayName();
        msg.message = event.getMessage();
        send(Keys.line_to_irc.chat, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        MCMessage msg = new MCMessage();
        msg.player = event.getPlayer();
        msg.name = event.getPlayer().getDisplayName();
        send(Keys.line_to_irc.join, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        MCMessage msg = new MCMessage();
        msg.player = event.getPlayer();
        msg.name = event.getPlayer().getDisplayName();
        send(Keys.line_to_irc.leave, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        MCMessage msg = new MCMessage();
        msg.player = event.getPlayer();
        msg.name = event.getPlayer().getDisplayName();
        msg.reason = event.getReason();
        
        send(Keys.line_to_irc.kick, msg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Check if death message is null or blank - if yes, it was cancelled.
        String dm = event.getDeathMessage();
        if (dm == null || dm.isEmpty()) return;
        
        MCMessage msg = new MCMessage();
        msg.player = event.getEntity();
        msg.message = dm;
        send(Keys.line_to_irc.death, msg);
    }
}
