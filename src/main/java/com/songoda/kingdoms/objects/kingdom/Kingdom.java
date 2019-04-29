package com.songoda.kingdoms.objects.kingdom;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.manager.managers.WorldManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Kingdom extends OfflineKingdom {

	private final PlayerManager playerManager;
	private final WorldManager worldManager;

	// Transforming OfflineKingdom to Kingdom.
	public Kingdom(OfflineKingdom kingdom) {
		this(kingdom, kingdom.getName());
	}

	// Renaming.
	public Kingdom(OfflineKingdom kingdom, String name) {
		super(kingdom.getOwner(), name);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
	}

	// Grabbing a Kingdom from database.
	public Kingdom(KingdomPlayer owner, String name) {
		super(owner, name);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
	}

	public Set<KingdomPlayer> getOnlinePlayers() {
		return members.parallelStream()
				.map(player -> player.getKingdomPlayer())
				.filter(player -> player.isPresent())
				.map(player -> player.get())
				.collect(Collectors.toSet());
	}

	public Set<KingdomPlayer> getOnlineAllies() {
		Set<KingdomPlayer> allies = new HashSet<>();
		Bukkit.getWorlds().parallelStream()
				.filter(world -> worldManager.acceptsWorld(world))
				.forEach(world -> {
					for (Player player : world.getPlayers()) {
						KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
						Kingdom playerKingdom = kingdomPlayer.getKingdom();
						if (playerKingdom == null)
							continue;
						if (isAllianceWith(playerKingdom) && playerKingdom.isAllianceWith(this))
							allies.add(kingdomPlayer);
					}
				});
		return allies;
	}

	public Set<KingdomPlayer> getOnlineEnemies() {
		Set<KingdomPlayer> enemies = new HashSet<>();
		Bukkit.getWorlds().parallelStream()
				.filter(world -> worldManager.acceptsWorld(world))
				.forEach(world -> {
					for (Player player : world.getPlayers()) {
						KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
						Kingdom playerKingdom = kingdomPlayer.getKingdom();
						if (playerKingdom == null)
							continue;
						if (isEnemyWith(playerKingdom))
							enemies.add(kingdomPlayer);
					}
				});
		return enemies;
	}

}
