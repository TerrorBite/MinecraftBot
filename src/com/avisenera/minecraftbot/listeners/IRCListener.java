package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.Configuration;
import com.avisenera.minecraftbot.Formatting;
import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.IRCMessage;
import org.bukkit.entity.Player;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 * This class handles the IRC connection.
 */
public class IRCListener extends PircBot implements Runnable {
    private MinecraftBot plugin;
    private Configuration config;
    
    // Always attempt to reconnect on disconnect unless this should stay disconnected.
    public boolean autoreconnect = true;
    
    // Configuration values (they are never changed by /minecraftbot reload)
    private final String c_server;
    private final String c_server_password;
    private final int c_server_port;
    private final int retries;
    private final String c_channel;
    private final String c_channel_key;
    private final String c_nick;
    private final String c_nick_password;
    
    public IRCListener(MinecraftBot instance, Configuration config) {
        plugin = instance;
        
        c_server = config.connection(Keys.connection.server);
        c_server_password = config.connection(Keys.connection.server_password);
        c_server_port = Integer.parseInt(config.connection(Keys.connection.server_port));
        c_channel = config.connection(Keys.connection.channel);
        c_channel_key = config.connection(Keys.connection.channel_key);
        c_nick = config.connection(Keys.connection.nick);
        c_nick_password = config.connection(Keys.connection.nick_password);
        String fc_retries = config.connection(Keys.connection.retries);
        int retry = 5;
        try {
            retry = Integer.parseInt(fc_retries);
            if (retry < 1) retry = 5;
        } catch (Exception e) {}
        retries = retry;
        
        this.config = config;
        
        int delay;
        try {
            delay = Integer.parseInt(config.connection(Keys.connection.bot_message_delay));
            if (delay < 0) delay = 1000;
        } catch (NumberFormatException e) {
            delay = 1000;
        }
        
        this.setMessageDelay(delay);
        
        // Initializing some other things
        this.setLogin(c_nick);
        this.setAutoNickChange(true);
        this.setFinger("That's very nice of you, but I'm not interested.");
        this.setVersion("MinecraftBot v" + plugin.getDescription().getVersion() +
                " - https://github.com/Rafa652/MinecraftBot");
    }
    
    /**
     * Begins attempting to connect to the server, if it isn't already connected.
     * The rest of the connection routine is in run().
     */
    public synchronized void connect() {
        if (isConnected()) {
            plugin.log(0, "Attempted to connect to IRC while already connected.");
            plugin.log(0, "To force reconnecting, reload the plugin.");
        }
        else plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, this);
    }
    // Used to prevent the connection method from running more than once at a time
    private boolean busyconnecting = false;
    @Override
    public void run() {
        if (busyconnecting) return;
        busyconnecting = true;

        int attempt = 0;
        
        super.setName(c_nick);
        
        while (attempt < retries) {
            attempt++;
            try {
                if (isConnected()) break; // Already connected
                plugin.log(0, "Connecting to " + c_server + "... (Attempt " + attempt + ")");
                if (c_server_password.isEmpty()) this.connect(c_server, c_server_port);
                else this.connect(c_server, c_server_port, c_server_password);
                plugin.log(0, "Connected to "+this.getServer());
                
                checkCurrentNick();
                joinChannel();
                break;
            } catch (NickAlreadyInUseException e) { // ignore this exception
            } catch (Exception e) {
                plugin.log(1, "Failed to connect: " + e.getMessage());
                try {
                    if (attempt < retries) Thread.sleep(5000);
                } catch (InterruptedException e1) {}
                continue;
            }
        }
        
        if (!isConnected())
            plugin.log(2, "Failed to connect to the server. Enter '/minecraftbot connect' to try again.");
        
        busyconnecting = false;
    }
    private synchronized void checkCurrentNick() {
        // Should ONLY be called by run()
        // Checks the current nick and does whatever it can to fix any problems.
        
        boolean hasNickPass = !(c_nick_password.isEmpty());
        
        if (this.getNick().equalsIgnoreCase(c_nick)) {
            if (hasNickPass) this.identify(c_nick_password);
            return;
        }
        
        if (hasNickPass) {
            plugin.log(0, "The nick '"+c_nick+"' appears to be taken. Attempting to retake it...");
            this.sendMessage("NickServ", "GHOST "+c_nick+" "+c_nick_password);
            try {Thread.sleep(3000);} catch (InterruptedException ex) {}
            this.changeNick(c_nick);
            try {Thread.sleep(3000);} catch (InterruptedException ex) {}
            
            if (this.getNick().equalsIgnoreCase(c_nick)) {
                plugin.log(0, "Nick successfully retaken. Current bot nick: " + this.getNick());
                this.identify(c_nick_password);
            } else {
                plugin.log(1, "Failed to retake nick. Current bot nick: " + this.getNick());
            }
        } else {
            plugin.log(1, "The nick '"+c_nick+"' appears to be taken. Current bot nick: " + this.getNick());
        }
        
    }
    @Override
    protected void onDisconnect() {
        plugin.log((autoreconnect?1:0), "Disconnected.");
        if (autoreconnect) connect();
        else autoreconnect = true;
    }
    
    /**
     * Gets a user's full nick. Full nick meaning, the nick with the prefix included.
     * @param nick The nick to check
     * @return The full nick, including a prefix if one exists.
     */
    public String getFullNick(String nick) {
        if (nick == null || nick.isEmpty()) return "";
        
        // If the prefix is already there, don't do anything
        switch (nick.charAt(0)) {
            case '~':
            case '&':
            case '@':
            case '%':
            case '+':
                return nick;
            default: break;
        }
        
        // Go through all users and find that one nick
        for (User u : this.getUsers(c_channel)) {
            if (u.getNick().equalsIgnoreCase(nick)) return u.toString();
        }
        
        // Nothing found
        return nick; 
    }

    /**
     * Sends a message to the IRC channel
     * @param message The message to send to the channel
     */
    public void sendMessage(String message) {
        this.sendMessage(c_channel, message);
    }
    
    /**
     * Sends an action (/me) to the IRC channel
     * @param action The action to send to the channel
     */
    public void sendAction(String action) {
        this.sendAction(c_channel, action);
    }
    
    public int usercount() {
        return this.getUsers(c_channel).length;
    }
    
    // Some methods for CommandListener
    
    public String userlist() {
        // Returns a list of users on IRC.
        User list[] = this.getUsers(c_channel);
        String nicks = c_channel + ":";
        
        // Raised the limit to 40 names due to being able to scroll the chat window now
        if (list.length <= 40)
            for (User u : list) nicks += " " + u.toString();
        else
            nicks += " "+list.length+" people - too many to list here.";
        
        return nicks;
    }
    
    public void op(String nick) {
        op(c_channel, nick);
    }
    public void deOp(String nick) {
        deOp(c_channel, nick);
    }
    public void voice(String nick) {
        voice(c_channel, nick);
    }
    public void deVoice(String nick) {
        deVoice(c_channel, nick);
    }
    public void doKick(String nick, String reason) {
        // Why is that method final?
        kick(c_channel, nick, reason);
    }
    public void joinChannel() {
        // Joins the channel defined in the configuration
        if (c_channel_key.isEmpty()) joinChannel(c_channel);
        else joinChannel(c_channel, c_channel_key);
    }
    public void partChannel() {
        partChannel(c_channel);
    }
    
    // Now for the IRC events
    
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        // There's the chance that some IRC op could force the bot to join another channel
        if (!channel.equalsIgnoreCase(c_channel)) return;
        
        if (isCommand(sender, message)) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = sender;
        msg.message = message;
        plugin.send.toMinecraft(Keys.line_to_minecraft.chat, msg);
    }
    
    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action) {
        if (!target.equalsIgnoreCase(c_channel)) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = sender;
        msg.message = action;
        plugin.send.toMinecraft(Keys.line_to_minecraft.action, msg);
    }
    
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        // Not checking for the channel here - everyone will know if it was forced into another channel
        
        IRCMessage msg = new IRCMessage();
        msg.name = sender;
        msg.channel = channel;
        plugin.send.toMinecraft(Keys.line_to_minecraft.join, msg);
    }
    
    @Override
    protected void onPart(String channel, String sender, String login, String hostname) {
        if (!channel.equalsIgnoreCase(c_channel)) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = sender;
        msg.channel = channel;
        plugin.send.toMinecraft(Keys.line_to_minecraft.part, msg);
    }
    
    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        IRCMessage msg = new IRCMessage();
        msg.name = sourceNick;
        msg.reason = reason;
        plugin.send.toMinecraft(Keys.line_to_minecraft.quit, msg);
    }
    
    @Override
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        if (!channel.equalsIgnoreCase(c_channel)) return;
        
        IRCMessage msg = new IRCMessage();
        msg.kicker = kickerNick;
        msg.name = recipientNick;
        msg.reason = reason;
        plugin.send.toMinecraft(Keys.line_to_minecraft.kick, msg);
    }
    
    @Override
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
        IRCMessage msg = new IRCMessage();
        msg.oldname = oldNick;
        msg.name = newNick;
        plugin.send.toMinecraft(Keys.line_to_minecraft.nick_change, msg);
    }
    
    @Override
    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        if (!channel.equalsIgnoreCase(c_channel)) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = sourceNick;
        msg.mode = mode;
        plugin.send.toMinecraft(Keys.line_to_minecraft.mode_change, msg);
    }
    
    @Override
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        if (!changed) return;
        if (!channel.equalsIgnoreCase(c_channel)) return;
        
        IRCMessage msg = new IRCMessage();
        msg.name = setBy;
        msg.topic = topic;
        plugin.send.toMinecraft(Keys.line_to_minecraft.topic_change, msg);
    }

    private boolean isCommand(String sender, String message) {
        // Place IRC commands in here. Return true if it was a command.
        // Returning true causes the line to disappear.
        // Returning false causes the line to be shown in Minecraft.
        
        // Player list
        if (message.toLowerCase().startsWith("!players")) {
            Player p[] = plugin.getServer().getOnlinePlayers();
            String o;
            int n = p.length;
            o = "There " + (n==1?"is ":"are ") + n + " player" + (n==1?"":"s") + " connected" + (n==0?".":":");
            for (int i=0; i<p.length; i++) o += " " + p[i].getDisplayName();
            sendMessage(Formatting.toIRC(o));
            
            if (config.settingsB(Keys.settings.show_players_command)) {
                // Notify Minecraft players that someone used this command
                IRCMessage msg = new IRCMessage();
                msg.name = sender;
                msg.message = "asked for the player list";
                plugin.send.toMinecraft(Keys.line_to_minecraft.action, msg);
            }
            
            return true;
        }
        
        return false;
    }
}
