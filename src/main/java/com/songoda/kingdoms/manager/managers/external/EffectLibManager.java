package com.songoda.kingdoms.manager.managers.external;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.google.common.collect.Sets;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.turrets.ParticleProjectile;
import com.songoda.kingdoms.objects.turrets.Turret;
import com.songoda.kingdoms.objects.turrets.TurretType;
import com.songoda.kingdoms.objects.turrets.ParticleProjectile.ParticleMode;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.LineEffect;
import de.slikey.effectlib.util.DynamicLocation;

public class EffectLibManager extends Manager {

	private final EffectManager effectManager;

	public EffectLibManager() {
		super("effectlib", false);
		effectManager = new EffectManager(instance);
	}

	public class LineBuilder {
		
		private final Location start, end;
		private final LineEffect effect;
		
		public LineBuilder(Location start, Location end) {
			this.start = start;
			this.end = end;
			effect = new LineEffect(effectManager);
			effect.setDynamicOrigin(new DynamicLocation(start));
			effect.setDynamicTarget(new DynamicLocation(end));
			effect.asynchronous = true;
		}
		
		public LineBuilder withColor(Color color) {
			effect.color = color;
			return this;
		}
		
		/**
		 * How many times in seconds should the effect last.
		 */
		public LineBuilder iterations(int iterations) {
			effect.iterations = iterations;
			return this;
		}
		
		public LineBuilder withCallback(Runnable runnable) {
			effect.callback = runnable;
			return this;
		}
		
		public LineBuilder withMaterial(Material material) {
			effect.material = material;
			return this;
		}
		
		public LineBuilder withParticle(Particle particle) {
			effect.particle = particle;
			return this;
		}
		
		public LineEffect send(int radius) {
			Set<Entity> entities = Sets.newHashSet(start.getWorld().getNearbyEntities(start, radius, radius, radius));
			entities.addAll(end.getWorld().getNearbyEntities(end, radius, radius, radius));
			effect.targetPlayers = entities.parallelStream()
					.filter(entity -> entity instanceof Player)
					.map(entity -> (Player) entity)
					.collect(Collectors.toList());
			effect.start();
			return effect;
		}
		
		public LineEffect send(List<Player> players) {
			effect.targetPlayers = players;
			effect.start();
			return effect;
		}
		
		public LineEffect send(Player player) {
			effect.targetPlayer = player;
			effect.start();
			return effect;
		}
		
	}

	public void shootParticle(Turret turret, Location from, LivingEntity target, Runnable runnable) {
		TurretType type = turret.getType();
		ParticleProjectile projectile = type.getParticleProjectile();
		ParticleMode mode = projectile.getMode();
		Location to = target.getEyeLocation();
		switch (mode) {
			case FOLLOW:
				if (turret.isFollowing(target))
					return;
				turret.addFollowing(target);
				Location past = from;
				int radius = 40;
				for (int i = 0; i < (to.distance(from) * 3) * projectile.getTimeout(); i++) {
					double x = (to.getX() - past.getX()) / (to.distance(past) / 4);
					double y = (to.getY() - past.getY()) / (to.distance(past) / 4);
					double z = (to.getZ() - past.getZ()) / (to.distance(past) / 4);
					past.add(x, y, z);
					Set<Entity> entities = Sets.newHashSet(from.getWorld().getNearbyEntities(from, radius, radius, radius));
					entities.addAll(to.getWorld().getNearbyEntities(to, radius, radius, radius));
					entities.parallelStream()
							.filter(entity -> entity instanceof Player)
							.map(entity -> (Player) entity)
							.collect(Collectors.toList());
					effectManager.display(projectile.getParticle(), past, 0, 0, 0, 0, projectile.getAmount(), projectile.getSize(), projectile.getColor(), projectile.getMaterial(), (byte)1, radius * 2, entities.parallelStream()
							.filter(entity -> entity instanceof Player)
							.map(entity -> (Player) entity)
							.collect(Collectors.toList()));
					if (to.distance(past) < 1)
						break;
				}
				runnable.run();
				turret.removeFollowing(target);
				break;
			case LINE:
				projectile.shootLineParticle(new LineBuilder(from, to), runnable);
				break;
		}
	}

	public EffectManager getEffectManager() {
		return effectManager;
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
