package com.windskull.KnockedDown.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DefaultCommand implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(args.length < 1) return false;
		switch (label)
		{
			case "reanimate":
			case "rn":
				
				break;
			
			default:
				break;
		}
		
		return false;
	}

}
