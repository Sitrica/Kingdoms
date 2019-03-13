package com.songoda.kingdoms.objects.kingdom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Location;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.CooldownManager.KingdomCooldown;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.objects.structures.WarpPad;

public class OfflineKingdom {

	private final Map<Rank, RankPermissions> permissions = new HashMap<>();
	protected final Set<OfflineKingdomPlayer> members = new HashSet<>();
	private final Set<OfflineKingdom> enemies = new HashSet<>();
	private final Set<OfflineKingdom> allies = new HashSet<>();
	private final Set<WarpPad> warps = new HashSet<>();
	protected final Set<Land> claims = new HashSet<>();
	private long resourcePoints = 0, invasionCooldown = 0, max;
	private final KingdomManager kingdomManager;
	private boolean neutral, first, invaded;
	private final RankManager rankManager;
	private KingdomCooldown shieldTime;
	protected final Kingdoms instance;
	private OfflineKingdomPlayer king;
	private KingdomChest kingdomChest;
	private DefenderInfo defenderInfo;
	private MiscUpgrade miscUpgrade;
	private Location nexus, spawn;
	private String name, lore;
	private final UUID uuid;
	private Powerup powerup;
	private int dynmapColor;
	
	/*
	private final HashMap<String, Long> cdTimeNeeded = new HashMap<>();
	private final Map<String, String> invasionLog = new HashMap<>();
	private final Map<String, Long> cooldowns = new HashMap<>();
	private final List<UUID> enemies= new ArrayList<>();
	private final List<UUID> allies = new ArrayList<>();
	private int shieldValue = 0, shieldRadius = 0;
	*/
	
	public OfflineKingdom(OfflineKingdomPlayer king) {
		this(UUID.randomUUID(), king);
	}
	
	public OfflineKingdom(UUID uuid, OfflineKingdomPlayer king) {
		this(uuid, king, false);
	}
	
	/**
	 * Creates an OfflineKingdom instance.
	 * 
	 * @param uuid UUID to be used for the Kingdom to be traced.
	 * @param king The owner of this Kingdom.
	 * @param safeUUID If you know the UUID for the 'uuid' already exists. Set this to true and it won't find a new UUID but use that UUID overriding..
	 */
	protected OfflineKingdom(UUID uuid, OfflineKingdomPlayer king, boolean safeUUID) {
		this.instance = Kingdoms.getInstance();
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.rankManager = instance.getManager("rank", RankManager.class);
		this.max = instance.getConfig().getInt("base-max-members", 10);
		this.dynmapColor = kingdomManager.getRandomColor();
		this.members.add(king);
		this.king = king;
		if (!kingdomManager.canUse(uuid) && !safeUUID) {
			while (true) {
				UUID check = UUID.randomUUID();
				if (kingdomManager.canUse(check)) {
					this.uuid = check;
					break;
				}
			}
		} else {
			this.uuid = uuid;
		}
	}
	
	public void addWarp(WarpPad warp) {
		warps.add(warp);
	}
	
	public void removeWarp(WarpPad warp) {
		warps.remove(warp);
	}
	
	public void removeWarpAt(Land land) {
		Iterator<WarpPad> iterator = warps.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getLand().equals(land))
				iterator.remove();
		}
	}
	
	public Set<WarpPad> getWarps() {
		return warps;
	}
	
	public long getMaxMembers() {
		return max;
	}

	public void setMaxMembers(long max) {
		this.max = max;
	}

	public OfflineKingdomPlayer getKing() {
		return king;
	}
	
	public void setKing(OfflineKingdomPlayer king) {
		this.king = king;
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

	public void setNeutral(boolean neutral) {
		this.neutral = neutral;
	}
	
	public void setName(String name) {
		if (kingdomManager.canRename(name))
			this.name = name;
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
	
	public UUID getUniqueId() {
		return uuid;
	}

	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}
	
	public KingdomChest getKingdomChest() {
		if (kingdomChest == null)
			kingdomChest = new KingdomChest(this);
		return kingdomChest;
	}
	
	public Powerup getPowerup() {
		if (powerup == null)
			powerup = new Powerup(this);
		return powerup;
	}
	
	public MiscUpgrade getMiscUpgrades() {
		if (miscUpgrade == null)
			miscUpgrade = new MiscUpgrade(this);
		return miscUpgrade;
	}
	
	public DefenderInfo getDefenderInfo() {
		if (defenderInfo == null)
			defenderInfo = new DefenderInfo(this);
		return defenderInfo;
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
		resourcePoints += points;
	}
	
	public void subtractResourcePoints(long points) {
		resourcePoints -= points;
	}
	
	public Location getNexusLocation() {
		return nexus;
	}

	public void setNexusLocation(Location nexus) {
		this.nexus = nexus;
	}
	
	public boolean isOnline() {
		return kingdomManager.isOnline(this);
	}

	public Kingdom getKingdom() {
		return kingdomManager.getKingdom(this);
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
		return members;
	}
	
	public void setShieldTime(long seconds) {
		this.shieldTime = new KingdomCooldown(this, "SHIELD", seconds);
	}
	
	public Map<Rank, RankPermissions> getPermissions() {
		return permissions;
	}
	
	public boolean equals(OfflineKingdom other) {
		return other.getUniqueId() == uuid;
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
		return rankManager.getLowestFor(this, predicate);
	}
	
	public Set<OfflineKingdom> getAllies() {
		return allies;
	}
	
	public void addAlliance(OfflineKingdom kingdom) {
		allies.add(kingdom);
	}
	
	public boolean isAllianceWith(OfflineKingdom kingdom) {
		return allies.contains(kingdom);
	}
	
	public void removeAlliance(OfflineKingdom kingdom) {
		allies.remove(kingdom);
	}
	
	public Set<OfflineKingdom> getEnemies() {
		return enemies;
	}
	
	public void addEnemy(OfflineKingdom kingdom) {
		enemies.add(kingdom);
	}
	
	public boolean isEnemyWith(OfflineKingdom kingdom) {
		return enemies.contains(kingdom);
	}
	
	public void removeEnemy(OfflineKingdom kingdom) {
		enemies.remove(kingdom);
	}
	
	/**
	 * Grabs the permissions of a rank for the Kingdom.
	 * 
	 * @param rank Rank to grab permissions for.
	 * @return RankPermissions which is an object for reading all permissions for a rank.
	 */
	public RankPermissions getPermissions(Rank rank) {
		RankPermissions permission = permissions.get(rank);
		if (permission == null) {
			permission = new RankPermissions(rank);
			permissions.put(rank, permission);
		}
		return permission;
	}

	/*public void onMemberQuitKingdom(OfflineKingdomPlayer kingdomPlayer) {
		//if (kingdomPlayer.isOnline()) {
		//	Player player = kingdomPlayer.getKingdomPlayer().getPlayer();
		//	player.closeInventory();
		//}
		//kingdomPlayer.setRank(rankManager.getDefaultRank());
		//kingdomPlayer.setKingdom(null);
		members.remove(kingdomPlayer);
		//sendAnnouncement(null, "[" + kp.getName() + "] has left your kingdom!", true);
	}*/
	
	public void onKingdomDelete(OfflineKingdom kingdom) {
		enemies.remove(kingdom);
		allies.remove(kingdom);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	public int getShieldValue() {
		return shieldValue;
	}


	public int getShieldRadius() {
		return shieldRadius;
	}

	public void setShieldValue(int shieldValue) {
		this.shieldValue = shieldValue;
		if(this.shieldValue < 0){
			this.shieldValue = 0;
		}
	}

	public void setShieldRadius(int shieldRadius) {
		this.shieldRadius = shieldRadius;
	}

	public boolean isShieldUp(){
		return getTimeLeft(SHIELD) > 0;
	}
	
	public void removeShield(){
		cancelCooldown(SHIELD);
	}
	
	public static final String CAMO = "CAMO";

	public void giveCamo(int camoTimeInMin){
		beginCooldown(CAMO, camoTimeInMin);
	}

	public boolean isCamoUp(){
		return getTimeLeft(CAMO) > 0;
	}
	
	public void removeCamo(){
		cancelCooldown(CAMO);
	}
	
	public Map<String, String> getInvasionLog() {
		return invasionLog;
	}
	
	public void clearInvasionLog() {
		invasionLog.clear();
	}
	
	public void addInvasionLog(OfflineKingdom victim, OfflineKingdomPlayer invader, boolean victorious, Land target) {
		SimpleDateFormat format1 = new SimpleDateFormat ("E dd MM YYYY");
		SimpleDateFormat format2 = new SimpleDateFormat ("hh:mm:ss a zzz");
		Date date = new Date();
		String format = format1.format(date) + "|" + format2.format(date);
		if (!invasionLog.containsKey("[" + invasionLog.size() + "] " +  format)) {
			invasionLog.put("[" + invasionLog.size() + "] " + format, victim.getKingdomName() + "," + invader.getName() + "," + victorious + "," + LocationUtils.chunkToString(target.getChunk()));
		}
	}

	public void setHasInvaded(boolean hasInvaded) {
		this.hasInvaded = hasInvaded;
	}
	
	public void beginCooldown(String name, int researchNeededTimeInMinutes){
		cooldowns.put(name.toLowerCase(), System.currentTimeMillis());
		cdTimeNeeded.put(name.toLowerCase(), TimeUnit.MINUTES.toMillis(researchNeededTimeInMinutes));
		Bukkit.getLogger().info("SET TIMER: " + System.currentTimeMillis() + ":" + researchNeededTimeInMinutes);
	}
	
	public long getTimeLeft(String name){
		String key = name.toLowerCase();
		if(!cooldowns.containsKey(key)) return 0;
		if(!cdTimeNeeded.containsKey(key)) return 0;
		if(cooldowns.get(key) == 0) return 0;
		if(cdTimeNeeded.get(key)== 0) return 0;
		return cdTimeNeeded.get(key)
				- (System.currentTimeMillis() 
				- cooldowns.get(key));
	}
	
	public void speedUp(String name, long time){
		String key = name.toLowerCase();
		cdTimeNeeded.put(key, cdTimeNeeded.get(key) - time);
		if(cdTimeNeeded.get(key) < 0)
			cdTimeNeeded.put(key, 0L);
	}
	
	public boolean isCooldownFinished(String name){
		String key = name.toLowerCase();
		return getTimeLeft(key) <= 0;
	}
	
	public void cancelCooldown(String name){
		String key = name.toLowerCase();
		cdTimeNeeded.put(key, 0L);
	}*/

}
