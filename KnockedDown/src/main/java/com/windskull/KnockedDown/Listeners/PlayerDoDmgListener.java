package com.windskull.KnockedDown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.windskull.KnockedDown.KnockedDownCore;

public class PlayerDoDmgListener implements Listener
{
	private final KnockedDownCore core;
	
	
	public PlayerDoDmgListener(KnockedDownCore core)
	{
		super();
		this.core = core;
	}


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDMGDo(EntityDamageByEntityEvent e)
	{
		Entity ent = e.getDamager();
		if(ent instanceof Player)
		{
			Player p = (Player)ent;
			if(core.knockedDownPlayers.containsKey(p) || core.helperPlayers.containsKey(p))
			{
				e.setCancelled(true);
			}
		}
	}
}
