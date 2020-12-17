package com.windskull.KnockedDown;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.windskull.KnockedDown.Events.PlayerKnockedDownEvent;
import com.windskull.KnockedDown.Events.PlayerReanimatedEvent;
import com.windskull.KnockedDown.Events.PlayerStartPickUpEvent;
import com.windskull.KnockedDown.Events.PlayerStartReanimateEvent;
import com.windskull.KnockedDown.Events.PlayerStopPickUpEvent;
import com.windskull.KnockedDown.Events.PlayerStopReanimateEvent;
import com.windskull.KnockedDown.Poses.CustomPose;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.PoseBuilder;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;

public class PlayerKnockedDown extends BukkitRunnable
{

	// %h Helper %p Player %kt Knoked time %rt Resurect time

	public static String KNOCKEDDOWN_TITLE;
	public static String REANIMATED_TITLE;
	public static String KNOCKEDDOWN_SUBTITLE;
	public static String REANIMATED_SUBTITLE;
	public static String RAISED_TITLE;
	public static String RAISED_SUBTITLE;

	public static String CANCEL_REANIMATION;
	public static String CANCEL_PICKUP_TITLE;
	public static String CANCEL_PICKUP_SUBTITLE;

	public static String CLICK_TO_DIE;

	public static String KNOCKEDDOWN_MESSAGE;

	public static int TIME_TO_DEATH = 60;
	public static int TIME_TO_REANIMATE = 15;
	public static int PLAYER_HEALTH_AFTER_REANIMATION = 10;
	public static int PLAYER_HEALTH_AFTER_KNOCKDOWN = 4;

	public static boolean HELPER_MOVE_WHILE_REANIMATION = false;
	public static boolean HELPER_CAN_PICKUP = true;
	public static boolean USE_SWIM_PICKUP = true;
	private final KnockedDownCore core;

	private Entity driver;
	private BukkitTask bukkitTask;
	
	private final Player player;
	private final PosePluginPlayer poseObject;

	private static Method method;
	private PlayerStatus playerStatus = PlayerStatus.KNOKED;
	
	private int timeToDeath = TIME_TO_DEATH;
	private int timeToReanimate = 0;

	public Player helper;

	public enum PlayerStatus
	{
		KNOKED, REANIMATED, RAISED
	}

	public PlayerKnockedDown(KnockedDownCore core, Player player)
	{
		this.core = core;
		this.player = player;
		poseObject = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player);
		player.setAllowFlight(true);

		if (method == null)
			try
			{
				method = PosePluginPlayer.class.getDeclaredMethod("setPose", IPluginPose.class);
				method.setAccessible(true);
			} catch (IllegalArgumentException | NoSuchMethodException | SecurityException  e)
			{
				e.printStackTrace();
			}

	}

	public boolean knockDown(Entity p)
	{
		PlayerKnockedDownEvent kde = new PlayerKnockedDownEvent(p, player);
		Bukkit.getServer().getPluginManager().callEvent(kde);
		if (!kde.isCancelled())
		{
			disablePlayerPose();
			player.sendMessage(KNOCKEDDOWN_MESSAGE);
			player.setHealth(PLAYER_HEALTH_AFTER_KNOCKDOWN);

			poseObject.changePose(PoseBuilder.builder(EnumPose.LYING).option(EnumPoseOption.HEAD_ROTATION, false).build(poseObject));

			return true;
		}
		return false;
	}

	@Override
	public void run()
	{
		switch (playerStatus)
		{
			case KNOKED:
				if (timeToDeath-- <= 0)
					killPlayer();
				else
					sendTitles();
				break;
			case REANIMATED:
				if (checkIfPlayerIsHelping())
					if (timeToReanimate++ >= TIME_TO_REANIMATE)
						playerReanimated();
					else
						sendTitles();
				else
					stopReanimate();
				break;
			case RAISED:

				sendTitles();
				break;
		}

	}

	@SuppressWarnings("deprecation")
	public void sendTitles()
	{
		player.sendTitle(getTitleString(), getStatusString());
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(CLICK_TO_DIE));
		if (playerStatus == PlayerStatus.REANIMATED)
			helper.sendTitle(getTitleString(), getStatusString());
	}

	@SuppressWarnings("deprecation")
	public void resetTitle(Player p)
	{
		p.sendTitle("", "");
	}

	public String getTitleString()
	{
		switch (playerStatus)
		{
			case KNOKED:
				return KNOCKEDDOWN_TITLE;
			case REANIMATED:
				return REANIMATED_TITLE;
			case RAISED:
				return RAISED_TITLE;
		}
		return "ERROR";
	}

	public String getStatusString()
	{
		switch (playerStatus)
		{
			case KNOKED:
				return KNOCKEDDOWN_SUBTITLE.replaceFirst("%kt", timeToDeath + "");
			case REANIMATED:
				return REANIMATED_SUBTITLE.replaceFirst("%rt", (TIME_TO_REANIMATE - timeToReanimate) + "");
			case RAISED:
				return RAISED_SUBTITLE.replaceFirst("%h", helper.getName());
		}
		return "ERROR";
	}

	public void killPlayer()
	{
		player.setHealth(0);
		resetTitle(player);
	}

	public void disablePlayerPose()
	{
		poseObject.getPose().stop();
		try
		{
			method.invoke(poseObject, AbstractPose.STANDING);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}

	}

	public void playerDeath()
	{
		if (helper != null)
		{
			if (playerStatus == PlayerStatus.REANIMATED)

				stopReanimate();
			else
				stopPickUp();
		}
		core.knockedDownPlayers.remove(player);
		player.setAllowFlight(false);
		disablePlayerPose();

		// poseObject.resetCurrentPose();
		this.cancel();
	}

	public boolean startPickUp(Player playerH)
	{
		if (HELPER_CAN_PICKUP)
		{
			if (playerStatus == PlayerStatus.KNOKED)
			{
				PlayerStartPickUpEvent kde = new PlayerStartPickUpEvent(playerH, this);
				Bukkit.getServer().getPluginManager().callEvent(kde);
				if (!kde.isCancelled())
				{
					playerStatus = PlayerStatus.RAISED;
					helper = playerH;
					disablePlayerPose();
					core.helperPlayers.put(helper, this);
					if(USE_SWIM_PICKUP)
					{
						poseObject.changePose(PoseBuilder.builder(EnumPose.SWIMMING).build(poseObject));
						bukkitTask = Bukkit.getScheduler().runTaskTimer(core, () -> player.teleport(helper.getLocation().add(0,2,0)), 0, 1);
					}
					else
					{
						CustomPose cp = new CustomPose(player);
						poseObject.changePose(cp);
						helper.addPassenger(cp.driver.seat);
						driver = cp.driver.seat;
					}
					return true;
				} else
					return false;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void stopPickUp()
	{
		PlayerStopPickUpEvent kde = new PlayerStopPickUpEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(kde);
		if (!kde.isCancelled())
		{
			playerStatus = PlayerStatus.KNOKED;
			helper.sendTitle(CANCEL_PICKUP_TITLE, CANCEL_PICKUP_SUBTITLE.replaceFirst("%p", player.getName()));
			if(USE_SWIM_PICKUP)
			{
				bukkitTask.cancel();
			}
			else
			{
				helper.removePassenger(driver);
			}
			core.helperPlayers.remove(helper);
			disablePlayerPose();
			player.teleport(helper);
			poseObject.changePose(PoseBuilder.builder(EnumPose.LYING).option(EnumPoseOption.HEAD_ROTATION, false).build(poseObject));
			helper = null;
		}
	}

	public boolean startReanimate(Player player)
	{
		if (playerStatus == PlayerStatus.KNOKED)
		{
			PlayerStartReanimateEvent kde = new PlayerStartReanimateEvent(player, this);
			Bukkit.getServer().getPluginManager().callEvent(kde);
			if (!kde.isCancelled())
			{
				helper = player;
				playerStatus = PlayerStatus.REANIMATED;
				if (!HELPER_MOVE_WHILE_REANIMATION)
					helperStartReanimateDisableMovment();
				return true;
			}
		}
		return false;
	}

	public void helperStartReanimateDisableMovment()
	{
		helper.setWalkSpeed(0);
		helper.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 250));
	}

	@SuppressWarnings("deprecation")
	public void stopReanimate()
	{
		PlayerStopReanimateEvent kde = new PlayerStopReanimateEvent(helper,this);
		Bukkit.getServer().getPluginManager().callEvent(kde);
		if (!kde.isCancelled())
		{
			if (!HELPER_MOVE_WHILE_REANIMATION)
				helperStartReanimateEnableMovment();
			playerStatus = PlayerStatus.KNOKED;
			helper.sendTitle(CANCEL_REANIMATION, "");
			helper = null;
			timeToReanimate = 0;
		}

	}

	public void helperStartReanimateEnableMovment()
	{
		helper.setWalkSpeed(0.2f);
		helper.removePotionEffect(PotionEffectType.JUMP);
	}

	public boolean checkIfPlayerIsHelping()
	{
		return helper.isSneaking() && player.getLocation().distance(helper.getLocation()) <= 2;
	}

	public void playerReanimated()
	{
		PlayerReanimatedEvent kde = new PlayerReanimatedEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(kde);
		if (!kde.isCancelled())
		{
			core.knockedDownPlayers.remove(player);
			player.setHealth(PLAYER_HEALTH_AFTER_REANIMATION);
			disablePlayerPose();
			player.setAllowFlight(false);
			if (!HELPER_MOVE_WHILE_REANIMATION)
				helperStartReanimateEnableMovment();
			this.cancel();
		}
	}

	// GETERS SETERS

	public PlayerStatus getPlayerStatus()
	{
		return playerStatus;
	}

	public void setPlayerStatus(PlayerStatus playerStatus)
	{
		this.playerStatus = playerStatus;
	}

	public Player getHelper()
	{
		return helper;
	}

	public void setHelper(Player helper)
	{
		this.helper = helper;
	}

	public Player getPlayer()
	{
		return player;
	}

	public PosePluginPlayer getPoseObject()
	{
		return poseObject;
	}

	public int getTimeToDeath()
	{
		return timeToDeath;
	}

	public void setTimeToDeath(int timeToDeath)
	{
		this.timeToDeath = timeToDeath;
	}

	public int getTimeToReanimate()
	{
		return timeToReanimate;
	}

	public void setTimeToReanimate(int timeToReanimate)
	{
		this.timeToReanimate = timeToReanimate;
	}

	public Entity getDriver()
	{
		return driver;
	}

	public void setDriver(Entity driver)
	{
		this.driver = driver;
	}

	public BukkitTask getBukkitTask()
	{
		return bukkitTask;
	}

	public void setBukkitTask(BukkitTask bukkitTask)
	{
		this.bukkitTask = bukkitTask;
	}

	@Override
	public String toString()
	{
		return "PlayerName: " + player.getName() + System.lineSeparator() + "Status: " + playerStatus;
	}

	@Override
	public int hashCode()
	{
		return player.hashCode();
	}
}
