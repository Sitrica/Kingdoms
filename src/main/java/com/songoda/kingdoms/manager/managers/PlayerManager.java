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

	private final Set<OfflineKingdomPlayer> users = new HashSet<>();
	private final Database<OfflineKingdomPlayer> database;
	private BukkitTask autoSaveThread;

	public PlayerManager() {
		super(true, "rank");
		String table = configuration.getString("database.player-table", "Players");
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(table, OfflineKingdomPlayer.class);
		else
			database = getFileDatabase(table, OfflineKingdomPlayer.class);
		if (configuration.getBoolean("database.auto-save.enabled", true)) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> users.forEach(player -> save(player)), 0, IntervalUtils.getInterval(interval) * 20);
		}
	}

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
		if (!kingdomPlayer.isPresent()) {
			Kingdoms.debugMessage("Adding new kingdoms player: " + uuid);
			KingdomPlayer newPlayer = new KingdomPlayer(player);
			users.add(newPlayer);
			save(newPlayer);
			return newPlayer;
		}
		OfflineKingdomPlayer value = kingdomPlayer.get();
		if (value instanceof KingdomPlayer)
			return (KingdomPlayer) value;
		Kingdoms.debugMessage("Converting offline player to online player: " + player.getUniqueId());
		KingdomPlayer converted = new KingdomPlayer(player, value);
		users.removeIf(remove -> remove.getUniqueId().equals(uuid));
		users.add(converted);
		return converted;
	}

	public Optional<OfflineKingdomPlayer> getOfflineKingdomPlayer(OfflinePlayer player) {
		return getOfflineKingdomPlayer(player.getUniqueId());
	}

	public Optional<OfflineKingdomPlayer> getOfflineKingdomPlayer(UUID uuid) {
		return Optional.ofNullable(users.parallelStream()
				.filter(player -> player.getUniqueId().equals(uuid))
				.findFirst()
				.orElseGet(() -> {
					OfflineKingdomPlayer player = database.get(uuid + "");
					if (player != null) {
						Kingdoms.debugMessage("Successfuly fetched data for kingdom player: " + uuid);
						users.add(player);
					}
					return player; // Null caught by optional ofNullable.
				}));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = getKingdomPlayer(player);
		Kingdoms.debugMessage("Loaded player " + player.getUniqueId());
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		if (configuration.getBoolean("kingdom.join-at-kingdom", false))
			player.teleport(kingdom.getSpawn());
		if (!kingdomPlayer.isVanished()) {
			MessageBuilder builder = new MessageBuilder("kingdoms.member-join")
					.toKingdomPlayers(kingdom.getOnlinePlayers())
					.toKingdomPlayers(kingdom.getOnlineAllies())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom);
			if (!configuration.getBoolean("see-self-join-message", true))
				builder.ignoreSelf(kingdomPlayer);
			builder.send();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		if (!configuration.getBoolean("kingdoms.respawn-at-kingdom", false))
			return;
		Player player = event.getPlayer();
		if (!instance.getManager(WorldManager.class).acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		Location spawn = kingdom.getSpawn();
		if (spawn != null)
			event.setRespawnLocation(spawn);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		KingdomPlayer kingdomPlayer = getKingdomPlayer(player);
		database.put(uuid + "", kingdomPlayer);
		if (kingdomPlayer.isVanished())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		new MessageBuilder("kingdoms.member-leave")
				.toKingdomPlayers(kingdom.getOnlinePlayers())
				.toKingdomPlayers(kingdom.getOnlineAllies())
				.setPlaceholderObject(player)
				.ignoreSelf(kingdomPlayer)
				.setKingdom(kingdom)
				.send();
		if (kingdom != null)
			instance.getManager(KingdomManager.class).onPlayerLeave(kingdomPlayer, kingdom);
		instance.getServer().getScheduler().runTaskLaterAsynchronously(instance, () -> users.removeIf(remove -> remove.getUniqueId().equals(uuid)), 1);
	}

	@Override
	public synchronized void onDisable() {
		if (autoSaveThread != null)
			autoSaveThread.cancel();
		if (users.isEmpty())
			return;
		Kingdoms.consoleMessage("Saving [" + users.size() + "] loaded players...");
		try {
			users.forEach(player -> save(player));
			Kingdoms.consoleMessage("Done!");
		} catch (Exception e) {
			Kingdoms.consoleMessage("SQL connection failed!");
		}
	}

	@Override
	public void initalize() {}

}
