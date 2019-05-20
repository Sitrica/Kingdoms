package com.songoda.kingdoms.objects.player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

public class OfflineKingdomPlayer {

	protected final Set<LandInfo> claims = new HashSet<>(); // Kingdom claims that this user has claimed.
	protected final Kingdoms instance;
	protected final String name;
	protected final UUID uuid;
	protected String kingdom;
	protected String rank;

	public OfflineKingdomPlayer(UUID uuid) {
		this(Bukkit.getOfflinePlayer(uuid));
	}

	public OfflineKingdomPlayer(OfflinePlayer player) {
		this(player, null);
	}

	public OfflineKingdomPlayer(OfflinePlayer player, Rank rank) {
		this.instance = Kingdoms.getInstance();
		this.uuid = player.getUniqueId();
		this.name = player.getName();
		this.rank = instance.getManager(RankManager.class).getDefaultRank().getName();
		if (rank != null)
			this.rank = rank.getName();
	}

	public boolean hasKingdom() {
		return kingdom == null;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public Rank getRank() {
		Optional<Rank> optional = instance.getManager(RankManager.class).getRank(rank);
		if (!optional.isPresent())
			return null;
		return optional.get();
	}

	public void setRank(Rank rank) {
		if (rank == null) {
			this.rank = null;
			return;
		}
		this.rank = rank.getName();
	}

	public OfflineKingdom getKingdom() {
		if (kingdom == null)
			return null;
		Optional<OfflineKingdom> optional = instance.getManager(KingdomManager.class).getOfflineKingdom(kingdom);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	public boolean isOnline() {
		return Bukkit.getPlayer(uuid) != null;
	}

	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	public Optional<KingdomPlayer> getKingdomPlayer() {
		return instance.getManager(PlayerManager.class).getKingdomPlayer(uuid);
	}

	public Set<Land> getClaims() {
		return claims.parallelStream()
				.map(info -> info.get())
				.collect(Collectors.toSet());
	}

	public void addClaim(LandInfo land) {
		claims.add(land);
	}

	public void addClaims(Collection<LandInfo> lands) {
		claims.addAll(lands);
	}

	public boolean equals(OfflineKingdomPlayer player) {
		return player.getUniqueId().equals(uuid);
	}

	public void onKingdomLeave() {
		claims.clear();
		kingdom = null;
		rank = null;
		instance.getManager(PlayerManager.class).save(this);
	}

}
