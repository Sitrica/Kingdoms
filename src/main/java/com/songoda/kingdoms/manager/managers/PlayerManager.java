package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class PlayerManager extends Manager {

	private final Map<UUID, OfflineKingdomPlayer> users = new HashMap<>();
	private Database<OfflineKingdomPlayer> database;
	private KingdomManager kingdomManager;
	private WorldManager worldManager;
	private BukkitTask autoSaveThread;

	public PlayerManager() {
		super("player", true, "rank");
	}

	@Override
	public void initalize() {
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		String table = configuration.getString("database.player-table", "Players");
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(table, OfflineKingdomPlayer.class);
		else
			database = getFileDatabase(table, OfflineKingdomPlayer.class);
		if (configuration.getBoolean("database.auto-save.enabled", true)) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, save, 0, IntervalUtils.getInterval(interval) * 20);
		}
	}

	private final Runnable save = new Runnable() {
		@Override 
		public void run() {
			for (Entry<UUID, OfflineKingdomPlayer> entry : users.entrySet()) {
				OfflineKingdomPlayer player = entry.getValue();
				Kingdoms.debugMessage("Saving player " + player.getName());
				database.put(entry.getKey() + "", player);
			}
		}
	};

	public void save(OfflineKingdomPlayer player) {
		database.put(player.getUniqueId() + "", player);
	}

	public Optional<KingdomPlayer> getKingdomPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null)
			return Optional.empty();
		return Optional.of(getKingdomPlayer(player));
	}

	public KingdomPlayer getKingdomPlayer(Player player) {
		UUID uuid = player.getUniqueId();
		Optional<OfflineKingdomPlayer> kingdomPlayer = getOfflineKingdomPlayer(uuid);
		if (kingdomPlayer.isPresent()) {
			OfflineKingdomPlayer value = kingdomPlayer.get();
			return value instanceof KingdomPlayer ? (KingdomPlayer) value : convert(player, value);
		}
		Kingdoms.debugMessage("Adding new kingdoms player: " + uuid);
		KingdomPlayer newPlayer = new KingdomPlayer(player);
		users.put(uuid, newPlayer);
		return newPlayer;
	}

	private KingdomPlayer convert(Player player, OfflineKingdomPlayer other) {
		KingdomPlayer kingdomPlayer = new KingdomPlayer(player, other);
		Kingdoms.debugMessage("Converting kingdom offline player to online player: " + player.getUniqueId());
		users.replace(player.getUniqueId(), kingdomPlayer);
		return kingdomPlayer;
	}

	public Optional<OfflineKingdomPlayer> getOfflineKingdomPlayer(OfflinePlayer player) {
		return getOfflineKingdomPlayer(player.getUniqueId());
	}

	public Optional<OfflineKingdomPlayer> getOfflineKingdomPlayer(UUID uuid) {
		return Optional.ofNullable(users.entrySet().parallelStream()
				.filter(entry -> entry.getKey().equals(uuid))
				.map(entry -> entry.getValue())
				.findFirst()
				.orElseGet(() -> {
					OfflineKingdomPlayer player = database.get(uuid + "");
					if (player != null) {
						Kingdoms.debugMessage("Successfuly fetched data for kingdoms player: " + uuid);
						users.put(uuid, player);
					}
					return player; // Null caught by optional ofNullable.
				}));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		//UUID uuid = player.getUniqueId();
		KingdomPlayer kingdomPlayer = getKingdomPlayer(player);
		//if (kingdomPlayer == null)
		//	loadKingdomPlayer(uuid);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		Kingdoms.debugMessage("Loaded player " + player.getUniqueId());
		if (configuration.getBoolean("kingdom.join-at-kingdom", false))
			player.teleport(kingdom.getSpawn());
		if (!kingdomPlayer.isVanished())
			new MessageBuilder("messages.member-join")
					.toKingdomPlayers(kingdom.getOnlinePlayers())
					.toKingdomPlayers(kingdom.getOnlineAllies())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send();
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
		Player eventPlayer = event.getPlayer();
		KingdomPlayer kingdomPlayer = getKingdomPlayer(eventPlayer);
		UUID uuid = eventPlayer.getUniqueId();
		users.entrySet().parallelStream()
				.filter(entry -> entry.getKey().equals(uuid))
				.map(entry -> entry.getValue())
				.map(player -> (KingdomPlayer) player)
				.forEach(player -> {
					database.put(uuid + "", player);
					if (player.isVanished())
						return;
					Kingdom kingdom = player.getKingdom();
					if (kingdom == null)
						return;
					new MessageBuilder("messages.member-leave")
							.toKingdomPlayers(kingdom.getOnlinePlayers())
							.toKingdomPlayers(kingdom.getOnlineAllies())
							.setPlaceholderObject(player)
							.setKingdom(kingdom)
							.send();
				});
		users.remove(uuid);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom != null)
			kingdomManager.onPlayerLeave(kingdomPlayer, kingdom);
	}

	@Override
	public synchronized void onDisable() {
		if (autoSaveThread != null)
			autoSaveThread.cancel();
		if (users.isEmpty())
			return;
		Kingdoms.consoleMessage("Saving [" + users.size() + "] loaded players...");
		try {
			save.run();
			Kingdoms.consoleMessage("Done!");
		} catch (Exception e) {
			Kingdoms.consoleMessage("SQL connection failed! Saving to file DB");
			save.run();
		}
		users.clear();
	}

}
