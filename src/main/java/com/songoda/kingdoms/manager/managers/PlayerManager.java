package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class PlayerManager extends Manager {
	
	private final Set<OfflineKingdomPlayer> users = new HashSet<>();
	private Optional<CitizensManager> citizensManager;
	private Database<OfflineKingdomPlayer> database;
	private WorldManager worldManager;
	private BukkitTask autoSaveThread;

	public PlayerManager() {
		super("player", true, "serializer");
	}

	@Override
	public void initalize() {
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(OfflineKingdomPlayer.class);
		else
			database = getSQLiteDatabase(OfflineKingdomPlayer.class);
		if (configuration.getBoolean("database.auto-save.enabled", true)) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, save, 0, IntervalUtils.getInterval(interval) * 20);
		}
	}
	
	private final Runnable save = new Runnable() {
		@Override 
		public void run() {
			for (OfflineKingdomPlayer player : users) {
				Kingdoms.debugMessage("Saving player " + player.getName());
				database.save(player.getUniqueId() + "", player);
			}
		}
	};
	
	public KingdomPlayer getKingdomPlayer(Player player) {
		if (player == null)
			return null;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
				return null;
		return getKingdomPlayer(player.getUniqueId());
	}

	public KingdomPlayer getKingdomPlayer(UUID uuid) {
		if (uuid == null)
			return null;
		Kingdoms.debugMessage("Getting session of player uuid: " + uuid.toString());
		return (KingdomPlayer) users.parallelStream()
				.filter(player -> player.getUniqueId().equals(uuid))
				.findFirst()
				.orElse(loadKingdomPlayer(uuid));
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
	
	private KingdomPlayer loadKingdomPlayer(UUID uuid) {
		Kingdoms.debugMessage("Loading player info for: " + uuid);
		KingdomPlayer kingdomPlayer = null;
		Player player = Bukkit.getPlayer(uuid);
		try {
			kingdomPlayer = (KingdomPlayer) database.get(uuid + "");
			users.add(kingdomPlayer);
		} catch (Exception e) {
			Kingdoms.consoleMessage("&cThe file, " + uuid.toString() + " under Players is corrupted.");
			if (player == null)
				return null;
			return new KingdomPlayer(player);
		}
		if (kingdomPlayer != null)
			return kingdomPlayer;
		if (player == null)
			return null;
		return new KingdomPlayer(player);
	}
	

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		if (configuration.getBoolean("kingdoms.respawn-at-kingdom", false)) {
			Player player = event.getPlayer();
			if (worldManager.acceptsWorld(player.getWorld())) {
				KingdomPlayer kingdomPlayer = getKingdomPlayer(player);
				if (kingdomPlayer.getKingdom() != null) {
					Kingdom kingdom = kingdomPlayer.getKingdom();
					Location spawn = kingdom.getSpawn();
					if (spawn != null)
						event.setRespawnLocation(spawn);
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		users.parallelStream()
				.filter(player -> player.getUniqueId().equals(uuid))
				.map(player -> (KingdomPlayer) player)
				.forEach(player -> {
					if (!player.isVanished()) {
						Kingdom kingdom = player.getKingdom();
						new MessageBuilder("messages.member-leave")
								.toKingdomPlayers(kingdom.getOnlinePlayers())
								.toKingdomPlayers(kingdom.getOnlineAllies())
								.setPlaceholderObject(player)
								.setKingdom(kingdom)
								.send();
					}
					database.save(uuid + "", player);
				});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		KingdomPlayer kingdomPlayer = getKingdomPlayer(uuid);
		if (kingdomPlayer == null)
			loadKingdomPlayer(player.getUniqueId());
		else if (configuration.getBoolean("kingdom.join-at-kingdom", false))
			player.teleport(kingdomPlayer.getKingdom().getSpawn());
		if (!kingdomPlayer.isVanished()) {
			Kingdom kingdom = kingdomPlayer.getKingdom();
			new MessageBuilder("messages.member-join")
					.toKingdomPlayers(kingdom.getOnlinePlayers())
					.toKingdomPlayers(kingdom.getOnlineAllies())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send();
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPreJoin(AsyncPlayerPreLoginEvent event) {
		if (!configuration.getBoolean("plugin.load-player-before-join", true))
			return;
		UUID uuid = event.getUniqueId();
		KingdomPlayer player = loadKingdomPlayer(uuid);
		// The player may not exist yet. That's ok.
		if (player == null)
			return;
		users.add(player);
	}
	
	@Override
	public synchronized void onDisable() {
		if (autoSaveThread != null)
			autoSaveThread.cancel();
		Kingdoms.consoleMessage("Saving [" + users.size() + "] loaded players...");
		try{
			save.run();
			Kingdoms.consoleMessage("Done!");
		} catch (Exception e) {
			Kingdoms.consoleMessage("SQL connection failed! Saving to file DB");
			database = getSQLiteDatabase(OfflineKingdomPlayer.class);
			save.run();
		}
		users.clear();
	}

}
