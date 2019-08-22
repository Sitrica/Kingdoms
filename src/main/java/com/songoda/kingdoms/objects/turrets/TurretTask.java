package com.songoda.kingdoms.objects.turrets;

import java.util.Comparator;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Lists;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.WorldManager;
import com.songoda.kingdoms.objects.land.Land;

public class TurretTask implements Runnable {

	private final Kingdoms instance;

	public TurretTask(Kingdoms instance) {
		this.instance = instance;
	}

	@Override
	public void run() {
		WorldManager worldManager = instance.getManager(WorldManager.class);
		for (Entry<Chunk, Land> entry : instance.getManager(LandManager.class).getLoadedLand()) {
			for (Turret turret : entry.getValue().getTurrets()) {
				Location head = turret.getHeadLocation();
				World world = head.getWorld();
				if (!worldManager.acceptsWorld(world))
					return;
				int range = turret.getType().getRange();
			    // Find the closest living entity and fire at them.
				Lists.newArrayList(world.getNearbyEntities(head, range, range, range)).parallelStream()
						.filter(entity -> entity instanceof LivingEntity)
						.map(entity -> (LivingEntity) entity)
						.sorted(new Comparator<LivingEntity>() {
							@Override
							public int compare(LivingEntity entity, LivingEntity other) {
								Location entityLocation = entity.getLocation();
								Location otherLocation = other.getLocation();
								return Double.compare(entityLocation.distance(otherLocation), otherLocation.distance(entityLocation));
							}
					    })
						.findAny()
						.ifPresent(target -> turret.fireAt(target));
			}
		}
	}

}
