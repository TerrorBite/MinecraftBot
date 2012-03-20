package me.rafa652.minecraftbot;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftBot extends JavaPlugin {
    private Logger log = Logger.getLogger("Minecraft");
    
    // Not instantiating yet because they use config
    public IRCHandler bot;
    
    // Configuration values
    private String bot_quitmessage;
    private boolean event_mc_opinfo;
    
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        
        MinecraftBotConfiguration config = new MinecraftBotConfiguration(this);
        
        // If config works, get everything started
        if (config.isGood()) {
            bot_quitmessage = config.bot_quitmessage;
            event_mc_opinfo = config.event_mc_opinfo;
            
            pm.registerEvents(new MCHandler(this, config), this);

            bot = new IRCHandler(this, config);
            bot.connect();
        } else {
            pm.disablePlugin(this);
        }
    }
    
    public void onDisable() {
        if (bot != null) {
            bot.attempt_reconnect = false;
            bot.quitServer(bot_quitmessage);
            bot.dispose();
        }
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        String command = cmd.getName().toLowerCase();

        // Get player list
        if (command.equals("names") || command.equals("n")) {
            sender.sendMessage(bot.userlist());
            return true;
        }
        
        // Admin IRC commands
        if (command.equals("irc")) {
            // Note: args[0] is what comes after "irc"
            // args[1] is what comes after subcommand
            if (args.length < 1) return false;
            
            boolean showusage = (args.length == 1);
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("op")) {
                if (!permitted(sender, "op")) return true;
                if (showusage) sender.sendMessage("/op nick");
                else bot.op(bot.getChannel(), args[1]);
            }
            else if (subcommand.equals("deop")) {
                if (!permitted(sender, "op")) return true;
                if (showusage) sender.sendMessage("/deop nick");
                else bot.deOp(bot.getChannel(), args[1]);
            }
            else if (subcommand.equals("voice")) {
                if (!permitted(sender, "voice")) return true;
                if (showusage) sender.sendMessage("/voice nick");
                else bot.voice(bot.getChannel(), args[1]);
            }
            else if (subcommand.equals("devoice")) {
                if (!permitted(sender, "voice")) return true;
                if (showusage) sender.sendMessage("/devoice nick");
                else bot.deVoice(bot.getChannel(), args[1]);
            }
            else if (subcommand.equals("kick")) {
                if (!permitted(sender, "kick")) return true;
                if (showusage) sender.sendMessage("/kick nick [reason]");
                else {
                    String reason = "";
                    for (int i=2;i<args.length;i++) reason += args[i] + " ";
                    if (reason.length() > 0) reason = reason.substring(0, reason.length()-1);
                    bot.kick(bot.getChannel(), args[1], reason);
                }
            }
            else if (subcommand.equals("connect")) {
                if (!permitted(sender, "manage")) return true;
                if (bot.isConnected()) sender.sendMessage("Already connected to IRC!");
                else bot.connect();
            }
            else if (subcommand.equals("rejoin")) {
                if (!permitted(sender, "manage")) return true;
                if (!bot.isConnected()) sender.sendMessage("Not connected to IRC!");
                else bot.joinChannel();
            }
            else if (subcommand.equals("disconnect")) {
                if (!permitted(sender, "manage")) return true;
                if (!bot.isConnected()) sender.sendMessage("Not connected to IRC!");
                else {
                    String quitmsg = "";
                    for (int i=2; i<args.length; i++)
                        quitmsg += args[i] + " ";
                    // Use default if quit message is blank
                    bot.attempt_reconnect = false;
                    bot.quitServer((quitmsg.isEmpty()?bot_quitmessage:quitmsg.substring(0, quitmsg.length()-1)));
                }
            }
            else return false; // to show /irc usage
            
            return true;
        }
        
        return false;
    }
    
    private boolean permitted(CommandSender sender, String permission) {
        if (sender instanceof ConsoleCommandSender) return true;
        boolean p = (sender.hasPermission("minecraftbot." + permission));
        
        if (!p) sender.sendMessage(Color.RED.mc + "You are not permitted to use this command.");
        return p;
    }
    
    /**
     * Sends to logger and in-game ops. Prepends [MinecraftBot] to it.
     * @param type 1 for warning, 2 for severe. Anything else will be info.
     * @param message The message to send to the log
     */
    public void log(int type, String message) {
        String l = "[MinecraftBot] " + message;
        
        if (type == 1) log.warning(l);
        else if (type == 2) log.severe(l);
        else log.info(l);
        
        if (event_mc_opinfo) {
            // gray color
            l = Color.GRAY.mc + l;
            
            for (Player p : getServer().getOnlinePlayers())
                if (p.isOp() || p.hasPermission("minecraftbot.manage"))
                    p.sendMessage(l);
        }
    }
}
