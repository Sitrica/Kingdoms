package com.songoda.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.Structure;
import com.songoda.kingdoms.objects.land.StructureType;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Extractor;
import com.songoda.kingdoms.objects.structures.Regulator;
import com.songoda.kingdoms.objects.structures.SiegeEngine;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.LandLoadEvent;
import com.songoda.kingdoms.events.StructureBreakEvent;
import com.songoda.kingdoms.events.StructurePlaceEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class StructureManager extends Manager {
	
	private final Queue<Land> loadQueue = new LinkedList<Land>();
	private final WarpPadManager warpPadManager;
	private final KingdomManager kingdomManager;
	private final PlayerManager playerManager;
	private final LandManager landManager;
	private final BukkitTask task;
	
	protected StructureManager() {
		super(true);
		this.landManager = instance.getManager("land", LandManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.warpPadManager = instance.getManager("warppad", WarpPadManager.class);
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				if (loadQueue.isEmpty())
					return;
				Land land = loadQueue.poll();
				if (land == null)
					return;
				Structure structure = land.getStructure();
				if (structure == null)
					return;
				Location location = structure.getLocation();
				Block block = location.getBlock();
				OfflineKingdom kingdom = land.getKingdomOwner();
				if (kingdom == null) {
					block.setType(Material.AIR);
					return;
				}
				block.setType(structure.getType().getMaterial());
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
	
	public void breakStructureAt(Land land) {
		Structure structure = land.getStructure();
		if (structure == null)
			return;
		StructureBreakEvent event = new StructureBreakEvent(land, structure, null, null);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		Location location = structure.getLocation();
		land.setStructure(null);
		Block block = location.getBlock();
		block.setType(Material.AIR);
		StructureType type = structure.getType();
		if (type == StructureType.NEXUS) {
			GameManagement.getNexusManager().breakNexus(land);
			return;
		}
		block.getWorld().dropItemNaturally(location, type.getDisk());
	}
	
	public boolean isInvadeable(KingdomPlayer invader, Chunk chunk) {
		if(!Config.getConfig().getBoolean("enable.structure.powercell"))
			return true;
		Land land = landManager.getLand(chunk);
		Structure structure = land.getStructure();
		if (structure != null && structure.getType() == StructureType.POWERCELL)
			return true;
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
				if (landAround.getKingdomOwner() == null)
					continue;
				if (land.getKingdomOwner().getUniqueId().equals(landAround.getKingdomOwner().getUniqueId())) {
					if (structureAround.getType() == StructureType.POWERCELL)
						return false;
				}
			}
		}
		return true;
	}

	@EventHandler
	public void onAnvilRenameStructure(InventoryClickEvent event) {
		Inventory inventory = event.getInventory();
		if (inventory.getType() != InventoryType.ANVIL)
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
				List<String> list = instance.getConfiguration("messages").get().getStringList("structures.additional-lore");
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
	
	@EventHandler
	public void onSetStructure(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Player player = event.getPlayer();
		ItemStack findItem;
		try {
			findItem = player.getItemInHand();
		} catch (Exception e) {
			findItem = player.getInventory().getItemInMainHand();
		}
		if (findItem == null)
			return;
		ItemStack item = findItem;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return;
		String name = meta.getDisplayName();
		if (name == null)
			return;
		Block block = event.getClickedBlock();
		Optional<StructureType> optional = Arrays.stream(StructureType.values())
				.filter(type -> item.getType() == type.getMaterial())
				.filter(type -> !block.hasMetadata(type.getMetaData()))
				.filter(type -> Formatting.colorAndStrip(type.getTitle()).equals(Formatting.stripColor(name)))
				.findFirst();
		if (!optional.isPresent()) {
			for (String message : configuration.getStringList("structures.additional-lore")) {
				player.sendMessage(Formatting.color(message));
			}
			return;
		}
		StructureType type = optional.get(); 
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("kingdoms.no-kingdom").send(player);
			return;
		}
		Land land = landManager.getLand(block.getChunk());
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
				.send(player);
		}
		if (land.getStructure() != null) {
			for (String message : configuration.getStringList("structures.additional-lore")) {
				player.sendMessage(Formatting.color(message));
			}
			return;
		}
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null || !kingdom.getUniqueId().equals(landKingdom.getUniqueId())) {
			new MessageBuilder("kingdoms.not-in-land")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		if (configuration.getStringList("unreplaceable-blocks").contains(block.getType().toString())) {
			new MessageBuilder("kingdoms.nexus-cannot-replace")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		Location location = block.getLocation();
		Structure structure = new Structure(location, type);
		//special cases
		if (type == StructureType.REGULATOR)
			structure = new Regulator(location, type);
		if (type == StructureType.EXTRACTOR)
			structure = new Extractor(location, type);
		if (type == StructureType.SIEGE_ENGINE)
			structure = new SiegeEngine(location, type);
		StructurePlaceEvent placeEvent = new StructurePlaceEvent(land, structure, kingdom, kingdomPlayer);
		Bukkit.getPluginManager().callEvent(placeEvent);
		if (placeEvent.isCancelled())
			return;
		if (item.getAmount() > 1)
			item.setAmount(item.getAmount() - 1);
		else {
			try {
				player.setItemInHand(null);
			} catch (Exception e) {
				player.getInventory().setItemInMainHand(null);
			}
		}
		land.setStructure(structure);
		block.setType(type.getMaterial());
		block.setMetadata(type.getMetaData(), new FixedMetadataValue(instance, kingdom.getName()));
		if (type == StructureType.WARPPAD || type == StructureType.OUTPOST)
			warpPadManager.addLand(kingdom, land);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onStructureBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Block block = event.getBlock();
		if (!isStructure(block))
			return;
		Land land = landManager.getLand(block.getChunk());
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		if (landKingdom != null) {
			if (!kingdom.getUniqueId().equals(landKingdom.getUniqueId()))
				return;
			if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildStructures()) {
				event.setCancelled(true);
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
				if (!type.equals(StructureType.NEXUS))
					block.getWorld().dropItemNaturally(block.getLocation(), type.getDisk());
				land.setStructure(null);
				warpPadManager.removeLand(kingdom, land);
			}
		}
		event.setCancelled(true);
	}
	

	@EventHandler
	public void onRightClickSiegeEngine(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) return;
		if(!e.getClickedBlock().hasMetadata(StructureType.SIEGEENGINE.getMetaData())) return;
		e.setCancelled(true);
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.getKingdom() == null) return;
		if(!Config.getConfig().getBoolean("enable.siegeengine")){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Structure_Disabled", kp.getLang()));
			return;
		}
		SimpleLocation loc = new SimpleLocation(e.getClickedBlock().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null){
			kp.sendMessage(ChatColor.RED+"The siege engine is at ["+chunk.toString()+"] but no kingdom owns the land.");
			kp.sendMessage(ChatColor.RED+"Please report this glitch.");
			return;
		}
		
		if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
			return;
		}
		if(land.getStructure().getType() == StructureType.SIEGEENGINE){
			if(land.getStructure() instanceof SiegeEngine){
			}else{
				
				SiegeEngine structure = new SiegeEngine(loc, StructureType.EXTRACTOR);
				land.setStructure(structure);
				e.getClickedBlock().setType(Material.DISPENSER);
				e.getClickedBlock().setMetadata(StructureType.SIEGEENGINE.getMetaData(), new FixedMetadataValue(plugin, kp.getKingdom().getKingdomName()));
			}
		}else return;
		GUIManagement.getSiegeEngineGUIManager().openMenu(kp, land);
	}

	@EventHandler
	public void onRightClickArsenal(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) return;
		if(!e.getClickedBlock().hasMetadata(StructureType.ARSENAL.getMetaData())) return;
		e.setCancelled(true);
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.getKingdom() == null) return;
		if(!Config.getConfig().getBoolean("enable.structure.arsenal")){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Structure_Disabled", kp.getLang()));
			return;
		}
		SimpleLocation loc = new SimpleLocation(e.getClickedBlock().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null){
			kp.sendMessage(ChatColor.RED+"The arsenal is at ["+chunk.toString()+"] but no kingdom owns the land.");
			kp.sendMessage(ChatColor.RED+"Please report this glitch.");
			return;
		}
		
		if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
			return;
		}
		GUIManagement.getArsenalGUIManager().openMenu(kp);
	}
	
	@EventHandler
	public void onRightClickOutpost(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) return;
		if(!e.getClickedBlock().hasMetadata(StructureType.OUTPOST.getMetaData())) return;
		
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.getKingdom() == null) return;
		if(!Config.getConfig().getBoolean("enable.structure.outpost")){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Structure_Disabled", kp.getLang()));
			return;
		}
		SimpleLocation loc = new SimpleLocation(e.getClickedBlock().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null){
			kp.sendMessage(ChatColor.RED+"The outpost is at ["+chunk.toString()+"] but no kingdom owns the land.");
			kp.sendMessage(ChatColor.RED+"Please report this glitch.");
			return;
		}
		
		if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
			return;
		}
		selected.put(kp, land);
		GUIManagement.getOutpostGUIManager().openMenu(kp);
	}
	
	@EventHandler
	public void onRightClickRegulator(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) return;
		if(!e.getClickedBlock().hasMetadata(StructureType.REGULATOR.getMetaData())) return;
		
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.getKingdom() == null) return;

		if(!Config.getConfig().getBoolean("enable.structure.powercell")){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Structure_Disabled", kp.getLang()));
			return;
		}
		SimpleLocation loc = new SimpleLocation(e.getClickedBlock().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null){
			kp.sendMessage(ChatColor.RED+"The regulator is at ["+chunk.toString()+"] but no kingdom owns the land.");
			kp.sendMessage(ChatColor.RED+"Please report this glitch.");
			return;
		}
		
		if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
			return;
		}
		
		if(!kp.getRank().isHigherOrEqualTo(kp.getKingdom().getPermissionsInfo().getOverrideRegulator())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low", kp.getLang()).replaceAll("%rank%", kp.getKingdom().getPermissionsInfo().getOverrideRegulator().toString()));
			return;
		}
		
		GUIManagement.getRegulatorGUIManager().openRegulatorMenu(kp, land);
	}
	
	public static HashMap<KingdomPlayer,Land> selected = new HashMap<KingdomPlayer,Land>();
	@EventHandler
	public void onRightClickWarpPad(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) return;
		if(!e.getClickedBlock().hasMetadata(StructureType.WARPPAD.getMetaData())) return;
		
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.getKingdom() == null) return;

		if(!Config.getConfig().getBoolean("enable.structure.warppad")){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Structure_Disabled", kp.getLang()));
			return;
		}
		SimpleLocation loc = new SimpleLocation(e.getClickedBlock().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null){
			kp.sendMessage(ChatColor.RED+"The warp pad is at ["+chunk.toString()+"] but no kingdom owns the land.");
			kp.sendMessage(ChatColor.RED+"Please report this glitch.");
			return;
		}
		
		if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
			return;
		}
		if(land.getStructure().getType() == StructureType.WARPPAD){
			selected.put(kp, land);
			GUIManagement.getWarppadGUIManager().openMenu(kp);
			
		}
	}
	
	public static HashMap<UUID, Extractor> extractors = new HashMap<UUID, Extractor>();
	@EventHandler
	public void onRightClickExtractor(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) return;
		if(!e.getClickedBlock().hasMetadata(StructureType.EXTRACTOR.getMetaData())) return;
		
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.getKingdom() == null) return;

		if(!Config.getConfig().getBoolean("enable.structure.extractor")){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Structure_Disabled", kp.getLang()));
			return;
		}
		SimpleLocation loc = new SimpleLocation(e.getClickedBlock().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null){
			kp.sendMessage(ChatColor.RED+"The extractor is at ["+chunk.toString()+"] but no kingdom owns the land.");
			kp.sendMessage(ChatColor.RED+"Please report this glitch.");
			return;
		}
		
		if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
			return;
		}
		if(land.getStructure().getType() == StructureType.EXTRACTOR){
			if(land.getStructure() instanceof Extractor){
				
				GUIManagement.getExtractorGUIManager().openExtractorMenu((Extractor) land.getStructure(), kp);
			}else{
				
				Extractor structure = new Extractor(loc, StructureType.EXTRACTOR);
				
				land.setStructure(structure);
				e.getClickedBlock().setType(Material.EMERALD_BLOCK);
				e.getClickedBlock().setMetadata("extractor", new FixedMetadataValue(plugin, kp.getKingdom().getKingdomName()));
				GUIManagement.getExtractorGUIManager().openExtractorMenu((Extractor) land.getStructure(), kp);
				
			}
			
		}
		extractors.put(kp.getUuid(), (Extractor) land.getStructure());
	}

	
	@EventHandler
	public void onPistonPushTurret(BlockPistonExtendEvent e){
		for(Iterator<Block> iter = e.getBlocks().iterator(); iter.hasNext();){
			Block block = iter.next();
			if(block == null || block.getType() == Material.AIR) continue;

			if(!isStructure(block)) continue;

			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onPistonPullTurret(BlockPistonRetractEvent e){
		for(Iterator<Block> iter = e.getBlocks().iterator(); iter.hasNext();){
			Block block = iter.next();
			if(block == null || block.getType() == Material.AIR) continue;

			if(!isStructure(block)) continue;

			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onEntityExplodeStructure(EntityExplodeEvent e){
		for(Iterator<Block> iter = e.blockList().iterator(); iter.hasNext();){
			Block block = iter.next();
			if(block == null || block.getType() == Material.AIR) continue;
			
//			if(block.getType() != Material.REDSTONE_LAMP_ON 
//					&& block.getType() != Material.HAY_BLOCK
//					&& block.getType() != Materials.BEACON.parseMaterial()) continue;

			if(!isStructure(block)) continue;
			iter.remove();
		}
	}
	
	public boolean isStructure(Block block){
		for(StructureType type:StructureType.values()){
			if(block.hasMetadata(type.getMetaData())) return true;
		}
		return false;
	}
	
	@EventHandler
	public void onLandLoad(LandLoadEvent e){
		Kingdoms.logDebug("structure manager land load event");
		/*initStructure(e.getLand());*/
		loadQueue.add(e.getLand());
		
		
		
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

}
