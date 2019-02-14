package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.events.LandLoadEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.Turret;
import com.songoda.kingdoms.objects.land.TurretType;
import com.songoda.kingdoms.utils.TurretUtil;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TurretManager extends Manager {
	
	static {
		registerManager("turret", new TurretManager());
	}
	
	private final LandManager landManager;
	
	protected TurretManager() {
		super(true);
		this.landManager = instance.getManager("land", LandManager.class);
	}

	@EventHandler
	public void onDispense(BlockDispenseEvent event) {
		Block block = event.getBlock();
		BlockState state = block.getState();
		if (!(state instanceof Dispenser))
			return;
		Dispenser dispenser = (Dispenser) state;
		BlockFace face = ((org.bukkit.material.Dispenser) dispenser.getData()).getFacing();
		if (isTurret(block.getRelative(face)))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockUnderPressurePlateBreak(BlockBreakEvent event) {
		Block block = event.getBlock().getRelative(0, 1, 0);
		if (block.getType().toString().contains("PLATE") && isTurret(block)) {
			Location location = block.getLocation();
			Land land = landManager.getLand(location.getChunk());
			Turret turret = land.getTurret(location);
			turret.breakTurret();
		}
	}

	// Fixes heads not being removed from 
	@EventHandler
	public void onBucketPlace(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			ItemStack item;
			try {
				item = player.getItemInHand();
			} catch (Exception e) {
				item = player.getInventory().getItemInMainHand();
			}
			if (item != null) {
				Material type = item.getType();
				if (type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET) {
					if (isTurret(event.getClickedBlock().getRelative(event.getBlockFace()))) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onWaterPassThrough(BlockFromToEvent event){
	if(isTurret(event.getToBlock()))
		event.setCancelled(true);
	}

	@EventHandler
	public void onAnvilRenameTurret(InventoryClickEvent event){
	if(event.getInventory().getType() != InventoryType.ANVIL) return;
	AnvilInventory inv = (AnvilInventory) event.getInventory();
	Bukkit.getScheduler().runTaskLater(plugin, new BukkitRunnable() {
		@Override
		public void run(){
		ItemStack renamed = inv.getItem(2);
		if(renamed == null) return;
		if(renamed.getItemMeta() == null) return;
		if(renamed.getItemMeta().getLore() == null) return;
		for(TurretType type : TurretType.values()){
			if(!(renamed.getItemMeta().getLore().contains(type.getTypeDecal()))) continue;
			inv.setItem(2, new ItemStack(Material.AIR));
			break;
		}

		}
	}, 1L);
	}


	@EventHandler
	public void onTurretBreak(BlockBreakEvent e){
	if(isTurret(e.getBlock())){
		e.setCancelled(true);
		SimpleLocation loc = new SimpleLocation(e.getBlock().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		Turret turret = land.getTurret(loc);
		if (land.getOwnerUUID()==null){
			KingdomTurretBreakEvent event = new KingdomTurretBreakEvent(loc.toLocation(), turret.getType(), null);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()){
				turret.breakTurret();
			}
		}
		if(land.getTurret(loc) == null) return;
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());

		Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
		if(!kp.isAdminMode() && kp.getKingdom() == null){
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Kingdom", kp.getLang()));
		return;
		}
		if(!kp.isAdminMode() &&
			(kingdom == null || !kingdom.equals(kp.getKingdom()))){
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
		return;
		}
		if(!kp.isAdminMode() && !kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getTurret())){
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getTurret().toString()));
		return;
		}

		KingdomTurretBreakEvent event = new KingdomTurretBreakEvent(loc.toLocation(), turret.getType(), kingdom);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()){
		turret.breakTurret();
		}
	}
	}

	public int getNumberOfTurretsInLand(Land land, TurretType type){
	int i = 0;
	for(Turret t : land.getTurrets()){
		if(t.getType() == null) continue;
		if(t.getType().equals(type)) i++;
	}
	return i;
	}

	public boolean isMaxHitInLand(Land land, TurretType type){
	return getNumberOfTurretsInLand(land, type) >= type.getPerLandMaxLimit();
	}

	public boolean isOverHitInLand(Land land, TurretType type){
	return getNumberOfTurretsInLand(land, type) > type.getPerLandMaxLimit();
	}

	private Collection<Material> illegal = new ArrayList<Material>() {{
	add(Material.JUKEBOX);
	add(Material.NOTE_BLOCK);
	add(Materials.PISTON.parseMaterial());
	add(Materials.PISTON_HEAD.parseMaterial());
	add(Materials.STICKY_PISTON.parseMaterial());
	add(Materials.MOVING_PISTON.parseMaterial());
	add(Material.FURNACE);
	add(Material.CHEST);
	add(Materials.CRAFTING_TABLE.parseMaterial());
	add(Material.ENDER_CHEST);
	add(Material.DISPENSER);
	add(Material.GLOWSTONE);
	add(Material.SEA_LANTERN);
	add(Materials.ENCHANTING_TABLE.parseMaterial());
	add(Material.ANVIL);
	add(Material.BREWING_STAND);

	}};

	@EventHandler
	public void onPlaceTurret(PlayerInteractEvent event){
	if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

	if(event.getBlockFace() != BlockFace.UP) return;
	TurretType type = TurretType.identifyTurret(event.getPlayer().getItemInHand());
	if(type == null) return;
	if(!event.getClickedBlock().getType().isSolid()) return;
	if(event.getClickedBlock().getType().isTransparent()) return;
	if(event.getClickedBlock().getType().toString().endsWith("PLATE") ||
		illegal.contains(event.getClickedBlock().getType())){
		event.setCancelled(true);
		return;
	}
	KingdomPlayer kp = GameManagement.getPlayerManager().getSession(event.getPlayer());
	if(kp.getKingdom() == null){
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Kingdom", kp.getLang()));
		return;
	}
	Kingdom kingdom = kp.getKingdom();
	if(!event.getClickedBlock().getType().toString().endsWith("FENCE") &&
		type != TurretType.MINE_CHEMICAL &&
		type != TurretType.MINE_PRESSURE){
		kp.sendMessage(Kingdoms.getLang().getString("Turrets_Must_Be_On_Fence", kp.getLang()));
		return;
	}

	Block turretBlock = event.getClickedBlock().getRelative(0, 1, 0);
	if(turretBlock.getType() != Material.AIR){
		kp.sendMessage(Kingdoms.getLang().getString("Turrets_Fence_Already_Occupied", kp.getLang()));
		return;
	}

	SimpleLocation loc = new SimpleLocation(turretBlock.getLocation());
	SimpleChunkLocation chunk = loc.toSimpleChunk();

	Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
	if(land.getOwnerUUID() == null || (!land.getOwnerUUID().equals(kingdom.getKingdomUuid()) && !kp.isAdminMode())){
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Land", kp.getLang()));
		return;
	}

	if(isMaxHitInLand(land, type)){
		kp.sendMessage(Kingdoms.getLang().getString("Misc_Turret_Limit", kp.getLang())
			.replaceAll("%type%", type.getTurretDisk().getItemMeta().getDisplayName())
			.replaceAll("%number%", "" + type.getPerLandMaxLimit()));
		return;
	}

	TurretPlaceEvent ev = new TurretPlaceEvent(land, loc.toLocation(), type, kingdom, kp);
	Bukkit.getPluginManager().callEvent(ev);

	if(ev.isCancelled()) return;
	int amount = event.getPlayer().getItemInHand().getAmount();
	if(amount > 1)
		event.getPlayer().getItemInHand().setAmount(amount - 1);
	else
		event.getPlayer().setItemInHand(null);

	Turret turret = new Turret(loc, type);//

	land.addTurret(turret);
	if(type == TurretType.MINE_CHEMICAL){
		turretBlock.setType(Materials.OAK_PRESSURE_PLATE.parseMaterial());
	}
	else if(type == TurretType.MINE_PRESSURE){
		turretBlock.setType(Materials.STONE_PRESSURE_PLATE.parseMaterial());
	}
	else{

//			ItemStack temp = new ItemStack(Material.SKULL_ITEM);
//			SkullMeta meta = (SkullMeta) temp.getItemMeta();
//			meta.setOwner(type.getSkin());
//			temp.setItemMeta(meta);
		turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
		BlockState state = turretBlock.getState();
		//turretBlock.setData((byte) 1);
		MaterialData data = state.getData();
		data.setData((byte) 1);
		state.setData(data);
		state.update();
		Skull s = (Skull) turretBlock.getState();
		s.setOwner(type.getSkin());
		s.update();
	}

	}
//	private int i = 0;
//	/**
//	 * @Deprecated causes severe performance issues despite working
//	 * @param turret
//	 */
//	private void setTurretHeadBlock(Turret turret){
//				Block turretBlock = turret.getLoc().toLocation().getBlock();
//				if(!(turretBlock.getState() instanceof Skull)) return;
//				i++;
//				Skull s = (Skull) turretBlock.getState();
//				if(s.getOwner() != null) return;
//				s.setOwner(turret.getType().getSkin());
//				s.update();
//				Kingdoms.logDebug("SkullUpdate: " + i);
//				Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
//						@Override
//						public void run() {setTurretHeadBlock(turret);}
//				}, 1L);
//		}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
	if(event.getClickedBlock() == null) return;
	if(event.getClickedBlock().getType() != Materials.SKELETON_SKULL.parseMaterial()) return;
	Skull s = (Skull) event.getClickedBlock().getState();
	Kingdoms.logDebug("SkullSkin: " + s.getOwner());
	}

	@EventHandler
	public void onArrowLand(ProjectileHitEvent event){
	if(event.getEntity().hasMetadata(TurretUtil.META_SHOOTER) ||
		event.getEntity().hasMetadata("CONQUESTARROW")){
		Bukkit.getScheduler().runTaskLater(plugin, new BukkitRunnable() {
		@Override
		public void run(){
			event.getEntity().remove();
		}
		}, 1L);
	}
	}

	@EventHandler
	public void onTurretArrowHit(EntityDamageByEntityEvent event){
	if(event.getDamager().hasMetadata(TurretUtil.META_DAMAGE)&&event.getDamager().hasMetadata(TurretUtil.META_SHOOTER)){
		if(TurretUtil.canBeTarget(GameManagement.getKingdomManager()
				.getOrLoadKingdom(event.getDamager()
					.getMetadata(TurretUtil.META_SHOOTER).get(0).asString()),
			event.getEntity())){
		event.setDamage(event.getDamager().getMetadata(TurretUtil.META_DAMAGE).get(0).asDouble());
		}
		else{
		event.setCancelled(true);
		}
	}
	}

	@EventHandler
	public void onTurretFireHit(EntityCombustByEntityEvent event){
	if(event.getCombuster().hasMetadata(TurretUtil.META_DAMAGE)){
		if(!TurretUtil.canBeTarget(GameManagement.getKingdomManager()
				.getOrLoadKingdom(event.getCombuster()
					.getMetadata(TurretUtil.META_SHOOTER).get(0).asString()),
			event.getEntity())){
		event.setCancelled(true);
		}
	}
	}

	@EventHandler
	public void onMineTrigger(PlayerInteractEvent event){
	if(event.getAction() != Action.PHYSICAL)
		return;
	SimpleLocation loc = new SimpleLocation(event.getClickedBlock().getLocation());
	Land land = GameManagement.getLandManager().getOrLoadLand(loc.toSimpleChunk());
	if(land.getOwnerUUID() == null) return;
	Turret turret = land.getTurret(loc);
	if(turret == null) return;
	Kingdom defender = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
	if(!TurretUtil.canBeTarget(defender, event.getPlayer())) return;
	event.setCancelled(true);
	if(!turret.getType().isEnabled()) return;
	if(turret.getType() == TurretType.MINE_CHEMICAL){
		turret.destroy();
		int dur = 100;
		if(defender.getTurretUpgrades().isVirulentPlague()) dur = 200;
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, dur, Config.getConfig().getInt("turret-specs.chemicalmine.poison-potency")));

	}
	else if(turret.getType() == TurretType.MINE_PRESSURE){
		turret.destroy();
		event.getClickedBlock().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), turret.getType().getDamage(), false, false);
		if(defender.getTurretUpgrades().isConcentratedBlast())
		event.getClickedBlock().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) turret.getType().getDamage(), false, false);
	}

	}

	boolean isTurret(Block block){
	SimpleLocation loc = new SimpleLocation(block.getLocation());
	Land land = GameManagement.getLandManager().getOrLoadLand(loc.toSimpleChunk());
	return land.getTurret(loc) != null;
	}

	private boolean isWithinViewDistance(Player p, Chunk c){
	int manhattan = (int) (Bukkit.getViewDistance() * 1.5);
	int mdist = Math.abs(p.getLocation().getChunk().getX() - c.getX()) + Math.abs(p.getLocation().getChunk().getZ() - c.getZ());

	return mdist <= manhattan;
	}

	private boolean canChunkBeUnloaded(Chunk c){
	for(Player i : c.getWorld().getPlayers()){
		if(isWithinViewDistance(i, c)){
		return false;
		}
	}

	return true;
	}

	@EventHandler
	public void onLandLoad(LandLoadEvent e){
	Iterator<Turret> iter = e.getLand().getTurrets().iterator();
	Turret turret = null;
	ArrayList<Turret> remove = new ArrayList();
	while(iter.hasNext()){
		turret = iter.next();
		if(turret == null) continue;

		if(remove != null && remove.size() > 0){
		remove.addAll(initTurret(e.getLand(), turret));
		}
	}
	for(Turret t : remove){
		t.destroy();
	}
	//2016-08-11
	//loadQueue.add(e.getLand());
	}

	private ArrayList<Turret> initTurret(Land land, Turret turret){
	if(land.getTurrets().size() == 0) return null;
	ArrayList<Turret> toBeRemoved = new ArrayList();
	for(Turret t : land.getTurrets()){

		TurretType type = t.getType();
		if(type == null){
		toBeRemoved.add(t);
		continue;
		}
		if(Config.getConfig().getBoolean("destroy-extra-turrets-to-enforce-max"))
		if(isOverHitInLand(land, type)){
			toBeRemoved.add(t);
			continue;
		}
		Block turretBlock = t.getLoc().toLocation().getBlock();
		if(turretBlock.getType().isSolid() &&
			turretBlock.getType() != Materials.SKELETON_SKULL.parseMaterial() &&
			turretBlock.getType() != Materials.OAK_PRESSURE_PLATE.parseMaterial() &&
			turretBlock.getType() != Materials.STONE_PRESSURE_PLATE.parseMaterial()){
		toBeRemoved.add(t);
		Kingdoms.logInfo("A turret at " + t.getLoc().toString() + " is not a skull or a pressure plate! Removing.");
		continue;
		}
		if(type == TurretType.MINE_CHEMICAL){
		turretBlock.setType(Materials.OAK_PRESSURE_PLATE.parseMaterial());
		}
		else if(type == TurretType.MINE_PRESSURE){
		turretBlock.setType(Materials.STONE_PRESSURE_PLATE.parseMaterial());
		}
		else{
		if(turretBlock.getType() != Materials.SKELETON_SKULL.parseMaterial()){
			turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
			//turretBlock.setData((byte) 1);
		}
		}

	}
	return toBeRemoved;
	}

	/**
	 * @param event
	 * @Deprecated now using onLandLoad
	 */
	public void onChunkLoad(ChunkLoadEvent event){

	Bukkit.getScheduler().runTaskLater(plugin, new BukkitRunnable() {
		@Override
		public void run(){

		Land land = GameManagement.getLandManager().getOrLoadLand(new SimpleChunkLocation(event.getChunk()));
		if(land.getTurrets().size() == 0) return;
		for(Turret t : land.getTurrets()){
			TurretType type = t.getType();
			Block turretBlock = t.getLoc().toLocation().getBlock();
			if(type != TurretType.MINE_CHEMICAL &&
				type != TurretType.MINE_PRESSURE){


			if(!(turretBlock.getState() instanceof Skull)){
				turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
				//turretBlock.setData((byte) 1);
			}
			Skull s = (Skull) turretBlock.getState();
			if(s == null) continue;
			if(type.getSkin() == null) continue;
			//s.setOwner(type.getSkin());
			//s.update();

			}
		}

		}
	}, 1L);

	}
	
	/*
	plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
		@Override
		public void run(){
		for(SimpleChunkLocation loc : GameManagement.getLandManager().getAllLandLoc()){
			Land land = GameManagement.getLandManager().getOrLoadLand(loc);
			World world = Bukkit.getWorld(loc.getWorld());
			if(world == null) continue;
			if(!world.isChunkLoaded(loc.getX(), loc.getZ())) continue;
			Chunk c = loc.toChunk();

			if(canChunkBeUnloaded(c)){
			continue;
			}

			if(land.getOwnerUUID() == null)
			continue;
			if(!land.getLoc().toChunk().isLoaded()) continue;
			Iterator<Turret> iter = land.getTurrets().iterator();


			while(iter.hasNext()){
			Turret turret = iter.next();
			if(turret == null){
				iter.remove();
				continue;
			}
			if(turret.getType() == null){
				iter.remove();
				continue;
			}
			if(!turret.getType().isEnabled()) continue;
			if(turret.getType().toString().startsWith("MINE")) continue;
			if(!turret.isValid()) continue;
			Collection<Entity> nearby = getNearbyChunkEntities(c, turret.getType().getRange());
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable() {
				@Override
				public void run(){

				if(turret.aim(nearby)){
					Bukkit.getScheduler().runTask(plugin, new BukkitRunnable() {
					@Override
					public void run(){
						turret.fire();
					}
					});

				}
				}
			});
			}
		}
		}
	}, 0, 5L);
	*/
	
	/*
	public Set<Entity> getChunkEntities(Chunk chunk, int range) {
		Set<Entity> entities = new HashSet<>();
		int radius = 1;
		double newRadius = (double) range / 16D;
		if (newRadius > radius) {
			radius = (int) Math.ceil(newRadius);
			if (radius < 1)
				radius = 1;
		}
		for(int x = -radius; x <= radius; x++){
			for(int z = -radius; z <= radius; z++){
				Chunk c = chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z);
				for(Entity e : c.getEntities()){
					if(e instanceof Player) mobs.add(e);
				}
			}
		}
		return entities;
	}
	*/

	@Override
	public void onDisable() {
		
	}

}
