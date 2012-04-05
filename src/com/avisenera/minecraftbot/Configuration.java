package com.avisenera.minecraftbot;

import java.io.*;
import java.util.EnumMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * This class manages the configuration values.
 * When it starts, it reads the configuration file and holds on
 * to all its contents.<br>
 * Other classes should always get the necessary values from this class
 * because the values will change when the configuration is reloaded.
 */
public class Configuration {
    private MinecraftBot plugin;
    
    private boolean valid = false;
    
    private EnumMap<Keys.connection, String> connection;
    private EnumMap<Keys.relay_to_irc, Boolean> relay_to_irc;
    private EnumMap<Keys.relay_to_minecraft, Boolean> relay_to_minecraft;
    private EnumMap<Keys.line_to_irc, String> line_to_irc;
    private EnumMap<Keys.line_to_minecraft, String> line_to_minecraft;

    /**
     * When instantiating this class, the configuration is not loaded.<br>
     * You must load the configuration by using {@link reload()}.
     */
    public Configuration(MinecraftBot instance) {
        plugin = instance;
    }
    
    /**
     * (Re)loads values from the configuration file. If an error occurs,
     * the current configuration values are not changed.
     * @return False if an error occured.
     */
    public boolean reload() {
        FileConfiguration config = getConfigFile(plugin);
        if (config == null) return false;
        
        EnumMap<Keys.connection, String> new_c = new EnumMap<Keys.connection, String>(Keys.connection.class);
        EnumMap<Keys.relay_to_irc, Boolean> new_rti = new EnumMap<Keys.relay_to_irc, Boolean>(Keys.relay_to_irc.class);
        EnumMap<Keys.relay_to_minecraft, Boolean> new_rtm = new EnumMap<Keys.relay_to_minecraft, Boolean>(Keys.relay_to_minecraft.class);
        EnumMap<Keys.line_to_irc, String> new_lti = new EnumMap<Keys.line_to_irc, String>(Keys.line_to_irc.class);
        EnumMap<Keys.line_to_minecraft, String> new_ltm = new EnumMap<Keys.line_to_minecraft, String>(Keys.line_to_minecraft.class);
        boolean accept = true;
        
        for (Keys.connection c : Keys.connection.values())
            new_c.put(c, config.getString("connection."+c));
        for (Keys.relay_to_irc c : Keys.relay_to_irc.values())
            new_rti.put(c, config.getBoolean("relay.to_irc."+c));
        for (Keys.relay_to_minecraft c : Keys.relay_to_minecraft.values())
            new_rtm.put(c, config.getBoolean("relay.to_minecraft."+c));
        for (Keys.line_to_irc c : Keys.line_to_irc.values())
            new_lti.put(c, config.getString("line_formatting.to_irc."+c));
        for (Keys.line_to_minecraft c : Keys.line_to_minecraft.values())
            new_ltm.put(c, config.getString("line_formatting.to_minecraft."+c));
        
        // Checking for all required values
        String scheck;
        scheck = new_c.get(Keys.connection.server);
        if (scheck == null || scheck.isEmpty()) {
            plugin.log(2, "The server to connect to is not defined.");
            accept = false;
        }
        scheck = new_c.get(Keys.connection.server_port);
        try {
            int icheck = Integer.parseInt(scheck);
            if (icheck > 65535 || icheck < 1) {
                plugin.log(2, "An invalid port number was defined in the configuration file.");
                accept = false;
            }
        } catch (NumberFormatException e) {
            plugin.log(2, "An invalid port number was defined in the configuration file.");
            accept = false;
        }
        
        if (accept) {
            connection = new_c;
            relay_to_irc = new_rti;
            relay_to_minecraft = new_rtm;
            line_to_irc = new_lti;
            line_to_minecraft = new_ltm;
            valid = true;
            plugin.log(0, "Configuration has been loaded.");
        }
        
        return valid;
    }
    
    /**
     * Checks to see if this object is usable. It's usable when it has data in it.
     * @return False if this object isn't holding any configuration values.
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Returns the given connection value in the configuration file.
     * @param value Equivalent to config.getString("connection.(value)")
     */
    public String connection(Keys.connection value) {
        return connection.get(value);
    }
    
    /**
     * Returns the given relay setting in the configuration file.
     * @param value Equivalent to config.getBoolean("relay.to_irc.(value)")
     */
    public boolean relay_to_irc(Keys.relay_to_irc value) {
        return relay_to_irc.get(value).booleanValue();
    }
    
    /**
     * Returns the given relay setting in the configuration file.
     * @param value Equivalent to config.getBoolean("relay.to_minecraft.(value)")
     */
    public boolean relay_to_minecraft(Keys.relay_to_minecraft value) {
        return relay_to_minecraft.get(value).booleanValue();
    }
    
    /**
     * Returns the formatting string value that was entered in the configuration file.
     * @param value Equivalent to config.getString("line_formatting.to_irc.(value)")
     */
    public String line_to_irc(Keys.line_to_irc value) {
        return line_to_irc.get(value);
    }
    
    /**
     * Returns the formatting string value that was entered in the configuration file.
     * @param value Equivalent to config.getString("line_formatting.to_irc.(value)")
     */
    public String line_to_minecraft(Keys.line_to_minecraft value) {
        return line_to_minecraft.get(value);
    }
    
    /**
     * Gets the configuration file. If the file does not exist, it tries to
     * create it. This method sends log information in case an error occurs.
     * @return null if the file was just created or an error occured.
     */
    private FileConfiguration getConfigFile(MinecraftBot plugin) {
        // Checks if the config file exists. If not, creates it.
        // Returns false if an error occured.
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            plugin.getConfig().load(new File(plugin.getDataFolder(), "config.yml"));
            return plugin.getConfig();
        } catch (FileNotFoundException e) {
            plugin.log(0, "No config file found. Creating a default configuration file.");
            plugin.log(0, "You must edit this file before being able to use this plugin.");
            saveFile(plugin);
        } catch (IOException e) {
            plugin.log(2, "Error while loading config! Check if config.yml or the plugins folder is writable.");
        } catch (InvalidConfigurationException e) {
            plugin.log(2, "Configuration is invalid. Check your syntax. (Remove any tab characters.)");
        }
        return null;
    }
    
    private void saveFile(MinecraftBot plugin) {
        try
        {
            File conf = new File(plugin.getDataFolder(), "config.yml");
            
            InputStream is = this.getClass().getResourceAsStream("/config.yml");
            if (!conf.exists())
                conf.createNewFile();
            OutputStream os = new FileOutputStream(conf);
            
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0)
                os.write(buf, 0, len);

            is.close();
            os.close();
        }
        catch (IOException e)
        {
            plugin.log(2, "Failed to save config.yml - Check the plugin's data directory!");
        }
        catch (NullPointerException e)
        {
            plugin.log(2, "Could not find the default config.yml! Is it in the .jar?");
        }
    }
}
