package com.windskull.KnockedDown.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.windskull.KnockedDown.PlayerKnockedDown;

public class PlayerStartPickUpEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	
	private Player helper;
	private PlayerKnockedDown player;
	private boolean isCanceled = false;
	
	public PlayerStartPickUpEvent(Player helper,PlayerKnockedDown player) 
	{
		super();
		this.player = player;
		this.helper = helper;
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
	
	public Player getHelper()
	{
		return helper;
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
