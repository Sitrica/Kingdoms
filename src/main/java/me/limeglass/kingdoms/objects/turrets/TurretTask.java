package me.limeglass.kingdoms.objects.turrets;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.manager.managers.LandManager;
import me.limeglass.kingdoms.manager.managers.WorldManager;
import me.limeglass.kingdoms.manager.managers.external.CitizensManager;
import me.limeglass.kingdoms.objects.land.Land;

public class TurretTask implements Runnable {

	@Override
	public void run() {
		Kingdoms instance = Kingdoms.getInstance();
		LandManager landManager = instance.getManager(LandManager.class);
		Optional<CitizensManager> citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		for (Player player : Bukkit.getOnlinePlayers()) {
			WorldManager worldManager = instance.getManager(WorldManager.class);
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
