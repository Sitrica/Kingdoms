package me.limeglass.kingdoms.objects.player;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.manager.managers.LandManager;
import me.limeglass.kingdoms.manager.managers.ChatManager.ChatChannel;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;

public class KingdomPlayer extends OfflineKingdomPlayer {

	private boolean autoClaiming, autoMapping, vanished, admin;
	public ChatChannel channel = ChatChannel.PUBLIC;
	private transient LivingEntity opponent;
	private final Player player;

	public KingdomPlayer(Player player) {
		super(player);
		this.player = player;
	}

	public KingdomPlayer(Player player, OfflineKingdomPlayer other) {
		super(player, other.getRank());
		this.kingdom = other.kingdom;
		claims.addAll(other.claims);
		this.player = player;
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

	public ChatChannel getChatChannel() {
		return channel;
	}

	public void setChatChannel(ChatChannel channel) {
		this.channel = channel;
	}

	public LivingEntity getOpponent() {
		return opponent;
	}

	public void setOpponent(LivingEntity opponent) {
		this.opponent = opponent;
	}

	public Land getLandAt() {
		return instance.getManager(LandManager.class).getLandAt(player.getLocation());
	}

}
