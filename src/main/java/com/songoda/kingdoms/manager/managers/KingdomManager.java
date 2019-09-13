package com.songoda.kingdoms.manager.managers;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.events.KingdomCreateEvent;
import com.songoda.kingdoms.events.KingdomDeleteEvent;
import com.songoda.kingdoms.events.KingdomLoadEvent;
import com.songoda.kingdoms.events.MemberLeaveEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.CooldownManager.KingdomCooldown;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class KingdomManager extends Manager {

	private final AsyncLoadingCache<String, Optional<OfflineKingdom>> cache = Caffeine.newBuilder()
			.expireAfterAccess(50, TimeUnit.MINUTES)
			.removalListener((String name, Optional<OfflineKingdom> kingdom, RemovalCause cause) -> {
				if (kingdom != null && kingdom.isPresent())
					save(kingdom.get());
			})
			.maximumSize(1000)
			.buildAsync(new CacheLoader<String, Optional<OfflineKingdom>>() {
					public Optional<OfflineKingdom> load(String name) {
						if (name == null)
							return Optional.empty();
						Kingdoms.debugMessage("Loading offline kingdom: " + name);
						OfflineKingdom databaseKingdom = database.get(name);
						if (databaseKingdom == null) {
							Kingdoms.debugMessage("No data found for kingdom: " + name);
							return Optional.empty();
						}
						Kingdom kingdom = new Kingdom(databaseKingdom);
						if (kingdom != null) {
							long invasionCooldown = kingdom.getInvasionCooldown();
							if (invasionCooldown > 0) {
								KingdomCooldown cooldown = new KingdomCooldown(kingdom, "attackcd", invasionCooldown);
								cooldown.start();
								kingdom.setInvasionCooldown(0);
							}
							updateUpgrades(kingdom);
							instance.getServer().getScheduler().runTask(instance, () -> Bukkit.getPluginManager().callEvent(new KingdomLoadEvent(kingdom)));
						}
						return Optional.ofNullable(kingdom);
					}
			});
	private Optional<CitizensManager> citizensManager;
	private Database<OfflineKingdom> database;
	private PlayerManager playerManager;
	private BukkitTask autoSaveThread;
	private LandManager landManager;

	public KingdomManager() {
		super(true);
	}

	@Override
	public void initalize() {
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.playerManager = instance.getManager( PlayerManager.class);
		this.landManager = instance.getManager(LandManager.class);
		String table = configuration.getString("database.kingdom-table", "Kingdoms");
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(table, OfflineKingdom.class);
		else
			database = getFileDatabase(table, OfflineKingdom.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> saveAll(), 0, IntervalUtils.getInterval(interval) * 20);
		}
	}

	private void save(OfflineKingdom kingdom) {
		CooldownManager cooldowns = instance.getManager(CooldownManager.class);
		String name = kingdom.getName();
		Kingdoms.debugMessage("Saving Kingdom: " + name);
		if (cooldowns.isInCooldown(kingdom, "attackcd"))
			kingdom.setInvasionCooldown(cooldowns.getTimeLeft(kingdom, "attackcd"));
		database.put(name, kingdom);
	}

	private void saveAll() {
		cache.asMap().entrySet().parallelStream()
				.map(future -> {
					try {
						return future.getValue().get(5, TimeUnit.SECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						return null;
					}
				})
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.forEach(kingdom -> save(kingdom));
	}

	/**
	 * @return All cached Kingdoms.
	 */
	public Set<OfflineKingdom> getKingdoms() {
		return cache.asMap().values().parallelStream()
				.map(future -> {
					try {
						return future.get(5, TimeUnit.SECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						return null;
					}
				})
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	public Set<OfflineKingdom> getOfflineKingdoms() {
		return database.getKeys().parallelStream()
				.map(name -> getOfflineKingdom(name))
				.filter(kingdom -> kingdom.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	/**
	 * Check if the kingdom exists in cache;
	 *
	 * @param kingdom OfflineKingdom to search
	 * @return true if exist; false if not exist
	 */
	public boolean hasKingdom(OfflineKingdom kingdom) {
		return cache.getIfPresent(kingdom) != null;
	}

	public Kingdom convert(OfflineKingdom other) {
		Kingdom kingdom = new Kingdom(other);
		String name = other.getName();
		Kingdoms.debugMessage("Converting offline kingdom to online kingdom: " + name);
		cache.put(name, CompletableFuture.completedFuture(Optional.of(kingdom)));
		return kingdom;
	}

	/**
	 * Check if the kingdom name exists in the loaded Kingdoms;
	 *
	 * @param kingdom OfflineKingdom to search
	 * @return Optional if the Kingdom was found.
	 */
	public boolean hasKingdom(String name) {
		return getOfflineKingdoms().parallelStream().anyMatch(kingdom -> kingdom.getName().equalsIgnoreCase(name));
	}

	public Optional<Kingdom> getKingdom(String name) {
		if (name == null || !hasKingdom(name))
			return Optional.empty();
		Kingdoms.debugMessage("Fetching info for online kingdom: " + name);
		Optional<OfflineKingdom> optional;
		try {
			optional = cache.get(name).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return Optional.empty();
		}
		if (!optional.isPresent())
			return Optional.empty();
		OfflineKingdom kingdom = optional.get();
		return Optional.of(kingdom instanceof Kingdom ? (Kingdom) kingdom : convert(kingdom));
	}

	/**
	 * Get OfflineKingdom. Reading from database directly.
	 *
	 * @param name Kingdom name.
	 * @return Optional if the OfflineKingdom was found.
	 */
	public Optional<OfflineKingdom> getOfflineKingdom(String name) {
		try {
			return cache.get(name).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return Optional.empty();
		}
	}

	/*
	 * When a player leaves the server NOT the Kingdom.
	 */
	public void onPlayerLeave(KingdomPlayer player, Kingdom kingdom) {
		database.put(kingdom.getName(), kingdom);
		MemberLeaveEvent event = new MemberLeaveEvent(player, kingdom);
		Bukkit.getPluginManager().callEvent(event);
		instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> {
			if (kingdom.getOnlinePlayers().isEmpty())
				cache.synchronous().invalidate(kingdom.getName());
		}, 1);
	}

	/**
	 * Check if a kingdom is online.
	 *
	 * @param kingdom OfflineKingdom instance
	 * @return true if online/loaded; false if not.
	 */
	public boolean isOnline(OfflineKingdom kingdom) {
		return Optional.ofNullable(cache.getIfPresent(kingdom.getName()))
				.map(future -> {
					try {
						return future.get(5, TimeUnit.SECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						return null;
					}
				})
				.filter(optional -> optional.get() instanceof Kingdom)
				.isPresent();
	}

	public int getRandomColor() {
		Random random = new Random();
		int color = 0;
		int r = random.nextInt(255);
		int g = random.nextInt(255);
		int b = random.nextInt(255);
		color = (r << 16) + (g << 8) + b;
		return color;
	}

	/**
	 * schedule kingdom delete task
	 *
	 * @param name name of kingdom to delete
	 */
	public boolean deleteKingdom(String name) {
		if (name == null)
			return false;
		Optional<OfflineKingdom> optional;
		try {
			optional = cache.get(name).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			optional = Optional.empty();
		}
		if (!optional.isPresent()) // Kingdom never existed.
			return false;
		OfflineKingdom kingdom = optional.get();
		for (OfflineKingdomPlayer player : kingdom.getMembers()) {
			player.onKingdomLeave();
			player.setKingdom(null);
			player.setRank(null);
			instance.getManager(PlayerManager.class).save(player);
		}
		Optional<OfflineKingdomPlayer> owner = kingdom.getOwner();
		if (owner.isPresent()) {
			Optional<KingdomPlayer> ownerPlayer = owner.get().getKingdomPlayer();
			if (ownerPlayer.isPresent())
				new MessageBuilder("kingdoms.deleted")
						.setPlaceholderObject(owner)
						.setKingdom(kingdom)
						.send(ownerPlayer.get());
		}
		database.delete(name);
		Bukkit.getPluginManager().callEvent(new KingdomDeleteEvent(kingdom));
		landManager.unclaimAllLand(kingdom);
		cache.synchronous().invalidate(name);
		kingdom.setOwner(null);
		return true;
	}

	/**
	 * This method will <b> overwrite existing data! </b> use hasKingdom() to
	 * check if kingdom already exists
	 *
	 * @param name The name of this new Kingdom.
	 * @param king The king of this kingdom
	 * @return Kingdom that is to be created.
	 */
	public Kingdom createNewKingdom(String name, KingdomPlayer owner) {
		Kingdom kingdom = new Kingdom(owner, name);
		database.put(kingdom.getName(), kingdom);
		cache.put(name, CompletableFuture.completedFuture(Optional.of(kingdom)));
		String interval = configuration.getString("kingdoms.base-shield-time", "5 minutes");
		kingdom.setShieldTime(IntervalUtils.getInterval(interval));
		updateUpgrades(kingdom);
		owner.setRank(instance.getManager(RankManager.class).getOwnerRank());
		owner.setKingdom(name);
		Bukkit.getPluginManager().callEvent(new KingdomCreateEvent(kingdom));
		return kingdom;
	}

	public void updateUpgrades(Kingdom kingdom) {
		if (kingdom == null)
			return;
		int max = configuration.getInt("kingdoms.max-members-via-upgrade", 30);
		if (kingdom.getMaxMembers() > max)
			kingdom.setMaxMembers(max);
		MiscUpgrade miscUpgrades = kingdom.getMiscUpgrades();
		for (MiscUpgradeType upgrade : MiscUpgradeType.values()) {
			if (upgrade.isDefault())
				miscUpgrades.setBought(upgrade, true);
		}
	}

	@EventHandler
	public void onMemberAttacksKingdomMember(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!(victim instanceof Player))
			return;
		if (!instance.getManager(WorldManager.class).acceptsWorld(victim.getWorld()))
			return;
		Entity attacker = event.getDamager();
		if (attacker.equals(victim))
			return;
		if (citizensManager.isPresent()) {
			CitizensManager citizens = citizensManager.get();
			if (citizens.isCitizen(victim) || citizens.isCitizen(attacker))
				return;
		}
		KingdomPlayer attacked = null;
		if (attacker instanceof Projectile) {
			if (((Projectile) attacker).getType() == EntityType.ENDER_PEARL)
				return;
			ProjectileSource shooter = ((Projectile) attacker).getShooter();
			if (shooter != null) {
				if (shooter instanceof Player) {
					if (citizensManager.isPresent())
						if (citizensManager.get().isCitizen((Player) shooter))
							return;
					attacked = playerManager.getKingdomPlayer((Player) shooter);
				}
			}

		} else if (attacker instanceof Player) {
			attacked = playerManager.getKingdomPlayer((Player) attacker);
		}
		if (attacked == null)
			return;
		if (attacked.hasAdminMode())
			return;
		if (attacked.getKingdom() == null)
			return;
		KingdomPlayer damaged = playerManager.getKingdomPlayer((Player) victim);
		Kingdom kingdom = damaged.getKingdom();
		if (kingdom == null)
			return;
		if (attacked.getKingdom().equals(kingdom)) {
			if (!configuration.getBoolean("kingdoms.friendly-fire", false)) {
				event.setDamage(0);
				event.setCancelled(true);
				new MessageBuilder("kingdoms.cannot-attack-members")
						.replace("%player%", attacked.getName())
						.setKingdom(kingdom)
						.send(attacked);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onMemberAttacksAllyMembers(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!(victim instanceof Player))
			return;
		if (!instance.getManager(WorldManager.class).acceptsWorld(victim.getWorld()))
			return;
		if (configuration.getBoolean("kingdoms.alliance-can-pvp", false))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(victim))
				return;
		Entity attacker = event.getDamager();
		if (attacker.getUniqueId().equals(victim.getUniqueId()))
			return;
		KingdomPlayer kingdomPlayer = null;
		if (attacker instanceof Projectile) {
			Projectile projectile = (Projectile)attacker;
			ProjectileSource shooter = projectile.getShooter();
			if (shooter != null) {
				if (shooter instanceof Player) {
					Player player = (Player)shooter;
					if (citizensManager.isPresent())
						if (citizensManager.get().isCitizen(player))
							return;
					kingdomPlayer = playerManager.getKingdomPlayer(player);
				}
			}
		} else if (attacker instanceof Player) {
			if (citizensManager.isPresent())
				if (citizensManager.get().isCitizen(attacker))
					return;
			kingdomPlayer = playerManager.getKingdomPlayer((Player)attacker);
		}
		if (kingdomPlayer == null)
			return;
		if (kingdomPlayer.hasAdminMode())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		KingdomPlayer victimKingdomPlayer = playerManager.getKingdomPlayer((Player) victim);
		Kingdom victimKingdom = victimKingdomPlayer.getKingdom();
		if (victimKingdom == null)
			return;
		if (kingdom.isAllianceWith(victimKingdom)) {
			new MessageBuilder("kingdoms.cannot-attack-ally")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send(kingdomPlayer);
			event.setDamage(0.0D);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onKingdomDelete(KingdomDeleteEvent event) {
		for (OfflineKingdom offlineKingdom : getKingdoms()) {
			if (!(offlineKingdom instanceof Kingdom))
				offlineKingdom = offlineKingdom.getKingdom();
			Kingdom kingdom = (Kingdom) offlineKingdom;
			kingdom.onKingdomDelete(event.getKingdom());
		}
	}

	@EventHandler
	public void onNeutralMemberAttackOrAttacked(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!instance.getManager(WorldManager.class).acceptsWorld(victim.getWorld()))
			return;
		if (!(victim instanceof Player))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(victim))
				return;
		if (!configuration.getBoolean("allow-pacifist"))
			return;
		if (!configuration.getBoolean("kingdoms.pacifist-cannot-fight-in-land"))
			return;
		Entity attacker = event.getDamager();
		if (attacker.getUniqueId().equals(victim.getUniqueId()))
			return;
		KingdomPlayer kingdomPlayer = null;
		if (attacker instanceof Projectile) {
			Projectile projectile = (Projectile) attacker;
			ProjectileSource shooter = projectile.getShooter();
			if (shooter == null)
				return;
			if (shooter instanceof Player) {
				Player player = (Player) shooter;
				if (citizensManager.isPresent())
					if (citizensManager.get().isCitizen(player))
						return;
				kingdomPlayer = playerManager.getKingdomPlayer(player);
			}
		} else if (attacker instanceof Player) {
			if (citizensManager.isPresent())
				if (citizensManager.get().isCitizen(attacker))
					return;
			kingdomPlayer = playerManager.getKingdomPlayer((Player) attacker);
		}
		if (kingdomPlayer == null)
			return;
		if (kingdomPlayer.hasAdminMode())
			return;
		KingdomPlayer damaged = playerManager.getKingdomPlayer((Player) victim);
		Land attackerLand = landManager.getLandAt(kingdomPlayer.getLocation());
		Land victimLand = landManager.getLandAt(damaged.getLocation());

		Kingdom attackerKingdom = kingdomPlayer.getKingdom();
		Kingdom victimKingdom = damaged.getKingdom();
		if (attackerKingdom == null && victimKingdom == null)
			return;
		Optional<OfflineKingdom> optionalVictim = victimLand.getKingdomOwner();
		if (!optionalVictim.isPresent())
			return;
		OfflineKingdom victimOwner = optionalVictim.get();
		if (attackerKingdom.isNeutral() && attackerKingdom.equals(victimOwner)) {
			new MessageBuilder("kingdoms.pacifist-cannot-fight-in-own-land")
					.setPlaceholderObject(damaged)
					.setKingdom(attackerKingdom)
					.send(kingdomPlayer);
			event.setCancelled(true);
			return;
		}
		Optional<OfflineKingdom> optionalAttacker = attackerLand.getKingdomOwner();
		if (!optionalAttacker.isPresent())
			return;
		OfflineKingdom attackerOwner = optionalAttacker.get();
		if (victimKingdom.isNeutral() && victimKingdom.equals(attackerOwner)) {
			new MessageBuilder("kingdoms.pacifist-cannot-be-damaged")
					.setPlaceholderObject(damaged)
					.setKingdom(attackerKingdom)
					.send(kingdomPlayer);
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (!instance.getManager(WorldManager.class).acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Land land = kingdomPlayer.getLandAt();
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (!optional.isPresent())
			return;
		OfflineKingdom landKingdom = optional.get();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			if (isCommandDisabled(event.getMessage(), "commands.denied-in-neutral")) {
				new MessageBuilder("commands.kingdom-denied-neutral")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				event.setCancelled(true);
			}
			return;
		}
		if (kingdom.getOnlineEnemies().stream().anyMatch(p -> p.equals(kingdomPlayer)) || kingdom.isEnemyWith(landKingdom)) {
			if (isCommandDisabled(event.getMessage(), "commands.denied-in-enemy")) {
				new MessageBuilder("commands.kingdom-denied-enemy")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				event.setCancelled(true);
			}
		} else if (!kingdom.getMembers().stream().anyMatch(p -> p.equals(kingdomPlayer)) && !kingdom.getOnlineAllies().stream().anyMatch(p -> p.equals(kingdomPlayer))) {
			if (isCommandDisabled(event.getMessage(), "commands.denied-in-neutral")) {
				new MessageBuilder("commands.kingdom-denied-other")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				event.setCancelled(true);
			}
		}
	}

	private boolean isCommandDisabled(String message, String node) {
		List<String> commands = configuration.getStringList(node);
		if (configuration.getBoolean("commands.contains", false))
			return commands.parallelStream().anyMatch(string -> string.contains(message));
		return commands.parallelStream().anyMatch(string -> string.equalsIgnoreCase(message));
	}

	@Override
	public synchronized void onDisable() {
		if (autoSaveThread != null)
			autoSaveThread.cancel();
		saveAll();
	}

}
