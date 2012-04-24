package com.avisenera.minecraftbot;

import com.avisenera.minecraftbot.hooks.Hook;
import com.avisenera.minecraftbot.listeners.CommandListener;
import com.avisenera.minecraftbot.listeners.IRCManager;
import com.avisenera.minecraftbot.listeners.MainListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftBot extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft");
    
    public Configuration config;
    private IRCManager irc;
    private MainListener playerListener;
    private CommandListener commandListener;
    
    private MessageFormatter format;
    
    private ArrayList<MBListener> extListeners = new ArrayList<MBListener>();
    
    @Override
    public void onEnable() {
        config = new Configuration(this);

        if (config.load()) { // If configuration properly loaded
            // Initialize everything
            irc = new IRCManager(this, extListeners);
            playerListener = new MainListener(this, mlc);
            commandListener = new CommandListener(this, irc);
            format = new MessageFormatter(this);
            
            // Register everything
            getServer().getPluginManager().registerEvents(playerListener, this);
            getCommand("n").setExecutor(commandListener);
            getCommand("names").setExecutor(commandListener);
            getCommand("irc").setExecutor(commandListener);
            getCommand("minecraftbot").setExecutor(commandListener);
            this.registerListener(playerListener);
            
            startMetrics();
            
            // Start the bot
            irc.connect();
        } else {
            log(2, "Error loading the configuration. Reload the plugin to try again.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if (irc != null) {
            String qm = config.settingsS(Keys.settings.quit_message);
            irc.disconnect(qm);
        }
    }
    
    public void log(int level, String message) {
        message = "[MinecraftBot] " + message;
        
        if (level == 1) logger.warning(message);
        else if (level == 2) logger.severe(message);
        else logger.info(message);
        
        if (config.settingsB(Keys.settings.send_log_to_ops))
            for (Player p : this.getServer().getOnlinePlayers())
                if (p.hasPermission("minecraftbot.manage"))
                    p.sendMessage(Formatting.GRAY + message);
    }
    
    /**
     * Returns the message formatter.
     */
    public MessageFormatter getFormatter() {
        return format;
    }
    
    /**
     * Registers a listener. By registering the listener, it will be able to send and receive IRC messages.
     * @param listener The listener object to register
     */
    public void registerListener(MBListener listener) {
        if (!extListeners.contains(listener)) {
            listener.initialize(this, irc);
            extListeners.add(listener);
        }
    }
    /**
     * Removes a listener. They will no longer receive IRC messages.
     * @param listener The listener object to remove
     */
    public void removeListener(MBListener listener) {
        extListeners.remove(listener);
    }
    
    // Metrics
    MetricsLineCount mlc = new MetricsLineCount();
    private void startMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            
            // Send number of users on the IRC channel
            metrics.addCustomData(new Metrics.Plotter("IRC Users") {
                @Override
                public int getValue() {
                    return irc.usercount();
                }
            });
            
            // Get stats on how often LineSender is being used ("Lines Relayed" count)
            metrics.addCustomData(mlc);
            
            metrics.start();
        } catch (IOException ex) {
            // Ignore errors
        }
    }
}
