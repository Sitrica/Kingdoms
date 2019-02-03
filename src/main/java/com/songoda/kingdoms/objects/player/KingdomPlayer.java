package com.songoda.kingdoms.objects.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import com.songoda.kingdoms.constants.ChatChannel;
import com.songoda.kingdoms.constants.Pioneer;
import com.songoda.kingdoms.constants.kingdom.Kingdom;
import com.songoda.kingdoms.constants.land.KChestSign;
import com.songoda.kingdoms.constants.land.SimpleChunkLocation;
import com.songoda.kingdoms.constants.player.Challenger;
import com.songoda.kingdoms.constants.player.Confirmable;
import com.songoda.kingdoms.constants.player.KSignModifier;
import com.songoda.kingdoms.constants.player.Markable;
import com.songoda.kingdoms.constants.player.Member;
import com.songoda.kingdoms.constants.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.constants.player.PrivateChat;
import com.songoda.kingdoms.events.KingdomPlayerChatChannelChangeEvent;
import com.songoda.kingdoms.main.Kingdoms;
import com.songoda.kingdoms.manager.game.GameManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class KingdomPlayer extends OfflineKingdomPlayer implements KSignModifier, Pioneer, Challenger, Member, PrivateChat, Markable, Confirmable {
	
	private transient Kingdom kingdom;
	private boolean temp;
	
	private KingdomPlayer() {
		super();
	}
	
	public KingdomPlayer(Player player) {
		super(player.getUniqueId());

	}

    public KingdomPlayer(UUID uuid) {
        super(uuid);

    }
	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
		//return player;
	}
	public Kingdom getKingdom() {
//		if(kingdom == null && this.getKingdomUuid() != null){
//			kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(kingdomUuid);
//		}
		if (kingdomUuid!=null) {
			return GameManagement.getKingdomManager().getOrLoadKingdom(kingdomUuid);
		}
		return null;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public void setKingdom(Kingdom kingdom) {
		this.kingdom = kingdom;
		super.setKingdomUuid(kingdom == null ? null : kingdom.getKingdomUuid());
	}
	public void sendMessage(String message){
		if (message.isEmpty() || message.equals("{null}")) return;

//		if(GameManagement.getApiManager().isPlaceHolderAPIEnabled()){
//			message = GameManagement.getApiManager().setPlaceHolder(player, message);
//		}
//		if(GameManagement.getApiManager().isMVdWPlaceholderAPIEnabled()){
//			message = GameManagement.getApiManager().setPlaceHolderMvdW(player, message);
//		}
		getPlayer().sendMessage(Kingdoms.getLang().getString("Plugin_Display") +
				" " +
				ChatColor.GRAY+message);
	}

	private boolean isKMapOn = false;
	public boolean isKMapOn(){
		return isKMapOn;
	}

	public void setKMapOn(boolean isKMapOn){
		this.isKMapOn = isKMapOn;
	}
	////////////////////////////////////////////////////////////
	private boolean isKAutoClaimOn = false;
	public boolean isKAutoClaimOn(){
		return isKAutoClaimOn;
	}

	public void setKAutoClaimOn(boolean isKAutoClaimOn){
		this.isKAutoClaimOn = isKAutoClaimOn;
	}
	////////////////////////////////////////////////////////////
	private transient ChunkLocation loc;
	public ChunkLocation getLoc() {
		return new ChunkLocation(this.getPlayer().getLocation().getChunk());
	}

//	public void setLoc(SimpleChunkLocation loc) {
//		this.loc = loc;
//	}
	///////////////////////////////////////////////////////////
	private transient boolean isAdminMode = false;
	public boolean isAdminMode(){
		return this.isAdminMode;
	}
	public void setAdminMode(boolean boo){
		this.isAdminMode = boo;
	}
	
	///////////////////////////////////////////////////////////
	private transient Location loginLocation;
	public Location getLoginLocation() {
		return loginLocation;
	}

	public void setLoginLocation(Location loginLocation) {
		this.loginLocation = loginLocation;
	}
	////////////////////////////////////////////////////////////
	private transient boolean creatingKingdom = false;
	@Override
	public boolean isProcessing() {
		return creatingKingdom;
	}

	@Override
	public void setProcessing(boolean bool) {
		creatingKingdom = bool;
	}

	/////////////////////////////////////////////////////////////
	private transient Entity champion = null;
	@Override
	public Entity getChampionPlayerFightingWith() {
		return champion;
	}

	@Override
	public void setChampionPlayerFightingWith(Entity champion) {
		this.champion = champion;
	}

	transient ChunkLocation fightZone = null;
	@Override
	public ChunkLocation getFightZone() {
		return fightZone;
	}

	public boolean isTemp() {
		return temp;
	}
	public void setTemp(boolean temp) {
		this.temp = temp;
	}
	@Override
	public void setInvadingChunk(ChunkLocation loc) {
		this.fightZone = loc;
	}
	/////////////////////////////////////////////////////////////
	private transient Kingdom invited;
	@Override
	public Kingdom getInvited() {
		return invited;
	}

	@Override
	public void setInvited(Kingdom kingdom) {
		this.invited = kingdom;
	}
	/////////////////////////////////////////////////////////////
	private transient ChatChannel channel = ChatChannel.PUBLIC;
	@Override
	public ChatChannel getChannel() {
		return channel;
	}

	@Override
	public void setChannel(ChatChannel channel) {
		KingdomPlayerChatChannelChangeEvent ev = new KingdomPlayerChatChannelChangeEvent(this, this.channel, channel);
		Bukkit.getPluginManager().callEvent(ev);
		if(ev.isCancelled()) return;
		this.channel = ev.getNewChatChannel();
	}
	////////////////////////////////////////////////////////////
	private transient Queue<Location> blocks = new LinkedList<Location>();
	@Override
	public Queue<Location> getLastMarkedChunk() {
		return blocks;
	}


	private transient long lastDisplayTime = 0;
	@Override
	public Long getLastDisplayTime() {
		return lastDisplayTime;
	}

	@Override
	public void setLastDisplayTime(Long time) {
		lastDisplayTime = time;
	}
	///////////////////////////////////////////////////////////////////////
	private transient List<String> isConfirmed = new ArrayList<String>();
	@Override
	public boolean resetAllConfirmation() {
		if(isConfirmed.isEmpty()) return false;

		isConfirmed.clear();
		return true;
	}
	@Override
	public boolean isConfirmed(String key) {
		return isConfirmed.remove(key);
	}
	@Override
	public void setConfirmed(String key) {
		isConfirmed.add(key);
	}
	/////////////////////////////////////////////////////////////////////////
	private transient KChestSign modifyingSign = null;
	@Override
	public KChestSign getModifyingSign() {
		return modifyingSign;
	}
	@Override
	public void setModifyingSign(KChestSign sign) {
		modifyingSign = sign;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getUuid().hashCode() + ((rank == null) ? 0 : rank.hashCode());
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
		KingdomPlayer other = (KingdomPlayer) obj;
		
		if (getPlayer() == null) {
			if (other.getPlayer() != null)
				return false;
		} else {
			if(other.getPlayer() == null) return false;
			
			if(!getPlayer().getUniqueId().equals(other.getPlayer().getUniqueId())){
				return false;
			}
		}
		return true;
	}
}
