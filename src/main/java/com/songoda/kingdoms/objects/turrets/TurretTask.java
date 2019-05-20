package com.songoda.kingdoms.objects.turrets;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.WorldManager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.land.Land;

public class TurretTask implements Runnable {

	private final Optional<CitizensManager> citizensManager;
	private final LandManager landManager;

	public TurretTask(Kingdoms instance) {
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			WorldManager worldManager = Kingdoms.getInstance().getManager(WorldManager.class);
			World world = player.getWorld();
			if (worldManager.acceptsWorld(world))
				return;
			if (citizensManager.isPresent())
				if (citizensManager.get().isCitizen(player))
					return;
			// Start turret test
			Land landAt = landManager.getLand(player.getLocation().getChunk());
			for (Land land : landAt.getSurrounding()) {
				for (Turret turret : land.getTurrets()) {
					//attempt to get the closest player to the turret
					Location turretLocation = turret.getLocation();
					Player closest = null;
					double closestDistance = 0.0;
					for (Player p : getNearbyPlayers(turretLocation, turret.getType().getRange())) {
						double distance = p.getLocation().distance(turretLocation);
						if (distance > closestDistance) {
							closest = p;
							closestDistance = distance;
						}
					}
					if (closest != null)
						turret.fireAt(closest);
				}
			}
		}
	}

	private Set<Player> getNearbyPlayers(Location location, double distance) {
		double distanceSquared = distance * distance;
		Set<Player> nearby = new HashSet<>();
		Bukkit.getOnlinePlayers().parallelStream()
				.filter(player -> player.getLocation().distanceSquared(location) < distanceSquared)
				.filter(player -> !player.getWorld().equals(location.getWorld()))
				.forEach(player -> nearby.add(player));
		return nearby;
	}

}
