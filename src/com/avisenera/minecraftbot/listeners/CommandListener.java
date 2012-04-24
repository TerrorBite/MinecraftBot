package com.avisenera.minecraftbot.listeners;

import com.avisenera.minecraftbot.Keys;
import com.avisenera.minecraftbot.MinecraftBot;
import com.avisenera.minecraftbot.message.IRCMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListener implements CommandExecutor {
    private MinecraftBot plugin;
    private IRCManager irc;
    public CommandListener(MinecraftBot instance, IRCManager irc) {
        plugin = instance;
        this.irc = irc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String command = cmd.getName().toLowerCase();
        
        if (command.equals("irc")) return irc(sender, args);
        
        else if (command.equals("minecraftbot")) return minecraftbot(sender, args);
        
        else if (command.equals("n") || command.equals("names")) {
            sender.sendMessage(irc.userlist());
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
                
                IRCMessage msg = new IRCMessage();
                msg.name = irc.getNick();
                msg.message = fullmsg;
                irc.sendMessage(Keys.line_to_minecraft.chat, msg);
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
                
                IRCMessage msg = new IRCMessage();
                msg.name = irc.getNick();
                msg.message = fullmsg;
                irc.sendMessage(Keys.line_to_minecraft.action, msg);
            } else {
                sender.sendMessage("/irc do (action) - Sends an action directly to IRC");
            }
            return true;
        }
        
        else if (cmd.equals("op")) {
            if (args.length == 2) {
                irc.op(args[1]);
            } else {
                sender.sendMessage("/irc op (nick) - Sets mode +o to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("deop")) {
            if (args.length == 2) {
                irc.deop(args[1]);
            } else {
                sender.sendMessage("/irc deop (nick) - Sets mode -o to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("voice")) {
            if (args.length == 2) {
                irc.voice(args[1]);
            } else {
                sender.sendMessage("/irc voice (nick) - Sets mode +v to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("devoice")) {
            if (args.length == 2) {
                irc.devoice(args[1]);
            } else {
                sender.sendMessage("/irc devoice (nick) - Sets mode -v to the given nick");
            }
            return true;
        }
        
        else if (cmd.equals("kick")) {
            if (args.length > 1) {
                String reason = "";
                for (int i=2;i<args.length;i++)
                    reason += " " + args[i];
                // empty kick messages are not handled too well
                if (reason.isEmpty()) irc.kick(args[1], args[1]);
                else irc.kick(args[1], reason.substring(1));
            } else {
                sender.sendMessage("/irc kick (nick) [reason] - Kicks the given nick on IRC");
            }
            return true;
        }
        
        else if (cmd.equals("ban")) {
            if (args.length == 2) {
                irc.ban(args[1]);
            } else {
                sender.sendMessage("/irc ban (nick) - Bans the given nick from the IRC channel.");
            }
            return true;
        }
        
        else if (cmd.equals("unban")) {
            if (args.length == 2) {
                irc.unban(args[1]);
            } else {
                sender.sendMessage("/irc unban (hostmask) - Removes the given hostmask from the banlist.");
            }
            return true;
        }
        
        return false;
    }
    
    // The /minecraftbot command
    private boolean minecraftbot(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecraftbot.manage")) return true;
        if (args.length < 1) return false;
        
        String cmd = args[0].toLowerCase();
        
        if (cmd.equals("connect")) {
            irc.connect();
            return true;
        }
        
        else if (cmd.equals("disconnect")) {
            String quitmessage = "";
            for (int i=1;i<args.length;i++)
                quitmessage += " " + args[i];
            if (!quitmessage.isEmpty()) quitmessage = quitmessage.substring(1);
            irc.disconnect(quitmessage);
            return true;
        }
        
        else if (cmd.equals("join")) {
            irc.joinChannel();
            return true;
        }
        
        else if (cmd.equals("part")) {
            irc.partChannel();
            return true;
        }
        
        else if (cmd.equals("reload")) {
            plugin.config.load();
            return true;
        }
        
        return false;
    }
}
