package com.songoda.kingdoms.objects.kingdom;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.CooldownManager.KingdomCooldown;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public class OfflineKingdom {

	private long might = 0, claims = 0, resourcePoints = 0, invasionCooldown = 0;
	protected final Set<OfflineKingdomPlayer> members = new HashSet<>();
	private final KingdomManager kingdomManager;
	private KingdomCooldown shieldTime;
	private OfflineKingdomPlayer king;
	protected final Kingdoms instance;
	private String name, lore;
	private final UUID uuid;
	private int dynmapColor;
	private boolean neutral;
	
	/*
	private final HashMap<String, Long> cdTimeNeeded = new HashMap<>();
	private final Map<String, String> invasionLog = new HashMap<>();
	private final Map<String, Long> cooldowns = new HashMap<>();
	private final List<UUID> enemies= new ArrayList<>();
	private final List<UUID> allies = new ArrayList<>();
	private int shieldValue = 0, shieldRadius = 0;
	private boolean hasInvaded;
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
	
	public long getMight() {
		return might;
	}
	
	public void setMight(int might) {
		this.might = might;
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
	
	public long getClaims() {
		return claims;
	}
	
	public void setClaims(int claims) {
		this.claims = claims;
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
	
	public long getResourcePoints() {
		return resourcePoints;
	}
	
	public void setResourcePoints(long points) {
		resourcePoints = points;
	}
	
	public void addResourcePoints(long points) {
		resourcePoints += points;
	}
	
	public boolean isOnline() {
		return kingdomManager.isOnline(this);
	}

	public Kingdom getKingdom() {
		return kingdomManager.getKingdom(this);
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
	
	public boolean hasInvaded() {
		return hasInvaded;
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
