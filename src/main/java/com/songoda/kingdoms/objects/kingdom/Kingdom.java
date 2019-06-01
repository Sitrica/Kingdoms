package com.songoda.kingdoms.objects.kingdom;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.WorldManager;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class Kingdom extends OfflineKingdom {

	// Used for Undo.
	private final Queue<LandInfo> lastClaims;

	// Transforming OfflineKingdom to Kingdom.
	public Kingdom(OfflineKingdom other) {
		this(other, other.getName());
	}

	// Renaming.
	public Kingdom(OfflineKingdom other, String name) {
		super(other.getOwner().getUniqueId(), name);
		int size = instance.getConfig().getInt("claiming.max-undo-claims", 20);
		this.lastClaims = new PriorityQueue<>(size);
		this.invasionCooldown = other.invasionCooldown;
		this.resourcePoints = other.resourcePoints;
		this.permissions.addAll(other.permissions);
		this.kingdomChest = other.kingdomChest;
		this.defenderInfo = other.defenderInfo;
		this.miscUpgrade = other.miscUpgrade;
		this.shieldTime = other.shieldTime;
		this.enemies.addAll(other.enemies);
		this.members.addAll(other.members);
		this.allies.addAll(other.allies);
		this.claims.addAll(other.claims);
		this.warps.addAll(other.warps);
		this.powerup = other.powerup;
		this.neutral = other.neutral;
		this.invaded = other.invaded;
		this.spawn = other.spawn;
		this.nexus = other.nexus;
		this.first = other.first;
		this.lore = other.lore;
		this.max = other.max;
	}

	// Grabbing a Kingdom from database.
	public Kingdom(KingdomPlayer owner, String name) {
		super(owner.getUniqueId(), name);
		int size = instance.getConfig().getInt("claiming.max-undo-claims", 20);
		this.lastClaims = new PriorityQueue<>(size);
	}

	public Set<KingdomPlayer> getOnlinePlayers() {
		return getMembers().parallelStream()
				.map(player -> player.getKingdomPlayer())
				.filter(player -> player.isPresent())
				.map(player -> player.get())
				.collect(Collectors.toSet());
	}

	public void addUndoClaim(Land land) {
		LandInfo info = land.toInfo();
		if (!lastClaims.offer(info)) {
			lastClaims.remove();
			lastClaims.add(info);
		}
	}

	public int undoClaims(int amount) {
		if (lastClaims.isEmpty())
			return 0;
		int size = lastClaims.size();
		if (size < amount)
			amount = size;
		for (int i = 0; i < amount; i++) {
			Land land = lastClaims.poll().get();
			instance.getManager(LandManager.class).unclaimLand(this, land);
		}
		return amount;
	}

	public Set<KingdomPlayer> getOnlineAllies() {
		Set<KingdomPlayer> allies = new HashSet<>();
		PlayerManager playerManager = instance.getManager(PlayerManager.class);
		WorldManager worldManager = instance.getManager(WorldManager.class);
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
		PlayerManager playerManager = instance.getManager(PlayerManager.class);
		WorldManager worldManager = instance.getManager(WorldManager.class);
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
