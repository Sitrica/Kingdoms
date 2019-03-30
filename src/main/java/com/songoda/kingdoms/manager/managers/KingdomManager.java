package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

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

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.events.KingdomCreateEvent;
import com.songoda.kingdoms.events.KingdomDeleteEvent;
import com.songoda.kingdoms.events.KingdomLoadEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.CooldownManager.KingdomCooldown;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.kingdom.BotKingdom;
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
	
	public static Set<OfflineKingdom> kingdoms = new HashSet<>(); // All Kingdoms contained in this Set that aren't bot kingdoms are actually online kingdoms.
	private final Set<String> processing = new HashSet<>(); // Names that are currently being created. Can't take these names.
	private final Set<BotKingdom> bots = new HashSet<>();
	private final Database<OfflineKingdom> database;
	private CitizensManager citizensManager;
	private PlayerManager playerManager;
	private WorldManager worldManager;
	private CooldownManager cooldowns;
	private LandManager landManager;
	private RankManager rankManager;
	private BukkitTask autoSaveThread;

	public KingdomManager() {
		super("kingdom", true);
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(OfflineKingdom.class);
		else
			database = getSQLiteDatabase(OfflineKingdom.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, saveTask, 0, IntervalUtils.getInterval(interval) * 20);
		}
	}
	
	@Override
	public void initalize() {
		this.landManager = instance.getManager("land", LandManager.class);
		this.rankManager = instance.getManager("rank", RankManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.cooldowns = instance.getManager("cooldown", CooldownManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.citizensManager = instance.getManager("citizens", CitizensManager.class);
	}
	
	private final Runnable saveTask = new Runnable() {
		@Override 
		public void run() {
			for (OfflineKingdom kingdom : kingdoms) {
				UUID uuid = kingdom.getUniqueId();
				String name = kingdom.getName();
				// Was an old mistake, this is bad management.
				// Still around for old database Kingdoms.
				// TODO make a reader to read all Kingdom names and delete if this ends.
				if (name.endsWith("_tmp")) {
					kingdoms.remove(kingdom);
					database.delete(kingdom.getUniqueId() + "");
					continue;
				}
				Kingdoms.debugMessage("Saving Kingdom: " + name);
				if (cooldowns.isInCooldown(kingdom, "attackcd"))
					kingdom.setInvasionCooldown(cooldowns.getTimeLeft(kingdom, "attackcd"));
				//null king means ready to remove
				if (kingdom.getKing() == null && !(kingdom instanceof BotKingdom)) {
					database.delete(uuid + "");
					continue;
				}
				database.save(uuid + "", kingdom);
			}
		}
	};
	
	public Set<OfflineKingdom> getKingdoms() {
		return kingdoms;
	}

	public Set<BotKingdom> getBotKingdoms() {
		return bots;
	}
	
	public Kingdom getKingdom(OfflineKingdom kingdom) {
		return getKingdom(kingdom.getUniqueId());
	}
	
	/**
	 * Check if the kingdom exists;
	 *
	 * @param kingdom OfflineKingdom to search
	 * @return true if exist; false if not exist
	 */
	public boolean hasKingdom(OfflineKingdom kingdom) {
		return kingdoms.contains(kingdom);
	}
	
	/**
	 * Check if the kingdom name exists;
	 *
	 * @param kingdom OfflineKingdom to search
	 * @return true if exist; false if not exist
	 */
	public boolean hasKingdom(String name) {
		return getOfflineKingdom(name).isPresent();
	}
	
	/**
	 * Checks if the following UUID can be used as it's not taken already.
	 * 
	 * @param uuid UUID to check for.
	 * @return boolean if the uuid is unused.
	 */
	public boolean canUse(UUID uuid) {
		return !kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getUniqueId().equals(uuid))
				.findFirst()
				.isPresent();
	}
	
	public boolean canRename(String name) {
		return kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getName().equals(name))
				.findFirst()
				.isPresent();
	}
	
	public void registerBotKingdom(BotKingdom kingdom) {
		bots.add(kingdom);
		kingdoms.add(kingdom);
		Kingdoms.debugMessage("Registered Bot Kingdom: " + kingdom.getName());
	}

	public boolean isBotKingdom(Kingdom kingdom) {
		if (kingdom instanceof BotKingdom)
			return true;
		if (bots.contains(kingdom))
			return true;
		return false;
	}

	public Kingdom getKingdom(UUID uuid) {
		if (uuid == null)
			return null;
		Kingdoms.debugMessage("Fetching info for kingdom: " + uuid);
		return kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getUniqueId().equals(uuid))
				.map(kingdom -> {
					if (kingdom instanceof Kingdom)
						return (Kingdom) kingdom;
					if (kingdom.getKing() == null && !(kingdom instanceof BotKingdom))
						database.delete(uuid + "");
					return null;
				})
				.findAny()
				.orElse(loadKingdom(uuid));
	}
	
	public Optional<Kingdom> getKingdomByName(String name) {
		if (name == null)
			return null;
		Kingdoms.debugMessage("Fetching info for kingdom: " + name);
		return kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getName().equals(name))
				.map(kingdom -> {
					if (kingdom instanceof Kingdom)
						return (Kingdom) kingdom;
					if (kingdom.getKing() == null && !(kingdom instanceof BotKingdom))
						database.delete(kingdom.getUniqueId() + "");
					return null;
				})
				.findAny();
	}
	
	private Kingdom loadKingdom(UUID uuid) {
		Kingdoms.consoleMessage("Loading kingdom: " + uuid);
		Kingdom kingdom = (Kingdom) database.get(uuid + "");
		if (kingdom != null) {
			long invasionCooldown = kingdom.getInvasionCooldown();
			if (invasionCooldown > 0) {
				KingdomCooldown cooldown = new KingdomCooldown(kingdom, "attackcd", invasionCooldown);
				cooldown.start();
				kingdom.setInvasionCooldown(0);
			}
			updateUpgrades(kingdom);
			kingdoms.add(kingdom);
			Bukkit.getPluginManager().callEvent(new KingdomLoadEvent(kingdom));
		}
		return kingdom;
	}
	
	/**
	 * Get OfflineKingdom. Only cached OfflineKingdoms will be searched.
	 *
	 * @param kingdomName kingdomName
	 * @return Kingdom instance; null if not exist
	 */
	public Optional<OfflineKingdom> getOfflineKingdom(String name) {
		Kingdoms.debugMessage("Fetching info for offline kingdom: " + name);
		Optional<OfflineKingdom> optional = kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getName().equals(name))
				.findFirst();
		if (optional.isPresent()) {
			OfflineKingdom kingdom = optional.get();
			if (kingdom.getKing() != null)
				return Optional.of(kingdom);
			database.delete(name);
		}
		return Optional.empty();
	}

	/**
	 * Get OfflineKingdom. Reading from database directly.
	 *
	 * @param kingdomName Kingdom name.
	 * @return Kingdom instance; null if not exist
	 */
	public Optional<OfflineKingdom> getOfflineKingdom(UUID uuid) {
		Kingdoms.debugMessage("Fetching info for offline kingdom: " + uuid);
		OfflineKingdom kingdom = loadKingdom(uuid);
		if (kingdom != null && kingdom.getKing() == null) {
			database.delete(kingdom.toString());
			return Optional.empty();
		}
		return Optional.of(kingdom);
	}
	
	/**
	 * Check if a kingdom is online.
	 *
	 * @param kingdom OfflineKingdom instance
	 * @return true if online/loaded; false if not.
	 */
	public boolean isOnline(OfflineKingdom kingdom) {
		Optional<OfflineKingdom> optional = kingdoms.parallelStream()
				.filter(k -> k == kingdom)
				.findFirst();
		if (!optional.isPresent())
			return false;
		return optional.get() instanceof Kingdom;
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
	 * @param kingdomName name of kingdom to delete
	 * @return true if scheduled; false if kingdom doesn't exist
	 */
	public boolean deleteKingdom(OfflineKingdom kingdom) {
		if (kingdom == null)
			return false;
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				if (kingdom instanceof BotKingdom) {
					Bukkit.getPluginManager().callEvent(new KingdomDeleteEvent(kingdom));
					landManager.unclaimAllLand(kingdom);
					return;
				}
				for (OfflineKingdomPlayer player : kingdom.getMembers()) {
					player.setKingdom(null);
					player.setRank(rankManager.getDefaultRank());
				}
				OfflineKingdomPlayer king = kingdom.getKing();
				kingdoms.remove(kingdom);
				database.delete(kingdom.getUniqueId() + "");
				Bukkit.getPluginManager().callEvent(new KingdomDeleteEvent(kingdom));
				landManager.unclaimAllLand(kingdom);
				if (king.isOnline())
					new MessageBuilder("kingdoms.deleted")
							.setPlaceholderObject(king)
							.setKingdom(kingdom)
							.send(king.getKingdomPlayer());
			}
		});
		return true;
	}
	
	/**
	 * This method will <b> overwrite existing data! </b> use hasKingdom() to
	 * check if kingdom already exists
	 *
	 * @param kingdomName
	 * @param king		the king of this kingdom
	 * @return false - already in progress, true - create task scheduled
	 */
	public boolean createNewKingdom(String name, KingdomPlayer king) {
		if (processing.contains(name))
			return false;
		processing.add(name);
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				Kingdom kingdom = new Kingdom(king);
				kingdom.setName(name);
				String interval = configuration.getString("kingdoms.base-shield-time", "5 minutes");
				kingdom.setShieldTime(IntervalUtils.getInterval(interval));
				king.setRank(rankManager.getOwnerRank());
				king.setKingdom(kingdom);
				kingdoms.add(kingdom);
				updateUpgrades(kingdom);
				new MessageBuilder("kingdoms.creation")
						.setKingdom(kingdom)
						.send(king);
				processing.remove(name);
				Bukkit.getPluginManager().callEvent(new KingdomCreateEvent(kingdom));
			}
		});
		return true;
	}
	
	public FutureTask<Map<OfflineKingdom, Long>> getTopResourcePoints() {
		return new FutureTask<Map<OfflineKingdom, Long>>(new ResourcePointsCallable());
	}
	
	private class ResourcePointsCallable implements Callable<Map<OfflineKingdom, Long>> {
		@Override
		public Map<OfflineKingdom, Long> call() {
			Map<OfflineKingdom, Long> points = new HashMap<>();
			for (String key : database.getKeys()) {
				UUID uuid = UUID.fromString(key);
				Kingdom kingdom = getKingdom(uuid);
				if (kingdom == null) {
					continue;
				} else {
					if (configuration.getBoolean("kingdoms.leaderboard-hide-pacifists", false) && kingdom.isNeutral())
						continue;
					points.put(kingdom, kingdom.getResourcePoints());
				}
			}
			return points;
		}
	}

	public void updateUpgrades(Kingdom kingdom) {
		if (kingdom == null)
			return;
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				int max = configuration.getInt("kingdoms.max-members-via-upgrade", 30);
				if (kingdom.getMaxMembers() > max)
					kingdom.setMaxMembers(max);
				MiscUpgrade miscUpgrades = kingdom.getMiscUpgrades();
				for (MiscUpgradeType upgrade : MiscUpgradeType.values()) {
					if (upgrade.isDefault())
						miscUpgrades.setBought(upgrade, true);
				}
			}
		});
	}

	@EventHandler
	public void onMemberAttacksKingdomMember(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!(victim instanceof Player))
			return;
		if (!worldManager.acceptsWorld(victim.getWorld()))
			return;
		Entity attacker = event.getDamager();
		if (attacker.equals(victim))
			return;
		if (citizensManager.isCitizen(victim) || citizensManager.isCitizen(attacker))
			return;
		KingdomPlayer attacked = null;
		if (attacker instanceof Projectile) {
			if (((Projectile) attacker).getType() == EntityType.ENDER_PEARL)
				return;
			ProjectileSource shooter = ((Projectile) attacker).getShooter();
			if (shooter != null) {
				if (shooter instanceof Player) {
					if (citizensManager.isCitizen((Player) shooter))
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
		if (!worldManager.acceptsWorld(victim.getWorld()))
			return;
		if (configuration.getBoolean("kingdoms.alliance-can-pvp", false))
			return;
		if (citizensManager.isCitizen(victim))
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
					if (citizensManager.isCitizen(player))
						return;
					kingdomPlayer = playerManager.getKingdomPlayer(player);
				}
			}
		} else if (attacker instanceof Player) {
			if (citizensManager.isCitizen(attacker))
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
		for (OfflineKingdom offlineKingdom : kingdoms) {
			if (!(offlineKingdom instanceof Kingdom))
				offlineKingdom = offlineKingdom.getKingdom();
			Kingdom kingdom = (Kingdom) offlineKingdom;
			kingdom.onKingdomDelete(event.getKingdom());
		}
	}
	
	@EventHandler
	public void onNeutralMemberAttackOrAttacked(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!worldManager.acceptsWorld(victim.getWorld()))
			return;
		if (!(victim instanceof Player))
			return;
		if (citizensManager.isCitizen(victim))
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
				if (citizensManager.isCitizen(player))
					return;
				kingdomPlayer = playerManager.getKingdomPlayer(player);
			}
		} else if (attacker instanceof Player) {
			if (citizensManager.isCitizen(attacker))
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

		if (attackerKingdom.isNeutral() && attackerKingdom.equals(victimLand.getKingdomOwner())) {
			new MessageBuilder("kingdoms.pacifist-cannot-fight-in-own-land")
					.setPlaceholderObject(damaged)
					.setKingdom(attackerKingdom)
					.send(kingdomPlayer);
			event.setCancelled(true);
			return;
		}
		if (victimKingdom.isNeutral() && victimKingdom.equals(attackerLand.getKingdomOwner())) {
			new MessageBuilder("kingdoms.pacifist-cannot-be-damaged")
					.setPlaceholderObject(damaged)
					.setKingdom(attackerKingdom)
					.send(kingdomPlayer);
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Land land = landManager.getLand(kingdomPlayer.getLocation().getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
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
		if (kingdom.getOnlineEnemies().contains(kingdomPlayer) || kingdom.isEnemyWith(landKingdom)) {
			if (isCommandDisabled(event.getMessage(), "commands.denied-in-enemy")) {
				new MessageBuilder("commands.kingdom-denied-enemy")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				event.setCancelled(true);
			}
		} else if (!kingdom.getMembers().contains(kingdomPlayer) && !kingdom.getOnlineAllies().contains(kingdomPlayer)) {
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
		autoSaveThread.cancel();
		saveTask.run();
		kingdoms.clear();
	}

}
