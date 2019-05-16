package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.DeprecationUtils;

public class VisualizerManager extends Manager {

	private final Map<UUID, Set<Location>> changes = new HashMap<>();
	private final Map<UUID, Long> visualizing = new HashMap<>();
	private final Map<UUID, Long> times = new HashMap<>();
	private PlayerManager playerManager;
	private WorldManager worldManager;
	private LandManager landManager;

	public VisualizerManager() {
		super("visualizer", true);
	}

	@Override
	public void initalize() {
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}

	public void removeVisualizer(UUID uuid) {
		visualizing.remove(uuid);
		times.remove(uuid);
	}

	private void addVisualizer(UUID uuid, long stay) {
		visualizing.put(uuid, System.currentTimeMillis());
		times.put(uuid, stay);
	}

	private boolean isPresent(UUID uuid) {
		if (!visualizing.containsKey(uuid)) {
			visualizing.remove(uuid);
			times.remove(uuid);
			return false;
		}
		long time = times.getOrDefault(uuid, 0L);
		if (System.currentTimeMillis() - visualizing.get(uuid) > time) {
			Iterator<Location> iterator = getBlockChanges(uuid).iterator();
			while (iterator.hasNext()) {
				Location location = iterator.next();
				location.getBlock().getState().update();
				iterator.remove();
			}
			visualizing.remove(uuid);
			times.remove(uuid);
			return false;
		}
		return true;
	}

	public Set<Location> getBlockChanges(UUID uuid) {
		return Optional.ofNullable(changes.get(uuid)).orElse(Sets.newHashSet());
	}

	private void addBlockChange(UUID uuid, Location location) {
		Set<Location> locations = getBlockChanges(uuid);
		locations.add(location);
		changes.put(uuid, locations);
	}

	public void visualizeLand(KingdomPlayer kingdomPlayer, Chunk chunk) {
		visualizeLand(kingdomPlayer, chunk, 20 * 1000); // 20 seconds
	}

	public void visualizeLand(KingdomPlayer kingdomPlayer, Chunk chunk, long stay) {
		Player player = kingdomPlayer.getPlayer();
		UUID uuid = player.getUniqueId();
		addVisualizer(uuid, stay);
		Land land = landManager.getLand(chunk);
		if (!worldManager.acceptsWorld(land.getWorld()))
			return;
		Optional<OfflineKingdom> landKingdom = land.getKingdomOwner();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Material material;
		if (kingdom == null || !landKingdom.isPresent()) {
			material = Material.QUARTZ_BLOCK;
		} else if (kingdom.equals(landKingdom.get())) {
			material = Material.EMERALD_BLOCK;
		} else if (kingdom.isAllianceWith(landKingdom.get())) {
			material = Material.GOLD_BLOCK;
		} else if (kingdom.isEnemyWith(landKingdom.get())) {
			material = Material.REDSTONE_BLOCK;
		} else {
			material = Material.QUARTZ_BLOCK;
		}
		BukkitScheduler scheduler = instance.getServer().getScheduler();
		scheduler.runTaskAsynchronously(instance, () -> {
			sendBlockChange(player, chunk, 0, 0, Material.SEA_LANTERN);
			sendBlockChange(player, chunk, 1, 0, material);
			sendBlockChange(player, chunk, 0, 1, material);

			sendBlockChange(player, chunk, 0, 15, Material.SEA_LANTERN);
			sendBlockChange(player, chunk, 0, 14, material);
			sendBlockChange(player, chunk, 1, 15, material);

			sendBlockChange(player, chunk, 15, 15, Material.SEA_LANTERN);
			sendBlockChange(player, chunk, 15, 14, material);
			sendBlockChange(player, chunk, 14, 15, material);

			sendBlockChange(player, chunk, 15, 0, Material.SEA_LANTERN);
			sendBlockChange(player, chunk, 14, 0, material);
			sendBlockChange(player, chunk, 15, 1, material);
		});
		long delay = (times.getOrDefault(player.getUniqueId(), 0L) / 1000) * 20;
		scheduler.runTaskLater(instance, () -> {
			Iterator<Location> iterator = getBlockChanges(uuid).iterator();
			while (iterator.hasNext()) {
				Location location = iterator.next();
				location.getBlock().getState().update();
				iterator.remove();
			}
			visualizing.remove(uuid);
			times.remove(uuid);
		}, delay);
	}

	private void sendBlockChange(Player player, Chunk chunk, int x, int z, Material material) {
		World world = chunk.getWorld();
		Location location = chunk.getBlock(x, 0, z).getLocation();
		location.setY(world.getHighestBlockYAt(location) - 1);
		DeprecationUtils.sendBlockChange(player, location, material);
		addBlockChange(player.getUniqueId(), location);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onClickBlock(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (!isPresent(uuid))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Block block = event.getClickedBlock();
		Chunk chunk = block.getChunk();
		Iterator<Location> iterator = getBlockChanges(uuid).iterator();
		while (iterator.hasNext()) {
			Location location = iterator.next();
			location.getBlock().getState().update();
			iterator.remove();
		}
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			@Override
			public void run() {
				Land land = landManager.getLand(chunk);
				if (land.getKingdomOwner() == null)
					return;
				visualizeLand(kingdomPlayer, chunk);
			}
		}, 5);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (!isPresent(uuid))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Block block = event.getBlock();
		Chunk chunk = block.getChunk();
		Iterator<Location> iterator = getBlockChanges(uuid).iterator();
		while (iterator.hasNext()) {
			Location location = iterator.next();
			location.getBlock().getState().update();
			iterator.remove();
		}
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			@Override
			public void run() {
				Land land = landManager.getLand(chunk);
				if (land.getKingdomOwner() == null)
					return;
				visualizeLand(kingdomPlayer, chunk);
			}
		}, 5);

	}

	@EventHandler
	public void onChunkChangeEvent(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (!isPresent(uuid))
			return;
		Iterator<Location> iterator = getBlockChanges(uuid).iterator();
		while (iterator.hasNext()) {
			Location location = iterator.next();
			location.getBlock().getState().update();
			iterator.remove();
		}
	}

	@Override
	public void onDisable() {
		changes.clear();
	}

}
