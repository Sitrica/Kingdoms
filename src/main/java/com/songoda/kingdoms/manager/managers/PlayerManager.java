package com.songoda.kingdoms.manager.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.naming.NamingException;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.database.DatabaseTransferTask;
import com.songoda.kingdoms.database.SQLiteDatabase;
import com.songoda.kingdoms.database.YamlDatabase;
import com.songoda.kingdoms.database.DatabaseTransferTask.TransferPair;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.manager.Manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.gson.JsonSyntaxException;

public class PlayerManager extends Manager {
	
	static {
		registerManager("player", new PlayerManager());
	}
	
	private final Set<OfflineKingdomPlayer> users = new HashSet<>();
	private final Database<OfflineKingdomPlayer> database;
	private BukkitTask autoSaveThread;

	protected PlayerManager() {
		super(true);
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(OfflineKingdomPlayer.class);
		else
			database = getSQLiteDatabase(OfflineKingdomPlayer.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
				@Override 
				public void run() {
					for (OfflineKingdomPlayer player : users) {
						Kingdoms.debugMessage("Saving player " + player.getName());
						database.save(player.getUniqueId() + "", player);
					}
				}
			}, 0, IntervalUtils.getInterval(interval) * 20);
		}
	}

	public OfflineKingdomPlayer getOfflineKingdomPlayer(OfflinePlayer player) {
		return getOfflineKingdomPlayer(player.getUniqueId());
	}

	public OfflineKingdomPlayer getOfflineKingdomPlayer(UUID uuid) {
		if (uuid == null)
			return null;
		Kingdoms.debugMessage("Getting session of offline kingdoms player: " + uuid);
		return users.parallelStream()
				.filter(player -> player.getUniqueId() == uuid)
				.findFirst()
				.orElseGet(() -> {
					OfflineKingdomPlayer player = database.get(uuid + "");
					if (player != null) {
						users.add(player);
						return player;
					}
					return new OfflineKingdomPlayer(uuid);
				});
	}

	public boolean isOnline(UUID uuid) {
		Optional<OfflineKingdomPlayer> optional = users.parallelStream()
				.filter(player -> player.getUniqueId().equals(uuid))
				.findAny();
		if (!optional.isPresent())
			return false;
		Player player = optional.get().getKingdomPlayer().getPlayer();
		if (player == null)
			return false;
		return player.isOnline();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPreJoin(AsyncPlayerPreLoginEvent event) {
		if (!configuration.getBoolean("Plugin.initiatePlayerLoadBeforePlayerJoin"))
			return;
		UUID uuid = event.getUniqueId();
		KingdomPlayer kp = loadKingdomPlayer(uuid);
		// kp cannot be null but just in case of bug
		if (kp != null) {
			users.put(uuid, kp);
			Kingdoms.logInfo("Loaded info for " + kp.getName());
			if (!Config.getConfig().getBoolean("markers-on-by-default")) {
				kp.setMarkerDisplaying(false);
			}
		} else {
			Kingdoms.logInfo("Failed to load info for " + e.getName());
			throw new RuntimeException("preload failed for "+e.getName());
		}
	}
	
	public void asyncLoadPlayer(Player player) {
		UUID uuid = player.getUniqueId();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){
			
			public void run(){
				KingdomPlayer kingdomPlayer = loadKingdomPlayer(uuid);

				// kp cannot be null but just in case of bug
				if (kingdomPlayer != null){
					userList.put(uuid, kingdomPlayer);
					Kingdoms.logInfo("[ERROR]: Player data for " + kingdomPlayer.getName() + " wasn't loaded on start! Attempting to load now!");
					if (!Config.getConfig().getBoolean("markers-on-by-default")) {
						kingdomPlayer.setMarkerDisplaying(false);
					}
				   
				}
			}
			
		});
	}
	public void asyncLoadPlayer(UUID uuid){
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable(){
			public void run(){
				KingdomPlayer kingdomPlayer = loadKingdomPlayer(uuid);
				// kp cannot be null but just in case of bug
				if (kingdomPlayer != null) {
					users.put(uuid, kingdomPlayer);
					Kingdoms.logInfo("[ERROR]: Player data for " + kingdomPlayer.getName() + " wasn't loaded on start! Attempting to load now!");
					if (!Config.getConfig().getBoolean("markers-on-by-default")) {
						kingdomPlayer.setMarkDisplaying(false);
					}
				}
				Kingdoms.debugMessage("Loaded player data for " + kingdomPlayer.getName() + " asyncLoadPlayer(uuid)");
			}
			
		});
	}
	
	//2017-05-09 -- We need to set most fresh Player object into the KingdomPlayer we've created in prelogin step.
	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();

		KingdomPlayer kp = getSession(uuid);
		if(kp == null || kp.isTemp()){
			//Kingdoms.logInfo("Info for " + player.getName() + " isn't loaded. Attempting to load");
			//throw new RuntimeException("Something went wrong. Player info is not loaded for "+player.getName());
			asyncLoadPlayer(player);
		
		
		}
		Bukkit.getPluginManager().callEvent(new KingdomPlayerLoginEvent(kp));
	}
	
	private KingdomPlayer loadKingdomPlayer(UUID uuid) {
		Kingdoms.debugMessage("Loading player info for " + uuid);
		KingdomPlayer kingdomPlayer = null;
		//here we delete offline info and save it if necessary
		if (users.containsKey(uuid)) {
			if (users.get(uuid) instanceof KingdomPlayer &&
					((KingdomPlayer) users.get(uuid)).isTemp()){
				users.remove(uuid);
			} else {
				database.save(uuid + "", users.remove(uuid));
			}
		}

		try {
			kingdomPlayer = (KingdomPlayer) database.get(uuid + "");
		} catch (IllegalStateException e) {
			Kingdoms.logInfo("[ERROR]: The file, " +uuid.toString() + " under Players is corrupted.");
			return new KingdomPlayer(Bukkit.getPlayer(uuid.toString()));
		} catch (JsonSyntaxException e) {
			Kingdoms.logInfo("[ERROR]: The file, " + uuid.toString() + " under Players is corrupted.");
			return new KingdomPlayer(Bukkit.getPlayer(uuid.toString()));
		}

		if (kingdomPlayer == null)
			kingdomPlayer = new KingdomPlayer(uuid);
		return kingdomPlayer;
	}
	

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		if (!Config.getConfig().getBoolean("respawn-in-kingdom-home"))
			return;
		if(Config.getConfig().getStringList("enabled-worlds").contains(event.getPlayer().getWorld().getName())){
			KingdomPlayer kp = getSession(event.getPlayer());
			if (kp.getKingdom() != null) {
				Kingdom kingdom = kp.getKingdom();
				Location spawn = kingdom.getSpawn();
				if (spawn != null)
					event.setRespawnLocation(spawn);
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (!users.containsKey(uuid))
			return;
		OfflineKingdomPlayer okp = users.remove(uuid);
		if (okp instanceof KingdomPlayer)
			plugin.getServer().getPluginManager().callEvent(new KingdomPlayerLogoffEvent((KingdomPlayer) okp));
		//2017-05-09 -- Yes. We do not want to lock main thread while saving this data.
		new Thread(new Runnable(){
			@Override
			public void run() {
				database.save(player.getUniqueId() + "", okp);
			}
		}).start();
	}

	public KingdomPlayer getSession(Player player) {
		if (player == null) {
			return null;
		}
		Kingdoms.debugMessage("Getting session of player: " + player.getName());
		if (ExternalManager.isCitizen(player))
			return null;
		return getSession(player.getUniqueId());
	}

	public KingdomPlayer getSession(UUID uuid) {
		if (uuid == null)
			return null;
		Kingdoms.debugMessage("Getting session of player uuid: " + uuid.toString());
		if (getUserFromList(uuid) instanceof KingdomPlayer) {
			return (KingdomPlayer) getUserFromList(uuid);
		} else {
			asyncLoadPlayer(uuid);
			return  (KingdomPlayer) getUserFromList(uuid);
		}
	}
	
	private OfflineKingdomPlayer getUserFromList(UUID uuid) {
		if(userList.get(uuid) == null){
			KingdomPlayer temp = new KingdomPlayer(uuid);
			temp.setTemp(true);
			return temp;
			
		}else{
			return users.get(uuid);
		}
	}
	
	@Override
	public synchronized void onDisable() {
		autoSaveThread.cancel();
		Kingdoms.consoleMessage("Saving [" + users.size() + "] loaded players...");
		try{
			saveAll();
			Kingdoms.consoleMessage("Done!");
		} catch(Exception e) {
			Kingdoms.consoleMessage("SQL connection failed! Saving to file DB");
			database = getSQLiteDatabase(OfflineKingdomPlayer.class);
			saveAll();
		}
		users.clear();
	}

}
