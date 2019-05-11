package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.events.NexusMoveEvent;
import com.songoda.kingdoms.events.NexusPlaceEvent;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.events.StructureBreakEvent;
import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.managers.TurretManager.TurretBlock;
import com.songoda.kingdoms.manager.managers.external.WorldGuardManager;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class NexusManager extends Manager {

	private final Set<KingdomPlayer> placing = new HashSet<>();
	private final StructureType type = StructureType.NEXUS;
	private Optional<WorldGuardManager> worldGuardManager;
	private InventoryManager inventoryManager;
	private TurretManager turretManager;
	private PlayerManager playerManager;
	private LandManager landManager;

	public NexusManager() {
		super("nexus", true);
	}

	@Override
	public void initalize() {
		this.worldGuardManager = instance.getExternalManager("worldguard", WorldGuardManager.class);
		this.inventoryManager = instance.getManager("inventory", InventoryManager.class);
		this.turretManager = instance.getManager("turret", TurretManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}

	public void startNexusSet(KingdomPlayer kingdomPlayer) {
		placing.add(kingdomPlayer);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent event) {
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		placing.remove(kingdomPlayer);
	}

	@EventHandler
	public void onChunkChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (!placing.contains(kingdomPlayer))
			return;
		new MessageBuilder("kingdoms.nexus-setting-cancelled")
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		new MessageBuilder("kingdoms.nexus-setting-cancelled-moved")
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		placing.remove(kingdomPlayer);
	}

	// Part of this is handled in the StructureManager
	public void onNexusPlace(PlayerInteractEvent event, Block block, Player player, Kingdom kingdom, Land land) {
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (!placing.contains(kingdomPlayer))
			return;
		if (worldGuardManager.isPresent())
			if (!worldGuardManager.get().canBuild(player, block.getLocation())) {
				new MessageBuilder("claiming.worldguard")
						.setKingdom(kingdom)
						.send(player);
				return;
			}
		//check if replacing with turret
		if (turretManager.getTurretBlock(block) != TurretBlock.NOT_TURRET) {
			new MessageBuilder("kingdoms.nexus-cannot-replace")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		placing.remove(kingdomPlayer);
		NexusPlaceEvent placeEvent = new NexusPlaceEvent(kingdomPlayer, kingdom, block.getLocation(), land);
		Bukkit.getPluginManager().callEvent(placeEvent);
		if (placeEvent.isCancelled())
			return;
		Location nexus = kingdom.getNexusLocation();
		if (nexus != null) {
			Land previousNexus = landManager.getLand(nexus.getChunk());
			NexusMoveEvent move = new NexusMoveEvent(kingdomPlayer, kingdom, block.getLocation(), nexus, land, previousNexus);
			Bukkit.getPluginManager().callEvent(move);
			if (move.isCancelled()) {
				player.sendMessage(Formatting.color(move.getMessage()));
				return;
			}
			previousNexus.setStructure(null);
			nexus.getBlock().setType(Material.AIR);
		}
		Structure structure = land.getStructure();
		if (structure != null) {
			Block structureBlock = structure.getLocation().getBlock();
			StructureBreakEvent breakEvent = new StructureBreakEvent(land, structure, kingdom);
			Bukkit.getPluginManager().callEvent(breakEvent);
			// If there is an issue with other old structure blocks not being replaced when placing a nexus
			// Check if they're using the API
			if (breakEvent.isCancelled())
				return;
			structureBlock.setType(Material.AIR);
			for (StructureType type : StructureType.values()) {
				if (structureBlock.hasMetadata(type.getMetaData())) {
					structureBlock.removeMetadata(type.getMetaData(), instance);
					structureBlock.getWorld().dropItemNaturally(block.getLocation(), type.build());
					land.setStructure(null);
					kingdom.removeWarpAt(land);
				}
			}
		}
		placeNexus(land, block, kingdom, kingdomPlayer);
	}

	public void placeNexus(Land land, Block block, Kingdom kingdom, KingdomPlayer player) {
		land.setStructure(new Structure(kingdom.getName(), block.getLocation(), type));
		block.setMetadata(type.getMetaData(), new FixedMetadataValue(instance, kingdom.getName()));
		block.setType(type.getBlockMaterial());
		kingdom.setNexusLocation(block.getLocation());
		new MessageBuilder("structures.nexus-placed")
				.setKingdom(kingdom)
				.send(player);
	}

	@EventHandler
	public void onCancelPlaceBlock(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK)
			return;
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			placing.remove(kingdomPlayer);
			return;
		}
		if (!placing.contains(kingdomPlayer))
			return;
		placing.remove(kingdomPlayer);
		new MessageBuilder("kingdoms.nexus-setting-cancelled")
				.setKingdom(kingdom)
				.send(player);
	}

	@EventHandler
	public void onNexusBlockBreakUnNatural(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Block block = event.getBlock();
		if (block.getType() != type.getBlockMaterial())
			return;
		if (!block.hasMetadata(type.getMetaData()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onNexusBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getType() != type.getBlockMaterial())
			return;
		if (!block.hasMetadata(type.getMetaData()))
			return;
		Player player = event.getPlayer();
		KingdomPlayer invader = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = invader.getKingdom();
		if (kingdom == null)
			return;
		Land land = landManager.getLand(block.getLocation().getChunk());
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (!optional.isPresent()) {
			event.setCancelled(true);
			breakNexus(land);
			return;
		}
		OfflineKingdom landKingdom = optional.get();
		if (kingdom.isAllianceWith(landKingdom)) {
			new MessageBuilder("kingdoms.cannot-break-alliance-nexus")
					.replace("%playerkingdom", kingdom)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		if (kingdom.equals(landKingdom)) {
			new MessageBuilder("kingdoms.cannot-break-own-nexus")
					.setKingdom(kingdom)
					.send(player);
			return;
		}
		long points = landKingdom.getResourcePoints();
		long cost = configuration.getInt("invading.nexus-break-cost", 20);
		if (points > cost) {
			long percent = ((points - cost) / points) * 100;
			landKingdom.subtractResourcePoints(cost);
			if (configuration.getBoolean("invading.nexus-break-adds", false)) {
				kingdom.addResourcePoints(cost);
				new MessageBuilder("invading.break-nexus-add")
						.replace("%playerkingdom%", kingdom.getName())
						.replace("%amount%", cost)
						.setKingdom(landKingdom)
						.send(player);
			}
			new MessageBuilder("invading.break-nexus")
					.replace("%playerkingdom%", kingdom.getName())
					.replace("%progress%", percent)
					.replace("%amount%", cost)
					.setKingdom(landKingdom)
					.send(player);
		} else {
			landKingdom.setResourcePoints(0);
			if (points > 0 && configuration.getBoolean("invading.nexus-break-adds", false)) {
				kingdom.addResourcePoints(cost);
				new MessageBuilder("invading.break-nexus-add")
						.replace("%playerkingdom%", kingdom.getName())
						.replace("%amount%", cost)
						.setKingdom(landKingdom)
						.send(player);
			}
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onNexusClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block block = event.getClickedBlock();
		if (block.getType() != StructureType.NEXUS.getBlockMaterial())
			return;
		if (!block.hasMetadata(StructureType.NEXUS.getMetaData()))
			return;
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		event.setCancelled(true);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("kingdoms.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		Land land = landManager.getLand(block.getChunk());
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (!optional.isPresent()) {
			block.setType(Material.AIR);
			return;
		}
		OfflineKingdom landKingdom = optional.get();
		NexusInventory inventory = inventoryManager.getInventory(NexusInventory.class);
		if (landKingdom.isAllianceWith(kingdom)) {
			inventory.openDonateInventory(landKingdom, kingdomPlayer);
		} else if (landKingdom.equals(kingdom)) {
			inventory.build(kingdomPlayer);
		} else {
			new MessageBuilder("kingdoms.cannot-use-others-nexus")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
		}
	}

	public void breakNexus(Land land) {
		if (land.getStructure() == null)
			return;
		Block block = land.getStructure().getLocation().getBlock();
		Optional<OfflineKingdom> kingdom = land.getKingdomOwner();
		if (kingdom.isPresent())
			kingdom.get().setNexusLocation(null);
		land.setStructure(null);
		block.setType(Material.AIR);
		Location location = block.getLocation();
		World world = location.getWorld();
		for (int i = 0; i < 4; i++) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				public void run() {
					for (int i = 0; i < 4; i++) {
						world.strikeLightningEffect(location);
					}
				}
			}, 10 * i);
		}
	}

	@Override
	public void onDisable() {
		placing.clear();
	}

}
