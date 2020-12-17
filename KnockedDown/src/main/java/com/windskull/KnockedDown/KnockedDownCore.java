package com.windskull.KnockedDown;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import ru.armagidon.poseplugin.api.events.StopPosingEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class KnockedDownCore extends JavaPlugin implements Listener
{

	private static KnockedDownCore sing;

	public static KnockedDownCore getInstance()
	{
		if (sing == null)
			sing = new KnockedDownCore();
		return sing;
	}

	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		saveConfigDefault();
		loadConfigValues();

	}

	public void saveConfigDefault()
	{
		FileConfiguration c = this.getConfig();

		c.addDefault("KNOCKEDDOWN_TITLE", "&4KNOCKED");
		c.addDefault("KNOCKEDDOWN_SUBTITLE", "&7Time to death: &6%kt");
		c.addDefault("REANIMATED_TITLE", "&2REANIMATED");
		c.addDefault("REANIMATED_SUBTITLE", "&7Time to reanimation: &6%rt");
		c.addDefault("RAISED_TITLE", "&7Raised by");
		c.addDefault("RAISED_SUBTITLE", "&6%h");

		c.addDefault("CANCEL_REANIMATION", "&7Reanimation canceled");
		c.addDefault("CANCEL_PICKUP_TITLE", "&7Put off");
		c.addDefault("CANCEL_PICKUP_SUBTITLE", "&6%p");

		c.addDefault("CLICK_TO_DIE", "&7Press F to give up");
		c.addDefault("KNOCKEDDOWN_MESSAGE", "&7You are struck down, ask the players for help (RMB + Shift to help or RMB to picku up)");

		c.addDefault("TIME_TO_DEATH", 60);
		c.addDefault("TIME_TO_REANIMATE", 15);
		c.addDefault("PLAYER_HEALTH_AFTER_REANIMATION", 10);
		c.addDefault("PLAYER_HEALTH_AFTER_KNOCKDOWN", 4);

		c.addDefault("HELPER_MOVE_WHILE_REANIMATION", false);
		c.addDefault("HELPER_CAN_PICKUP", true);
		c.addDefault("USE_SWIM_PICKUP", false);
		c.options().copyDefaults(true);
		saveConfig();
	}

	public void loadConfigValues()
	{
		FileConfiguration c = this.getConfig();
		PlayerKnockedDown.KNOCKEDDOWN_TITLE = ChatColor.translateAlternateColorCodes('&', c.getString("KNOCKEDDOWN_TITLE", "&4KNOKED"));
		PlayerKnockedDown.KNOCKEDDOWN_SUBTITLE = ChatColor.translateAlternateColorCodes('&', c.getString("KNOCKEDDOWN_SUBTITLE", "&7Time to death: &6%kt"));
		PlayerKnockedDown.REANIMATED_TITLE = ChatColor.translateAlternateColorCodes('&', c.getString("REANIMATED_TITLE", "&2REANIMATED"));
		PlayerKnockedDown.REANIMATED_SUBTITLE = ChatColor.translateAlternateColorCodes('&', c.getString("REANIMATED_SUBTITLE", "&7Time to reanimation: &6%rt"));
		PlayerKnockedDown.RAISED_TITLE = ChatColor.translateAlternateColorCodes('&', c.getString("RAISED_TITLE", "&7Raised by"));
		PlayerKnockedDown.RAISED_SUBTITLE = ChatColor.translateAlternateColorCodes('&', c.getString("RAISED_SUBTITLE", "&6%h"));

		PlayerKnockedDown.CANCEL_REANIMATION = ChatColor.translateAlternateColorCodes('&', c.getString("CANCEL_REANIMATION", "&7Reanimation canceled"));
		PlayerKnockedDown.CANCEL_PICKUP_TITLE = ChatColor.translateAlternateColorCodes('&', c.getString("CANCEL_PICKUP_TITLE", "&7Put off"));
		PlayerKnockedDown.CANCEL_PICKUP_SUBTITLE = ChatColor.translateAlternateColorCodes('&', c.getString("CANCEL_PICKUP_SUBTITLE", "&6%p"));

		PlayerKnockedDown.CLICK_TO_DIE = ChatColor.translateAlternateColorCodes('&', c.getString("CLICK_TO_DIE", "&7Press F to give up"));
		PlayerKnockedDown.KNOCKEDDOWN_MESSAGE = ChatColor.translateAlternateColorCodes('&', c.getString("KNOCKEDDOWN_MESSAGE"));

		PlayerKnockedDown.TIME_TO_DEATH = c.getInt("TIME_TO_DEATH", 60);
		PlayerKnockedDown.TIME_TO_REANIMATE = c.getInt("TIME_TO_REANIMATE", 15);
		PlayerKnockedDown.PLAYER_HEALTH_AFTER_REANIMATION = c.getInt("PLAYER_HEALTH_AFTER_REANIMATION", 10);
		PlayerKnockedDown.PLAYER_HEALTH_AFTER_KNOCKDOWN = c.getInt("PLAYER_HEALTH_AFTER_KNOCKDOWN", 4);

		PlayerKnockedDown.HELPER_MOVE_WHILE_REANIMATION = c.getBoolean("HELPER_MOVE_WHILE_REANIMATION", false);
		PlayerKnockedDown.HELPER_CAN_PICKUP = c.getBoolean("HELPER_CAN_PICKUP", true);
		PlayerKnockedDown.USE_SWIM_PICKUP = c.getBoolean("USE_SWIM_PICKUP", false);
	}

	public ConcurrentHashMap<Player, PlayerKnockedDown> knockedDownPlayers = new ConcurrentHashMap<Player, PlayerKnockedDown>();
	public ConcurrentHashMap<Player, PlayerKnockedDown> helperPlayers = new ConcurrentHashMap<Player, PlayerKnockedDown>();
	
	/**
	 * @param Player 
	 * @return null if not knocked
	 */
	public PlayerKnockedDown getKnockedPlayer(Player p)
	{
		return knockedDownPlayers.get(p);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent e)
	{
		if (e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			if (!knockedDownPlayers.containsKey(player))
			{
				if (player.getHealth() - e.getFinalDamage() < 1)
				{
					PlayerKnockedDown knoked = new PlayerKnockedDown(this, player);
					if (knoked.knockDown(e.getEntity()))
					{
						knoked.runTaskTimer(this, 0, 20);
						knockedDownPlayers.put(player, knoked);
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		PlayerKnockedDown knoked = knockedDownPlayers.get(e.getEntity());
		if (knoked != null)
		{
			knoked.playerDeath();
		}
	}

	// Shift + rmb to renimate rmp
	@EventHandler
	public void playerInteractionWithKnocked(PlayerInteractAtEntityEvent e)
	{
		Entity ent = e.getRightClicked();
		if (ent instanceof Player)
		{
			PlayerKnockedDown knocked = knockedDownPlayers.get(ent);
			if (knocked != null)
			{
				if (e.getPlayer().isSneaking())
					knocked.startReanimate(e.getPlayer());
				else

					knocked.startPickUp(e.getPlayer());
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		PlayerKnockedDown knoked = knockedDownPlayers.get(e.getPlayer());
		if (knoked != null)
		{
			e.getPlayer().setHealth(0);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerSwap(PlayerSwapHandItemsEvent e)
	{

		PlayerKnockedDown knocked = knockedDownPlayers.get(e.getPlayer());
		if (knocked != null)
		{
			knocked.killPlayer();
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent e)
	{
		Player player = e.getPlayer();
		PlayerKnockedDown knocked = helperPlayers.get(player);
		if(knocked != null)
		{
			knocked.stopPickUp();
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onStop(StopPosingEvent event)
	{
		if (event.getPose().equals(EnumPose.LYING))
		{
			event.setCancelled(true);
		}
	}

}
