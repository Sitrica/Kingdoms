package com.songoda.kingdoms.manager.managers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.NamingException;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.constants.StructureType;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.database.DatabaseTransferTask;
import com.songoda.kingdoms.database.DatabaseTransferTask.TransferPair;
import com.songoda.kingdoms.database.MySQLDatabase;
import com.songoda.kingdoms.database.SQLiteDatabase;
import com.songoda.kingdoms.database.YamlDatabase;
import com.songoda.kingdoms.manager.external.ExternalManager;
import com.songoda.kingdoms.manager.game.ConquestManager;
import com.songoda.kingdoms.manager.game.GameManagement;
import com.songoda.kingdoms.manager.gui.GUIManagement;
import com.songoda.kingdoms.objects.KingdomPlayer;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.SimpleChunkLocation;
import com.songoda.kingdoms.objects.land.SimpleLocation;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.events.LandClaimEvent;
import com.songoda.kingdoms.events.LandLoadEvent;
import com.songoda.kingdoms.events.LandUnclaimEvent;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.ManagerHandler;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class LandManager extends Manager {
	
	static {
		registerManager("land", new LandManager());
	}
	
	private final Map<Chunk, Land> lands = new ConcurrentHashMap<>();
	private final List<String> unclaiming = new ArrayList<>(); //TODO test if this is even requried. It's a queue to avoid claiming and removing at same time.
	private final FileConfiguration configuration;
	private final WorldManager worldManager;
	private static Database<Land> database;
	private BukkitTask autoSaveThread;
	private Kingdoms instance;

	public LandManager() {
		super(true);
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(Land.class);
		else
			database = getSQLiteDatabase(Land.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
				@Override 
				public void run() {
					Kingdoms.debugMessage("Saved [" + save() + "] lands");
				}
			}, 0, IntervalUtils.getInterval(interval) * 20);
		}
		initLands();
		if (configuration.getBoolean("taxes.enabled")) {
			String timeString = configuration.getString("taxes.interval", "2 hours");
			long time = IntervalUtils.getInterval(timeString) * 20;
			int amount = configuration.getInt("taxes.amount", 10);
			Bukkit.getScheduler().scheduleAsyncRepeatingTask(instance, new Runnable() {
				@Override
				public void run() {
					Kingdoms.debugMessage("Land taxes executing...");
					new MessageBuilder("taxes.take")
							.toPlayers(Bukkit.getOnlinePlayers())
							.replace("%interval%", timeString)
							.replace("%amount%", amount)
							.send();
					Map<Chunk, Land> lands = Collections.unmodifiableMap(lands);
					boolean disband = configuration.getBoolean("taxes.disband-cant-afford", false);
					for (Land land : lands.values()) {
						if (land.getOwnerUUID() != null) {
							Kingdom kingdom = Kingdoms.getManagers().getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
							if (kingdom == null)
								return;
							if (kingdom.getResourcepoints() < amount && disband) {
								new MessageBuilder("taxes.disband")
										.toPlayers(Bukkit.getOnlinePlayers())
										.replace("%kingdom%", kingdom.getKingdomName())
										.replace("%amount%", amount)
										.send();
								GameManagement.getKingdomManager().deleteKingdom(kingdom.getKingdomName());
								return;
							}
							kingdom.setResourcepoints(kingdom.getResourcepoints() - amount);
							if (configuration.getBoolean("taxes.reverse", false))
								kingdom.setResourcepoints(kingdom.getResourcepoints() + (amount * 2));
						}
					}
				}
			}, time, time);
		}
	}

	private synchronized Land databaseLoad(String name, Land land) {
		return database.load(name, land);
	}

	//TODO why does this exist.
	private HashMap<Chunk, Land> toLoad = new HashMap<>();

	private void initLands() {
		Set<String> keys = database.getKeys();
		for (String name : keys) {
			// Old data
			if (name.equals("LandData") || name.endsWith("_temp"))
				continue;
			Kingdoms.debugMessage("Loading land: " + name);
			try{
				Land land = databaseLoad(name, null);
				Chunk chunk = LocationUtils.stringToChunk(name);
				if (chunk == null) {
					if (!toLoad.containsKey(chunk))
						toLoad.put(chunk, land);
					continue;
				}
				//the land has owner but the owner kingdom doesn't exist
				if (land.getOwnerUUID() != null) {
					Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
					if (kingdom == null) {
						Kingdoms.consoleMessage("Land data [" + name + "] is corrupted! ignoring...");
						Kingdoms.consoleMessage("The land owner is [" + land.getOwnerName() + "] but no such kingdom with the name exists");
					}
				}
				LandLoadEvent event = new LandLoadEvent(land);
				Bukkit.getPluginManager().callEvent(new LandLoadEvent(land));
				if (!!event.isCancelled() && !lands.containsKey(chunk))
					lands.put(chunk, land);
			} catch(Exception e) {
				Kingdoms.consoleMessage("Land data [" + name + "] is corrupted! ignoring...");
				if (instance.getConfig().getBoolean("debug", true))
					e.printStackTrace();
			}
		}
		database.save("LandData", null);
		Kingdoms.consoleMessage("Total of [" + getLoadedLand().size() + "] lands are initialized");
	}

	private synchronized int save() {
		Kingdoms.debugMessage("Starting Land Save");
		int i = 0;
		Kingdoms.debugMessage("Starting Land Save");
		Set<String> saved = new HashSet<>();
		for (Chunk chunk : getLoadedLand()) {
			String name = LocationUtils.chunkToString(chunk);
			if (saved.contains(name))
				continue;
			Kingdoms.debugMessage("Saving land: " + name);
			Land land = getOrLoadLand(chunk);
			if (land.getOwnerUUID() == null && land.getTurrets().size() <= 0 && land.getStructure() == null) {
				database.save(name, null);
				saved.add(name);
				i++;
				continue;
			}
			if (land.getOwnerUUID() != null && Kingdoms.getManagers().getKingdomManager().getOrLoadKingdom(land.getOwnerUUID()) == null) {
				database.save(name, null);
				saved.add(name);
				i++;
				continue;
			}
			try{
				database.save(name, land);
				saved.add(name);
				i++;
			} catch(Exception e) {
				Bukkit.getLogger().severe("[Kingdoms] Failed autosave for land at: " + name);
			}
		}
		return i;
	}

	/**
	 * @return Set<Chunk> of all loaded land locations.
	 */
	public Set<Chunk> getLoadedLand() {
		return Collections.unmodifiableMap(lands).keySet();
	}

	/**
	 * Load land if exist; create if not exist.
	 * 
	 * @param loc Chunk of land to get from.
	 * @return Land even if not loaded.
	 */
	public Land getOrLoadLand(Chunk chunk) {
		if (chunk == null)
			return null;
		String name = LocationUtils.chunkToString(chunk);
		Kingdoms.debugMessage("Fetching info for land: " + name);
		Land land = lands.get(chunk);
		//new land so create empty one
		if (land == null) {
			land = new Land(chunk);
			if (!lands.containsKey(chunk))
				lands.put(chunk, land);
		}
		return land;
	}

	/**
	 * Claim a new land. This does not check if chunk is already occupied.
	 * 
	 * @param chunk Chunk location
	 * @param kingdom Kingdom owner
	 */
	public void claimLand(Kingdom kingdom, Chunk... chunks) {
		for (Chunk chunk : chunks) {
			Land land = getOrLoadLand(chunk);
			LandClaimEvent event = new LandClaimEvent(land, kingdom);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				continue;
			land.setClaimTime(new Date().getTime());
			land.setOwnerUUID(kingdom.getKingdomUuid());
			String name = LocationUtils.chunkToString(land.getChunk());
			database.save(name, land);
			//Dynmap object here
			if (GameManagement.getDynmapManager() != null)
				GameManagement.getDynmapManager().updateClaimMarker(chunk);
		}
	}

	/**
	 * Unclaim the land. This does not check if chunk is occupied.
	 * 
	 * @param chunk Chunk to unclaim
	 * @param kingdom Kingdom whom is unclaiming.
	 */
	public void unclaimLand(Kingdom kingdom, Chunk... chunks) {
		for (Chunk chunk : chunks) {
			Land land = getOrLoadLand(chunk);
			if (land.getOwnerUUID() == null) {
				continue;
			}
			LandUnclaimEvent event = new LandUnclaimEvent(land, kingdom);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				continue;
			land.setClaimTime(0L);
			land.setOwnerUUID(null);
			String name = LocationUtils.chunkToString(land.getChunk());
			database.save(name, null);
			if (land.getStructure() != null) {
				Bukkit.getScheduler().runTask(instance, new Runnable() {
					@Override
					public void run() {
						GameManagement.getStructureManager().breakStructure(land);
					}
				});
			}
			//Dynmap object here
			if (GameManagement.getDynmapManager() != null)
				GameManagement.getDynmapManager().updateClaimMarker(chunk);
		}
	}

	/**
	 * Unclaims ALL existing land in database
	 * Use at own risk.
	 */
	public void unclaimAllExistingLand() {
		for (OfflineKingdom kingdom : GameManagement.getKingdomManager().getKingdomList().values()) {
			unclaimAllLand(GameManagement.getKingdomManager().getOrLoadKingdom(kingdom.getKingdomName()));
		}
	}
	
	/**
	 * Unclaim all lands thatbelong to kingdom.
	 * 
	 * @param kingdom Kingdom owner
	 * @return number of lands unclaimed
	 */
	public int unclaimAllLand(Kingdom kingdom) {
		String name = kingdom.getKingdomName();
		if (unclaiming.contains(name))
			return -1;
		unclaiming.add(name);
		Stream<Land> stream = getLoadedLand().parallelStream()
				.map(chunk -> getOrLoadLand(chunk))
				.filter(land -> land.getOwnerUUID() != null)
				.filter(land -> land.getOwnerUUID().equals(kingdom.getKingdomUuid()));
		long count = stream.count();
		stream.forEach(land -> unclaimLand(kingdom, land.getChunk()));
		unclaiming.remove(name);
		return (int)count;
	}
	
	public boolean isConnectedToNexus(Land land) {
		if (land.getOwnerUUID() == null)
			return false;
		Kingdom kingdom = Kingdoms.getManagers().getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
		if (kingdom == null)
			return false;
		Location nexusLocation = kingdom.getNexusLocation();
		if (nexusLocation == null)
			return false;
		Land nexus = getOrLoadLand(nexusLocation.getChunk());
		return getAllConnectingLand(nexus).contains(land);
	}

	/**
	 * Unclaim all lands not connected to the kingdom, and with no structures. TODO It checks for only structures though...
	 * 
	 * @param kingdom Kingdom owner
	 * @return number of lands unclaimed
	 */
	public int unclaimDisconnectedLand(Kingdom kingdom) {
		String name = kingdom.getName();
		if (unclaiming.contains(name))
			return -1;
		unclaiming.add(name);
		Set<Land> connected = new HashSet<>();
		getLoadedLand().parallelStream()
				.map(chunk -> getOrLoadLand(chunk))
				.filter(land -> land.getStructure() != null)
				.filter(land -> land.getOwnerUUID() != null)
				.filter(land -> land.getOwnerUUID().equals(kingdom.getUniqueId()))
				.forEach(land -> connected.addAll(getAllConnectingLand(land)));
		Stream<Land> stream = getLoadedLand().parallelStream()
				.map(chunk -> getOrLoadLand(chunk))
				.filter(land -> land.getOwnerUUID() != null)
				.filter(land -> land.getOwnerUUID().equals(kingdom.getUniqueId()))
				.filter(land -> !connected.contains(land));
		long count = stream.count();
		stream.forEach(land -> unclaimLand(kingdom, land.getChunk()));
		unclaiming.remove(name);
		return (int)count;
	}
	
	public Set<Land> getConnectingLand(Land center, Collection<Land> checked) {
		return center.getSurrounding().parallelStream()
				.filter(land -> land.getOwnerUUID() != null)
				.filter(land -> land.getOwnerUUID().equals(center.getOwnerUUID()))
				.collect(Collectors.toSet());
	}
	
	public Set<Land> getOutwardLands(Collection<Land> surroundings, Collection<Land> checked) {
		Set<Land> connected = new HashSet<>();
		for (Land land : surroundings) {
			for (Land furtherSurroundings : land.getSurrounding()) {
				if (surroundings.contains(furtherSurroundings))
					continue;
				if (checked.contains(furtherSurroundings))
					continue;
				if (connected.contains(furtherSurroundings))
					continue;
				connected.add(furtherSurroundings);
			}
		}
		return connected;
	}

	public Set<Land> getAllConnectingLand(Land center) {
		Set<Land> connected = new HashSet<>();
		Set<Land> checked = new HashSet<>();
		connected.add(center);
		Set<Land> outwards = getOutwardLands(getConnectingLand(center, checked), checked);
		boolean check = true;
		while (check) {
			check = false;
			Set<Land> newOutwards = new HashSet<>();
			for (Land land : outwards) {
				Kingdoms.debugMessage("Checking Claim: " + LocationUtils.chunkToString(land.getChunk()));
				if (checked.contains(land))
					continue;
				checked.add(land);
				if (land.getOwnerUUID() == null)
					continue;
				if (!land.getOwnerUUID().equals(center.getOwnerUUID()))
					continue;
				connected.add(land);
				newOutwards.add(land);
				check = true;
			}
			outwards = getOutwardLands(newOutwards, checked);
		}
		return connected;
	}
	
	public Optional<Land> getLandAt(Chunk chunk) {
		return Optional.ofNullable(lands.get(chunk));
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		for (Land land : toLoad.values()) {
			//the land has owner but the owner kingdom doesn't exist
			if (land.getOwnerUUID() != null) {
				Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
				if (kingdom == null) {
					String name = LocationUtils.chunkToString(land.getChunk());
					Kingdoms.consoleMessage("Land data [" + name + "] is corrupted! ignoring...");
					Kingdoms.consoleMessage("The land owner is [" + land.getOwnerName() + "] but no such kingdom with the name exists");
				}
			}
			if (!lands.containsKey(land.getChunk()))
				lands.put(land.getChunk(), land);
			Bukkit.getPluginManager().callEvent(new LandLoadEvent(land));
			WarpPadManager warpPadManager = instance.getManager("warp-pad", WarpPadManager.class).orElseCreate();
			warpPadManager.checkLoad(land);
			toLoad.remove(land);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		if (ExternalManager.isCitizen(player))
			return;
		KingdomPlayer kingdomPlayer = GameManagement.getPlayerManager().getSession(player);
		if (kingdomPlayer.isKMapOn()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Kingdoms.getInstance(), new Runnable() {
				public void run() {
					GUIManagement.getMapManager().displayMap(player, false);
				}
			}, 1L);
		}
		if (kingdomPlayer.isKAutoClaimOn()){
			Bukkit.getScheduler().scheduleSyncDelayedTask(Kingdoms.getInstance(), new Runnable() {
				public void run() {
					attemptNormalLandClaim(kingdomPlayer);
				}
			}, 1L);
		}
	}
	
	public void attemptNormalLandClaim(KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld().getName())) {
			new MessageBuilder("claiming.world-disabled")
					.replace("%player%", player.getName())
					.replace("%player%", player.getName())
					.send(player);
			return;
		}
		
		Kingdom kingdom = kp.getKingdom();
		if(kingdom == null){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_In_Kingdom", kp.getLang()));
			return;
		}
		
		if(!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getClaim())){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getClaim().toString()));
			return;
		}
		
		if (ExternalManager.cannotClaimInRegion(kp.getPlayer().getLocation())) {
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Worldguard_Claim_Off_Limits", kp.getLang()));
			return;
		}
		
		Land land = GameManagement.getLandManager().getOrLoadLand(kp.getLoc());
		if(land.getOwnerUUID() != null){
			if(land.getOwnerUUID().equals(kingdom.getKingdomUuid())){
				kp.sendMessage(Kingdoms.getLang().getString("Command_Claim_Land_Owned_Error", kp.getLang()));
				return;
			}
			
			kp.sendMessage(Kingdoms.getLang().getString("Command_Claim_Land_Occupied_Error", kp.getLang()).replaceAll("%kingdom%", land.getOwner()));
			kp.sendMessage("You may conquer this land by invading. (/k invade)");
			return;
		}
		
		//land amount < land-per-member * kingdomMemberNumb
		//land amount < maximum-land-claims , maximum-land-claims > 0
		if((kingdom.getLand() >= (Config.getConfig().getInt("land-per-member")*kingdom.getMembersList().size() + kingdom.getExtraLandClaims()))){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Members_Needed", kp.getLang()).replaceAll("%amount%", Config.getConfig().getInt("land-per-member")*kingdom.getMembersList().size() + "").replaceAll("%members%", kingdom.getMembersList().size() + ""));
			return;
		}
		
		if(Config.getConfig().getInt("maximum-land-claims") > 0 && (kingdom.getLand() >= Config.getConfig().getInt("maximum-land-claims"))){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Max_Land_Reached", kp.getLang()));
			return;
		}
		
		if(kingdom.getLand() <= 0){//check if first land
			kp.sendMessage(Kingdoms.getLang().getString("Command_Claim_FirstTime1", kp.getLang()).replaceAll("%kingdom%", kingdom.getKingdomName()));
			kp.sendMessage(Kingdoms.getLang().getString("Command_Claim_FirstTime2", kp.getLang()));
			
			if(kingdom.getHome_loc() == null){
				kingdom.setHome_loc(kp.getPlayer().getLocation());
				kp.sendMessage(Kingdoms.getLang().getString("Command_Sethome_Success", kp.getLang()).replaceAll("%coords%", new SimpleLocation(kingdom.getHome_loc()).toString()));
			}
		
		}else{
			if(Config.getConfig().getBoolean("land-must-be-connected")){
				boolean conn = false;
				Chunk main = kp.getPlayer().getLocation().getChunk();
				World w = kp.getPlayer().getWorld();
				for(int x = -1; x <= 1; x++){
					for(int z = -1; z <= 1; z++){
						if(x == 0 && z == 0) continue;
						Chunk c = w.getChunkAt(main.getX() + x, main.getZ() + z);
						Land adj = Kingdoms.getManagers().getLandManager().getOrLoadLand(new SimpleChunkLocation(c));
						if(adj.getOwnerUUID() != null){
							if(adj.getOwnerUUID().equals(kingdom.getKingdomUuid())){
								conn = true;
								break;
							}
						}
					}
				}
				if(!conn){
					kp.sendMessage(Kingdoms.getLang().getString("Misc_Must_Be_Connected", kp.getLang()));
					return;
				}
			}
			
			int cost = Config.getConfig().getInt("claim-cost");
			if(!kp.isAdminMode() && kingdom.getResourcepoints() < cost){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_Enough_Points", kp.getLang()).replaceAll("%cost%", "" + cost));
				return;
			}
			if(!kp.isAdminMode()){
				
				kingdom.setResourcepoints(kingdom.getResourcepoints() - Config.getConfig().getInt("claim-cost"));
				kp.sendMessage(Kingdoms.getLang().getString("Command_Claim_Success", kp.getLang()).replaceAll("%cost%", "" + Config.getConfig().getInt("claim-cost")));
			}else{
				kp.sendMessage(Kingdoms.getLang().getString("Command_Claim_Op", kp.getLang()));
			}
		}

		GameManagement.getLandManager().claimLand(kp.getLoc(), kingdom);
		GameManagement.getVisualManager().visualizeLand(kp, land.getLoc());
	}
	
	private ArrayList<String> forbidden = new ArrayList<String>(){{
		add(Material.DROPPER.toString());
		add(Material.DISPENSER.toString());
		add(Material.HOPPER.toString());
		add(Material.TRAPPED_CHEST.toString());
		add(Material.CHEST.toString());
		add(Material.FURNACE.toString());
		add("DOOR");
		add(Material.LEVER.toString());
		add(Materials.OAK_BUTTON.parseMaterial().toString());
		add(Material.STONE_BUTTON.toString());
		add(Material.ANVIL.toString());
		add(Materials.CRAFTING_TABLE.parseMaterial().toString());
		add(Materials.ENCHANTING_TABLE.parseMaterial().toString());
		add(Materials.FURNACE.parseMaterial().toString());
		add("SHULKER_BOX");
		add(Material.DROPPER.toString());

	}};
	
	private boolean isInForbiddenList(Material mat){
		if(forbidden.contains(mat.toString())) return true;
		for(String s:forbidden){
			if(mat.toString().endsWith(s)){
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteractOnOtherKingdom(PlayerInteractEvent e){
		if(e.isCancelled()) return;
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		if(e.getClickedBlock() == null) return;

		if(Config.getConfig().getBoolean("can-open-storage-blocks-in-other-kingdom-land")) return;
		
		if(e.getPlayer().isSneaking()){
			if(!isInForbiddenList(e.getClickedBlock().getType())){
				if(e.getPlayer().getItemInHand() == null) return;
				if(e.getPlayer().getItemInHand().getType() != Material.ARMOR_STAND
						&& e.getPlayer().getItemInHand().getType() != Material.ITEM_FRAME){
					return;
				}
			}
		}


		Location bukkitLoc = e.getClickedBlock().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null) return;

		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.isAdminMode()) return;
		if(kp.getKingdom() == null){//not in kingdom
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

			e.setCancelled(true);
		}else{//in kingdom
			Kingdom kingdom = kp.getKingdom();
			if(!kingdom.getKingdomUuid().equals(land.getOwnerUUID())){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

				e.setCancelled(true);
				return;
			}
			if(land.getStructure() != null &&
					land.getStructure().getType() == StructureType.NEXUS &&
					!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getBuildInNexus())){
				e.setCancelled(true);
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low_NexusBuild", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getBuildInNexus().toString()));
				return;
			}
		}
	}

	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent e){
		SimpleLocation loc = new SimpleLocation(e.getRightClicked().getLocation());
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null) return;
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.isAdminMode()) return;
		if(kp.getKingdom() == null){//not in kingdom
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

			e.setCancelled(true);
		}else{//in kingdom
			Kingdom kingdom = kp.getKingdom();

			if(!kingdom.getKingdomUuid().equals(land.getOwnerUUID())){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

				e.setCancelled(true);
				return;
			}

			if(land.getStructure() != null &&
					land.getStructure().getType() == StructureType.NEXUS &&
					!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getBuildInNexus())){
				e.setCancelled(true);
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low_NexusBuild", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getBuildInNexus().toString()));
				return;
			}

		}
	}

	@EventHandler
	public void onSpecialLandExplode(EntityExplodeEvent e){
		for(Iterator<Block> iter = e.blockList().iterator(); iter.hasNext();){
			Block block = iter.next();
			if(block == null || block.getType() == Material.AIR) continue;

			SimpleLocation loc = new SimpleLocation(block.getLocation());
			SimpleChunkLocation chunk = loc.toSimpleChunk();

			Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
			if(land.getOwnerUUID() == null) continue;

		}


	}

	@EventHandler
	public void onBucketEmptyOnUnoccupiedLand(PlayerBucketEmptyEvent event) {
		if(!Config.getConfig().getStringList("enabled-worlds").contains(event.getBlockClicked().getWorld().getName())) return;
		if(!Config.getConfig().getStringList("worlds-with-no-building-in-unoccupied-land").contains(event.getBlockClicked().getWorld().getName())) return;Location bukkitLoc = event.getBlockClicked().getRelative(event.getBlockFace()).getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(event.getPlayer());
		if(land.getOwnerUUID() == null && !kp.isAdminMode()){
			event.setCancelled(true);
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Build_In_Unoccupied_Land", kp.getLang()));
		}
	}

	@EventHandler
	public void onBucketFillOnUnoccupiedLand(PlayerBucketFillEvent event) {
		if(!Config.getConfig().getStringList("enabled-worlds").contains(event.getBlockClicked().getWorld().getName())) return;
		if(!Config.getConfig().getStringList("worlds-with-no-building-in-unoccupied-land").contains(event.getBlockClicked().getWorld().getName())) return;
		Location bukkitLoc = event.getBlockClicked().getRelative(event.getBlockFace()).getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(event.getPlayer());
		if(land.getOwnerUUID() == null && !kp.isAdminMode()){
			event.setCancelled(true);
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Build_In_Unoccupied_Land", kp.getLang()));
		}
	}


	@EventHandler
	public void onBreakBlockUnoccupied(BlockBreakEvent e){
		if(e.isCancelled()) return;
		if(Config.getConfig().getStringList("enabled-worlds").contains(e.getBlock().getWorld().getName())) return;
		if(e.getBlock() == null) return;
		if(!Config.getConfig().getStringList("worlds-with-no-building-in-unoccupied-land").contains(e.getBlock().getWorld().getName())) return;
		if(Kingdoms.getManagers().getConquestManager() != null && e.getBlock().getWorld().equals(ConquestManager.world)) return;
		Location bukkitLoc = e.getBlock().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(land.getOwnerUUID() == null && !kp.isAdminMode()){
			e.setCancelled(true);
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Build_In_Unoccupied_Land", kp.getLang()));
		}
	}

	@EventHandler
	public void onBuildBlockUnoccupied(BlockPlaceEvent e){

		if(e.isCancelled()) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getBlock().getWorld().getName())) return;
		if(e.getBlock() == null) return;
		if(!Config.getConfig().getStringList("worlds-with-no-building-in-unoccupied-land").contains(e.getBlock().getWorld().getName())) return;
		if(Kingdoms.getManagers().getConquestManager() != null && e.getBlock().getWorld().equals(ConquestManager.world)) return;
		Location bukkitLoc = e.getBlock().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(land.getOwnerUUID() == null && !kp.isAdminMode()){
			e.setCancelled(true);
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Build_In_Unoccupied_Land", kp.getLang()));
		}
	}


	@EventHandler
	public void onBreakArmorStandOrFrame(EntityDamageByEntityEvent e){
		if(e.isCancelled()) return;

		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getEntity().getWorld().getName())) return;
		if(e.getEntity().getType() != EntityType.ARMOR_STAND && e.getEntity().getType() != EntityType.ITEM_FRAME) return;
		if(!(e.getDamager() instanceof Player)) return;
		Location bukkitLoc = e.getEntity().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null) return;

		KingdomPlayer kp = GameManagement.getPlayerManager().getSession((Player)e.getDamager());
		if(kp.isAdminMode()) return;
		if(kp.getKingdom() == null){//not in kingdom
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

			e.setCancelled(true);
		}else{//in kingdom
			Kingdom kingdom = kp.getKingdom();

			if(!kingdom.getKingdomUuid().equals(land.getOwnerUUID())){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

				e.setCancelled(true);
				return;
			}
			if(land.getStructure() != null &&
					land.getStructure().getType() == StructureType.NEXUS &&
					!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getBuildInNexus())){
				e.setCancelled(true);
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low_NexusBuild", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getBuildInNexus().toString()));
				return;
			}
		}
	}

	@EventHandler
	public void onBreakBlockOnOtherKingdom(BlockBreakEvent e){
		if(e.isCancelled()) return;

		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getBlock().getWorld().getName())) return;
		if(e.getBlock() == null) return;

		Location bukkitLoc = e.getBlock().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null) return;

		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.isAdminMode()) return;
		if(kp.getKingdom() == null){//not in kingdom
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

			e.setCancelled(true);
		}else{//in kingdom
			Kingdom kingdom = kp.getKingdom();

			if(!kingdom.getKingdomUuid().equals(land.getOwnerUUID())){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

				e.setCancelled(true);
				return;
			}
			if(land.getStructure() != null &&
					land.getStructure().getType() == StructureType.NEXUS &&
					!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getBuildInNexus())){
				e.setCancelled(true);
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low_NexusBuild", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getBuildInNexus().toString()));
				return;
			}
		}
	}

	@EventHandler
	public void onBucketEmptyOnOtherKingdom(PlayerBucketEmptyEvent event) {
		Location bukkitLoc = event.getBlockClicked().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		Land land = this.getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null) return;

		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(event.getPlayer());
		if(kp.isAdminMode()) return;
		if(kp.getKingdom() == null){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));
			event.setCancelled(true);
		}else{
			if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBucketFillOnOtherKingdom(PlayerBucketFillEvent event) {
		Location bukkitLoc = event.getBlockClicked().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();
		Land land = this.getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null) return;

		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(event.getPlayer());
		if(kp.isAdminMode()) return;
		if(kp.getKingdom() == null){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));
			event.setCancelled(true);
		}else{
			if(!land.getOwnerUUID().equals(kp.getKingdom().getKingdomUuid())){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPlaceOnOtherKingdom(BlockPlaceEvent e) {
		if(e.getBlock() == null) return;
		Location bukkitLoc = e.getBlock().getLocation();
		SimpleLocation loc = new SimpleLocation(bukkitLoc);
		SimpleChunkLocation chunk = loc.toSimpleChunk();

		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		if(land.getOwnerUUID() == null) return;

		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp.isAdminMode()) return;
		if(kp.getKingdom() == null){//not in kingdom
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

			e.setCancelled(true);
		}else{//in kingdom
			Kingdom kingdom = kp.getKingdom();

			if(!kingdom.getKingdomUuid().equals(land.getOwnerUUID())){
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Break_In_Other_Land", kp.getLang()));

				e.setCancelled(true);
				return;
			}
			if(land.getStructure() != null &&
					land.getStructure().getType() == StructureType.NEXUS &&
					!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getBuildInNexus())){
				e.setCancelled(true);
				kp.sendMessage(Kingdoms.getLang().getString("Misc_Rank_Too_Low_NexusBuild", kp.getLang()).replaceAll("%rank%", kingdom.getPermissionsInfo().getBuildInNexus().toString()));
				return;
			}
		}
	}

	@EventHandler
	public void onFlowIntoKingdomLand(BlockFromToEvent e){
		if(!Config.getConfig().getBoolean("disableFlowIntoLand")) return;

		SimpleLocation locFrom = new SimpleLocation(e.getBlock().getLocation());
		SimpleLocation locTo = new SimpleLocation(e.getToBlock().getLocation());

		Land landFrom = GameManagement.getLandManager().getOrLoadLand(locFrom.toSimpleChunk());
		Land landTo = GameManagement.getLandManager().getOrLoadLand(locTo.toSimpleChunk());

		if(landFrom.getOwnerUUID() == null){
			if(landTo.getOwnerUUID() != null){
				e.setCancelled(true);
			}
		}else if(landFrom.getOwnerUUID().equals(landTo.getOwnerUUID())){
		}else{
			e.setCancelled(true);
		}
	}


	public void stopAutoSave(){
		autoSaveThread.interrupt();
		// 2016-08-22
		try {
			autoSaveThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		//2016-08-11
		stopAutoSave();
		Kingdoms.logInfo("Saving lands to db...");
		try{
			int i = saveAll();
			Kingdoms.logInfo("[" + i + "] lands saved!");
		}catch(Exception e){
			Kingdoms.logInfo("SQL connection failed! Saving to file DB");
			db = createFileDB();
			try {
				int i = saveAll();
				Kingdoms.logInfo("[" + i + "] lands saved offline. Files will be saved to SQL server when connection is restored in future");
			} catch (InterruptedException e1) {
			}
			Config.getConfig().set("DO-NOT-TOUCH.grabLandFromFileDB",true);
		}
		landList.clear();
	}

}
