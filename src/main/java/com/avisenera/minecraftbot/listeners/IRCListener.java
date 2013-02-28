package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.Formatting;
import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MBListener;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.IRCMessage;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

@SuppressWarnings("rawtypes")
public class IRCListener extends ListenerAdapter {
    private MinecraftBot plugin;
    private IRCManager manager;
    private ArrayList<MBListener> extListeners;
    IRCListener(MinecraftBot instance, IRCManager irc, ArrayList<MBListener> listeners) {
        plugin = instance;
        manager = irc;
        extListeners = listeners;
    }
    
    // Server-related handlers
    @Override
    public void onConnect(ConnectEvent event) {
        // Check for any nick issues
        nickCheck();
        
        // Join channel
        manager.joinChannel();
    }
    private void nickCheck() {
        String nick = manager.config.get(Keys.connection.nick);
        String nickpass = manager.config.get(Keys.connection.nick_password);
        if (nickpass.isEmpty()) return;
        
        // Have nickpass - must identify
        if (manager.getServer().getUserBot().getNick().equals(nick)) {
            manager.getServer().sendMessage("NickServ", "IDENTIFY "+nickpass);
            pause(2000);
            return;
        }
        
        // Ghosting
        manager.getServer().sendMessage("NickServ", "GHOST "+nick+" "+nickpass);
        pause(2000);
        manager.getServer().changeNick(nick);
        pause(2000);
        
        // Try again
        if (manager.getServer().getUserBot().getNick().equals(nick)) {
            manager.getServer().sendMessage("NickServ", "IDENTIFY "+nickpass);
            pause(2000);
        }
        // If the original nick is still not being used, nothing is done.
    }
    private void pause(long millis) { // It's annoying having to surround everything in try-catch statements.
        try { Thread.sleep(millis); }
        catch (InterruptedException ex) { }
    }

    @Override
    public void onDisconnect(DisconnectEvent e) {
        plugin.log((autoreconnect?1:0), "Disconnected.");
        if (autoreconnect) manager.connect();
        else autoreconnect = true;
    }
    public boolean autoreconnect = true;
    
    // With most events, the channel is checked. This is because it's possible for an IRC
    // op to force the bot into another channel. This bot should only be concerned with
    // what's happening in one channel.
    // The exception is on joins and parts.

    @Override
    public void onMessage(MessageEvent e) {
        if (!e.getChannel().equals(manager.getChannel())) return;
        if (isCommand(e.getUser().getNick(), e.getMessage())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = e.getUser().getNick();
        msg.message = e.getMessage();
        
        send(Keys.line_to_minecraft.chat, msg);
    }
    
    @Override
    public void onAction(ActionEvent e) {
        if (!e.getChannel().equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = e.getUser().getNick();
        msg.message = e.getAction();
        send(Keys.line_to_minecraft.action, msg);
    }

    
    @Override
    public void onJoin(JoinEvent e) {
        IRCMessage msg = new IRCMessage();
        msg.name = e.getUser().getNick();
        msg.channel = e.getChannel().getName();
        send(Keys.line_to_minecraft.join, msg);
    }

    @Override
    public void onPart(PartEvent e) {
        IRCMessage msg = new IRCMessage();
        msg.name = e.getUser().getNick();
        msg.channel = e.getChannel().getName();
        if (e.getReason() != null) msg.reason = e.getReason();
        send(Keys.line_to_minecraft.part, msg);
    }
    
    @Override
    public void onQuit(QuitEvent e) {
        IRCMessage msg = new IRCMessage();
        msg.name = e.getUser().getNick();
        msg.reason = e.getReason();
        send(Keys.line_to_minecraft.quit, msg);
    }
    
    @Override
    public void onKick(KickEvent e) {
        if (!e.getChannel().equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.kicker = e.getSource().getNick();
        msg.name = e.getRecipient().getNick();
        msg.reason = e.getReason();
        send(Keys.line_to_minecraft.kick, msg);
    }
    
    @Override
    public void onNickChange(NickChangeEvent e) {
        IRCMessage msg = new IRCMessage();
        msg.oldname = e.getOldNick();
        msg.name = e.getNewNick();
        send(Keys.line_to_minecraft.nick_change, msg);
    }

    @Override
    public void onMode(ModeEvent e) {
        if (!e.getChannel().equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = e.getUser().getNick();
        msg.mode = e.getMode();
        send(Keys.line_to_minecraft.mode_change, msg);
    }
    
    @Override
    public void onTopic(TopicEvent e) {
        if (!e.isChanged()) return; // Only looking for new topics
        if (!e.getChannel().equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = e.getUser().getNick();
        msg.topic = e.getTopic();
        send(Keys.line_to_minecraft.topic_change, msg);
    }

    
    /**
     * Checks if the message is actually a command.
     * @param sender The User that sent the message
     * @param message The message that is sent
     * @return True if the message was a command. If true, stop the message.
     */
    private boolean isCommand(String sender, String message) {
        // Player list
        if (message.toLowerCase().startsWith("!players") && plugin.config.commandsB(Keys.commands.players)) {
            Player p[] = plugin.getServer().getOnlinePlayers();
            String o;
            int n = p.length;
            o = "There " + (n==1?"is ":"are ") + n + " player" + (n==1?"":"s") + " connected" + (n==0?".":":");
            for (int i=0; i<p.length; i++) o += " " + p[i].getDisplayName();
            manager.sendMessage(Formatting.toIRC(o));
            
            if (plugin.config.commandsB(Keys.commands.show_to_mc)) {
                // Notify Minecraft players that someone used this command
                IRCMessage msg = new IRCMessage();
                msg.name += sender;
                msg.message = "viewed the player list";
                send(Keys.line_to_minecraft.action, msg);
            }
            
            return true;
        }
        
        // Show world time
        if (message.toLowerCase().startsWith("!time") && plugin.config.commandsB(Keys.commands.time)) {
        	String worldtimes = "";
        	for (World w : plugin.getServer().getWorlds()) {
        		// Only get time from normal environments
        		if (!(w.getEnvironment() == World.Environment.NORMAL)) continue;
        		worldtimes += ", "+w.getName()+": ";
        		
        		int hr = 0; int min = 0; float time = w.getTime();
        		// Correct the time so 0600 corresponds to morning
        		time += 6000;
        		if (time >= 24000) time -= 24000;
        		// Format the time
        		while (time >= 1000) { hr++; time -= 1000; } // 1000 units for each hour
        		while (time >= 16.7) { min++; time -= 16.7; } // 16 2/3 units for each minute
        		worldtimes += String.format("%02d:%02d", hr, min);
        	}
        	manager.sendMessage(Formatting.toIRC(worldtimes.substring(2)));
        	
        	if (plugin.config.commandsB(Keys.commands.show_to_mc)) {
                // Notify Minecraft players that someone used this command
                IRCMessage msg = new IRCMessage();
                msg.name += sender;
                msg.message = "viewed the time";
                send(Keys.line_to_minecraft.action, msg);
            }
        	
        	return true;
        }
        
        // Kick a player
        if (message.toLowerCase().startsWith("!mckick") && plugin.config.commandsB(Keys.commands.mckick)) {
        	// Divide the command up into its parts ([0] command, [1] target player, [2] kick reason)
        	String[] parts = message.split(" ", 3);
        	if (manager.userHasOp(sender) && parts.length >= 2) {
        		// Kick player
        		Player playerToKick = Bukkit.getServer().getPlayer(parts[1]);
        		String kickReason = (parts.length == 3) ? parts[2] : "Kicked!";
        		playerToKick.kickPlayer(kickReason);
        		
        		if (plugin.config.commandsB(Keys.commands.show_to_mc)) {
                    // Notify Minecraft players that someone used this command
                    IRCMessage msg = new IRCMessage();
                    msg.name += sender;
                    msg.message = "kicked "+playerToKick.getDisplayName()+" from IRC: "+kickReason;
                    send(Keys.line_to_minecraft.action, msg);
                }
        	}
        	
        	return true;
        }
        
        // Ban a player
        if (message.toLowerCase().startsWith("!mcban") && plugin.config.commandsB(Keys.commands.mcban)) {
        	// Divide the command up into its parts ([0] command, [1] target player)
        	String[] parts = message.split(" ", 2);
        	if (manager.userHasOp(sender) && parts.length == 2) {
        		// Ban player
        		Player playerToBan = Bukkit.getServer().getPlayer(parts[1]);
        		String banReason = "Banned!";
        		playerToBan.setBanned(true);
        		playerToBan.kickPlayer(banReason);
        		
        		if (plugin.config.commandsB(Keys.commands.show_to_mc)) {
                    // Notify Minecraft players that someone used this command
                    IRCMessage msg = new IRCMessage();
                    msg.name += sender;
                    msg.message = "banned "+playerToBan.getDisplayName()+" from IRC: "+banReason;
                    send(Keys.line_to_minecraft.action, msg);
                }
        	}
        	
        	return true;
        }
        
        return false;
    }
    
    /**
     * Passes an IRC message to the listeners.
     * @param format The formatting string the message should use
     * @param message The message object that contains formatting values
     */
    void send(Keys.line_to_minecraft format, IRCMessage message) {
        String send = plugin.getFormatter().toMinecraft(format, message);
        if (send == null) return; // Blank line - ignore
        
        for (MBListener l : extListeners) {
            l.onMessage(send);
        }
    }
}
