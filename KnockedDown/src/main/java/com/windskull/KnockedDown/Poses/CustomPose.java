package com.windskull.KnockedDown.Poses;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import lombok.SneakyThrows;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

public class CustomPose extends AbstractPose implements Tickable
{

	public final CustomSeat driver;

	public CustomPose(Player target)
	{
		super(target);
		registerProperties();
		this.driver = new CustomSeat(target, (e, a) ->
		{
			if (!getPosePluginPlayer().resetCurrentPose())
			{
				e.setCancelled(true);
				a.pushBack();
			}
		});
	}

	private void registerProperties()
	{
		getProperties().register();
	}

	@Override
	public void initiate()
	{
		super.initiate();
		PosePluginAPI.getAPI().getTickingBundle().addToTickingBundle(CustomPose.class,this);
		
		driver.takeASeat();
		// PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
		PosePluginAPI.getAPI().getNameTagHider().hideTag(getPlayer());
		PosePluginAPI.getAPI().getArmorHider().hideArmor(getPlayer());
	}

	@Override
	public void play(Player receiver)
	{
	}

	@Override
	public void stop()
	{
		super.stop();
		PosePluginAPI.getAPI().getTickingBundle().removeFromTickingBundle(CustomPose.class, this);
		driver.standUp();
		//getPlayer().setGliding(false);
		//getPlayer().setSwimming(false);
		// PosePluginAPI.getAPI().getPlayerHider().show(getPlayer());
		PosePluginAPI.getAPI().getNameTagHider().showTag(getPlayer());
		PosePluginAPI.getAPI().getArmorHider().showArmor(getPlayer());
	}

	@Override
	public EnumPose getType()
	{
		return EnumPose.LYING;
	}

	@EventHandler
	public void onMount(EntityMountEvent event)
	{
		if (event.getEntity().equals(getPlayer()))
		{
			getPosePluginPlayer().resetCurrentPose();
		}
	}

	@EventHandler
	public void onFly(PlayerToggleFlightEvent event)
	{
		if (event.getPlayer().equals(getPlayer()))
		{
			getPosePluginPlayer().stopPosingSilently();
		}
	}

	@SneakyThrows
	private boolean isInWater(Player player)
	{
		try
		{
			return (boolean) ReflectionTools.getNmsClass("Entity").getDeclaredMethod("isInWater")
				.invoke(NMSUtils.asNMSCopy(player));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
			| SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;// FakePlayer.asNMSCopy(player).isInWater();
	}

	@Override
	public void tick()
	{
		//getPlayer().setGliding(true);
		//getPlayer().setSwimming(true);
	}

}
