package com.songoda.kingdoms.objects.player;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.manager.managers.ChatManager.ChatChannel;

public class KingdomPlayer extends OfflineKingdomPlayer implements Challenger {//Pioneer, Member, PrivateChat, Confirmable {
	
	private boolean autoClaiming, autoMapping, vanished, admin;
	public ChatChannel channel = ChatChannel.PUBLIC;
	private transient LivingEntity opponent;
	private transient Chunk invading;
	private final Player player;
	
	public KingdomPlayer(Player player) {
		super(player);
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Chunk getChunkAt() {
		return player.getLocation().getChunk();
	}
	
	public boolean hasKingdom() {
		return kingdom == null;
	}
	
	public Location getLocation() {
		return player.getLocation();
	}
	
	public boolean isAdminMode() {
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
	
	public void setAutoClaiming(boolean autoClaiming) {
		this.autoClaiming = autoClaiming;
	}
	
	@Override
	public Chunk getInvadingChunk() {
		return invading;
	}

	@Override
	public void setInvadingChunk(Chunk invading) {
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
	
	
	
	
	
	
	





	



	/*private transient boolean creatingKingdom = false;
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
	*/

}
