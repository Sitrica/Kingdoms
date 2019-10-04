package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;

import com.songoda.kingdoms.events.NexusMoveEvent;
import com.songoda.kingdoms.events.NexusPlaceEvent;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.events.StructureBreakEvent;
import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.managers.TurretManager.TurretBlock;
import com.songoda.kingdoms.manager.managers.external.WorldGuardManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class NexusManager extends Manager {

	private final StructureType type = StructureType.NEXUS;
	private Optional<WorldGuardManager> worldGuardManager;
	private final Set<UUID> placing = new HashSet<>();

	public NexusManager() {
		super(true);
	}

	@Override
	public void initalize() {
		this.worldGuardManager = instance.getExternalManager("worldguard", WorldGuardManager.class);
		Bukkit.getScheduler().runTaskTimer(instance, () -> {
			placing.parallelStream()
					.map(uuid -> instance.getManager(PlayerManager.class).getKingdomPlayer(uuid))
					.filter(optional -> optional.isPresent())
					.map(optional -> optional.get())
					.filter(kingdomPlayer -> kingdomPlayer.hasKingdom())
					.forEach(kingdomPlayer -> {
							Player player = kingdomPlayer.getPlayer();
							if (instance.getConfig().getBoolean("kingdoms.nexus-move-actionbar", true))
								new MessageBuilder(false, "commands.nexus.actionbar")
										.setPlaceholderObject(kingdomPlayer)
										.sendActionbar(player);
							if (!instance.getConfig().getBoolean("kingdoms.nexus-move-ghost-block", true))
								return;
							Block block = player.getTargetBlockExact(5);
							if (block == null)
								return;
							Block ghostBlock = block.getRelative(BlockFace.UP);
							Location location = ghostBlock.getLocation();
							if (location.getBlockY() > player.getLocation().getY() + 1)
								return;
							Land land = instance.getManager(LandManager.class).getLandAt(block.getLocation());
							BlockData error = Bukkit.createBlockData(Material.REDSTONE_BLOCK);
							if (!land.hasOwner())
								Bukkit.getScheduler().runTaskAsynchronously(instance, () -> player.sendBlockChange(location, error));
							else if (!land.getKingdomName().equalsIgnoreCase(kingdomPlayer.getKingdom().getName()))
								Bukkit.getScheduler().runTaskAsynchronously(instance, () -> player.sendBlockChange(location, error));
							else
								Bukkit.getScheduler().runTaskAsynchronously(instance, () -> player.sendBlockChange(location, Bukkit.createBlockData(StructureType.NEXUS.getBlockMaterial())));
							Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> player.sendBlockChange(location, ghostBlock.getBlockData()), 5);
						});
		}, 0, 10);
	}

	public void startNexusSet(UUID uuid) {
		placing.add(uuid);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent event) {
		placing.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onChunkChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		if (!placing.contains(uuid))
			return;
		new MessageBuilder("kingdoms.nexus-setting-cancelled")
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		new MessageBuilder("kingdoms.nexus-setting-cancelled-moved")
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		placing.remove(uuid);
	}

	// Used for moving
	public void onNexusPlace(PlayerInteractEvent event, Block block, Player player, Kingdom kingdom, Land land) {
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		UUID uuid = player.getUniqueId();
		if (!placing.contains(uuid) || kingdom == null)
			return;
		if (worldGuardManager.isPresent())
			if (!worldGuardManager.get().canBuild(player, block.getLocation())) {
				new MessageBuilder("claiming.worldguard")
						.setKingdom(kingdom)
						.send(player);
				return;
			}
		//check if replacing with turret
		if (instance.getManager(TurretManager.class).getTurretBlock(block) != TurretBlock.NOT_TURRET) {
			new MessageBuilder("kingdoms.nexus-cannot-replace")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		placing.remove(uuid);
		NexusPlaceEvent placeEvent = new NexusPlaceEvent(kingdomPlayer, kingdom, block.getLocation(), land);
		Bukkit.getPluginManager().callEvent(placeEvent);
		if (placeEvent.isCancelled())
			return;
		Location nexus = kingdom.getNexusLocation();
		if (nexus != null) {
			LandManager landManager = instance.getManager(LandManager.class);
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
		UUID uuid = player.getUniqueId();
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			placing.remove(uuid);
			return;
		}
		if (!placing.contains(uuid))
			return;
		placing.remove(uuid);
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
		KingdomPlayer invader = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = invader.getKingdom();
		if (kingdom == null)
			return;
		Land land = instance.getManager(LandManager.class).getLand(block.getLocation().getChunk());
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
		Player player = event.getPlayer();
		if (Utils.methodExists(PlayerInteractEvent.class, "getHand") && event.getHand() != EquipmentSlot.HAND)
			return;
		Block block = event.getClickedBlock();
		// Was a client side block.
		if (block == null)
			return;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		if (block.getType() != StructureType.NEXUS.getBlockMaterial()) {
			// Handle nexus moving.
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && placing.contains(player.getUniqueId())) {
				if (!block.hasMetadata(StructureType.NEXUS.getMetaData())) {
					Land land = instance.getManager(LandManager.class).getLandAt(block.getLocation());
					Kingdom kingdom = kingdomPlayer.getKingdom();
					if (kingdom != null && land.hasOwner() && land.getKingdomName().equalsIgnoreCase(kingdom.getName())) {
						Block above = block.getRelative(BlockFace.UP);
						if (above.getType() == Material.AIR)
							onNexusPlace(event, block.getRelative(BlockFace.UP), player, kingdom, land);
					}
				}
			}
			return;
		}
		if (!block.hasMetadata(StructureType.NEXUS.getMetaData()))
			return;
		event.setCancelled(true);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("kingdoms.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		Land land = instance.getManager(LandManager.class).getLand(block.getChunk());
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (!optional.isPresent()) {
			breakNexus(land);
			return;
		}
		OfflineKingdom landKingdom = optional.get();
		NexusInventory inventory = instance.getManager(InventoryManager.class).getInventory(NexusInventory.class);
		if (landKingdom.isAllianceWith(kingdom)) {
			inventory.openDonateInventory(landKingdom, kingdomPlayer);
		} else if (!landKingdom.equals(kingdom)) {
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
