package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.Turret;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerMovementManager extends Manager {

	private long spam = System.currentTimeMillis();
	private final FileConfiguration configuration;
	private final WorldManager worldManager;
	private final LandManager landManager;
	private final Kingdoms instance;
	
	public PlayerMovementManager() {
		super(true);
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.landManager = instance.getManager("land", LandManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (ExternalManager.isCitizen(player))
			return;
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			@Override
			public void run() {
				if (!player.isOnline() || !player.isValid())
					return;
				PlayerChangeChunkEvent event = new PlayerChangeChunkEvent(player, null, player.getLocation().getChunk());
				Bukkit.getPluginManager().callEvent(event);
			}
		}, 5);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (ExternalManager.isCitizen(player))
			return;
		Location from = event.getFrom();
		Location to = event.getTo();
		if (from.getChunk().equals(to.getChunk()))
			return;
		PlayerChangeChunkEvent change = new PlayerChangeChunkEvent(player, null, to.getChunk());
		Bukkit.getPluginManager().callEvent(change);
		if (change.isCancelled())
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		World world = player.getWorld();
		if (worldManager.acceptsWorld(world))
			return;
		if (ExternalManager.isCitizen(player))
			return;
		Location from = event.getFrom();
		Location to = event.getTo();
		Chunk chunkFrom = from.getChunk();
		Chunk chunkTo = to.getChunk();
		int centerX = chunkTo.getX(), centerZ = chunkTo.getZ();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				Chunk next = world.getChunkAt(centerX + x, centerZ + z);
				Optional<Land> optional = landManager.getLandAt(next);
				if (optional.isPresent()) {
					Land land = optional.get();
					List<Turret> turrets = land.getTurrets();
					for (Turret turret : turrets) {
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
							turret.fire(closest);
					}
				}
			}
		}
		if (chunkTo != chunkFrom) {
			//Check if player is in a fightzone
			boolean invadingDeny = configuration.getBoolean("invading-deny-chunk-change", true);
			if (GameManagement.getPlayerManager().getSession(player).getFightZone() != null && invadingDeny) {
				// Direction from to to.
				Vector vector = from.toVector().subtract(to.toVector()).normalize().multiply(2);
				// This used to be teleport to player.getLocation().add(vector)
				// Changed to velocity because I think pushing them back with an animation looks better.
				player.setVelocity(vector);
				player.setFallDistance(0);
				event.setCancelled(true);
				new MessageBuilder("kingdoms.invading.invading-deny-chunk-change")
						.replace("%chunkFrom%", LocationUtils.chunkToString(chunkFrom))
						.replace("%chunkTo%", LocationUtils.chunkToString(chunkTo))
						.replace("%kingdom%", kingdom)
						.replace("%land%", kingdom)
						.replace("%player%", player.getName())
						.send(player);
				return;
			}
			PlayerChangeChunkEvent change = new PlayerChangeChunkEvent(player, chunkFrom, chunkTo);
			Bukkit.getServer().getPluginManager().callEvent(change);
			if (change.isCancelled()) {
				player.setFallDistance(0);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onChunkChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		World world = player.getWorld();
		if (worldManager.acceptsWorld(world))
			return;
		//TODO add configuration option to ignore or not regions.
		if (ExternalManager.isInRegion(player.getLocation()))
			return;
		KingdomPlayer kingdomPlayer = GameManagement.getPlayerManager().getSession(player);
		//TODO Should not be possible to get null
		if (kingdomPlayer == null)
			return;
		Chunk chunkFrom = event.getFromChunk();
		Chunk chunkTo = event.getToChunk();
		Land landTo = landManager.getOrLoadLand(chunkTo);
		if (chunkFrom != null) {
			Land landFrom = landManager.getOrLoadLand(chunkFrom);
			UUID fromOwner = landFrom.getOwnerUUID();
			UUID toOwner = landTo.getOwnerUUID();
			if (fromOwner == null && toOwner == null)
				return;
			else if (fromOwner.equals(toOwner))
				return;
		}
		//Check if land is unoccupied. 
		if (landTo.getOwnerUUID() == null) {
			if (configuration.getBoolean("kingdoms.land-enter-unoccupied.actionbar", true))
				new MessageBuilder(false, "map.unoccupied-land.actionbar")
						.setPlaceholderObject(LocationUtils.chunkToString(chunkFrom)) // %string% can be used.
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.sendActionbar(player);
			if (configuration.getBoolean("kingdoms.message-spam", true)) {
				long time = IntervalUtils.getInterval(configuration.getString("kingdoms.message-spam-cooldown", "5 seconds"));
				if ((System.currentTimeMillis() - spam) * 1000 <= time)
					return;
			}
			if (configuration.getBoolean("kingdoms.land-enter-unoccupied.message", false))
				new MessageBuilder(false, "map.unoccupied-land.message")
						.setPlaceholderObject(LocationUtils.chunkToString(chunkFrom))
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.send(player);
			if (configuration.getBoolean("kingdoms.land-enter-unoccupied.title.enabled", true))
				new MessageBuilder(false, "kingdoms.land-enter-unoccupied.title")
						.setPlaceholderObject(LocationUtils.chunkToString(chunkFrom))
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.fromConfiguration(configuration)
						.sendTitle(player);
			spam = System.currentTimeMillis();
			return;
		}
		//Check status of the land.
		Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(landTo.getOwnerUUID());
		ChatColor color = ChatColor.WHITE;
		// Player has no Kingdom, so they have no alliances nor enemies.
		if (kingdomPlayer.getKingdom() == null)
			color = ChatColor.WHITE;
		else if (kingdomPlayer.getKingdom().isAllianceWith(kingdom))
			color = ChatColor.YELLOW;
		else if (kingdomPlayer.getKingdom().isEnemyWith(kingdom))
			color = ChatColor.RED;
		else if (kingdomPlayer.getKingdom().equals(kingdom))
			color = ChatColor.GREEN;
		// Player walked into a Kingdom that is neutral to them.
		if (kingdom.isNeutral() || color == ChatColor.WHITE) {
			if (configuration.getBoolean("kingdoms.land-enter-neutral.actionbar", true))
				new MessageBuilder(false, "map.neutral-land.actionbar")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.sendActionbar(player);
			if (configuration.getBoolean("kingdoms.message-spam", true)) {
				long time = IntervalUtils.getInterval(configuration.getString("kingdoms.message-spam-cooldown", "5 seconds"));
				if ((System.currentTimeMillis() - spam) * 1000 <= time)
					return;
			}
			if (configuration.getBoolean("kingdoms.land-enter-neutral.message", false))
				new MessageBuilder(false, "map.neutral-land.message")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.send(player);
			if (configuration.getBoolean("kingdoms.land-enter-neutral.title.enabled", true))
				new MessageBuilder(false, "kingdoms.land-enter-neutral.title")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.fromConfiguration(configuration)
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.sendTitle(player);
			spam = System.currentTimeMillis();
			return;
		}
		if (color == ChatColor.YELLOW) {
			if (configuration.getBoolean("kingdoms.land-enter-allience.actionbar", true))
				new MessageBuilder(false, "map.allience-land.actionbar")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.sendActionbar(player);
			if (configuration.getBoolean("kingdoms.message-spam", true)) {
				long time = IntervalUtils.getInterval(configuration.getString("kingdoms.message-spam-cooldown", "5 seconds"));
				if ((System.currentTimeMillis() - spam) * 1000 <= time)
					return;
			}
			if (configuration.getBoolean("kingdoms.land-enter-allience.message", false))
				new MessageBuilder(false, "map.allience-land.message")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.setPlaceholderObject(kingdom)
						.send(player);
			if (configuration.getBoolean("kingdoms.land-enter-allience.title.enabled", true))
				new MessageBuilder(false, "kingdoms.land-enter-allience.title")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.fromConfiguration(configuration)
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.sendTitle(player);
			spam = System.currentTimeMillis();
			return;
		}
		if (configuration.getBoolean("kingdoms.land-enter-enemy.actionbar", true))
			new MessageBuilder(false, "map.enemy-land.actionbar")
					.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
					.replace("%player%", player.getName())
					.replace("%world%", world.getName())
					.setPlaceholderObject(kingdom)
					.replace("%color%", color)
					.sendActionbar(player);
		if (configuration.getBoolean("kingdoms.message-spam", true)) {
			long time = IntervalUtils.getInterval(configuration.getString("kingdoms.message-spam-cooldown", "5 seconds"));
			if ((System.currentTimeMillis() - spam) * 1000 <= time)
				return;
		}
		if (configuration.getBoolean("kingdoms.land-enter-enemy.message", false))
			new MessageBuilder(false, "map.enemy-land.message")
					.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
					.replace("%player%", player.getName())
					.replace("%world%", world.getName())
					.setPlaceholderObject(kingdom)
					.send(player);
		if (configuration.getBoolean("kingdoms.land-enter-enemy.title.enabled", true))
			new MessageBuilder(false, "kingdoms.land-enter-enemy.title")
					.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
					.replace("%player%", player.getName())
					.replace("%world%", world.getName())
					.fromConfiguration(configuration)
					.setPlaceholderObject(kingdom)
					.replace("%color%", color)
					.sendTitle(player);
		spam = System.currentTimeMillis();
		return;
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

	@Override
	public void onDisable() {}

}
