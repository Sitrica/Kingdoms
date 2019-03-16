package com.songoda.kingdoms.manager.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.NamingException;

import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.database.DatabaseTransferTask;
import com.songoda.kingdoms.database.MySqlDatabase;
import com.songoda.kingdoms.database.SQLiteDatabase;
import com.songoda.kingdoms.main.Config;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.KingdomPlayer;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.KingdomCooldown;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.songoda.kingdoms.constants.conquest.ActiveConquestBattle;
import com.songoda.kingdoms.constants.conquest.ConquestLand;
import com.songoda.kingdoms.constants.conquest.ConquestMap;
import com.songoda.kingdoms.constants.conquest.ConquestTurret;
import com.songoda.kingdoms.constants.conquest.SchematicParser;
import com.songoda.kingdoms.main.Kingdoms;

public class ConquestManager extends Manager {
	
	private final Map<Kingdom, ActiveConquestBattle> kingdomsMissions = new HashMap<>();
	private final Map<String, ConquestMap> maps = new HashMap<>();
	private final WorldManager worldManager;
	
	//TODO recode below.
	private final int highestX = 15;
	private final int highestZ = 15;
	private final Database<ConquestLand> database;
	private Thread autoSaveThread;
	public static World world;

	protected ConquestManager() {
		super(false);
		this.world = Bukkit.getWorld("KingdomsConquest");
		this.worldManager = instance.getManager("world", WorldManager.class);
		if (world == null) {
			this.world = WorldCreator.name("KingdomsConquest")
					.generatorSettings("3;minecraft:air;2")
					.generateStructures(false)
					.type(WorldType.FLAT)
					.createWorld();
		}
		
		
		
		
		
		
		//TODO recode below.
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(ConquestLand.class);
		else
			database = getSQLiteDatabase(ConquestLand.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes"); //Make a configuration for conquests.
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
				@Override 
				public void run() {
					saveTask.run();
				}
			}, 0, IntervalUtils.getInterval(interval) * 20);
		}
		
		
		
		
		
		//TODO recode below.
		database = createFileDB();
		autoSaveThread = new Thread(new AutoSaveTask());
		//2016-08-11
		autoSaveThread.setPriority(Thread.MIN_PRIORITY);
		autoSaveThread.start();
		
		loadSchematics();
		loadAll();
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(instance, new ConquestMapLogisticsTask(this), 0L, 20 * 60 * Config.getConfig().getInt("conquests.time-in-minutes-to-apply-rewards-and-upkeep"));
		
	}
	
	public boolean createNewConquestMap(String name){
		if(!maps.containsKey(name.toLowerCase())){
			ConquestMap map = new ConquestMap(name.toLowerCase());
			maps.put(name.toLowerCase(), map);
//			for(ConquestLand land:map.lands){
//				lands.put(land.getDataID(), land);
//			}
			return true;
		}
		return false;
	}
	
	public boolean deleteConquestMap(String name){
		if(maps.containsKey(name.toLowerCase())){
			ArrayList<Kingdom> delete = new ArrayList<Kingdom>();
			for(Kingdom kingdom:kingdomsMissions.keySet()){
				ActiveConquestBattle battle = kingdomsMissions.get(kingdom);
				if(battle.land.map.equals(name.toLowerCase())){
					battle.stopInvasionServerStop();
					delete.add(kingdom);
				}
			}
			for(Kingdom k:delete){
				kingdomsMissions.remove(k);
			}

//			for(ConquestLand land:maps.get(name.toLowerCase()).lands){
//				lands.remove(land.getDataID());
//			}
			maps.remove(name.toLowerCase());
			return true;
		}
		return false;
	}
	
	public static ActiveConquestBattle createNewArena(ConquestLand land, Kingdom invader){		
		int nextChunk = 5;
		
		ActiveConquestBattle c = new ActiveConquestBattle(ConquestManager.world.getChunkAt(highestX + nextChunk, highestZ + nextChunk), land);
		c.getMiddle().load();
		try {
				if(land.getWalllevel() == 0){
				SchematicParser.pasteSchematic(c.getMiddle().getWorld(),
						"nowall.schematic", 
						c.getMiddle().getBlock(0,100,0).getLocation());
				}else if(land.getWalllevel() == 1){
					SchematicParser.pasteSchematic(c.getMiddle().getWorld(), 
							"wall1.schematic", 
							c.getMiddle().getBlock(0,100,0).getLocation());
				}else if(land.getWalllevel() == 2){
					SchematicParser.pasteSchematic(c.getMiddle().getWorld(), 
							"wall2.schematic", 
							c.getMiddle().getBlock(0,100,0).getLocation());
				}else if(land.getWalllevel() == 3){
					SchematicParser.pasteSchematic(c.getMiddle().getWorld(), 
							"wall3.schematic", 
							c.getMiddle().getBlock(0,100,0).getLocation());
				}
				
				Chunk tchunk = c.getMiddle().getWorld().getChunkAt(c.getMiddle().getX(),c.getMiddle().getZ()+1);
				c.turrets.put(1, new ConquestTurret(c, tchunk.getBlock(12, 102, 0).getLocation(), land.getTurretLevelAtSlot(1)));
				c.turrets.put(2, new ConquestTurret(c, tchunk.getBlock(9, 102, 0).getLocation(), land.getTurretLevelAtSlot(2)));
				c.turrets.put(3, new ConquestTurret(c, tchunk.getBlock(6, 102, 0).getLocation(), land.getTurretLevelAtSlot(3)));
				c.turrets.put(4, new ConquestTurret(c, tchunk.getBlock(3, 102, 0).getLocation(), land.getTurretLevelAtSlot(4)));
				
				if(land.getTurretLevelAtSlot(1) > 0){
					Block turretBlock = tchunk.getBlock(12, 102, 0);
					turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
					//turretBlock.setData((byte) 1);
					Skull s = (Skull) turretBlock.getState();
					s.setSkullType(SkullType.SKELETON);
					s.setRotation(BlockFace.SOUTH);
					s.update();
				}
				if(land.getTurretLevelAtSlot(2) > 0){
					Block turretBlock = tchunk.getBlock(9, 102, 0);
					turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
					//turretBlock.setData((byte) 1);
					Skull s = (Skull) turretBlock.getState();
					s.setSkullType(SkullType.SKELETON);
					s.setRotation(BlockFace.SOUTH);
					s.update();
				}
				if(land.getTurretLevelAtSlot(3) > 0){
					Block turretBlock = tchunk.getBlock(6, 102, 0);
					turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
					//turretBlock.setData((byte) 1);
					Skull s = (Skull) turretBlock.getState();
					s.setSkullType(SkullType.SKELETON);
					s.setRotation(BlockFace.SOUTH);
					s.update();
				}
				if(land.getTurretLevelAtSlot(4) > 0){
					Block turretBlock = tchunk.getBlock(3, 102, 0);
					turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
					//turretBlock.setData((byte) 1);
					Skull s = (Skull) turretBlock.getState();
					s.setSkullType(SkullType.SKELETON);
					s.setRotation(BlockFace.SOUTH);
					s.update();
				}	
				
				
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		highestX = c.getMiddle().getX();
		highestZ = c.getMiddle().getZ();
		return c;
		
	}
	
	public static boolean startOffensive(KingdomPlayer first, Kingdom invader, ConquestLand land){
		
		if(land.isUnderSiege) return false;
		land.isUnderSiege = true;
		ActiveConquestBattle battle = createNewArena(land, invader);
		battle.invadingKingdom = invader;
		kingdomsMissions.put(invader, battle);
		KingdomCooldown cooldown = new KingdomCooldown(invader.getKingdomName(), "attackcd", 60*Config.getConfig().getInt("conquests.attack-cooldown-in-minutes"));
		cooldown.start();
		joinOffensive(first);
		return true;
	}
	
	
	public static HashMap<UUID, Location> locations = new HashMap<UUID, Location>();
	public static HashMap<UUID, Double> hp = new HashMap<UUID, Double>();
	public static HashMap<UUID, Integer> food = new HashMap<UUID, Integer>();
	
	public static void storePreGameInfo(Player p){
		p.setGameMode(GameMode.SURVIVAL);
		locations.put(p.getUniqueId(), p.getLocation());
		hp.put(p.getUniqueId(), p.getHealth());
		food.put(p.getUniqueId(), p.getFoodLevel());
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		//p.getInventory().clear();
		//p.getInventory().setArmorContents(null);
	}

	public static void restorePreGameInfo(Player p){
		p.setHealth(hp.get(p.getUniqueId()));
		p.setFoodLevel(food.get(p.getUniqueId()));
		p.teleport(locations.get(p.getUniqueId()));
	}
	
	//public static HashMap<UUID, Location> lastLocs = new HashMap<UUID, Location>();
	public static void joinOffensive(KingdomPlayer kp){
		Kingdom k = kp.getKingdom();
		ActiveConquestBattle battle = kingdomsMissions.get(k);
		battle.invaders.add(kp);
		storePreGameInfo(kp.getPlayer());
		Chunk invaderchunk = ConquestManager.world.getChunkAt(battle.getMiddle().getX(), battle.getMiddle().getZ() + 2);
		Location invaderLoc = invaderchunk.getBlock(8, 101, 8).getLocation();
		invaderLoc.setYaw(180);
		kp.getPlayer().setGameMode(GameMode.SURVIVAL);
		kp.getPlayer().setAllowFlight(false);
		kp.getPlayer().teleport(invaderLoc);
		
	}
	
	public static void leaveOffensive(KingdomPlayer kp){
		Kingdom k = kp.getKingdom();
		if(k == null) return;
		ActiveConquestBattle battle = kingdomsMissions.get(k);
		if(battle == null) return;
		if(battle.invaders.contains(kp)){
			restorePreGameInfo(kp.getPlayer());
			battle.invaders.remove(kp);
		}
		if(battle.invaders.size() == 0){
			
			battle.concludeDefeat();
		}
	}
		
	@Override
	public void onDisable() {
		for(Kingdom kingdom:kingdomsMissions.keySet()){
			kingdomsMissions.get(kingdom).stopInvasionServerStop();
			
		}
		kingdomsMissions.clear();

		// 2016-08-11
		autoSaveThread.interrupt();
		// 2016-08-22
		try {
			autoSaveThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Kingdoms.logInfo("Saving loaded conquest lands and maps...");
		try{
			saveAll();
			Kingdoms.logInfo("Done!");
		}catch(Exception e){
			Kingdoms.logInfo("SQL connection failed! Saving to file DB");
			db = createFileDB();
			saveAll();
			Config.getConfig().set("DO-NOT-TOUCH.grabConquestMapsFromFileDB",true);
		}
		maps.clear();
	
	}

	
	private void loadSchematics(){
		ArrayList<String> schematics = new ArrayList<String>(){{
			
			add("nowall.schematic");
			add("wall1.schematic");
			add("wall2.schematic");
			add("wall3.schematic");
			
		}};
		for(String name:schematics){
			File schematic = new File(name);
			if(!schematic.exists()){
				InputStream ddlStream = Kingdoms.class
					    .getClassLoader().getResourceAsStream(name);
				try{
					@SuppressWarnings("resource")
					FileOutputStream fos = new FileOutputStream(name);
					    byte[] buf = new byte[2048];
					    int r;
					    while(-1 != (r = ddlStream.read(buf))) {
					        fos.write(buf, 0, r);
					    }
				}catch(IOException e){
					
				}
			}
		}
	}
	
	private class AutoSaveTask implements Runnable {

		@Override
		public void run() {
			while (plugin.isEnabled()) {
				try {
					Thread.sleep(5 * 1000L);
				} catch (InterruptedException e) {
					Kingdoms.logInfo("Kingdom auto save is interrupted.");
					
					//2016-08-22
					return;
				}

				saveAll();
			}
		}
	}	
	
	private class ConquestMapLogisticsTask implements Runnable {
		ConquestManager manager;
		public ConquestMapLogisticsTask(ConquestManager manager) {
			super();
			this.manager = manager;
		}
			
		@Override
		public void run() {
			//Kingdoms.logDebug("ConquestLogistics Loop Initiate");
			for(ConquestMap map:maps.values()){	
				for(ConquestLand land:map.lands){
					if(land.getOwner() == null)continue;
					int income = Config.getConfig().getInt("conquests.rp-rewards-per-hour.perland");
					if(land.isCapital()) income = Config.getConfig().getInt("conquests.rp-rewards-per-hour.percapital");
					int upkeep = land.getUpKeepAmount();
					Kingdom kingdom = Kingdoms.getManagers().getKingdomManager().getOrLoadKingdom(land.getOwner());
					income -= upkeep;
					//Kingdoms.logDebug(income + " + " + land.getDataID());
					if(land.isEncircled() && upkeep > 0){
						income = 0;
						land.setSupplylevel(land.getSupplylevel() - upkeep);
						kingdom.sendAnnouncement(null, Kingdoms.getLang().getString("Conquests_Land_Is_Encircled").replaceAll("%land%", land.getDataID()), true);
					}
					if(kingdom.getResourcepoints() + income > 0){
						kingdom.setResourcepoints(kingdom.getResourcepoints() + income);
					}else if(!land.isEncircled()){
						land.setSupplylevel(land.getSupplylevel() - upkeep);
						kingdom.sendAnnouncement(null, Kingdoms.getLang().getString("Conquests_Land_Is_UnderSupplied").replaceAll("%land%", land.getDataID()), true);
					}
				}
			}
		}
	}
	
	// 2016-05-18
	private synchronized void saveAll() {
		synchronized (maps) {
			for (ConquestMap map : maps.values()) {
				for (ConquestLand land: map.lands) {
					Kingdoms.logColor("Saving conquest land: " + land.getDataID());
					
					try{
						db.save(land.getDataID(), land);
					}catch(Exception e){
						if(Config.getConfig().getBoolean("Plugin.Debug")){
							e.printStackTrace();
						}
						Bukkit.getLogger().severe("[Kingdoms] Failed autosave for a conquest map!");
					}
				}
			}
		}
	}

	private synchronized void loadAll(){
		HashMap<ConquestLand, String> landToMap = new HashMap<ConquestLand, String>();
		ArrayList<String> conquestMapNames = new ArrayList<String>();
		if(db == null)return;
		for(String s:db.getKeys()){
			Kingdoms.logColor("Loading conquest land: " + s);
			ConquestLand land = db.load(s, null);
			if(land == null){
				Kingdoms.logInfo("Error: Conquestland, " + s + " could not be loaded!");
				continue;
			}
			if(land.map == null){
				Kingdoms.logInfo("Error: Conquestland, " + s + " could not be loaded!");
				continue;
			}
			if(!conquestMapNames.contains(land.map))conquestMapNames.add(land.map);
			landToMap.put(land, land.map);
		}
		
		for(String name:conquestMapNames){
			ArrayList<ConquestLand> lands = new ArrayList<ConquestLand>();
			for(ConquestLand land:landToMap.keySet()){
				land.isUnderSiege = false;
				if(land.map.equalsIgnoreCase(name)) lands.add(land);
				
			}
			maps.put(name, new ConquestMap(name, lands));
			
		}
	
	
	}
	
	public SQLiteDatabase<ConquestLand> createFileDB() {
		return new SQLiteDatabase<>(plugin.getDataFolder(),"db.db", "conquestmaps", ConquestLand.class);
	}

	public MySqlDatabase<ConquestLand> createMysqlDB() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, NamingException {
		return new MySqlDatabase<>(
				Config.getConfig().getString("MySql.DBAddr"),
				Config.getConfig().getString("MySql.DBName"),
				"conquestmaps",
				Config.getConfig().getString("MySql.DBUser"),
				Config.getConfig().getString("MySql.DBPassword"),
				Kingdom.class);
	}

	public DatabaseTransferTask.TransferPair<ConquestLand> getTransferPair(Database<ConquestLand> from) {
		return new DatabaseTransferTask.TransferPair<ConquestLand>(from, db);
	}

}
