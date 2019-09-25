package com.songoda.kingdoms.objects.kingdom;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Location;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.CooldownManager.KingdomCooldown;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.objects.structures.WarpPad.Warp;

public class OfflineKingdom {

	protected final Set<RankPermissions> permissions = new HashSet<>();
	protected final Set<String> enemies = new HashSet<>();
	protected final Set<String> allies = new HashSet<>();
	protected final Set<UUID> members = new HashSet<>();
	protected final Set<Land> claims = new HashSet<>();
	protected final Set<Warp> warps = new HashSet<>();
	protected long resourcePoints = 0, invasionCooldown = 0;
	protected int dynmapColor, max = 0, extraPurchased = 0;
	protected boolean neutral, first, invaded;
	protected Powerup powerup = new Powerup();
	protected KingdomCooldown shieldTime;
	protected KingdomChest kingdomChest;
	protected DefenderInfo defenderInfo;
	protected final Kingdoms instance;
	protected MiscUpgrade miscUpgrade;
	protected String lore = "Not set";
	protected Location nexus, spawn;
	protected final String name;
	protected UUID owner;

	public OfflineKingdom(String name) {
		this.instance = Kingdoms.getInstance();
		this.dynmapColor = instance.getManager(KingdomManager.class).getRandomColor();
		this.max = instance.getConfig().getInt("base-max-members", 10);
		this.members.add(owner);
		this.name = name;
	}

	/**
	 * Creates an OfflineKingdom instance.
	 * 
	 * @param uuid UUID to be used for the OfflineKingdom owner.
	 * @param name The name of the OfflineKingdom.
	 */
	public OfflineKingdom(UUID owner, String name) {
		this.instance = Kingdoms.getInstance();
		this.dynmapColor = instance.getManager(KingdomManager.class).getRandomColor();
		this.max = instance.getConfig().getInt("base-max-members", 10);
		this.members.add(owner);
		this.owner = owner;
		this.name = name;
	}

	public void addWarp(Warp warp) {
		warps.add(warp);
	}

	public void removeWarp(Warp warp) {
		warps.remove(warp);
	}

	public void removeWarpAt(Land land) {
		warps.removeIf(warp -> warp.getLand().equals(land));
	}

	public void setExtraPurchased(int purchased) {
		extraPurchased = purchased;
	}

	/**
	 * @return The amount of extra claims added to this Kingdom.
	 */
	public int getExtraPurchased() {
		return extraPurchased;
	}

	public void addExtraPurchase(int add) {
		extraPurchased += add;
	}

	public void subtractExtraPurchase(int subtract) {
		extraPurchased += subtract;
	}

	public Set<Warp> getWarps() {
		return warps;
	}

	public int getMaxMembers() {
		return max;
	}

	public void setMaxMembers(int max) {
		this.max = max;
	}

	public Optional<OfflineKingdomPlayer> getOwner() {
		return instance.getManager(PlayerManager.class).getOfflineKingdomPlayer(owner);
	}

	public void setOwner(OfflineKingdomPlayer owner) {
		if (owner == null) {
			this.owner = null;
			return;
		}
		this.owner = owner.getUniqueId();
	}

	public int getDynmapColor() {
		return dynmapColor;
	}

	public String getName() {
		return name;
	}

	public boolean isNeutral() {
		return neutral;
	}

	public int getMemberSize() {
		return members.size();
	}

	public void setNeutral(boolean neutral) {
		this.neutral = neutral;
	}

	public void addMember(OfflineKingdomPlayer member) {
		members.add(member.getUniqueId());
	}

	public void addMember(UUID member) {
		members.add(member);
	}

	public Set<Land> getClaims() {
		return claims;
	}

	public void addClaim(Land land) {
		claims.add(land);
	}

	public void removeClaim(Land land) {
		claims.remove(land);
	}

	public String getLore() {
		return lore;
	}

	public void setLore(String lore) {
		this.lore = lore;
	}

	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}

	public boolean isMember(UUID uuid) {
		return members.stream().anyMatch(match -> match.equals(uuid));
	}

	public boolean isMember(OfflineKingdomPlayer kingdomPlayer) {
		return isMember(kingdomPlayer.getUniqueId());
	}

	public void removeMember(OfflineKingdomPlayer player) {
		removeMember(player.getUniqueId());
	}

	public void removeMember(UUID player) {
		members.removeIf(uuid -> player.equals(uuid));
	}

	public KingdomChest getKingdomChest() {
		if (kingdomChest == null)
			kingdomChest = new KingdomChest(this);
		return kingdomChest;
	}

	public void setKingdomChest(KingdomChest kingdomChest) {
		this.kingdomChest = kingdomChest;
	}

	public Powerup getPowerup() {
		return powerup;
	}

	public void setPowerup(Powerup powerup) {
		this.powerup = powerup;
	}

	public MiscUpgrade getMiscUpgrades() {
		if (miscUpgrade == null)
			miscUpgrade = new MiscUpgrade(this);
		return miscUpgrade;
	}

	public void setMiscUpgrades(MiscUpgrade miscUpgrade) {
		this.miscUpgrade = miscUpgrade;
	}

	public DefenderInfo getDefenderInfo() {
		if (defenderInfo == null)
			defenderInfo = new DefenderInfo(this);
		return defenderInfo;
	}

	public void setDefenderInfo(DefenderInfo defenderInfo) {
		this.defenderInfo = defenderInfo;
	}

	public boolean hasInvaded() {
		return invaded;
	}

	public void setInvaded(boolean invaded) {
		this.invaded = invaded;
	}

	public long getResourcePoints() {
		return resourcePoints;
	}

	public void setResourcePoints(long points) {
		resourcePoints = points;
	}

	public void addResourcePoints(long points) {
		resourcePoints = resourcePoints + points;
	}

	public void subtractResourcePoints(long points) {
		resourcePoints = resourcePoints - points;
		if (resourcePoints < 0)
			resourcePoints = 0;
	}

	public Location getNexusLocation() {
		return nexus;
	}

	public void setNexusLocation(Location nexus) {
		this.nexus = nexus;
	}

	public boolean isOnline() {
		return instance.getManager(KingdomManager.class).isOnline(this);
	}

	public Kingdom getKingdom() {
		KingdomManager kingdomManager = instance.getManager(KingdomManager.class);
		Optional<Kingdom> optional = kingdomManager.getKingdom(name);
		if (optional.isPresent())
			return optional.get();
		return kingdomManager.convert(this);
	}

	public boolean hasUsedFirstClaim() {
		return first;
	}

	public void setUsedFirstClaim(boolean first) {
		this.first = first;
	}

	public long getInvasionCooldown() {
		return invasionCooldown;
	}

	public void setInvasionCooldown(long invasionCooldown) {
		this.invasionCooldown = invasionCooldown;
	}

	public Set<OfflineKingdomPlayer> getMembers() {
		PlayerManager playerManager = instance.getManager(PlayerManager.class);
		return members.parallelStream()
				.map(uuid -> playerManager.getKingdomPlayer(uuid))
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	public void setShieldTime(long seconds) {
		this.shieldTime = new KingdomCooldown(this, "SHIELD", seconds);
	}

	public Set<RankPermissions> getPermissions() {
		return permissions;
	}

	public boolean equals(OfflineKingdom other) {
		return other.getName().equals(name);
	}

	/**
	 * Grabs the cooldown instance loader of the Sheild.
	 * This is not the actual countdown time of the Shield.
	 * Use this object for getting of time left.
	 * 
	 * @return KingdomCooldown
	 */
	public KingdomCooldown getShieldTime() {
		return shieldTime;
	}

	/**
	 * Grabs the lowest priority rank for the RankPermissions predicate.
	 * 
	 * @param predicate The RankPermissions predicate to check all ranks for.
	 * @return Optional<Rank> Which is the returned value if any are present.
	 */
	public Optional<Rank> getLowestRankFor(Predicate<RankPermissions> predicate) {
		return instance.getManager(RankManager.class).getLowestFor(this, predicate);
	}

	public Rank getLowestRankOrDefault(Predicate<RankPermissions> predicate) {
		return instance.getManager(RankManager.class).getLowestAndDefault(this, predicate);
	}

	public Set<OfflineKingdom> getAllies() {
		KingdomManager kingdomManager = instance.getManager( KingdomManager.class);
		return allies.parallelStream()
				.map(name -> kingdomManager.getOfflineKingdom(name))
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	public Rank getHighestRank() {
		return getSortedRanks().get(0);
	}

	public void addAlliance(OfflineKingdom kingdom) {
		addAlliance(kingdom.getName());
	}

	public void addAlliance(String kingdom) {
		allies.add(kingdom);
	}

	public boolean isAllianceWith(OfflineKingdom kingdom) {
		return isAllianceWith(kingdom.getName());
	}

	public boolean isAllianceWith(String kingdom) {
		return allies.parallelStream().anyMatch(ally -> ally.equalsIgnoreCase(kingdom));
	}

	public void removeAlliance(OfflineKingdom kingdom) {
		removeAlliance(kingdom.getName());
	}

	public void removeAlliance(String kingdom) {
		allies.remove(kingdom);
	}

	public Set<OfflineKingdom> getEnemies() {
		KingdomManager kingdomManager = instance.getManager(KingdomManager.class);
		return enemies.parallelStream()
				.map(name -> kingdomManager.getOfflineKingdom(name))
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	public void addEnemy(OfflineKingdom kingdom) {
		enemies.add(kingdom.getName());
	}

	public void addEnemy(String kingdom) {
		enemies.add(kingdom);
	}

	public boolean isEnemyWith(OfflineKingdom kingdom) {
		return isEnemyWith(kingdom.getName());
	}

	public boolean isEnemyWith(String kingdom) {
		return enemies.parallelStream().anyMatch(enemy -> enemy.equalsIgnoreCase(kingdom));
	}

	public void removeEnemy(OfflineKingdom kingdom) {
		removeEnemy(kingdom.getName());
	}

	public void removeEnemy(String kingdom) {
		enemies.remove(kingdom);
	}

	/**
	 * Grabs the permissions of a rank for the Kingdom.
	 * 
	 * @param rank Rank to grab permissions for.
	 * @return RankPermissions which is an object for reading all permissions for a rank.
	 */
	public RankPermissions getPermissions(Rank rank) {
		return permissions.stream()
				.filter(permissions -> permissions.getRank().equals(rank))
				.findFirst()
				.orElseGet(() -> {
					RankPermissions permission = new RankPermissions(rank);
					permissions.add(permission);
					return permission;
				});
	}

	public List<Rank> getSortedRanks() {
		return permissions.parallelStream()
				.sorted(Comparator.comparing(RankPermissions::getRank))
				.map(rankPermissions -> rankPermissions.getRank())
				.collect(Collectors.toList());
	}

	public Rank getPreviousRank(Rank rank) {
		List<Rank> sorted = getSortedRanks();
		int index = sorted.indexOf(rank);
		if (index <= 0)
			return rank;
		return sorted.get(index - 1);
	}

	public Rank getNextRank(Rank rank) {
		List<Rank> sorted = getSortedRanks();
		int index = sorted.indexOf(rank);
		if (index >= sorted.size() - 1)
			return rank;
		return sorted.get(index + 1);
	}

	public void setRankPermissions(RankPermissions rankPermissions) {
		permissions.removeIf(permissions -> permissions.getRank().equals(rankPermissions.getRank()));
		permissions.add(rankPermissions);
	}

	public void onKingdomDelete(OfflineKingdom kingdom) {
		onKingdomDelete(kingdom.getName());
	}

	public void onKingdomDelete(String kingdom) {
		enemies.remove(kingdom);
		allies.remove(kingdom);
	}

}
