package com.songoda.kingdoms.manager.managers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class MasswarManager extends Manager {

	private final SimpleDateFormat format = new SimpleDateFormat("HH'h' mm'm' ss's'");
	private long start, time; //in seconds

	public MasswarManager() {
		super(true);
		format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				if (time <= -1)
					return;
				if (!isWarOn())
					stopMassWar();
			}
		}, 0, 20 * 60); //1 minute
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {
		stopMassWar();
	}

	public boolean isWarOn() {
		return getTimeLeft() > 0 ? true : false;
	}

	public long getTimeLeft() {
		if (time == -1)
			return -1;
		return (start + time * 1000L) - System.currentTimeMillis();
	}

	public String getTimeLeftInString() {
		if (time == -1)
			return new MessageBuilder(false, "masswar.not-on").get();
		Date date = new Date(getTimeLeft() < 0 ? 0 : getTimeLeft());
		return format.format(date)+" left.";
	}

	public void startWar(long time) {
		WorldManager worldManager = instance.getManager(WorldManager.class);
		this.time = time;
		new ListMessageBuilder("masswar.start")
				.replace("%time%", time / 60)
				.toPlayers(instance.getServer().getOnlinePlayers().parallelStream()
						.filter(player -> worldManager.acceptsWorld(player.getWorld()))
						.collect(Collectors.toList()))
				.send();
		start = System.currentTimeMillis();
	}

	public void stopMassWar() {
		WorldManager worldManager = instance.getManager(WorldManager.class);
		if (time >= 0) {
			new ListMessageBuilder("masswar.end")
			.replace("%time%", time / 60)
			.toPlayers(instance.getServer().getOnlinePlayers().parallelStream()
					.filter(player -> worldManager.acceptsWorld(player.getWorld()))
					.collect(Collectors.toList()))
			.send();
		}
		time = -1;
	}

}
