package com.windskull.KnockedDown.Events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKnockedDownEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	

	private Player player;
	private Entity knocker;
	private boolean isCanceled = false;
	
	public PlayerKnockedDownEvent(Entity knocker,Player player) 
	{
		super();
		this.player = player;
		this.knocker = knocker;
	}
	
	
	@Override
	public HandlerList getHandlers() 
	{
		return handlers;
	}

	public static HandlerList getHandlerList() 
	{
		return handlers;
	}
	
	public Player getPlayer() 
	{
		return player;
	}
	
	public Entity getKnocker()
	{
		return knocker;
	}


	@Override
	public boolean isCancelled() 
	{
		return isCanceled;
	}


	@Override
	public void setCancelled(boolean cancel) 
	{
		isCanceled = cancel;
	}
}
