package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.events.PlayerUnwaterlogEvent;
import com.songoda.kingdoms.events.PlayerWaterlogEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.manager.managers.external.WorldGuardManager;
import com.songoda.kingdoms.objects.Relation;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class PlayerMovementManager extends Manager {

	private Optional<WorldGuardManager> worldGuardManager;
	private Optional<CitizensManager> citizensManager;
	private Map<UUID, Long> spam = new HashMap<>();
	private PlayerManager playerManager;
	private LandManager landManager;

	public PlayerMovementManager() {
		super(true);
	}

	@Override
	public void initalize() {
		this.worldGuardManager = instance.getExternalManager("worldguard", WorldGuardManager.class);
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.playerManager = instance.getManager(PlayerManager.class);
		this.landManager = instance.getManager(LandManager.class);
		final long time = IntervalUtils.getInterval(configuration.getString("kingdoms.message-spam-cooldown", "5 seconds"));
		instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, () -> {
			spam.entrySet().removeIf(entry -> (System.currentTimeMillis() - entry.getValue()) * 1000 > time);
		}, 0, 20);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
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
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Player player = event.getPlayer();
		ItemStack bucket;
		try {
			bucket = player.getItemInHand();
		} catch (Exception e) {
			bucket = player.getInventory().getItemInMainHand();
		}
		if (bucket == null)
			return;
		Material type = bucket.getType();
		if (type != Material.WATER_BUCKET)
			return;
		Block block = event.getClickedBlock();
		BlockData data = block.getBlockData();
		if (!(data instanceof Waterlogged))
			return;
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

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
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
	public void onChunkChange(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		Chunk chunkFrom = from.getChunk();
		Chunk chunkTo = to.getChunk();
		if (chunkTo.equals(chunkFrom))
			return;
		Player player = event.getPlayer();
		PlayerChangeChunkEvent change = new PlayerChangeChunkEvent(player, chunkFrom, chunkTo);
		Bukkit.getServer().getPluginManager().callEvent(change);
		if (change.isCancelled()) {
			Vector vector = from.toVector().subtract(to.toVector()).normalize().multiply(2);
			// This used to be teleport to player.getLocation().add(vector)
			// Changed to velocity because I think pushing them back with an animation looks better.
			player.setVelocity(vector);
			player.setFallDistance(0);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onChunkChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		World world = player.getWorld();
		if (!instance.getManager(WorldManager.class).acceptsWorld(world))
			return;
		//TODO add configuration option to ignore or not regions.
		if (worldGuardManager.isPresent())
			if (worldGuardManager.get().isInRegion(player.getLocation()))
				return;
		Chunk chunkFrom = event.getFromChunk();
		Chunk chunkTo = event.getToChunk();
		Land landTo = landManager.getLand(chunkTo);
		if (chunkFrom != null) {
			Land landFrom = landManager.getLand(chunkFrom);
			Optional<OfflineKingdom> fromOwner = landFrom.getKingdomOwner();
			Optional<OfflineKingdom> toOwner = landTo.getKingdomOwner();
			if (!fromOwner.isPresent() && !toOwner.isPresent())
				return;
			else if (fromOwner.isPresent() && toOwner.isPresent())
				if (toOwner.get().equals(fromOwner.get()))
					return;
		}
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Optional<OfflineKingdom> optional = landTo.getKingdomOwner();
		if (!optional.isPresent()) {
			if (configuration.getBoolean("kingdoms.land-enter-unoccupied.actionbar", true))
				new MessageBuilder(false, "chunk-changing.unoccupied-land.actionbar")
						.setPlaceholderObject(LocationUtils.chunkToString(chunkFrom)) // %string% can be used.
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.sendActionbar(player);
			if (configuration.getBoolean("kingdoms.message-spam", true)) {
				long time = IntervalUtils.getInterval(configuration.getString("kingdoms.message-spam-cooldown", "5 seconds"));
				if ((System.currentTimeMillis() - spam.getOrDefault(player.getUniqueId(), 0L)) * 1000 <= time)
					return;
			}
			if (configuration.getBoolean("kingdoms.land-enter-unoccupied.message", false))
				new MessageBuilder(false, "chunk-changing.unoccupied-land.message")
						.setPlaceholderObject(LocationUtils.chunkToString(chunkFrom))
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.send(player);
			new MessageBuilder(false, "kingdoms.land-enter-unoccupied.title")
					.setPlaceholderObject(LocationUtils.chunkToString(chunkFrom))
					.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
					.replace("%player%", player.getName())
					.replace("%world%", world.getName())
					.fromConfiguration(configuration)
					.sendTitle(player);
			spam.put(player.getUniqueId(), System.currentTimeMillis());
			return;
		}
		if (configuration.getBoolean("kingdoms.message-spam", true)) {
			long time = IntervalUtils.getInterval(configuration.getString("kingdoms.message-spam-cooldown", "5 seconds"));
			if ((System.currentTimeMillis() - spam.getOrDefault(player.getUniqueId(), 0L)) * 1000 <= time)
				return;
		}
		OfflineKingdom kingdom = optional.get();
		Relation relation = Relation.getRelation(kingdom, kingdomPlayer.getKingdom());
		ChatColor color = relation.getColor();
		switch (relation) {
			case ALLIANCE:
				if (configuration.getBoolean("kingdoms.land-enter-alliance.actionbar", true))
					new MessageBuilder(false, "chunk-changing.alliance-land.actionbar")
							.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
							.replace("%player%", player.getName())
							.replace("%world%", world.getName())
							.setPlaceholderObject(kingdom)
							.replace("%color%", color)
							.sendActionbar(player);
				if (configuration.getBoolean("kingdoms.land-enter-alliance.message", false))
					new MessageBuilder(false, "chunk-changing.alliance-land.message")
							.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
							.replace("%player%", player.getName())
							.replace("%world%", world.getName())
							.setPlaceholderObject(kingdom)
							.send(player);
				new MessageBuilder(false, "kingdoms.land-enter-alliance.title")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.fromConfiguration(configuration)
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.sendTitle(player);
				break;
			case ENEMY:
				if (configuration.getBoolean("kingdoms.land-enter-enemy.actionbar", true))
					new MessageBuilder(false, "chunk-changing.enemy-land.actionbar")
							.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
							.replace("%player%", player.getName())
							.replace("%world%", world.getName())
							.setPlaceholderObject(kingdom)
							.replace("%color%", color)
							.sendActionbar(player);
				if (configuration.getBoolean("kingdoms.land-enter-enemy.message", false))
					new MessageBuilder(false, "chunk-changing.enemy-land.message")
							.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
							.replace("%player%", player.getName())
							.replace("%world%", world.getName())
							.setPlaceholderObject(kingdom)
							.send(player);
				new MessageBuilder(false, "kingdoms.land-enter-enemy.title")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.fromConfiguration(configuration)
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.sendTitle(player);
				break;
			case NEUTRAL:
				if (configuration.getBoolean("kingdoms.land-enter-neutral.actionbar", true))
					new MessageBuilder(false, "chunk-changing.neutral-land.actionbar")
							.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
							.replace("%player%", player.getName())
							.replace("%world%", world.getName())
							.setPlaceholderObject(kingdom)
							.replace("%color%", color)
							.sendActionbar(player);
				if (configuration.getBoolean("kingdoms.land-enter-neutral.message", false))
					new MessageBuilder(false, "chunk-changing.neutral-land.message")
							.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
							.replace("%player%", player.getName())
							.replace("%world%", world.getName())
							.setPlaceholderObject(kingdom)
							.replace("%color%", color)
							.send(player);
				new MessageBuilder(false, "kingdoms.land-enter-neutral.title")
						.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
						.replace("%player%", player.getName())
						.replace("%world%", world.getName())
						.fromConfiguration(configuration)
						.setPlaceholderObject(kingdom)
						.replace("%color%", color)
						.sendTitle(player);
				break;
			case OWN:
				return;
		}
		spam.put(player.getUniqueId(), System.currentTimeMillis());
		return;
	}

	@Override
	public void onDisable() {}

}
