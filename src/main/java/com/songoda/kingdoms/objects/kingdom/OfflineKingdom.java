package com.songoda.kingdoms.objects.kingdom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.songoda.kingdoms.manager.game.GameManagement;
import com.songoda.kingdoms.manager.game.KingdomManager;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.LocationUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class OfflineKingdom {

	private int might = 0, resourcepoints = 0, extraLandClaims = 0, timeLeftToNextInvasion = 0;
	private final Map<String, String> invasionLog = new HashMap<>();
	private final Map<String, Long> cooldowns = new HashMap<>();
	private final List<UUID> members = new ArrayList<>();
	private final List<UUID> enemies= new ArrayList<>();
	private final List<UUID> allies = new ArrayList<>();
	private String kingName, kingdomName, kingdomLore;
	private boolean isNeutral, hasInvaded;
	private UUID uuid, king;
	private int dynmapColor = KingdomManager.getRandomColor();
	
	HashMap<String, Long> cdTimeNeeded = new HashMap<String, Long>();
	
	protected OfflineKingdom() {
		this(UUID.randomUUID());
	}
	
	public OfflineKingdom(UUID uuid) {
		this.uuid = uuid;
	}
	
	public void clearInvasionLog(){
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

	
	private int shieldValue = 0;
	private int shieldRadius = 0;
	
	
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

	public boolean isOnline(){
		return GameManagement.getKingdomManager().isOnline(kingdomName);
	}
	
	public String getKingdomName() {
		return kingdomName;
	}

	public void setKingdomName(String kingdomName) {
		this.kingdomName = kingdomName;
	}

	public Kingdom getKingdom(){
		return GameManagement.getKingdomManager().getOrLoadKingdom(kingdomName);
	}
	
	public String getKingName(){
		if(kingName == null){
			OfflinePlayer p = Bukkit.getOfflinePlayer(king);
			if(p != null) kingName = p.getName();
		}
		
		return kingName;
	}
	public int getResourcepoints() {
		return resourcepoints;
	}

	public int getMight() {
		return might;
	}

	public String getKingdomLore() {
		return kingdomLore;
	}

	public void setKing(UUID uuid){
		this.king = uuid;
	}
	
	public UUID getKing() {
		return king;
	}
	
	public int getDynmapColor() {
		return dynmapColor;
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

	public boolean isNeutral() {
		return isNeutral;
	}

	public boolean hasInvaded() {
		return hasInvaded;
	}

	public void setNeutral(boolean isNeutral) {
		this.isNeutral = isNeutral;
	}

	public int getExtraLandClaims() {
		return extraLandClaims;
	}

	public void setExtraLandClaims(int additionalLandMax) {
		this.extraLandClaims = additionalLandMax;
	}

	public void setHasInvaded(boolean hasInvaded) {
		this.hasInvaded = hasInvaded;
	}
	
	//private long researchStart = 0;
	//private long researchNeededTimeInMS = 0;
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
		//return researchNeededTimeInMS - (System.currentTimeMillis() - researchStart);
		return cdTimeNeeded.get(key)
				- (System.currentTimeMillis() 
				- cooldowns.get(key));
	}
	
	public void speedUp(String name, long time){
		String key = name.toLowerCase();
		cdTimeNeeded.put(key, cdTimeNeeded.get(key) - time);
		//researchNeededTimeInMS -= time;
		//if(researchNeededTimeInMS < 0) researchNeededTimeInMS = 0;
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

	public UUID getKingdomUuid() {
		return uuid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kingdomName == null) ? 0 : kingdomName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OfflineKingdom other = (OfflineKingdom) obj;
		if (kingdomName == null) {
			if (other.kingdomName != null)
				return false;
		} else if (!kingdomName.equals(other.kingdomName))
			return false;
		return true;
	}
}
