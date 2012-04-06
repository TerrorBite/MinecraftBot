package com.avisenera.minecraftbot.hooks;

import com.avisenera.minecraftbot.Formatting;
import com.avisenera.minecraftbot.message.MCMessage;
import com.avisenera.minecraftbot.message.Message;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.plugin.PluginManager;

public class FactionsHook extends Hook {

    @Override
    protected String get(PluginManager pm, String line, Message msg) {
        if (!line.toLowerCase().contains("%factions%")) return line;
        if (!(msg instanceof MCMessage)) return line; // Factions data can only come from Minecraft
        if (pm.getPlugin("Factions") == null) return line;
        
        FPlayer f_player = FPlayers.i.get(msg.name);
        Faction f = f_player.getFaction();
        
        if (f == null || f_player.getFactionId().equals("0")) return line;
        else return line.replace("%faction%", f.getTag() + Formatting.RESET);
    }
}
