package com.avisenera.minecraftbot;

import com.avisenera.minecraftbot.hooks.Hook;
import com.avisenera.minecraftbot.listeners.CommandListener;
import com.avisenera.minecraftbot.listeners.IRCManager;
import com.avisenera.minecraftbot.listeners.PlayerListener;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftBot extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft");
    
    public Configuration config;
    private IRCManager irc;
    private PlayerListener playerListener;
    private CommandListener commandListener;
    
    public LineSender send;
    
    @Override
    public void onEnable() {
        config = new Configuration(this);

        if (config.load()) { // If configuration properly loaded
            // Initialize everything
            irc = new IRCManager(this);
            playerListener = new PlayerListener(this);
            commandListener = new CommandListener(this, irc);
            send = new LineSender(this, irc);
            
            // Register everything
            getServer().getPluginManager().registerEvents(playerListener, this);
            getCommand("n").setExecutor(commandListener);
            getCommand("names").setExecutor(commandListener);
            getCommand("irc").setExecutor(commandListener);
            getCommand("minecraftbot").setExecutor(commandListener);
            
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
    
    // Several metrics methods
    LinesRelayedCount lrc = new LinesRelayedCount();
    
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
            metrics.addCustomData(lrc);
            
            // Get stats on all available hooks and their usage
            Metrics.Graph ghooks = metrics.createGraph("Hooks used");
            for (final String hook : Hook.available_hooks) {
                ghooks.addPlotter(new Metrics.Plotter(hook) {
                    @Override
                    public int getValue() {
                        return config.metricsCountHooks(hook);
                    }
                });
            }
            
            metrics.start();
        } catch (IOException ex) {
            // Ignore errors
        }
    }
    
    class LinesRelayedCount extends Metrics.Plotter {
        public LinesRelayedCount() {
            super("Lines Relayed");
            this.count = 0;
        }
        private int count;
        
        @Override
        public int getValue() {
            return this.count;
        }
        
        @Override
        public void reset() {
            this.count = 0;
        }
        
        public void increment() {
            this.count++;
        }
    }
}
