package com.windskull.KnockedDown.Events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.windskull.KnockedDown.PlayerKnockedDown;

public class PlayerReanimatedEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	

	private PlayerKnockedDown player;
	private boolean isCanceled = false;
	
	public PlayerReanimatedEvent(PlayerKnockedDown player) 
	{
		super();
		this.player = player;
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
	
	public PlayerKnockedDown getPlayerKnokedDown() 
	{
		return player;
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
