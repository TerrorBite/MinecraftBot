package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.Formatting;
import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MBListener;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.IRCMessage;
import com.sorcix.sirc.Channel;
import com.sorcix.sirc.IrcAdaptor;
import com.sorcix.sirc.IrcConnection;
import com.sorcix.sirc.User;
import java.util.ArrayList;
import org.bukkit.entity.Player;

public class IRCListener extends IrcAdaptor {
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
    public void onConnect(IrcConnection irc) {
        // Join channel
        manager.joinChannel();
        
        IrcConnection server = manager.getServer();
        
        // Check for any nick issues
        String currentnick = server.getClient().getNick();
        String desirednick = manager.config.get(Keys.connection.nick);
        String nickpass = manager.config.get(Keys.connection.nick_password);
        if (currentnick.equals(desirednick)) { // All good; Must authenticate
            if (!nickpass.isEmpty())
                server.createUser("NickServ").send("IDENTIFY " + nickpass);
        } else { // Not good - we have a different nick
            if (!nickpass.isEmpty()) { // Use the password to ghost the other nick
                plugin.log(0, "The desired nick appears to be taken. Attempting to retake it...");
                server.createUser("NickServ").send("GHOST " + desirednick + " " + nickpass);
                try {Thread.sleep(3000);} catch (InterruptedException ex) {}
                server.setNick(desirednick);
                try {Thread.sleep(3000);} catch (InterruptedException ex) {}
                
                currentnick = server.getClient().getNick();
                if (currentnick.equals(desirednick)) { // All good; Must authenticate
                    plugin.log(0, "Nick successfully retaken.");
                    server.createUser("NickServ").send("IDENTIFY " + nickpass);
                } else {
                    plugin.log(2, "Failed to retake nick. Current nick: " + currentnick);
                }
            } else { // No password. Nothing can be done.
                plugin.log(1, "The desired nick is already taken. Current nick: " + currentnick);
            }
        }
    }

    @Override
    public void onDisconnect(IrcConnection irc) {
        plugin.log((autoreconnect?1:0), "Disconnected.");
        if (autoreconnect) manager.connect();
        else autoreconnect = true;
    }
    public boolean autoreconnect = true;
    
    // With most events, the channel is checked. This is because it's possible for an IRC
    // op to force the bot into another channel. This bot should only be concerned with
    // what's happening in one channel.

    @Override
    public void onMessage(IrcConnection irc, User sender, Channel target, String message) {
        if (!target.equals(manager.getChannel())) return;
        if (isCommand(sender, message)) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = sender.getNick();
        msg.message = message;
        
        send(Keys.line_to_minecraft.chat, msg);
    }
    
    @Override
    public void onAction(IrcConnection irc, User sender, Channel target, String action) {
        if (!target.equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = sender.getNick();
        msg.message = action;
        send(Keys.line_to_minecraft.action, msg);
    }

    
    @Override
    public void onJoin(IrcConnection irc, Channel channel, User user) {
        IRCMessage msg = new IRCMessage();
        msg.name = user.getNick();
        if (channel == null) msg.channel = manager.config.get(Keys.connection.channel);
        else msg.channel = channel.getName();
        send(Keys.line_to_minecraft.join, msg);
    }

    @Override
    public void onPart(IrcConnection irc, Channel channel, User user, String message) {
        IRCMessage msg = new IRCMessage();
        msg.name = user.getNick();
        if (channel == null) msg.channel = manager.config.get(Keys.connection.channel);
        else msg.channel = channel.getName();
        if (message != null) msg.reason = message;
        send(Keys.line_to_minecraft.part, msg);
    }
    
    @Override
    public void onQuit(IrcConnection irc, User user, String message) {
        IRCMessage msg = new IRCMessage();
        msg.name = user.getNick();
        msg.reason = message;
        send(Keys.line_to_minecraft.quit, msg);
    }
    
    @Override
    public void onKick(IrcConnection irc, Channel channel, User sender, User user, String message) {
        if (!channel.equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.kicker = sender.getNick();
        msg.name = user.getNick();
        msg.reason = message;
        send(Keys.line_to_minecraft.kick, msg);
    }
    
    @Override
    public void onNick(IrcConnection irc, User oldUser, User newUser) {
        IRCMessage msg = new IRCMessage();
        msg.oldname = oldUser.getNick();
        msg.name = newUser.getNick();
        send(Keys.line_to_minecraft.nick_change, msg);
    }

    @Override
    public void onMode(IrcConnection irc, Channel channel, User sender, String mode) {
        if (!channel.equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        if (sender != null) msg.name = sender.getNick();
        else msg.name = "(could not get nick)";
        msg.mode = mode;
        send(Keys.line_to_minecraft.mode_change, msg);
    }
    
    @Override
    public void onTopic(IrcConnection irc, Channel channel, User sender, String topic) {
        if (sender == null) return; // Only looking for new topics
        if (!channel.equals(manager.getChannel())) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = sender.getNick();
        msg.topic = topic;
        send(Keys.line_to_minecraft.topic_change, msg);
    }

    
    /**
     * Checks if the message is actually a command.
     * @param sender The User that sent the message
     * @param message The message that is sent
     * @return True if the message was a command. If true, stop the message.
     */
    private boolean isCommand(User sender, String message) {
        // Player list
        if (message.toLowerCase().startsWith("!players")) {
            Player p[] = plugin.getServer().getOnlinePlayers();
            String o;
            int n = p.length;
            o = "There " + (n==1?"is ":"are ") + n + " player" + (n==1?"":"s") + " connected" + (n==0?".":":");
            for (int i=0; i<p.length; i++) o += " " + p[i].getDisplayName();
            manager.sendMessage(Formatting.toIRC(o));
            
            if (plugin.config.settingsB(Keys.settings.show_players_command)) {
                // Notify Minecraft players that someone used this command
                IRCMessage msg = new IRCMessage();
                msg.name += sender.getNick();
                msg.message = "asked for the player list";
                send(Keys.line_to_minecraft.action, msg);
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
