package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.Sets;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.DeprecationUtils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class VisualizerManager extends Manager {
	
	private final Map<KingdomPlayer, Set<Location>> changes = new HashMap<>();
	private final PlayerManager playerManager;
	private final WorldManager worldManager;
	private final LandManager landManager;
	
	protected VisualizerManager() {
		super("visualizer", true);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}
	
	public Set<Location> getBlockChanges(KingdomPlayer kingdomPlayer) {
		return Optional.ofNullable(changes.get(kingdomPlayer)).orElse(Sets.newHashSet());
	}
	
	private void addBlockChange(KingdomPlayer kingdomPlayer, Location location) {
		Set<Location> locations = getBlockChanges(kingdomPlayer);
		locations.add(location);
		changes.put(kingdomPlayer, locations);
	}

	public void visualizeLand(KingdomPlayer kingdomPlayer, Chunk chunk) {
		Land land = landManager.getLand(chunk);
		if (!worldManager.acceptsWorld(land.getWorld()))
			return;
		OfflineKingdom landKingdom = land.getKingdomOwner();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Material material;
		if (kingdom == null || landKingdom == null) {
			material = Material.QUARTZ_BLOCK;
		} else if (kingdom.equals(landKingdom)) {
			material = Material.EMERALD_BLOCK;
		} else if (kingdom.isAllianceWith(landKingdom)) {
			material = Material.GOLD_BLOCK;
		} else if (kingdom.isEnemyWith(landKingdom)) {
			material = Material.REDSTONE_BLOCK;
		} else {
			material = Material.QUARTZ_BLOCK;
		}
		sendBlockChange(kingdomPlayer, chunk, 0, 0, Material.SEA_LANTERN);
		sendBlockChange(kingdomPlayer, chunk, 1, 0, material);
		sendBlockChange(kingdomPlayer, chunk, 0, 1, material);
		
		sendBlockChange(kingdomPlayer, chunk, 0, 15, Material.SEA_LANTERN);
		sendBlockChange(kingdomPlayer, chunk, 0, 14, material);
		sendBlockChange(kingdomPlayer, chunk, 1, 15, material);
		
		
		sendBlockChange(kingdomPlayer, chunk, 15, 15, Material.SEA_LANTERN);
		sendBlockChange(kingdomPlayer, chunk, 15, 14, material);
		sendBlockChange(kingdomPlayer, chunk, 14, 15, material);
		
		sendBlockChange(kingdomPlayer, chunk, 15, 0, Material.SEA_LANTERN);
		sendBlockChange(kingdomPlayer, chunk, 14, 0, material);
		sendBlockChange(kingdomPlayer, chunk, 15, 1, material);
	}
	
	private void sendBlockChange(KingdomPlayer kingdomPlayer, Chunk chunk, int x, int z, Material material) {
		World world = chunk.getWorld();
		Location location = chunk.getBlock(x, 0, z).getLocation();
		location.setY(world.getHighestBlockYAt(location) - 1);
		DeprecationUtils.sendBlockChange(kingdomPlayer.getPlayer(), location, material);
		addBlockChange(kingdomPlayer, location);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onClickBlock(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		Block block = event.getClickedBlock();
		Chunk chunk = block.getChunk();
		Iterator<Location> iterator = getBlockChanges(kingdomPlayer).iterator();
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
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		Block block = event.getBlock();
		Chunk chunk = block.getChunk();
		Iterator<Location> iterator = getBlockChanges(kingdomPlayer).iterator();
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
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		Iterator<Location> iterator = getBlockChanges(kingdomPlayer).iterator();
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
