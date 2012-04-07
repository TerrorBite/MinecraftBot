package com.avisenera.minecraftbot.hooks;

import com.avisenera.minecraftbot.Formatting;
import com.avisenera.minecraftbot.message.MCMessage;
import com.avisenera.minecraftbot.message.Message;
import com.massivecraft.factions.P;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class FactionsHook extends Hook {

    @Override
    protected String get(PluginManager pm, String line, Message msg) {
        if (!line.toLowerCase().contains("%faction%")) return line;
        if (!(msg instanceof MCMessage)) return line; // Factions data can only come from Minecraft
        
        Plugin factionsPlugin = pm.getPlugin("Factions");
        if (factionsPlugin == null) return line;
        if (!(factionsPlugin instanceof com.massivecraft.factions.P)) return line;
        
        P p = (P) factionsPlugin;
        MCMessage message = (MCMessage) msg;
        String faction = p.getPlayerFactionTag(message.player);
        
        if (faction == null) return line;
        else return line.replace("%faction%", faction + Formatting.RESET);
    }
}
