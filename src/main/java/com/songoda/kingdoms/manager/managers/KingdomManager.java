package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	public static Set<OfflineKingdom> kingdoms = new HashSet<>();
	private Optional<CitizensManager> citizensManager;
	private Database<OfflineKingdom> database;
	private PlayerManager playerManager;
	private WorldManager worldManager;
	private CooldownManager cooldowns;
	private BukkitTask autoSaveThread;
	private LandManager landManager;
	private RankManager rankManager;

	public KingdomManager() {
		super("kingdom", true);
	}

	@Override
	public void initalize() {
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.cooldowns = instance.getManager("cooldown", CooldownManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
		this.rankManager = instance.getManager("rank", RankManager.class);
		String table = configuration.getString("database.kingdom-table", "Kingdoms");
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(table, OfflineKingdom.class);
		else
			database = getFileDatabase(table, OfflineKingdom.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, saveTask, 0, IntervalUtils.getInterval(interval) * 20);
		}
	}

	private final Runnable saveTask = new Runnable() {
		@Override 
		public void run() {
			for (OfflineKingdom kingdom : kingdoms) {
				String name = kingdom.getName();
				Kingdoms.debugMessage("Saving Kingdom: " + name);
				if (cooldowns.isInCooldown(kingdom, "attackcd"))
					kingdom.setInvasionCooldown(cooldowns.getTimeLeft(kingdom, "attackcd"));
				database.save(name, kingdom);
			}
		}
	};

	/**
	 * @return All cached Kingdoms.
	 */
	public Set<OfflineKingdom> getKingdoms() {
		return kingdoms;
	}

	/**
	 * Check if the kingdom exists in cache;
	 *
	 * @param kingdom OfflineKingdom to search
	 * @return true if exist; false if not exist
	 */
	public boolean hasKingdom(OfflineKingdom kingdom) {
		return kingdoms.contains(kingdom);
	}

	public Kingdom convert(OfflineKingdom other) {
		Kingdom kingdom = new Kingdom(other);
		String name = other.getName();
		Iterator<OfflineKingdom> iterator = kingdoms.iterator();
		while (iterator.hasNext()) {
			OfflineKingdom existing = iterator.next();
			if (existing.getName().equalsIgnoreCase(name))
				iterator.remove();
		}
		kingdoms.add(kingdom);
		return kingdom;
	}

	/**
	 * Check if the kingdom name exists in the loaded Kingdoms;
	 *
	 * @param kingdom OfflineKingdom to search
	 * @return Optional if the Kingdom was found.
	 */
	public boolean hasKingdom(String name) {
		return getOfflineKingdom(name).isPresent();
	}

	public Optional<Kingdom> getKingdom(String name) {
		if (name == null)
			return Optional.empty();
		Kingdoms.debugMessage("Fetching info for online kingdom: " + name);
		return kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getName().equalsIgnoreCase(name))
				.map(kingdom -> kingdom instanceof Kingdom ? (Kingdom) kingdom : null)
				.findAny();
	}

	/**
	 * Get OfflineKingdom. Reading from database directly.
	 *
	 * @param name Kingdom name.
	 * @return Optional if the OfflineKingdom was found.
	 */
	public Optional<OfflineKingdom> getOfflineKingdom(String name) {
		if (name == null)
			return Optional.empty();
		Kingdoms.debugMessage("Fetching info for offline kingdom: " + name);
		return Optional.ofNullable(kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getName().equals(name))
				.findFirst()
				.orElse(loadKingdom(name)));
	}

	private Kingdom loadKingdom(String name) {
		Kingdoms.debugMessage("Attemping loading for kingdom: " + name);
		FutureTask<Kingdom> future = new FutureTask<>(() -> {
			OfflineKingdom databaseKingdom = database.get(name);
			if (databaseKingdom == null)
				return null;
			Kingdom kingdom = new Kingdom(databaseKingdom);
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
		});
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(future);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void onPlayerLeave(KingdomPlayer player, Kingdom kingdom) {
		if (kingdom.getOnlinePlayers().isEmpty()) {
			instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
				database.save(kingdom.getName(), kingdom);
				kingdoms.remove(kingdom);
			});
		}
	}

	/**
	 * Check if a kingdom is online.
	 *
	 * @param kingdom OfflineKingdom instance
	 * @return true if online/loaded; false if not.
	 */
	public boolean isOnline(OfflineKingdom kingdom) {
		return kingdoms.contains(kingdom);
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
		for (OfflineKingdomPlayer player : kingdom.getMembers()) {
			player.setKingdom(null);
			player.setRank(null);
		}
		OfflineKingdomPlayer owner = kingdom.getOwner();
		kingdoms.remove(kingdom);
		database.delete(kingdom.getName());
		Bukkit.getPluginManager().callEvent(new KingdomDeleteEvent(kingdom));
		landManager.unclaimAllLand(kingdom);
		Optional<KingdomPlayer> kingPlayer = owner.getKingdomPlayer();
		if (kingPlayer.isPresent())
			new MessageBuilder("kingdoms.deleted")
					.setPlaceholderObject(owner)
					.setKingdom(kingdom)
					.send(kingPlayer.get());
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
	public Kingdom createNewKingdom(String name, KingdomPlayer king) {
		FutureTask<Kingdom> future = new FutureTask<>(() -> {
			Kingdom kingdom = new Kingdom(king, name);
			kingdoms.add(kingdom);
			String interval = configuration.getString("kingdoms.base-shield-time", "5 minutes");
			kingdom.setShieldTime(IntervalUtils.getInterval(interval));
			king.setRank(rankManager.getOwnerRank());
			king.setKingdom(name);
			updateUpgrades(kingdom);
			database.save(kingdom.getName(), kingdom);
			Bukkit.getPluginManager().callEvent(new KingdomCreateEvent(kingdom));
			return kingdom;
		});
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(future);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return A map of all the online kingdoms sorted by resource points.
	 */
	public FutureTask<Map<Kingdom, Long>> getTopResourcePoints() {
		return new FutureTask<Map<Kingdom, Long>>(new ResourcePointsCallable());
	}

	private class ResourcePointsCallable implements Callable<Map<Kingdom, Long>> {
		@Override
		public Map<Kingdom, Long> call() {
			Map<Kingdom, Long> points = new HashMap<>();
			for (String key : database.getKeys()) {
				Optional<Kingdom> optional = getKingdom(key);
				if (!optional.isPresent()) {
					continue;
				} else {
					Kingdom kingdom = optional.get();
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
		if (!worldManager.acceptsWorld(victim.getWorld()))
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
		if (!worldManager.acceptsWorld(victim.getWorld()))
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

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Land land = landManager.getLand(kingdomPlayer.getLocation().getChunk());
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
		if (autoSaveThread != null)
			autoSaveThread.cancel();
		saveTask.run();
		kingdoms.clear();
	}

}
