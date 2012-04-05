package com.avisenera.minecraftbot;

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
        playerListener = new PlayerListener(this, config);
        commandListener = new CommandListener(this, config);
        ircListener = new IRCListener(this, config);
        
        send = new LineSender(this, config, ircListener);
        
        boolean goodconfig = config.reload();
        if (goodconfig) {
            // TODO start the IRC bot, register listeners
        } else {
            // figure out how to make only the reload command work
        }
    }
    
    @Override
    public void onDisable() {
        String qm;
        qm = config.connection(Keys.connection.quit_message);
        if (qm == null) qm = "";
        
        ircListener.autoreconnect = false;
        ircListener.quitServer(qm);
        ircListener.dispose();
    }
    
    public void log(int level, String message) {
        message = "[MinecraftBot] " + message;
        
        if (level == 1) logger.warning(message);
        else if (level == 2) logger.severe(message);
        else logger.info(message);
        
        if (!config.relay_to_minecraft(Keys.relay_to_minecraft.plugin_log))
            for (Player p : this.getServer().getOnlinePlayers())
                if (p.hasPermission("minecraftbot.manage"))
                    p.sendMessage(Formatting.GRAY + message);
    }
}
