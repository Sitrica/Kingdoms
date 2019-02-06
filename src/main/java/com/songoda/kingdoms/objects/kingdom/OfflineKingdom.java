package com.songoda.kingdoms.objects.kingdom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.LocationUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class OfflineKingdom {

	private long might = 0, resourcepoints = 0, claims = 0, invasionCooldown = 0;
	private final HashMap<String, Long> cdTimeNeeded = new HashMap<>();
	private final Map<String, String> invasionLog = new HashMap<>();
	private final Map<String, Long> cooldowns = new HashMap<>();
	private final List<UUID> members = new ArrayList<>();
	private final List<UUID> enemies= new ArrayList<>();
	private final List<UUID> allies = new ArrayList<>();
	private int shieldValue = 0, shieldRadius = 0;
	private final KingdomManager kingdomManager;
	private String kingName, kingdomName, lore;
	private boolean neutral, hasInvaded;
	private UUID uuid, king;
	private int dynmapColor;
	
	protected OfflineKingdom() {
		this(UUID.randomUUID());
	}
	
	public OfflineKingdom(UUID uuid) {
		this.kingdomManager = Kingdoms.getInstance().getManager("kingdom", KingdomManager.class);
		this.dynmapColor = kingdomManager.getRandomColor();
		this.uuid = uuid;
	}
	
	public UUID getKing() {
		return king;
	}
	
	public void setKing(UUID king) {
		this.king = king;
	}
	
	public String getName() {
		return kingdomName;
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
	
	public void setName(String kingdomName) {
		if (kingdomManager.renameKingdom(this, kingdomName))
			this.kingdomName = kingdomName;
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
	
	public long getResourcepoints() {
		return resourcepoints;
	}
	
	public boolean isOnline() {
		return kingdomManager.isOnline(kingdomName);
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


	public static final String SHIELD = "SHIELD";
  
    public void giveShield(int shieldTimeInMin){
    	beginCooldown(SHIELD, shieldTimeInMin);
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
	
	public String getKingName(){
		if(kingName == null){
			OfflinePlayer p = Bukkit.getOfflinePlayer(king);
			if(p != null) kingName = p.getName();
		}
		
		return kingName;
	}
	
	public int getDynmapColor() {
		return dynmapColor;
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
	
	public List<UUID> getMembersList() {
		ArrayList<UUID> contained = new ArrayList<UUID>();
		for(UUID uuid : members) {
			if(!contained.contains(uuid))contained.add(uuid);
		}
		members = contained;
		return members;
	}
	
	public List<UUID> getEnemies() {
		return enemies;
	}

	public List<UUID> getAllies() {
		return allies;
	}
	
	public boolean isAllianceWith(Kingdom ally){
		if(ally == null) return false;
		if(allies.contains(ally.getKingdomUuid())&&
				ally.getAllies().contains(getKingdomUuid())) return true;
		
		return false;
	}
	
	public boolean isEnemyWith(Kingdom enemy){
		if(enemy == null) return false;
		if(enemies.contains(enemy.getKingdomUuid())) return true;
		
		return false;
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
	}

}
