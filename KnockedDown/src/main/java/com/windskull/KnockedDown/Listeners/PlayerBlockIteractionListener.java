package com.windskull.KnockedDown.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.windskull.KnockedDown.KnockedDownCore;

public class PlayerBlockIteractionListener implements Listener
{
	private final KnockedDownCore core;

	public PlayerBlockIteractionListener(KnockedDownCore core)
	{
		super();
		this.core = core;
	};
	
	@EventHandler
	public void disableBlockPlace(BlockPlaceEvent e)
	{
		Player p = e.getPlayer();
		if(core.helperPlayers.containsKey(p) || core.knockedDownPlayers.containsKey(p))
			e.setCancelled(true);
		
	}
	@EventHandler
	public void disableBlockBreak(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		if(core.helperPlayers.containsKey(p) || core.knockedDownPlayers.containsKey(p))
			e.setCancelled(true);
	}
}
