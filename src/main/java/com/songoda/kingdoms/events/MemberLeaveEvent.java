package com.songoda.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

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
