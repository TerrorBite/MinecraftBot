package com.avisenera.minecraftbot;

import java.io.*;
import java.nio.charset.Charset;
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
    private EnumMap<Keys.settings, String> settings;
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
    public boolean load() {
        FileConfiguration config = getConfigFile(plugin);
        if (config == null) return false;
        
        EnumMap<Keys.connection, String> new_c = new EnumMap<Keys.connection, String>(Keys.connection.class);
        EnumMap<Keys.settings, String> new_s = new EnumMap<Keys.settings, String>(Keys.settings.class);
        EnumMap<Keys.line_to_irc, String> new_lti = new EnumMap<Keys.line_to_irc, String>(Keys.line_to_irc.class);
        EnumMap<Keys.line_to_minecraft, String> new_ltm = new EnumMap<Keys.line_to_minecraft, String>(Keys.line_to_minecraft.class);
        
        for (Keys.connection c : Keys.connection.values())
            new_c.put(c, config.getString("connection."+c, ""));
        for (Keys.settings c : Keys.settings.values())
            new_s.put(c, config.getString("settings."+c, ""));
        for (Keys.line_to_irc c : Keys.line_to_irc.values())
            new_lti.put(c, config.getString("line_formatting.to_irc."+c, ""));
        for (Keys.line_to_minecraft c : Keys.line_to_minecraft.values())
            new_ltm.put(c, config.getString("line_formatting.to_minecraft."+c, ""));
        
        boolean accepted = true;
        
        if (!valid) { // First time loading the config - must check for required values
            // Checking for all required values
            String scheck;
            // Server name
            scheck = new_c.get(Keys.connection.server);
            if (scheck == null || scheck.isEmpty()) {
                plugin.log(2, "The server to connect to is not defined.");
                accepted = false;
            }
            // Server port
            scheck = new_c.get(Keys.connection.server_port);
            try {
                int icheck = Integer.parseInt(scheck);
                if (icheck > 65535 || icheck < 1) {
                    plugin.log(2, "An invalid port number was defined in the configuration file.");
                    accepted = false;
                }
            } catch (NumberFormatException e) {
                plugin.log(2, "An invalid port number was defined in the configuration file.");
                accepted = false;
            }
            // Channel name
            scheck = new_c.get(Keys.connection.channel);
            if (scheck == null || scheck.isEmpty()) {
                plugin.log(2, "A channel was not defined in the configuration file.");
                accepted = false;
            }
            // Fix channel name
            if (!scheck.startsWith("#")) {
                scheck = "#" + scheck;
                new_c.put(Keys.connection.channel, scheck);
            }
        }
        
        if (accepted) {
            connection = new_c;
            settings = new_s;
            line_to_irc = new_lti;
            line_to_minecraft = new_ltm;
            plugin.log(0, "Configuration has been loaded.");
            
            valid = true;
        }
        else plugin.log(2, "Configuration did not load successfully.");
        
        return accepted;
    }
    
    /**
     * Returns the given connection value in the configuration file.
     * @param value Equivalent to config.getString("connection.(value)")
     */
    public String connection(Keys.connection value) {
        if (!valid || value == null) return "";
        
        String rv = connection.get(value);
        if (rv == null) return "";
        else return rv;
    }
    
    /**
     * Returns the given settings value in the configuration file.
     * @param value Equivalent to config.getString("settings.(value)")
     */
    public String settings(Keys.settings value) {
        if (!valid || value == null) return "";
        
        String rv = settings.get(value);
        if (rv == null) return "";
        else return rv;
    }
    
    /**
     * Returns the formatting string value that was entered in the configuration file.
     * @param value Equivalent to config.getString("line_formatting.to_irc.(value)")
     */
    public String line_to_irc(Keys.line_to_irc value) {
        if (!valid || value == null) return "";
        
        String rv = line_to_irc.get(value);
        if (rv == null) return "";
        else return rv;
    }
    
    /**
     * Returns the formatting string value that was entered in the configuration file.
     * @param value Equivalent to config.getString("line_formatting.to_irc.(value)")
     */
    public String line_to_minecraft(Keys.line_to_minecraft value) {
        if (!valid || value == null) return "";
        
        String rv = line_to_minecraft.get(value);
        if (rv == null) return "";
        else return rv;
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
