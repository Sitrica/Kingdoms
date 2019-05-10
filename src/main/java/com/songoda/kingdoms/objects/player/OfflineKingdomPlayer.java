package com.songoda.kingdoms.objects.player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

public class OfflineKingdomPlayer {

	private final Set<Land> claims = new HashSet<>(); // Kingdom claims that this user has claimed.
	protected final KingdomManager kingdomManager;
	protected final PlayerManager playerManager;
	protected final Kingdoms instance;
	protected final String name;
	protected final UUID uuid;
	protected String kingdom;
	protected Rank rank;

	public OfflineKingdomPlayer(UUID uuid) {
		this(Bukkit.getOfflinePlayer(uuid));
	}

	public OfflineKingdomPlayer(OfflinePlayer player) {
		this.name = player.getName();
		this.uuid = player.getUniqueId();
		this.instance = Kingdoms.getInstance();
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.rank = instance.getManager("rank", RankManager.class).getDefaultRank();
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
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public OfflineKingdom getKingdom() {
		if (kingdom == null)
			return null;
		Optional<OfflineKingdom> optional = kingdomManager.getOfflineKingdom(kingdom);
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
		return playerManager.getKingdomPlayer(uuid);
	}

	public Set<Land> getClaims() {
		return claims;
	}

	public void addClaim(Land land) {
		claims.add(land);
	}

	public void addClaims(Collection<Land> lands) {
		claims.addAll(lands);
	}

	private void resetClaims() {
		claims.clear();
	}

	public boolean equals(OfflineKingdomPlayer player) {
		return player.getUniqueId().equals(uuid);
	}

	public void onKingdomLeave() {
		resetClaims();
		kingdom = null;
		rank = null;
	}

}
