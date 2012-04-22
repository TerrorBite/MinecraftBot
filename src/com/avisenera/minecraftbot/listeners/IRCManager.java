package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MBListener;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.IRCMessage;
import com.sorcix.sirc.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;

/**
 * Manages the connection to the IRC server. 
 */
public class IRCManager implements Runnable {
    private MinecraftBot plugin;
    
    private IrcConnection server;
    private IRCListener listener;
    EnumMap<Keys.connection, String> config;
    
    // Only listeners in this package should have access to server and channel
    IrcConnection getServer() { return server; }
    Channel getChannel() {
        // An annoying thing about sIRC - after joining a channel, we need to get the channel object again.
        // The one used to join the channel doesn't update as soon as it's actually joined.
        // This returns the object if it already exists, or else it creates a new one.
        Iterator<Channel> chs = server.getChannels();
        while (chs.hasNext()) {
            Channel c = chs.next();
            if (c.getName().equalsIgnoreCase(config.get(Keys.connection.channel))) {
                return c;
            }
        }
        
        return server.createChannel(config.get(Keys.connection.channel));
    }
    
    public IRCManager(MinecraftBot instance, ArrayList<MBListener> listeners) {
        this.plugin = instance;
        this.listener = new IRCListener(instance, this, listeners);
        server = new IrcConnection();
        server.setCharset(Charset.forName("UTF-8"));
        server.setVersion("MinecraftBot v" + plugin.getDescription().getVersion() +
                " - https://github.com/Rafa652/MinecraftBot");
        
        server.addMessageListener(listener);
        server.addServerListener(listener);
    }
    
    /**
     * Begins attempting to connect to the server, if it isn't already connected.
     */
    public synchronized void connect() {
        if (server.isConnected()) {
            plugin.log(0, "Attempted to connect to IRC while already connected.");
            plugin.log(0, "To force reconnecting, reload the plugin.");
        }
        else plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, this);
    }
    
    /**
     * Quits the IRC server.
     * @param message The message to use when quitting the server
     */
    public void disconnect(String message) {
        listener.autoreconnect = false;
        server.disconnect(message);
    }
    
    private boolean busyconnecting = false;
    @Override
    public void run() {
        if (busyconnecting) return;
        
        busyconnecting = true;
        start();
        busyconnecting = false;
    }
    
    /**
     * Begins the connection to IRC. This method should never be called directly. Use run() instead.
     */
    private synchronized void start() {
        // Get current config
        config = plugin.config.connection();
        
        // IrcServer doesn't expect an empty string but does expect null
        String serverpass = config.get(Keys.connection.server_password);
        if (serverpass.isEmpty()) serverpass = null;
        IrcServer connection = new IrcServer(
                config.get(Keys.connection.server),
                Integer.parseInt(config.get(Keys.connection.server_port)),
                serverpass,
                config.get(Keys.connection.use_ssl).equalsIgnoreCase("true"));
        
        start(connection, config.get(Keys.connection.nick), 1,
                Integer.parseInt(config.get(Keys.connection.retries)));
    }
    
    /**
     * Attempts to connect to the IRC server.
     * @param connection IrcConnection object containing all the connection details
     * @param nick The nick to connect with
     * @param current The connect attempt number
     * @param maxtries The max number of connect attempts
     */
    private void start(IrcServer connection, String nick, int current, int maxtries) {
        if (current > maxtries) {
            plugin.log(2, "Exceeded number of reconnect attempts. Failed to connect to IRC.");
            return;
        } else if (current > 1) { // Wait 5 seconds before another attempt
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {}
        } else {
            // Set up server
            server.setServer(connection);
            server.setMessageDelay(Integer.parseInt(config.get(Keys.connection.bot_message_delay)));
        }
        server.setNick(nick);
        
        plugin.log(0, "Connecting to " + connection.getAddress() + "... (Attempt " + current + ")");
        
        try {
            server.connect();
        } catch (UnknownHostException ex) {
            plugin.log(1, "Failed to connect: Unable to find server.");
            start(connection, nick, (current + 1), maxtries);
        } catch (IOException ex) {
            plugin.log(1, "Failed to connect: " + ex.getMessage());
            start(connection, nick, (current + 1), maxtries);
        } catch (NickNameException ex) {
            plugin.log(1, "Failed to connect: Nick is not available. Connecting using a different nick.");
            start(connection, nick + "_", (current + 1), maxtries);
        }
        // Listener's onConnect() takes over from here
    }
    
// Other methods
    public int usercount() {
        try {
            int count = 0;
            Iterator users = getChannel().getUsers();
            while (users.hasNext()) {
                users.next();
                count++;
            }
            return count;
        } catch (NullPointerException ex) {
            return 0;
        }
    }
    
// Methods used by commands
    /**
     * Gets the list of all users in the channel.
     * @return Formatted list of users to display to the player. 30 nicks maximum.
     */
    public String userlist() {
        int totalnicks = usercount();
        int displayed = (totalnicks>30?30:totalnicks);
        String list = "Displaying "+displayed+" out of "+totalnicks+" nicks in "+getChannel().getName()+":";
        try {
            Iterator<User> users = getChannel().getUsers();
            int count = 1;
            while (users.hasNext()) {
                if (count > 30) break;
                User user = users.next();
                list += " " + user.getNick();
                count++;
            }
        } catch (NullPointerException ex) {
            list += "An error occured when getting the user list.";
        }
        
        return list;
    }
    
    public String getNick() {
        return server.getClient().getNick();
    }
    
    public void joinChannel() {
        String ckey = config.get(Keys.connection.channel_key);
        Channel ch = server.createChannel(config.get(Keys.connection.channel));
        if (ckey.isEmpty()) ch.join();
        else ch.join(ckey);
    }
    public void partChannel() {
        getChannel().part();
    }
    
    public void op(String nick) {
        getChannel().setMode("+o " + nick);
    }
    public void deop(String nick) {
        getChannel().setMode("-o " + nick);
    }
    public void voice(String nick) {
        getChannel().setMode("+v " + nick);
    }
    public void devoice(String nick) {
        getChannel().setMode("-v " + nick);
    }
    public void kick(String nick, String reason) {
        try {
            getChannel().kick(getUser(nick), reason);
        } catch (NullPointerException ex) {}
    }
    public void ban(String nick) {
        try {
            getChannel().ban(getUser(nick), false);
        } catch (NullPointerException ex) {}
    }
    public void unban(String hostmask) {
        getChannel().setMode("-b " + hostmask);
    }
    
    public void sendMessage(String message) {
        getChannel().sendMessage(message);
    }
    public void sendAction(String message) {
        getChannel().sendAction(message);
    }
    
    /**
     * Sends a message to IRC and to the listeners.
     */
    public void sendMessage(Keys.line_to_minecraft format, IRCMessage message) {
        listener.send(format, message);
        
        if (format != Keys.line_to_minecraft.action) sendMessage(message.message);
        else sendAction(message.message);
    }
    
    private User getUser(String nick) {
        // See comments for getChannel() - it applies to users too
        // createUser(nick, channel) does not work
        Iterator<User> u = getChannel().getUsers();
        while (u.hasNext()) {
            User us = u.next();
            if (us.getNick().equalsIgnoreCase(nick)) {
                return us;
            }
        }
        return null;
    }
}
