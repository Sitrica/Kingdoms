package me.limeglass.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.player.OfflineKingdomPlayer;

public class MemberLeaveEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdomPlayer kingdomPlayer;
	private final OfflineKingdom kingdom;

	public MemberLeaveEvent(OfflineKingdomPlayer kingdomPlayer, OfflineKingdom kingdom) {
		this.kingdomPlayer = kingdomPlayer;
		this.kingdom = kingdom;
	}

	public OfflineKingdomPlayer getKingdomPlayer() {
		return kingdomPlayer;
	}

	public OfflineKingdom getKingdom() {
		return kingdom;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
