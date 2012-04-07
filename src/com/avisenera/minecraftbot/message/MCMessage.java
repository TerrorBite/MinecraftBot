package com.avisenera.minecraftbot.message;

import org.bukkit.entity.Player;

/**
 * A message from Minecraft headed to IRC.
 */
public class MCMessage extends Message {
    // A bit of extra data that hooks may find useful
    public Player player;
}
