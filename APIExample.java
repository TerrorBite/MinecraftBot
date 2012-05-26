import com.avisenera.minecraftbot.MBListener;
import com.avisenera.minecraftbot.MinecraftBot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * This is an example that shows a plugin using this API.
 * 
 * The simple API in MinecraftBot allows another plugin to receive the same IRC message that are being
 * sent to the game as well as allowing the plugin to send their own messages.
 * 
 * This API should be enough for other plugins to use. Even MinecraftBot uses this same system
 * to do its main function of passing messages back and forth between IRC and Minecraft.
 */
public class APIExample extends JavaPlugin implements Listener {
    
    // The listener class must inherit from the MBListener class.
    // The MBListener class contains three methods: onMessage, sendToIRC and unregister.
    private class IRC extends MBListener {
        
        @Override
        public void onMessage(String line) {
            // This method is called by MinecraftBot when an IRC line is received. The way the
            // line is formatted depends on the user's configuration. This method is also only
            // called if the IRC event wasn't cancelled by the user using the configuration file.
            
            // If the user didn't change the configuration file, the string would look similar to
            // "#> <Someone> Hey there."
            
            // You can override this method so that your plugin can process the incoming line.
        }
    }
    
    private IRC irc;
    // Before a listener becomes useful, it must be registered.
    
    @Override
    public void onEnable() {
        irc = new IRC();
        MinecraftBot mb = (MinecraftBot) this.getServer().getPluginManager().getPlugin("MinecraftBot");
        mb.registerListener(irc); // It is now able to send and receive from MinecraftBot.
        
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
        // There are two ways to remove the listener from MinecraftBot.
        irc.unregister(); // The easiest is calling unregister() from MBListener
        // The other way is to call removeListener() from MinecraftBot's main class
    }
    
    // The listener's sendToIRC method is public.
    // The first parameter is the line to send to IRC and the second controls whether to send it
    // as a regular message or as an action.
    // Anything sent to IRC using this method will not show up in Minecraft's chat. Only in IRC.
    
    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        // Here, it sends a regular message to IRC when someone gets in a bed.
        irc.sendToIRC(e.getPlayer().getDisplayName() + " got into a bed.", false);
        // "<MinecraftBot> Player got into a bed."
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // Here it sends an action (/me) to IRC
        irc.sendToIRC("notices " + e.getEntity().getDisplayName() + "'s death", true);
        // "* MinecraftBot notices Player's death"
    }
}
