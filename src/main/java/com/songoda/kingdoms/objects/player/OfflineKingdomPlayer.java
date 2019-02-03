package com.songoda.kingdoms.objects.player;

import java.util.Date;
import java.util.UUID;

import com.songoda.kingdoms.manager.game.GameManagement;
import com.songoda.kingdoms.objects.KingdomPlayer;
import com.songoda.kingdoms.constants.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class OfflineKingdomPlayer{
	protected UUID kingdomUuid;
	protected Rank rank;
	protected UUID uuid;
	protected String lastKnownName;
	protected int donatedAmt;
	protected Date lastTimeDonated;
	protected int lastDonatedAmt;
	protected String lang;
	protected OfflineKingdomPlayer(){super();}
	public OfflineKingdomPlayer(UUID uuid){
		super();
		this.uuid = uuid;
		if(this.rank == null) this.rank = Rank.ALL;
		if(this.donatedAmt <= 0) this.donatedAmt = 0;
		if(this.lastDonatedAmt <= 0) this.lastDonatedAmt = 0;
		if (this.lang == null) this.lang = "eng";
	}
	public OfflineKingdomPlayer(OfflinePlayer offp){
		super();
		this.uuid = offp.getUniqueId();
		if(this.rank == null) this.rank = Rank.ALL;
		if(this.donatedAmt <= 0) this.donatedAmt = 0;
		if(this.lastDonatedAmt <= 0) this.lastDonatedAmt = 0;
	}
	
	protected boolean isVanishMode = false;
	public boolean isVanishMode(){
		return this.isVanishMode;
	}
	public void setVanishMode(boolean boo){
		this.isVanishMode = boo;
	}
	
	protected boolean markDisplaying;
	public void setMarkDisplaying(boolean bool) {
		this.markDisplaying = bool;
	}

	public boolean isMarkDisplaying() {
		return markDisplaying;
	}
	
	public int getDonatedAmt(){
		return donatedAmt;
	}
	public Rank getRank() {
		//Kingdom kingdom = Kingdoms.getManagers().getKingdomManager().
		return rank;
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setKingdomName(String kingdomName) {
		this.kingdomUuid = GameManagement.getKingdomManager().getOrLoadKingdom(kingdomName).getKingdomUuid();
	}

	public UUID getKingdomUuid() {
		return kingdomUuid;
	}

	public void setKingdomUuid(UUID kingdomUuid) {
		this.kingdomUuid = kingdomUuid;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}
	public void setDonatedAmt(int donatedAmt){
		this.donatedAmt = donatedAmt;
	}
	public String getKingdomName() {
		if(this.kingdomUuid!=null){
		    return GameManagement.getKingdomManager().getOrLoadKingdom(kingdomUuid).getKingdomName();
		}else{
			return null;
		}
	}
	public String getName(){
		OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
		if(offp == null) return null;
		
		lastKnownName = offp.getName();
		return lastKnownName;
	}
	
	public KingdomPlayer getKingdomPlayer(){
		return GameManagement.getPlayerManager().getSession(uuid);
	}
	
	public Date getLastTimeDonated() {
		return lastTimeDonated;
	}
	public void setLastTimeDonated(Date lastTimeDonated) {
		this.lastTimeDonated = lastTimeDonated;
	}
	public int getLastDonatedAmt() {
		return lastDonatedAmt;
	}
	public void setLastDonatedAmt(int lastDonatedAmt) {
		this.lastDonatedAmt = lastDonatedAmt;
	}
	public boolean isOnline(){
		return GameManagement.getPlayerManager().isOnline(uuid);
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}
