package com.songoda.kingdoms.manager.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class TeleportManager extends Manager {

	public TeleportManager() {
		super(true);
	}

	public void teleport(KingdomPlayer kingdomPlayer, Location location) {
		Player player = kingdomPlayer.getPlayer();
		long time = IntervalUtils.getInterval(configuration.getString("plugin.teleport-delay", "0 ticks"));
		if (time <= 0) {
			player.teleport(location);
			return;
		}
		Location starting = player.getLocation();
		new BukkitRunnable() {

			private int i = 0;

			@Override
			public void run() {
				if (starting.distance(player.getLocation()) > 1) {
					new MessageBuilder("messages.teleport-moved")
							.replace("%location%", LocationUtils.locationToStringExcludePitch(location))
							.setPlaceholderObject(kingdomPlayer)
							.send(player);
					cancel();
					return;
				}
				i++;
				if (i >= time) {
					cancel();
					player.teleport(location);
				}
			}

		}.runTaskTimer(instance, 5, 1); // 5 tick delay so they can stop moving, 1 tick for running timer checking.
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
