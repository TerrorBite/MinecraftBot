package me.rafa652.minecraftbot;

// Following three imports are used only in commented-out code.
//import java.util.logging.Level;
//import org.bukkit.entity.Player;
//import org.bukkit.event.entity.EntityDamageEvent;
import me.rafa652.minecraftbot.MinecraftBotConfiguration.ColorContext;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EntityHandler implements Listener {

	public static MinecraftBot plugin;
	
	// Values from config
	private String cd; // color for death
	private boolean event_mc_death;

	public EntityHandler(MinecraftBot instance, MinecraftBotConfiguration config) {
		plugin = instance;
		
		cd = config.getIRCColor(ColorContext.Death);
		event_mc_death = config.event_mc_death;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event_mc_death == false) return;
		
		if (event instanceof PlayerDeathEvent) {
			PlayerDeathEvent death = (PlayerDeathEvent)event;
			plugin.bot.sendMessage(cd + "* "+ death.getDeathMessage());
		}

		// The following code was never used, and is incomplete and probably buggy.
		// I only wrote it in case the above code didn't work. However, this could
		// possibly be used to make custom death messages for IRC. --TerrorBite

		/*
		else if(event.getEntity() instanceof org.bukkit.entity.Player) {
			// Convert entity to player
			Player player = (Player)event.getEntity();
			plugin.log.log(Level.INFO, "[MinecraftBot] " + player.getName() + "died");

			// Get the player's last damage event so we can see why they died
			EntityDamageEvent cause = player.getLastDamageCause();

			// Work out the death message from here
			String reason = " died.";
			switch(cause.getCause()) {
			case BLOCK_EXPLOSION:
			case ENTITY_EXPLOSION:
				reason = " blew up"; break;
			case CONTACT:
				reason = " was pricked to death"; break;
			case DROWNING:
				reason = " drowned"; break;
			case ENTITY_ATTACK:
				// Need more logic here to work out the name
				// of the attacking mob or player
				reason = " was slain by a monster"; break;
			case FALL:
				reason = " hit the ground too hard"; break;
			case FIRE: // Died from direct contact with fire
			case FIRE_TICK: // Died from being on fire afterwards
				reason = " burned to death"; break;
			case LAVA:
				reason = " tried to swim in lava"; break;
			case LIGHTNING:
				reason = " was struck by lightning"; break;
			case PROJECTILE:
				// Need more logic here to determine the name
				// of the mob or player who fired the projectile
				reason = " was shot by a monster"; break;
			case STARVATION:
				reason = " starved to death"; break;
			case SUFFOCATION:
				reason = " suffocated in a wall"; break;
			case VOID:
				reason = " fell out of the world"; break;
			case SUICIDE: // e.g. /kill
			default:
				reason = " died"; break;
			}
			plugin.bot.sendMessage(player.getName() + reason);
		} */
	}
}