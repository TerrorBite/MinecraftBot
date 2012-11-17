package com.avisenera.minecraftbot.listeners;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Set;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UtilSSLSocketFactory;

import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MBListener;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.IRCMessage;

/**
 * Manages the connection to the IRC server. 
 */
public class IRCManager implements Runnable {
    public MinecraftBot plugin;
    
    private PircBotX bot;
    private IRCListener listener;
    EnumMap<Keys.connection, String> config;
    
    // Only listeners in this package should have access to server and channel
    PircBotX getServer() { return bot; }
    Channel getChannel() {
        return bot.getChannel(config.get(Keys.connection.channel));
    }
    
    public IRCManager(MinecraftBot instance, ArrayList<MBListener> listeners) {
        this.plugin = instance;
        this.listener = new IRCListener(instance, this, listeners);
        
        bot = new PircBotX();
        bot.setAutoNickChange(true);
        bot.setLogin("MinecraftBot");
        bot.setFinger("What are you doing? Stop it.");
        bot.setVersion("MinecraftBot v" + plugin.getDescription().getVersion() +
                " - https://github.com/TerrorBite/MinecraftBot");
        
        bot.getListenerManager().addListener(listener);
    }
    
    /**
     * Begins attempting to connect to the server, if it isn't already connected.
     */
    public synchronized void connect() {
        if (bot.isConnected()) {
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
        bot.quitServer(message);
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
        config = plugin.config.connection(); // Get a copy of the current configuration
        bot.setMessageDelay(Integer.parseInt(config.get(Keys.connection.bot_message_delay)));
        bot.setName(config.get(Keys.connection.nick));
        start(
                config.get(Keys.connection.server),
                Integer.parseInt(config.get(Keys.connection.server_port)),
                config.get(Keys.connection.server_password),
                (config.get(Keys.connection.use_ssl).equalsIgnoreCase("true")),
                1,
                Integer.parseInt(config.get(Keys.connection.retries))
                );
    }
    
    /**
     * Attempts to connect to the IRC server.
     * @param server The server name to connect to
     * @param port The port nubmer to connect to
     * @param password Server password, or a blank string if none
     * @param ssl Connecting through SSL?
     * @param current The connect attempt number
     * @param maxtries The max number of connect attempts
     */
    private void start(String server, int port, String password, boolean ssl, int current, int maxtries) {
        if (current > maxtries) {
            plugin.log(2, "Exceeded number of reconnect attempts. Failed to connect to IRC.");
            return;
        } else if (current > 1) { // Wait 5 seconds before another attempt
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {}
        }
        
        plugin.log(0, "Connecting to " + server + "... (Attempt " + current + ")");
        
        try {
            if (password.isEmpty()) {
                if (ssl) bot.connect(server, port, new UtilSSLSocketFactory().trustAllCertificates());
                else bot.connect(server, port);
            } else {
                if (ssl) bot.connect(server, port, password, new UtilSSLSocketFactory().trustAllCertificates());
                else bot.connect(server, port, password);
            }
        } catch (Exception ex) {
            plugin.log(1, "Failed to connect: " + ex.getMessage());
        }
        if (!bot.isConnected()) start(server, port, password, ssl, (current + 1), maxtries);
        // Listener's onConnect() takes over from here
    }
    
// Other methods
    public int usercount() {
        return getChannel().getUsers().size();
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
            Iterator<User> users = getChannel().getUsers().iterator();
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
        return bot.getUserBot().getNick();
    }
    
    public void joinChannel() {
        String channel = config.get(Keys.connection.channel);
        String key = config.get(Keys.connection.channel_key);
        if (key.isEmpty()) bot.joinChannel(channel);
        else bot.joinChannel(channel, key);
    }
    public void partChannel() {
        bot.partChannel(getChannel());
    }
    
    public void op(String nick) {
        bot.setMode(getChannel(), "+o "+nick);
    }
    public void deop(String nick) {
        bot.setMode(getChannel(), "-o "+nick);
    }
    public void voice(String nick) {
        bot.setMode(getChannel(), "+v "+nick);
    }
    public void devoice(String nick) {
        bot.setMode(getChannel(), "-v "+nick);
    }
    public void kick(String nick) {
        bot.kick(getChannel(), bot.getUser(nick));
    }
    public void kick(String nick, String reason) {
        bot.kick(getChannel(), bot.getUser(nick), reason);
    }
    public void ban(String nick) {
        bot.ban(getChannel(), "*!*@"+bot.getUser(nick).getHostmask());
    }
    public void unban(String hostmask) {
        bot.unBan(getChannel(), hostmask);
    }
    
    public void sendMessage(String message) {
        (new SendThread(message, false)).start();
    }
    public void sendAction(String action) {
        (new SendThread(action, true)).start();
    }
    
    // 1.3.1 introduced AsyncPlayerChatEvent which appears to be interrupted if it happens to take more
    // than an instant to work through. This is a quick, messy workaround to it. A better solution will come later.
    
    private class SendThread extends Thread {
        private final String message;
        private final boolean action;
        public SendThread(String message, boolean action) {
            this.message = message;
            this.action = action;
        }
        
        public void run() {
            if (action) bot.sendAction(getChannel(), message);
            else bot.sendMessage(getChannel(), message);
        }
    }
    
    /**
     * Sends a message to IRC and to the listeners.
     */
    public void sendMessage(Keys.line_to_minecraft format, IRCMessage message) {
        listener.send(format, message);
        
        if (format != Keys.line_to_minecraft.action) sendMessage(message.message);
        else sendAction(message.message);
    }
    
    /**
     * Checks if the specified IRC user has voice in the channel
     * @param nick IRC nickname
     * @return Whether or not user is voiced
     */
    public boolean userHasVoice(String nick) {
    	// Get a reference to the user
    	User u = bot.getUser(nick);
    	// Check if user has voice
    	Set<Channel> channelsVoiceIn = u.getChannelsVoiceIn();
    	if (channelsVoiceIn.contains(getChannel())) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Checks if the specified IRC user has operator in the channel
     * @param nick IRC nickname
     * @return Whether or not user is an IRC operator
     */
    public boolean userHasOp(String nick) {
    	// Get a reference to the user
    	User u = bot.getUser(nick);
    	// Check if user has op
    	Set<Channel> channelsOpIn = u.getChannelsOpIn();
    	if (channelsOpIn.contains(getChannel())) {
    		return true;
    	}
    	return false;
    }
}
