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
		OfflineKingdom kingdom = super.getKingdom();
		if (kingdom == null)
			return null;
		return kingdom.getKingdom();
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

}
