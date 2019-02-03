package com.songoda.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.songoda.kingdoms.main.Config;
import com.songoda.kingdoms.manager.gui.GUIManagement;
import com.songoda.kingdoms.objects.KingdomPlayer;
import com.songoda.kingdoms.objects.StructureType;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Extractor;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.Regulator;
import com.songoda.kingdoms.objects.land.SiegeEngine;
import com.songoda.kingdoms.objects.land.SimpleChunkLocation;
import com.songoda.kingdoms.objects.land.SimpleLocation;
import com.songoda.kingdoms.objects.land.Structure;
import com.songoda.kingdoms.objects.land.WarpPadManager;
import com.songoda.kingdoms.events.LandLoadEvent;
import com.songoda.kingdoms.events.StructureBreakEvent;
import com.songoda.kingdoms.events.StructurePlaceEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.game.GameManagement;
import com.songoda.kingdoms.utils.LoreOrganizer;
import com.songoda.kingdoms.utils.Materials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.songoda.kingdoms.main.Kingdoms;

public class StructureManager extends Manager {
	
	protected StructureManager() {
		super(true);
		new Thread(new SyncLoadTask()).start();
	}
	
	public void breakStructure(Land land){
		if(land.getStructure() == null) return;
		
		Block block = land.getStructure().getLoc().toLocation().getBlock();
		World world = block.getWorld();
		
		block.setType(Material.AIR);
		StructureType type = land.getStructure().getType();
		land.setStructure(null);
		StructureBreakEvent event = new StructureBreakEvent(land, block.getLocation(), type, null, null);
		Bukkit.getPluginManager().callEvent(event);
		if(type == StructureType.NEXUS){
			GameManagement.getNexusManager().breakNexus(land);
			return;
		}

		world.dropItemNaturally(block.getLocation(), type.getDisk());
	}
	
	public boolean isInvadeable(KingdomPlayer invader, Chunk chunk) {
		if(!Config.getConfig().getBoolean("enable.structure.powercell")){
			return true;
		}
		Land landInvading = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(landInvading.getStructure() != null){
			if(landInvading.getStructure().getType() == StructureType.POWERCELL){
				return true;
			}
		}
		int radius = 1;
		
		String world = chunk.getWorld();
		int originX = chunk.getX();
		int originZ = chunk.getZ();
		
		for(int x = -radius; x<=radius; x++){
			for(int z = -radius; z<=radius; z++){
				if(x == 0 && z == 0) continue;
				
				SimpleChunkLocation target = new SimpleChunkLocation(world, originX + x, originZ + z);
				Land landAround = GameManagement.getLandManager().getOrLoadLand(target);
				
				if(landAround.getStructure() == null) continue;
				if(landAround.getOwnerUUID() == null) continue;
				
				if(landInvading.getOwnerUUID().equals(landAround.getOwnerUUID()) &&
						landAround.getStructure().getType() == StructureType.POWERCELL) 
					return false;
			}
		}
		
		return true;
	}
	
	@EventHandler
	public void onAnvilRenameStructure(InventoryClickEvent event){
		if(event.getInventory().getType() != InventoryType.ANVIL) return;
		AnvilInventory inv = (AnvilInventory) event.getInventory();
		Bukkit.getScheduler().runTaskLater(plugin, new BukkitRunnable(){
			@Override
			public void run(){
				ItemStack renamed = inv.getItem(2);
				if(renamed == null) return;
				if(renamed.getItemMeta() == null) return;
				if(renamed.getItemMeta().getLore() == null) return;
				if(renamed.getItemMeta().getLore().contains(Kingdoms.getLang().getString("Structures_Placement_Instructions"))){
					inv.setItem(2, new ItemStack(Material.AIR));
				}
			}
		}, 1L);
	}
	
	@EventHandler
	public void onSetStructure(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(event.getPlayer().getItemInHand() == null) return;
		if(event.getPlayer().getItemInHand().getType() != Materials.MUSIC_DISC_BLOCKS.parseMaterial());
		if(event.getPlayer().getItemInHand().getItemMeta() == null) return;
		if(event.getPlayer().getItemInHand().getItemMeta().getDisplayName() == null) return;
		String displayName = event.getPlayer().getItemInHand().getItemMeta().getDisplayName();
		StructureType placingType = null;
		for(StructureType type:StructureType.values()){
			if(type.getDisk().getItemMeta().getDisplayName().equals(displayName)){
				placingType = type;
				break;
			}
		}
		if(placingType == null) return;
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(event.getPlayer());

		Kingdom kingdom = kp.getKingdom();
		if(kingdom == null){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Kingdom", kp.getLang()));
			return;
		}
		Block clickedBlock = event.getClickedBlock();
		SimpleLocation loc = new SimpleLocation(clickedBlock.getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getStructures())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getStructures().toString()));
			return;
		}
		if(isStructure(clickedBlock)){
			kp.sendMessage(Kingdoms.getLang().getString("Structures_Placement_Instructions", kp.getLang()));
			return;
		}
		
		
		
		if(land.getStructure() != null){
			kp.sendMessage(Kingdoms.getLang().getString("Structures_Placement_Instructions", kp.getLang()));
			return;
		}
		
		if(land.getOwnerUUID() == null || !land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
			return;
		}
		
		if(Config.getConfig().getStringList("unreplaceableblocks").contains(clickedBlock.getType().toString())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Nexus_Setting_Cannot_Replace", kp.getLang()));
			return;
		}
		

		Structure structure = new Structure(loc, placingType);
		
		//special cases
		if(placingType == StructureType.REGULATOR)
			structure = new Regulator(loc, placingType);
		if(placingType == StructureType.EXTRACTOR)
			structure = new Extractor(loc, placingType);
		if(placingType == StructureType.SIEGEENGINE)
			structure = new SiegeEngine(loc, placingType);
		
		
		StructurePlaceEvent ev = new StructurePlaceEvent(land, loc.toLocation(), placingType, kingdom, kp);
		Bukkit.getPluginManager().callEvent(ev);
		if(ev.isCancelled()) return;
		
		ItemStack IS = kp.getPlayer().getItemInHand();
		if (IS.getAmount() > 1) IS.setAmount(IS.getAmount() - 1);
		else kp.getPlayer().setItemInHand(null);
		
		land.setStructure(structure);
		clickedBlock.setType(placingType.getMaterial());
		clickedBlock.setMetadata(placingType.getMetaData(), new FixedMetadataValue(plugin, kp.getKingdom().getKingdomName()));
		if(placingType == StructureType.WARPPAD||
				placingType == StructureType.OUTPOST)
			WarpPadManager.addLand(kp.getKingdom(), land);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onStructureBreak(BlockBreakEvent e) {
		if(e.isCancelled()) return;
		
		if(!isStructure(e.getBlock())) return;
		Land land = GameManagement.getLandManager().getOrLoadLand(new SimpleChunkLocation(e.getBlock().getChunk()));
		KingdomPlayer player = Kingdoms.getManagers().getPlayerManager().getSession(e.getPlayer());
		if(land.getOwnerUUID() != null){
			if(player.getKingdomName() != null){
				if(player.getKingdomUuid().equals(land.getOwnerUUID())){
					Kingdom kingdom = player.getKingdom();
					if(!player.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getStructures())){
							e.setCancelled(true);
							player.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low", player.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getStructures().toString()));
							return;
					}
				}else return;
			}else return;
		}
		if(land.getStructure() == null){
			e.getBlock().setType(Material.AIR);
			return;
		}

		StructureBreakEvent event = new StructureBreakEvent(land, e.getBlock().getLocation(), land.getStructure().getType(), player.getKingdom(), player);
		Bukkit.getPluginManager().callEvent(event);
		e.getBlock().setType(Material.AIR);
		
		for(StructureType type:StructureType.values()){
			if(e.getBlock().hasMetadata(type.getMetaData())){
				e.getBlock().removeMetadata(type.getMetaData(), plugin);
				if (!type.equals(StructureType.NEXUS)){
				e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), type.getDisk());
				}
				land.setStructure(null);
				WarpPadManager.removeLand(player.getKingdom(), land);
			}
		}
		
		e.setCancelled(true);
	}
	

	@EventHandler
	public void onRightClickSiegeEngine(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) return;
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
	
	private static Queue<Land> loadQueue = new LinkedList<Land>();
	private class SyncLoadTask implements Runnable{

		@Override
		public void run() {
			Land land = null;
			while(plugin.isEnabled()){
				if(Kingdoms.isDisabling()) return;
				try {
					Thread.sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(loadQueue.isEmpty()) continue;
				land = loadQueue.poll();
				if(land == null) continue;
				
				initStructure(land);
			}
		}
		
		private void initStructure(final Land land) {
			//suspicious2
			new BukkitRunnable(){
				@Override
				public void run() {
					Structure structure = land.getStructure();
					if(structure == null) return;
					if(structure.getLoc() == null) return;
					if(structure.getLoc().toLocation() == null) return;
					if(structure.getLoc().toLocation().getWorld() == null) return;
					if(land.getOwnerUUID() == null){
						Block block = structure.getLoc().toLocation().getBlock();

						block.setType(Material.AIR);
						return;
					}
					
					if(structure.getType() == null){
						Kingdoms.logDebug("WTF? Init structure");
						return;
					}
					if(structure.getType().getMaterial() == null){
						Kingdoms.logDebug("Well that's totally normal? Init structure");
						return;
					}
					
					Block block = structure.getLoc().toLocation().getBlock();
					
					block.setType(structure.getType().getMaterial());
					block.setMetadata(structure.getType().getMetaData(), new FixedMetadataValue(plugin, land.getOwnerUUID()));
						
					if (structure.getType() == StructureType.NEXUS) {
						Kingdom kingdom = Kingdoms.getManagers().getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
						if(kingdom == null){
							block.setType(Material.AIR);
							land.setStructure(null);
							return;
						}
						if(kingdom.getNexus_loc() == null){
							block.setType(Material.AIR);
							land.setStructure(null);
							return;
						}
						if(!kingdom.getNexus_loc().equals(block.getLocation())){
							block.setType(Material.AIR);
							land.setStructure(null);
							return;
						}
						Kingdoms.logDebug("" + structure.getLoc().toSimpleChunk().toString());
						

						block.setType(Materials.BEACON.parseMaterial());
					}
					
//						if (structure.getType() == StructureType.OUTPOST) {
//						Block block = structure.getLoc().toLocation().getBlock();
//
//						block.setType(Material.HAY_BLOCK);
//						block.setMetadata("outpost", new FixedMetadataValue(plugin, land.getOwner()));
//					} else if (structure.getType() == StructureType.POWERCELL) {
//						Block block = structure.getLoc().toLocation().getBlock();
//
//						block.setType(Material.REDSTONE_LAMP_ON);
//						block.setMetadata("powercell", new FixedMetadataValue(plugin, land.getOwner()));
//					} else if (structure.getType() == StructureType.EXTRACTOR) {
//						Block block = structure.getLoc().toLocation().getBlock();
//						block.setType(Material.EMERALD_BLOCK);
//						block.setMetadata("extractor", new FixedMetadataValue(plugin, land.getOwner()));
//					} else if (structure.getType() == StructureType.WARPPAD) {
//						Block block = structure.getLoc().toLocation().getBlock();
//						block.setType(Material.SEA_LANTERN);
//						block.setMetadata("warppad", new FixedMetadataValue(plugin, land.getOwner()));
//					} 
////					else if (structure.getType() == StructureType.LAB) {
////						Block block = structure.getLoc().toLocation().getBlock();
////						//block.setType(Material.);
////						block.setMetadata("lab", new FixedMetadataValue(plugin, land.getOwner()));
////					} else if (structure.getType() == StructureType.CRYSTAL) {
////						Block block = structure.getLoc().toLocation().getBlock();
////						block.setType(Material.LAPIS_BLOCK);
////						block.setMetadata("crystal", new FixedMetadataValue(plugin, land.getOwner()));
////					} 
//					else if (structure.getType() == StructureType.REGULATOR){
//						Block block = structure.getLoc().toLocation().getBlock();
//						block.setType(Material.REDSTONE_BLOCK);
//						Kingdoms.logDebug("Loaded Regulator");
//						block.setMetadata("regulator", new FixedMetadataValue(plugin, land.getOwner()));
//					} else if (structure.getType() == StructureType.RADAR){
//						Block block = structure.getLoc().toLocation().getBlock();
//						block.setType(Material.STAINED_GLASS);
//						Kingdoms.logDebug("Loaded Radar");
//						block.setMetadata("radar", new FixedMetadataValue(plugin, land.getOwner()));
//					}  else if (structure.getType() == StructureType.SIEGEENGINE){
//						Block block = structure.getLoc().toLocation().getBlock();
//						block.setType(Material.FURNACE);
//						Kingdoms.logDebug("Loaded Siege Engine");
//						block.setMetadata("siegeengine", new FixedMetadataValue(plugin, land.getOwner()));
//					} else 
						
				}
			}.runTask(plugin);

		}
		
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

}
