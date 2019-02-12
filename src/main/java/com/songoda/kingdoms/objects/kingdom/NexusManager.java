package com.songoda.kingdoms.objects.kingdom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.Structure;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.events.StructureBreakEvent;
import com.songoda.kingdoms.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class NexusManager extends Manager {
	
	static {
		registerManager("nexus", new NexusManager());
	}
	
	private final Set<KingdomPlayer> placing = new HashSet<>();
	private final PlayerManager playerManager;
	
	protected NexusManager() {
		super(true);
		this.playerManager = instance.getManager("player", PlayerManager.class);
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
	public void onPlayerClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block block = event.getClickedBlock();
		//TODO READ WHAT BLOCK IT COULD BE.
		//if (block.getType() != Material.BEACON)
		//	return;
		if (!event.getClickedBlock().hasMetadata(StructureType.NEXUS.getMetaData())) return;
		
		KingdomPlayer clicked = GameManagement.getPlayerManager().getSession(event.getPlayer());
		if(clicked == null){
			Kingdoms.logInfo("kp is null!");
			return;
		}
		
		event.setCancelled(true);
		
		
		if(clicked.getKingdom() == null){
			clicked.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Kingdom", clicked.getLang()));
			return;
		}
		
		Block nexusBlock = event.getClickedBlock();
		SimpleLocation loc = new SimpleLocation(nexusBlock.getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land == null){
			Kingdoms.logInfo("clicked nexus in ["+chunk+"] but land was null!");
			return;
		}
		
		Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
		if(kingdom == null){
			clicked.sendMessage(ChatColor.RED + "Kingdom ["+land.getOwner()+"] not found!");
			clicked.sendMessage(ChatColor.RED + "Something is glitched; please report");
			
			nexusBlock.setType(Material.AIR);
			return;
		}
		
		if(kingdom.isAllianceWith(clicked.getKingdom())){
			Inventory dumpgui = Bukkit.createInventory(null, 54,
					ChatColor.DARK_BLUE + "Donate to " + ChatColor.DARK_GREEN + kingdom.getKingdomName());
			clicked.getPlayer().openInventory(dumpgui);
		}else if(kingdom.equals(clicked.getKingdom())){
			GUIManagement.getNexusGUIManager().openNexusGui(clicked);
		}else{
			clicked.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Use_Other_Nexus", clicked.getLang()));
			return;
		}
	}
	
	@EventHandler
	public void onChunkChange(PlayerChangeChunkEvent event) {
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		if (!placing.contains(kingdomPlayer))
			return;
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Nexus_Setting_Disabled", kp.getLang()));
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Nexus_Setting_Different_Land", kp.getLang()));
		placing.remove(kingdomPlayer);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlaceNexusBlock(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(!placingNexusList.contains(kp)) return;
		
		Kingdom kingdom = kp.getKingdom();
		
		//check if replacing in worldguard region
		if(!GameManagement.getApiManager().canBuild(kp.getPlayer(), e.getClickedBlock())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Claim_In_WG", kp.getLang()));
			return;
		}
		
		//check if placed in own land
		SimpleChunkLocation clickedChunk = new SimpleChunkLocation(e.getClickedBlock().getChunk());
		Land land = GameManagement.getLandManager().getOrLoadLand(clickedChunk);
		if(land.getOwnerUUID() == null || !land.getOwnerUUID().equals(kingdom.getKingdomUuid())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));
			return;
		}
		
/*		if(land.getStructure() != null){
			kp.sendMessage(ChatColor.RED+"This land has another structure.");
			kp.sendMessage(ChatColor.RED+"Only one structure is allowed in a land.");
			kp.sendMessage(ChatColor.GREEN+"Structures: NEXUS, POWERCELL, OUTPOST");
			return;
		}*/
		
		Block clicked = e.getClickedBlock();
		//check if replacing with turret
		if(plugin.getManagers().getTurretManager().isTurret(clicked)){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Nexus_Setting_Cannot_Replace", kp.getLang()));
			return;
		}
		
		//check if replacing with black listed blocks
		if(Config.getConfig().getStringList("unreplaceableblocks").contains(clicked.getType().toString())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Nexus_Setting_Cannot_Replace", kp.getLang()));
			return;
		}
		
		placingNexusList.remove(kp);
		KingdomPlayerPlaceNexusEvent lce = new KingdomPlayerPlaceNexusEvent(kp, kingdom, clicked.getLocation(), land.getLoc());
		Bukkit.getPluginManager().callEvent(lce);
		setNexus(land, clicked);
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Nexus_Setting_Success", kp.getLang()));
	}
	
	public void setNexus(Land land, Block target){
		if(land.getOwnerUUID() == null){
			Kingdoms.logInfo(Kingdoms.getLang().getString("Misc_Nexus_Setting_Not_In_Land"));
			return;
		}
		
		Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
		
		if (kingdom.getNexus_loc() != null) {
			
			SimpleLocation loc = new SimpleLocation(kingdom.getNexus_loc());
			SimpleChunkLocation chunk = loc.toSimpleChunk();
			
			Land previousNexusLand = GameManagement.getLandManager().getOrLoadLand(chunk);
			previousNexusLand.setStructure(null);
			
			Block previous_nexus = kingdom.getNexus_loc().getBlock();
			previous_nexus.setType(Material.AIR);
		}
		
		if(land.getStructure() != null){
			Block str = land.getStructure().getLoc().toLocation().getBlock();
			StructureBreakEvent event = new StructureBreakEvent(land, str.getLocation(), land.getStructure().getType(), kingdom, null);
			Bukkit.getPluginManager().callEvent(event);
			str.setType(Material.AIR);
			
			for(StructureType type:StructureType.values()){
				if(str.hasMetadata(type.getMetaData())){
					str.removeMetadata(type.getMetaData(), plugin);
					str.getWorld().dropItemNaturally(target.getLocation(), type.getDisk());
					land.setStructure(null);
					WarpPadManager.removeLand(kingdom, land);
				}
			}
		}
		
		SimpleLocation loc = new SimpleLocation(target.getLocation());
		Structure structure = new Structure(loc, StructureType.NEXUS);
		land.setStructure(structure);
		
		target.setMetadata(StructureType.NEXUS.getMetaData(), new FixedMetadataValue(plugin, kingdom.getKingdomName()));
		target.setType(Materials.BEACON.parseMaterial());
		
		kingdom.setNexus_loc(loc.toLocation());
	}
	
	@EventHandler
	public void onCancelPlaceBlock(PlayerInteractEvent e){
		if(e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.getKingdom() == null){
			placingNexusList.remove(kp);
			return;
		}
		
		if(!placingNexusList.contains(kp)) return;
		
		placingNexusList.remove(kp);
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Nexus_Setting_Disabled", kp.getLang()));
	}
	
	
	@EventHandler
	public void onNexusBlockBreakUnNatural2(BlockBreakEvent e){
		if(e.isCancelled()) return;
		
		if(e.getBlock() == null) return;
		
		if(e.getBlock().getType() != Materials.BEACON.parseMaterial()) return;
		
		Block nexusBlock = e.getBlock();
		if(!nexusBlock.hasMetadata(StructureType.NEXUS.getMetaData())) return;
		
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onNexusMining(BlockBreakEvent e){
		Kingdoms.logDebug("nexus mine event 1");
		if(e.getBlock() == null) return;
		
		if(e.getBlock().getType() != Materials.BEACON.parseMaterial()) return;
		
		Block nexusBlock = e.getBlock();
		if(!nexusBlock.hasMetadata(StructureType.NEXUS.getMetaData())) return;
		
		KingdomPlayer invader = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(invader == null) return;
		
		Kingdom kInvader = invader.getKingdom();
		if(kInvader == null) return;
		
		SimpleLocation loc = new SimpleLocation(nexusBlock.getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		
		Kingdoms.logDebug("nexus mine event 2");
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land == null) return; //something is not good if it's null
		
		if(land.getOwnerUUID() == null){
			Kingdoms.logInfo("There was a nexus at ["+chunk.toString()+"] but no owner.");
			Kingdoms.logInfo("Removed nexus.");
			
			e.getBlock().setType(Material.AIR);
			e.setCancelled(true);
			return;
		}
		
		Kingdoms.logDebug("nexus mine event 3");
		Kingdom kNexusOwner = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
		if(kNexusOwner == null) return;
		
		Kingdoms.logDebug("nexus mine event 4");
		if(kInvader.isAllianceWith(kNexusOwner)){
			invader.sendMessage(Kingdoms.getLang().getString("Nexus_Mining_isAlliance", invader.getLang()));
			return;
		}
		
		if(kInvader.getKingdomName().equals(kNexusOwner.getKingdomName())){
			invader.sendMessage(Kingdoms.getLang().getString("Nexus_Mining_isOwn", invader.getLang()));
			return;
		}
		Kingdoms.logDebug("nexus mine event 4");
		int rp = kNexusOwner.getResourcepoints();
		
		if(rp > Config.getConfig().getInt("nexusMiningAmount")){
			kNexusOwner.setResourcepoints(rp - Config.getConfig().getInt("nexusMiningAmount"));
			
			kInvader.setResourcepoints(kInvader.getResourcepoints() + Config.getConfig().getInt("nexusMiningAmount"));
			//Kingdoms.getLang().addInteger(Kingdoms.config.nexusMiningAmount);
			invader.sendMessage(Kingdoms.getLang().getString("Nexus_Mining_EarnedRP", invader.getLang()).replace("%amount%",String.valueOf(Config.getConfig().getInt("nexusMiningAmount"))));
		}else{
			int earned = kNexusOwner.getResourcepoints();
			kNexusOwner.setResourcepoints(0);
			
			if(!(earned <= 0)){
				kInvader.setResourcepoints(kInvader.getResourcepoints() + earned);
				//Kingdoms.getLang().addInteger(earned);
				invader.sendMessage(Kingdoms.getLang().getString("Nexus_Mining_EarnedRP", invader.getLang()).replace("%amount%",String.valueOf(earned)));
			}
		}
		
		if(GameManagement.getApiManager().getScoreboardManager() != null){
			ExternalManager.getScoreboardManager().updateScoreboard(invader);
		}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityExplodeInKingdomLand(EntityExplodeEvent e){
		for(Iterator<Block> iter = e.blockList().iterator(); iter.hasNext();){
			Block block = iter.next();
			if(block == null || block.getType() == Material.AIR) continue;
			
			if(block.getType() != Materials.BEACON.parseMaterial()) continue;
			Block nexusBlock = block;
			if(!nexusBlock.hasMetadata(StructureType.NEXUS.getMetaData())) continue;
			
			iter.remove();
		}
	}
	
	public void breakNexus(Land land){
		if(land.getStructure() == null){
			Kingdoms.logInfo("Could not break nexus at ["+land.getLoc().toString()+"]");
			Kingdoms.logInfo("There is no structure in this land.");
			return;
		}
		
		if(land.getStructure().getType() != StructureType.NEXUS){
			Kingdoms.logInfo("Could not break nexus at ["+land.getLoc().toString()+"]");
			Kingdoms.logInfo("Structure is not nexus.");
			return;
		}
		
		Block nexusBlock = land.getStructure().getLoc().toLocation().getBlock();
		
		SimpleLocation nexusLoc = land.getStructure().getLoc();
		SimpleChunkLocation chunk = nexusLoc.toSimpleChunk();
		
		if(land.getOwnerUUID() == null){
			Kingdoms.logInfo("The nexus is destroyed but nobody owns the land ["+chunk+"]!");
			return;
		}
		
		if(land.getStructure() == null){
			Kingdoms.logInfo("The nexus destroy is requested at ["+chunk+"] but no structure in that land!");
			return;
		}
		
		if(land.getStructure().getType() != StructureType.NEXUS){
			Kingdoms.logInfo("The nexus destroy is requested at ["+chunk+"] but structure is not nexus!");
			return;
		}
		
		land.setStructure(null);
		
		Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
		kingdom.setNexus_loc(null);
		
		nexusBlock.setType(Material.AIR);
		
		createNexusBlast(nexusBlock.getLocation(), plugin);
	}
	
	private void createNexusBlast(final Location loc, Kingdoms plugin) {
		loc.getWorld().strikeLightningEffect(loc);
		loc.getWorld().strikeLightningEffect(loc);
		loc.getWorld().strikeLightningEffect(loc);
		loc.getWorld().strikeLightningEffect(loc);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
			}
		}, 10);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
			}
		}, 20);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
			}
		}, 30);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
				loc.getWorld().strikeLightningEffect(loc);
			}
		}, 40);

	}

	@Override
	public void onDisable() {
		placingNexusList.clear();
	}
}
