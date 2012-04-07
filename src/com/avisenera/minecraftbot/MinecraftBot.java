package com.avisenera.minecraftbot;

import com.avisenera.minecraftbot.listeners.CommandListener;
import com.avisenera.minecraftbot.listeners.IRCListener;
import com.avisenera.minecraftbot.listeners.PlayerListener;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftBot extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft");
    
    private Configuration config;
    private IRCListener ircListener;
    private PlayerListener playerListener;
    private CommandListener commandListener;
    
    public LineSender send;
    
    @Override
    public void onEnable() {
        config = new Configuration(this);
        ircListener = new IRCListener(this, config);
        playerListener = new PlayerListener(this);
        commandListener = new CommandListener(this, config, ircListener);
        
        send = new LineSender(this, config, ircListener);
        
        boolean goodconfig = config.reload();
        if (goodconfig) {
            // Maybe send a bit of data
            if (config.settings(Keys.settings.ping_developer).equalsIgnoreCase("true"))
                MetricsSender.send("CB_"+getServer().getBukkitVersion(), this.getDescription().getVersion());
            
            // Get everything started
            getServer().getPluginManager().registerEvents(playerListener, this);
            getCommand("n").setExecutor(commandListener);
            getCommand("names").setExecutor(commandListener);
            getCommand("irc").setExecutor(commandListener);
            getCommand("minecraftbot").setExecutor(commandListener);
        } else {
            log(2, "Error loading the configuration. Reload the plugin to try again.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        String qm = config.settings(Keys.settings.quit_message);
        
        ircListener.autoreconnect = false;
        ircListener.quitServer(qm);
        try {ircListener.dispose();}
        catch (Exception e) {/*Exception is thrown if the IRC threads haven't started*/}
    }
    
    public void log(int level, String message) {
        message = "[MinecraftBot] " + message;
        
        if (level == 1) logger.warning(message);
        else if (level == 2) logger.severe(message);
        else logger.info(message);
        
        if (config.settings(Keys.settings.send_log_to_ops).equalsIgnoreCase("true"))
            for (Player p : this.getServer().getOnlinePlayers())
                if (p.hasPermission("minecraftbot.manage"))
                    p.sendMessage(Formatting.GRAY + message);
    }
}
