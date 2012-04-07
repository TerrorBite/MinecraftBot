package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.Configuration;
import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.IRCMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListener implements CommandExecutor {
    private MinecraftBot plugin;
    private Configuration config;
    private IRCListener bot;
    public CommandListener(MinecraftBot instance, Configuration cfg, IRCListener irc) {
        plugin = instance;
        config = cfg;
        bot = irc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String command = cmd.getName().toLowerCase();
        
        if (command.equals("irc")) return irc(sender, args);
        
        else if (command.equals("minecraftbot")) return minecraftbot(sender, args);
        
        else if (command.equals("n") || command.equals("names")) {
            sender.sendMessage(bot.userlist());
            return true;
        }
        return false;
    }
    
    // The /irc command
    private boolean irc(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecraftbot.op")) return true;
        if (args.length < 1) return false;
        
        String cmd = args[0].toLowerCase();
        
        if (cmd.equals("say")) {
            if (args.length > 1) {
                String fullmsg = "";
                for (int i=1;i<args.length;i++)
                    fullmsg += args[i] + " ";
                plugin.send.rawToIRC(fullmsg, false);
                
                // Need to send the same message to Minecraft chat
                IRCMessage msg = new IRCMessage();
                msg.name = bot.getNick();
                msg.message = fullmsg;
                plugin.send.toMinecraft(Keys.line_to_minecraft.chat, msg);
            } else {
                sender.sendMessage("/irc say (message) - Sends a message directly to IRC");
            }
            return true;
        }
        
        else if (cmd.equals("do")) {
            if (args.length > 1) {
                String fullmsg = "";
                for (int i=1;i<args.length;i++)
                    fullmsg += args[i] + " ";
                plugin.send.rawToIRC(fullmsg, true);
                
                // Need to send the same message to Minecraft chat
                IRCMessage msg = new IRCMessage();
                msg.name = bot.getNick();
                msg.message = fullmsg;
                plugin.send.toMinecraft(Keys.line_to_minecraft.action, msg);
            } else {
                sender.sendMessage("/irc do (action) - Sends an action directly to IRC");
            }
            return true;
        }
        
        else if (cmd.equals("op")) {
            if (args.length == 2) {
                bot.op(args[1]);
            } else {
                sender.sendMessage("/irc op (nick) - Sets mode +o to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("deop")) {
            if (args.length == 2) {
                bot.deOp(args[1]);
            } else {
                sender.sendMessage("/irc deop (nick) - Sets mode -o to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("voice")) {
            if (args.length == 2) {
                bot.voice(args[1]);
            } else {
                sender.sendMessage("/irc voice (nick) - Sets mode +v to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("devoice")) {
            if (args.length == 2) {
                bot.deVoice(args[1]);
            } else {
                sender.sendMessage("/irc devoice (nick) - Sets mode -v to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("kick")) {
            if (args.length > 1) {
                String reason = "";
                for (int i=2;i<args.length;i++)
                    reason += args[i] + " ";
                bot.doKick(args[1], reason);
            } else {
                sender.sendMessage("/irc kick (nick) [reason] - Kicks the given nick on IRC");
            }
            return true;
        }
        
        return false;
    }
    
    // The /minecraftbot command
    private boolean minecraftbot(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecraftbot.op")) return true;
        if (args.length < 1) return false;
        
        String cmd = args[0].toLowerCase();
        
        if (cmd.equals("connect")) {
            bot.connect();
            return true;
        }
        
        else if (cmd.equals("disconnect")) {
            String quitmessage = "";
            for (int i=1;i<args.length;i++)
                quitmessage += args[i] + " ";
            bot.quitServer(quitmessage);
            return true;
        }
        
        else if (cmd.equals("join")) {
            bot.joinChannel();
            return true;
        }
        
        else if (cmd.equals("part")) {
            bot.partChannel();
            return true;
        }
        
        else if (cmd.equals("reload")) {
            config.load();
            return true;
        }
        
        return false;
    }
}
