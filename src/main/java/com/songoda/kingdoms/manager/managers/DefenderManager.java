package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.songoda.kingdoms.events.DefenderDamageByPlayerEvent;
import com.songoda.kingdoms.events.DefenderDamageEvent;
import com.songoda.kingdoms.events.DefenderDamageEvent.DefenderDamageCause;
import com.songoda.kingdoms.events.DefenderDamagePlayerEvent;
import com.songoda.kingdoms.events.DefenderDeathEvent;
import com.songoda.kingdoms.events.DefenderTargetEvent;
import com.songoda.kingdoms.events.InvadingStopEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.Defender;
import com.songoda.kingdoms.objects.DefenderAbility;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic.StopReason;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class DefenderManager extends Manager {

	private final SetMultimap<UUID, AbilitySnapshot> tracking = MultimapBuilder.hashKeys().hashSetValues().build();
	private final Map<String, DefenderInfo> infos = new HashMap<>(); // Kingdom Name, Defender Info
	private final Set<DefenderAbility> abilities = new HashSet<>();
	private Optional<CitizensManager> citizensManager;

	public DefenderManager() {
		super(true);
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		Set<String> enabled = configuration.getConfigurationSection("invading.defenders.abilities").getKeys(false).parallelStream()
				.filter(key -> configuration.getBoolean("invading.defenders.abilities." + key + ".enabled", true))
				.map(key -> key)
				.collect(Collectors.toSet());
		Utils.getClassesOf(instance, instance.getPackageName() + ".objects.invasions.defenders", DefenderAbility.class).parallelStream().map(clazz -> {
			try {
				DefenderAbility ability = clazz.newInstance();
				return ability;
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		}).filter(ability -> ability != null && ability.initialize(instance))
		.filter(ability -> {
			for (String enable : enabled) {
				for (String name : ability.getNames()) {
					if (enable.equalsIgnoreCase(name))
						return true;
				}
			}
			return false;
		})
		.forEach(ability -> abilities.add(ability));
	}

	/**
	 * Get the Kingdom defender's information.
	 *
	 * @param kingdom OfflineKingdom to search for.
	 * @return DefenderInfo
	 */
	public DefenderInfo getDefenderInfo(OfflineKingdom kingdom) {
		return Optional.ofNullable(infos.get(kingdom.getName())).orElseGet(() -> {
			DefenderInfo defenderInfo = new DefenderInfo(kingdom);
			infos.put(kingdom.getName(), defenderInfo);
			return defenderInfo;
		});
	}

	public void startTicking(LivingEntity defender) {
		UUID uuid = defender.getUniqueId();
		abilities.forEach(ability -> {
			tracking.put(uuid, new AbilitySnapshot(ability));
			ability.onAdd(defender);
		});
	}

	public void death(LivingEntity defender) {
		UUID uuid = defender.getUniqueId();
		abilities.forEach(ability -> {
			tracking.removeAll(uuid);
			ability.onDeath(defender);
		});
	}

	@Override
	public void initalize() {
		InvadingManager invadingManager = instance.getManager(InvadingManager.class);
		InvasionMechanic<?> mechanic = invadingManager.getInvasionMechanic();
		instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, () -> {
			Set<UUID> remove = new HashSet<>();
			for (UUID uuid : tracking.keySet()) {
				LivingEntity defender = (LivingEntity) Bukkit.getEntity(uuid);
				Optional<OfflineKingdom> optional = mechanic.getDefenderOwner(defender);
				if (defender == null || !optional.isPresent() || defender.isDead() || !defender.isValid()) {
					defender.remove();
					remove.add(uuid);
					continue;
				}
				OfflineKingdom owner = optional.get();
				Optional<Invasion> invasion = invadingManager.getInvasions().parallelStream()
						.filter(inv -> inv.getTarget().equals(owner))
						.findFirst();
				if (!invasion.isPresent()) {
					defender.remove();
					remove.add(uuid);
					continue;
				}
				for (AbilitySnapshot snapshot : tracking.get(uuid)) {
					if (!snapshot.canTick())
						continue;
					snapshot.reset();
					abilities.parallelStream().filter(ability -> {
						for (String snapshotName : snapshot.getNames()) {
							for (String name : ability.getNames()) {
								if (snapshotName.equalsIgnoreCase(name))
									return true;
							}
						}
						return false;
					}).forEach(ability -> {
						DefenderInfo info = getDefenderInfo(owner);
						if (ability.isAsynchronous()) {
							ability.tick(defender, info, invasion.get());
							return;
						}
						instance.getServer().getScheduler().runTask(instance, () -> ability.tick(defender, info, invasion.get()));
					});
				}
			}
			remove.forEach(key -> tracking.removeAll(key));
		}, 0, 1);
	}

	@Override
	public void onDisable() {
		abilities.clear();
	}

	private class AbilitySnapshot {

		private final String[] names;
		private final int trigger;
		private int tick = 1;

		public AbilitySnapshot(DefenderAbility ability) {
			this.trigger = ability.getTick();
			this.names = ability.getNames();
		}

		public String[] getNames() {
			return names;
		}

		public void tick() {
			tick++;
			if (tick >= 20)
				tick = 1;
		}

		public void reset() {
			tick = 1;
		}

		public boolean canTick() {
			if (tick >= trigger)
				return true;
			tick();
			return false;
		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTurretDamageDefender(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!instance.getManager(WorldManager.class).acceptsWorld(victim.getWorld()))
			return;
		InvadingManager invadingManager = instance.getManager(InvadingManager.class);
		InvasionMechanic<?> mechanic = invadingManager.getInvasionMechanic();
		Optional<Defender> optional = mechanic.getDefender(victim);
		if (!optional.isPresent())
			return;
		Defender defender = optional.get();
		Entity attacker = event.getDamager();
		Optional<OfflineKingdom> turretOwner = instance.getManager(TurretManager.class).getProjectileKingdom(attacker);
		if (!turretOwner.isPresent())
			return;
		OfflineKingdom turretKingdom = turretOwner.get();
		if (turretKingdom.equals(defender.getOwner())) {
			event.setCancelled(true);
			event.setDamage(0);
			return;
		}
		DefenderDamageEvent damageEvent = new DefenderDamageEvent(defender, event.getDamage(), DefenderDamageCause.TURRET);
		Bukkit.getPluginManager().callEvent(damageEvent);
		if (!damageEvent.isCancelled())
			event.setDamage(damageEvent.getDamage());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderDeath(EntityDeathEvent event) {
		if (!configuration.getBoolean("invading.defender.defender-death-ends-invasion"))
			return;
		LivingEntity entity = event.getEntity();
		if (!instance.getManager(WorldManager.class).acceptsWorld(entity.getWorld()))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(entity))
				return;
		InvadingManager invadingManager = instance.getManager(InvadingManager.class);
		InvasionMechanic<?> mechanic = invadingManager.getInvasionMechanic();
		Optional<Defender> optional = mechanic.getDefender(entity);
		if (!optional.isPresent())
			return;
		Defender defender = optional.get();
		Invasion invasion = defender.getInvasion();
		Bukkit.getPluginManager().callEvent(new DefenderDeathEvent(defender, event));
		mechanic.death(entity);
		if (mechanic.getDefenders(defender.getOwner()).size() > 0)
			return;
		Bukkit.getPluginManager().callEvent(new InvadingStopEvent(StopReason.WIN, invasion));
		OfflineKingdom target = invasion.getTarget();
		invadingManager.stopInvasion(StopReason.WIN, target);
		new MessageBuilder("kingdoms.defender-death")
				.setPlaceholderObject(invasion.getInstigator())
				.setKingdom(target)
				.send(invasion.getInvolved());
		event.getDrops().clear();
	}

	@EventHandler
	public void onTargetChange(EntityTargetLivingEntityEvent event) {
		Entity entity = event.getEntity();
		if (!instance.getManager(WorldManager.class).acceptsWorld(entity.getWorld()))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(entity))
				return;
		InvadingManager invadingManager = instance.getManager(InvadingManager.class);
		InvasionMechanic<?> mechanic = invadingManager.getInvasionMechanic();
		Optional<Defender> optional = mechanic.getDefender(entity);
		if (!optional.isPresent())
			return;
		Defender defender = optional.get();
		Invasion invasion = defender.getInvasion();
		KingdomPlayer target = null;
		if (configuration.getBoolean("invading.defender.focus-on-instigator", false)) {
			target = invasion.getInstigator();
		}
		if (invasion.getInvolved().stream().anyMatch(kingdomPlayer -> kingdomPlayer.getPlayer().getUniqueId().equals(entity.getUniqueId()))) {
			Optional<KingdomPlayer> newTarget = invasion.getInvolved().stream()
					.filter(kingdomPlayer -> kingdomPlayer.getPlayer().getUniqueId().equals(event.getTarget().getUniqueId()))
					.findFirst();
			target = newTarget.orElse(invasion.getInstigator());
		}
		if (target == null)
			return;
		DefenderTargetEvent targetEvent = new DefenderTargetEvent(defender, target);
		Bukkit.getPluginManager().callEvent(targetEvent);
		if (targetEvent.isCancelled())
			return;
		event.setTarget(targetEvent.getTarget().getPlayer());
	}

	@EventHandler
	public void onDefenderDamaging(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(damager))
				return;
		if (!instance.getManager(WorldManager.class).acceptsWorld(damager.getWorld()))
			return;
		InvasionMechanic<?> mechanic = instance.getManager(InvadingManager.class).getInvasionMechanic();
		Optional<Defender> optional = mechanic.getDefender(damager);
		if (!optional.isPresent())
			return;
		Defender defender = optional.get();
		Entity entity = event.getEntity();
		if (!(entity instanceof Player))
			return;
		KingdomPlayer victim = instance.getManager(PlayerManager.class).getKingdomPlayer((Player)entity);
		Kingdom kingdom = victim.getKingdom();
		if (kingdom == null)
			return;
		DefenderDamagePlayerEvent damageEvent = new DefenderDamagePlayerEvent(defender, victim, event.getDamage());
		abilities.forEach(ability -> ability.onDamaging(damageEvent));
		if (damageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		event.setDamage(damageEvent.getDamage());
	}

	@EventHandler
	public void onDefenderDamage(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(entity))
				return;
		if (!instance.getManager(WorldManager.class).acceptsWorld(entity.getWorld()))
			return;
		Entity attacker = event.getDamager();
		if (!(attacker instanceof Player))
			return;
		Player damager = (Player) attacker;
		KingdomPlayer challenger = instance.getManager(PlayerManager.class).getKingdomPlayer(damager);
		Kingdom kingdom = challenger.getKingdom();
		if (kingdom == null)
			return;
		InvadingManager invadingManager = instance.getManager(InvadingManager.class);
		InvasionMechanic<?> mechanic = invadingManager.getInvasionMechanic();
		Optional<Defender> optional = mechanic.getDefender(entity);
		if (!optional.isPresent())
			return;
		Defender defender = optional.get();
		if (kingdom.equals(defender.getOwner())) {
			new MessageBuilder("kingdoms.defender-own")
					.setPlaceholderObject(challenger)
					.setKingdom(kingdom)
					.send(damager);
			event.setDamage(0.0D);
			return;
		}
		DefenderDamageByPlayerEvent damageEvent = new DefenderDamageByPlayerEvent(defender, challenger, event.getDamage());
		abilities.forEach(ability -> ability.onDamage(damageEvent));
		if (damageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		event.setDamage(damageEvent.getDamage());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderPotionDamage(PotionSplashEvent event) {
		Entity victim = event.getHitEntity();
		if (!instance.getManager(WorldManager.class).acceptsWorld(victim.getWorld()))
			return;
		Iterator<LivingEntity> iterator = event.getAffectedEntities().iterator();
		InvasionMechanic<?> mechanic = instance.getManager(InvadingManager.class).getInvasionMechanic();
		PlayerManager playerManager = instance.getManager(PlayerManager.class);
		TurretManager turretManager = instance.getManager(TurretManager.class);
		ProjectileSource thrower = event.getPotion().getShooter();
		while (iterator.hasNext()) {
			Entity entity = iterator.next();
			if (entity instanceof Player) {
				Player player = (Player) entity;
				GameMode gamemode = player.getGameMode();
				if (gamemode != GameMode.SURVIVAL && gamemode != GameMode.ADVENTURE) {
					iterator.remove();
					continue;
				}
				if (playerManager.getKingdomPlayer(player).hasAdminMode()) {
					iterator.remove();
					continue;
				}
			}
			OfflineKingdom throwerKingdom = null;
			if (thrower instanceof Player) {
				throwerKingdom = playerManager.getKingdomPlayer((Player) thrower).getKingdom();
			} else {
				Optional<OfflineKingdom> optional = turretManager.getProjectileKingdom(entity);
				if (optional.isPresent())
					throwerKingdom = optional.get();
			}
			if (throwerKingdom == null)
				continue;
			Optional<Defender> optional = mechanic.getDefender(entity);
			if (optional.isPresent()) {
				Defender defender = optional.get();
				OfflineKingdom defenderKingdom = defender.getOwner();
				if (defenderKingdom.equals(throwerKingdom) || defenderKingdom.isAllianceWith(throwerKingdom))
					iterator.remove();
				continue;
			}
		}
	}

}
