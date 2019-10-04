package com.songoda.kingdoms.manager.managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

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
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import com.songoda.kingdoms.events.LandLoadEvent;
import com.songoda.kingdoms.events.PlayerWaterlogEvent;
import com.songoda.kingdoms.events.StructureBreakEvent;
import com.songoda.kingdoms.events.StructurePlaceEvent;
import com.songoda.kingdoms.inventories.structures.ArsenalInventory;
import com.songoda.kingdoms.inventories.structures.ExtractorInventory;
import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.inventories.structures.OutpostInventory;
import com.songoda.kingdoms.inventories.structures.WarppadInventory;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Extractor;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.objects.structures.WarpPad;
import com.songoda.kingdoms.objects.structures.WarpPad.Warp;
import com.songoda.kingdoms.objects.turrets.Turret;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class StructureManager extends Manager {

	private final Queue<LandInfo> loadQueue = new LinkedList<LandInfo>();
	private InventoryManager inventoryManager;
	private PlayerManager playerManager;
	private WorldManager worldManager;
	private NexusManager nexusManager;
	private LandManager landManager;
	private final BukkitTask task;

	public StructureManager() {
		super(true);
		task = Bukkit.getScheduler().runTaskTimer(instance, new Runnable() {
			@Override
			public void run() {
				if (loadQueue.isEmpty())
					return;
				LandInfo info = loadQueue.poll();
				if (info == null)
					return;
				Land land = info.get();
				Structure structure = land.getStructure();
				if (structure == null)
					return;
				Location location = structure.getLocation();
				Block block = location.getBlock();
				Optional<OfflineKingdom> optional = land.getKingdomOwner();
				if (!optional.isPresent()) {
					block.setType(Material.AIR);
					return;
				}
				OfflineKingdom kingdom = optional.get();
				block.setType(structure.getType().getBlockMaterial());
				block.setMetadata(structure.getType().getMetaData(), new FixedMetadataValue(instance, kingdom));
				if (structure.getType() == StructureType.NEXUS) {
					Location nexus = kingdom.getNexusLocation();
					if (nexus == null) {
						block.setType(Material.AIR);
						land.setStructure(null);
						return;
					}
					if (!nexus.equals(block.getLocation())) {
						block.setType(Material.AIR);
						land.setStructure(null);
						return;
					}
					block.setType(Material.BEACON);
				}
			}
		}, 0, 1);
	}

	@Override
	public void initalize() {
		this.inventoryManager = instance.getManager(InventoryManager.class);
		this.playerManager = instance.getManager(PlayerManager.class);
		this.nexusManager = instance.getManager(NexusManager.class);
		this.worldManager = instance.getManager(WorldManager.class);
		this.landManager = instance.getManager(LandManager.class);
	}

	public void breakStructureAt(Land land) {
		Structure structure = land.getStructure();
		if (structure == null)
			return;
		StructureBreakEvent event = new StructureBreakEvent(land, structure, null, null);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		//Delete turrets as they're special structures.
		Iterator<Turret> iterator = land.getTurrets().iterator();
		while (iterator.hasNext()) {
			Turret turret = iterator.next();
			instance.getManager(TurretManager.class).breakTurret(turret);
		}

		Location location = structure.getLocation();
		land.setStructure(null);
		Block block = location.getBlock();
		block.setType(Material.AIR);
		StructureType type = structure.getType();
		if (type == StructureType.NEXUS) {
			nexusManager.breakNexus(land);
			return;
		}
		block.getWorld().dropItemNaturally(location, type.build());
	}

	public boolean isInvadeable(KingdomPlayer invader, Land land) {
		if (!StructureType.POWERCELL.isEnabled())
			return true;
		Structure structure = land.getStructure();
		if (structure != null && structure.getType() == StructureType.POWERCELL)
			return true;
		Chunk chunk = land.getChunk();
		World world = chunk.getWorld();
		int originX = chunk.getX();
		int originZ = chunk.getZ();
		int radius = 1;
		for (int x =- radius; x <= radius; x++) {
			for (int z = -radius; z<=radius; z++) {
				if (x == 0 && z == 0)
					continue;
				Chunk target = world.getChunkAt(originX + x, originZ + z);
				Land landAround = landManager.getLand(target);
				Structure structureAround = landAround.getStructure();
				if (structureAround == null)
					continue;
				Optional<OfflineKingdom> optional = landAround.getKingdomOwner();
				if (!optional.isPresent())
					continue;
				OfflineKingdom owner = optional.get();
				Optional<OfflineKingdom> landOptional = land.getKingdomOwner();
				if (!landOptional.isPresent())
					continue;
				if (landOptional.get().equals(owner)) {
					if (structureAround.getType() == StructureType.POWERCELL)
						return false;
				}
			}
		}
		return true;
	}

	@EventHandler
	public void onAnvilRenameStructure(InventoryClickEvent event) {
		Inventory inventory = event.getClickedInventory();
		if (inventory == null)
			return;
		if (inventory.getType() != InventoryType.ANVIL)
			return;
		if (!(inventory instanceof AnvilInventory))
			return;
		AnvilInventory anvil = (AnvilInventory) inventory;
		Bukkit.getScheduler().runTaskLater(instance, new Runnable(){
			@Override
			public void run(){
				ItemStack renamed = anvil.getItem(2);
				if (renamed == null)
					return;
				ItemMeta meta = renamed.getItemMeta();
				if (meta == null)
					return;
				List<String> lores = meta.getLore();
				if (lores == null || lores.isEmpty())
					return;
				List<String> list = instance.getConfiguration("turrets").get().getStringList("structures.additional-lore");
				int checks = 0;
				for (String required : list) {
					String colored = Formatting.color(required);
					for (String lore : lores) {
						if (Formatting.stripColor(lore).contains(Formatting.stripColor(colored))) {
							checks++;
						}
					}
				}
				if (checks >= list.size())
					anvil.setItem(2, new ItemStack(Material.AIR));
			}
		}, 1);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onStructureBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Block block = event.getBlock();
		if (!isStructure(block))
			return;
		event.setCancelled(true);
		Land land = landManager.getLand(block.getChunk());
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null && !kingdomPlayer.hasAdminMode())
			return;
		if (!kingdomPlayer.hasAdminMode() && optional.isPresent()) {
			OfflineKingdom landKingdom = optional.get();
			if (!kingdom.equals(landKingdom)) {
				new MessageBuilder("kingdoms.not-in-land")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(landKingdom)
						.send(kingdomPlayer);
				return;
			}
			if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildStructures()) {
				new MessageBuilder("kingdoms.rank-too-low-structure-build")
						.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildStructures()), new Placeholder<Optional<Rank>>("%rank%") {
							@Override
							public String replace(Optional<Rank> rank) {
								if (rank.isPresent())
									return rank.get().getName();
								return "(Not attainable)";
							}
						})
						.setKingdom(kingdom)
						.send(kingdomPlayer);
				return;
			}
		}
		Structure structure = land.getStructure();
		if (structure == null) {
			block.setType(Material.AIR);
			return;
		}
		StructureBreakEvent breakEvent = new StructureBreakEvent(land, structure, kingdom, kingdomPlayer);
		Bukkit.getPluginManager().callEvent(event);
		if (breakEvent.isCancelled())
			return;
		block.setType(Material.AIR);
		for (StructureType type : StructureType.values()) {
			if (block.hasMetadata(type.getMetaData())) {
				block.removeMetadata(type.getMetaData(), instance);
				if (type != StructureType.NEXUS)
					block.getWorld().dropItemNaturally(block.getLocation(), type.build());
				land.setStructure(null);
				kingdom.removeWarpAt(land);
			}
		}
		kingdom.getWarps().removeIf(warp -> warp.getLand().getStructure() == null);
	}

	@EventHandler(ignoreCancelled = true)
	public void onStructurePlace(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (Utils.methodExists(PlayerInteractEvent.class, "getHand") && event.getHand() != EquipmentSlot.HAND)
			return;
		Player player = event.getPlayer();
		ItemStack findItem = DeprecationUtils.getItemInMainHand(player);
		if (findItem == null)
			return;
		ItemStack item = findItem;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return;
		String name = meta.getDisplayName();
		if (name == null)
			return;
		Block block = event.getClickedBlock().getRelative(event.getBlockFace());
		Optional<StructureType> optional = Arrays.stream(StructureType.values())
				.filter(type -> item.getType() == type.getItemMaterial())
				.filter(type -> !block.hasMetadata(type.getMetaData()))
				.filter(type -> Formatting.colorAndStrip(type.getTitle()).equals(Formatting.stripColor(name)))
				.findFirst();
		if (!optional.isPresent())
			return;
		StructureType type = optional.get();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null && !kingdomPlayer.hasAdminMode()) {
			new MessageBuilder("kingdoms.no-kingdom").send(player);
			return;
		}
		Land land = landManager.getLand(block.getChunk());
		Optional<OfflineKingdom> optionalLand = land.getKingdomOwner();
		if (!kingdomPlayer.hasAdminMode() && !optionalLand.isPresent()) {
			new MessageBuilder("kingdoms.not-in-land")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		OfflineKingdom landKingdom = optionalLand.get();
		if (!kingdom.equals(landKingdom)) {
			new MessageBuilder("kingdoms.not-in-land")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		if (!kingdomPlayer.hasAdminMode() && configuration.getStringList("unreplaceable-blocks").contains(block.getType().toString())) {
			new MessageBuilder("kingdoms.nexus-cannot-replace")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		if (!kingdomPlayer.hasAdminMode() && !kingdom.getPermissions(kingdomPlayer.getRank()).canBuildStructures()) {
			new MessageBuilder("kingdoms.rank-too-low-structure-build")
				.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildStructures()), new Placeholder<Optional<Rank>>("%rank%") {
					@Override
					public String replace(Optional<Rank> rank) {
						if (rank.isPresent())
							return rank.get().getName();
						return "(Not attainable)";
					}
				})
				.setKingdom(kingdom)
				.send(player);
		}
		if (land.getStructure() != null) {
			new MessageBuilder("structures.already-structure")
					.replace("%structure%", land.getStructure().getType().name())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		Location location = block.getLocation();
		Structure structure = new Structure(kingdom.getName(), location, type);
		if (type == StructureType.EXTRACTOR)
			structure = new Extractor(kingdom.getName(), location);
		if (type == StructureType.WARPPAD)
			structure = new WarpPad(kingdom.getName(), location, type.build().getItemMeta().getDisplayName());
		StructurePlaceEvent placeEvent = new StructurePlaceEvent(land, structure, kingdom, kingdomPlayer);
		Bukkit.getPluginManager().callEvent(placeEvent);
		if (placeEvent.isCancelled())
			return;
		if (item.getAmount() > 1)
			item.setAmount(item.getAmount() - 1);
		else
			DeprecationUtils.setItemInMainHand(player, null);
		land.setStructure(structure);
		block.setType(type.getBlockMaterial());
		block.setMetadata(type.getMetaData(), new FixedMetadataValue(instance, kingdom.getName()));
		if (type == StructureType.WARPPAD || type == StructureType.OUTPOST)
			kingdom.addWarp(new Warp(type.getTitle(), location));
	}

	private final Map<KingdomPlayer, Extractor> extractors = new HashMap<>();
	private final Map<KingdomPlayer, Land> selected = new HashMap<>();

	public Map<KingdomPlayer, Extractor> getOpenExtractors() {
		return extractors;
	}

	@EventHandler
	public void onRightClickStructure(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		Block block = event.getClickedBlock();
		Optional<StructureType> optional = Arrays.stream(StructureType.values())
				.filter(type -> block.hasMetadata(type.getMetaData()))
				.findFirst();
		if (!optional.isPresent())
			return;
		StructureType type = optional.get();
		event.setCancelled(true);
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		if (!type.isEnabled()) {
			new MessageBuilder("structures.structure-disabled")
					.setKingdom(kingdom)
					.send(kingdomPlayer);
			return;
		}
		Land land = landManager.getLand(block.getLocation().getChunk());
		Optional<OfflineKingdom> optionalLand = land.getKingdomOwner();
		if (!optionalLand.isPresent()) {
			breakStructureAt(land);
			return;
		}
		OfflineKingdom landKingdom = optionalLand.get();
		if (!landKingdom.equals(kingdom)) {
			new MessageBuilder("kingdoms.not-in-land")
					.setKingdom(landKingdom)
					.send(kingdomPlayer);
			return;
		}
		Structure structure = land.getStructure();
		if (structure == null)
			return;
		if (structure.getType() != type)
			return;
		switch (type) {
			case ARSENAL:
				inventoryManager.getInventory(ArsenalInventory.class).open(kingdomPlayer);
				break;
			case EXTRACTOR:
				Extractor extractor;
				if (structure instanceof Extractor) {
					extractor = (Extractor) structure;
				} else {
					extractor = new Extractor(kingdom.getName(), structure.getLocation());
					land.setStructure(extractor);
					block.setType(type.getBlockMaterial());
					block.setMetadata(type.getMetaData(), new FixedMetadataValue(instance, kingdom.getName()));
				}
				inventoryManager.getInventory(ExtractorInventory.class).openExtractorMenu(extractor, kingdomPlayer);
				extractors.put(kingdomPlayer, extractor);
				break;
			case NEXUS:
				if (kingdom.equals(landKingdom)) {
					inventoryManager.getInventory(NexusInventory.class).open(kingdomPlayer);
				} else if (kingdom.isAllianceWith(landKingdom)) {
					Inventory inventory = Bukkit.createInventory(null, 54, Formatting.color("&1Donate to &2" + kingdom.getName()));
					player.openInventory(inventory);
				} else {
					new MessageBuilder("kingdoms.cannot-access-nexus")
							.setKingdom(landKingdom)
							.send(player);
					return;
				}
				break;
			case OUTPOST:
				selected.put(kingdomPlayer, land);
				inventoryManager.getInventory(OutpostInventory.class).open(kingdomPlayer);
				break;
			case WARPPAD:
				selected.put(kingdomPlayer, land);
				inventoryManager.getInventory(WarppadInventory.class).open(kingdomPlayer);
				break;
			case POWERCELL:
			case RADAR:
			default:
				break;
		}
	}

	@EventHandler
	public void onPistonPush(BlockPistonExtendEvent event) {
		for (Iterator<Block> iter = event.getBlocks().iterator(); iter.hasNext();) {
			Block block = iter.next();
			if (block == null || block.getType() == Material.AIR)
				continue;
			if (!isStructure(block))
				continue;
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onPistonPull(BlockPistonRetractEvent event) {
		for (Iterator<Block> iter = event.getBlocks().iterator(); iter.hasNext();) {
			Block block = iter.next();
			if (block == null || block.getType() == Material.AIR)
				continue;
			if (!isStructure(block))
				continue;
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onEntityExplodeStructure(EntityExplodeEvent event) {
		for (Iterator<Block> iter = event.blockList().iterator(); iter.hasNext();) {
			Block block = iter.next();
			if (block == null || block.getType() == Material.AIR)
				continue;
			if (!isStructure(block))
				continue;
			iter.remove();
		}
	}

	@EventHandler
	public void onBucketPlace(PlayerWaterlogEvent event) {
		if (isStructure(event.getBlockClicked()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onWaterPassThrough(BlockFromToEvent event) {
		if (isStructure(event.getToBlock()))
			event.setCancelled(true);
	}

	public boolean isStructure(Block block) {
		for (StructureType type : StructureType.values()) {
			if (block.hasMetadata(type.getMetaData()))
				return true;
		}
		return false;
	}

	@EventHandler
	public void onLandLoad(LandLoadEvent event) {
		loadQueue.add(event.getLand().toInfo());
	}

	@Override
	public void onDisable() {
		extractors.clear();
		loadQueue.clear();
		selected.clear();
		task.cancel();
	}

}
