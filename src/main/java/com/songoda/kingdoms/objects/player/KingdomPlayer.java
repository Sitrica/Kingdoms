package com.songoda.kingdoms.objects.player;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.manager.managers.ChatManager.ChatChannel;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

public class KingdomPlayer extends OfflineKingdomPlayer implements Challenger {

	private boolean autoClaiming, autoMapping, vanished, admin;
	public ChatChannel channel = ChatChannel.PUBLIC;
	private transient LivingEntity opponent;
	private transient Land invading;
	private final Player player;

	public KingdomPlayer(Player player) {
		super(player);
		this.player = player;
	}

	public KingdomPlayer(Player player, OfflineKingdomPlayer other) {
		super(player);
		this.player = player;
		this.rank = other.getRank();
		OfflineKingdom kingdom = other.getKingdom();
		if (kingdom != null)
			this.kingdom = kingdom.getName();
	}

	public Player getPlayer() {
		return player;
	}

	public Chunk getChunkAt() {
		return player.getLocation().getChunk();
	}

	public Location getLocation() {
		return player.getLocation();
	}

	public boolean hasAdminMode() {
		return admin;
	}

	public void setAdminMode(boolean admin) {
		this.admin = admin;
	}

	public boolean isVanished() {
		return vanished;
	}

	public void setVanished(boolean vanished) {
		this.vanished = vanished;
	}

	public boolean isAutoMapping() {
		return autoMapping;
	}

	public void setAutoMapping(boolean autoMapping) {
		this.autoMapping = autoMapping;
	}

	public boolean isAutoClaiming() {
		return autoClaiming;
	}

	public Kingdom getKingdom() {
		return super.getKingdom().getKingdom();
	}

	public void setAutoClaiming(boolean autoClaiming) {
		this.autoClaiming = autoClaiming;
	}

	@Override
	public boolean isInvading() {
		return invading != null;
	}

	@Override
	public Land getInvadingLand() {
		return invading;
	}

	@Override
	public void setInvadingLand(Land invading) {
		this.invading = invading;
	}

	public ChatChannel getChatChannel() {
		return channel;
	}

	public void setChatChannel(ChatChannel channel) {
		this.channel = channel;
	}

	@Override
	public LivingEntity getOpponent() {
		return opponent;
	}

	@Override
	public void setOpponent(LivingEntity opponent) {
		this.opponent = opponent;
	}
	
	
	
	
	
	
	





	




	/*
	private transient Entity champion = null;
	@Override
	public Entity getChampionPlayerFightingWith() {
		return champion;
	}

	@Override
	public void setChampionPlayerFightingWith(Entity champion) {
		this.champion = champion;
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
	*/

}
