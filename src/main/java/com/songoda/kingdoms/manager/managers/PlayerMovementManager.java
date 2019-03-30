package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.events.PlayerUnwaterlogEvent;
import com.songoda.kingdoms.events.PlayerWaterlogEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.manager.managers.external.WorldGuardManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.turrets.Turret;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class PlayerMovementManager extends Manager {

	private long spam = System.currentTimeMillis();
	private WorldGuardManager worldGuardManager;
	private CitizensManager citizensManager;
	private PlayerManager playerManager;
	private WorldManager worldManager;
	private LandManager landManager;
	
	public PlayerMovementManager() {
		super("player-movement", true);
	}

	@Override
	public void initalize() {
		this.worldGuardManager = instance.getManager("worldguard", WorldGuardManager.class);
		this.citizensManager = instance.getManager("citizens", CitizensManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (citizensManager.isCitizen(player))
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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onWaterlog(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			ItemStack bucket;
			try {
				bucket = player.getItemInHand();
			} catch (Exception e) {
				bucket = player.getInventory().getItemInMainHand();
			}
			if (bucket != null) {
				Material type = bucket.getType();
				if (type == Material.WATER_BUCKET) {
					Block block = event.getClickedBlock();
					BlockData data = block.getBlockData();
					if (data instanceof Waterlogged) {
						Waterlogged waterlogged = (Waterlogged) data;
						if (waterlogged.isWaterlogged()) {
							PlayerUnwaterlogEvent waterlog = new PlayerUnwaterlogEvent(player, block, event.getBlockFace(), type, new ItemStack(Material.WATER_BUCKET));
							Bukkit.getPluginManager().callEvent(waterlog);
							if (waterlog.isCancelled())
								event.setCancelled(true);
						} else {
							PlayerWaterlogEvent waterlog = new PlayerWaterlogEvent(player, block, event.getBlockFace(), type, new ItemStack(Material.BUCKET));
							Bukkit.getPluginManager().callEvent(waterlog);
							if (waterlog.isCancelled())
								event.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (citizensManager.isCitizen(player))
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
		if (citizensManager.isCitizen(player))
			return;
		Location from = event.getFrom();
		Location to = event.getTo();
		Chunk chunkFrom = from.getChunk();
		Chunk chunkTo = to.getChunk();
		int centerX = chunkTo.getX(), centerZ = chunkTo.getZ();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				Chunk next = world.getChunkAt(centerX + x, centerZ + z);
				Land land = landManager.getLand(next);
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
		if (chunkTo != chunkFrom) {
			if (configuration.getBoolean("invading.invading-deny-chunk-change", true)) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				if (kingdomPlayer.isInvading()) {
					// Direction from to to.
					Vector vector = from.toVector().subtract(to.toVector()).normalize().multiply(2);
					// This used to be teleport to player.getLocation().add(vector)
					// Changed to velocity because I think pushing them back with an animation looks better.
					player.setVelocity(vector);
					player.setFallDistance(0);
					event.setCancelled(true);
					new MessageBuilder("invading.invading-deny-chunk-change")
							.replace("%chunkFrom%", LocationUtils.chunkToString(chunkFrom))
							.replace("%chunkTo%", LocationUtils.chunkToString(chunkTo))
							.setKingdom(kingdomPlayer.getKingdom())
							.setPlaceholderObject(kingdomPlayer)
							.send(kingdomPlayer);
					return;
				}
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
		if (worldGuardManager.isInRegion(player.getLocation()))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Chunk chunkFrom = event.getFromChunk();
		Chunk chunkTo = event.getToChunk();
		Land landTo = landManager.getLand(chunkTo);
		if (chunkFrom != null) {
			Land landFrom = landManager.getLand(chunkFrom);
			OfflineKingdom fromOwner = landFrom.getKingdomOwner();
			OfflineKingdom toOwner = landTo.getKingdomOwner();
			if (fromOwner == null && toOwner == null)
				return;
			else if (fromOwner.getUniqueId() == toOwner.getUniqueId())
				return;
		}
		OfflineKingdom kingdom = landTo.getKingdomOwner();
		if (kingdom == null) {
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
