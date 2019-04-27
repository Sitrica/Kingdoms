package com.songoda.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class KingdomDeleteEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom kingdom;

	public KingdomDeleteEvent(OfflineKingdom kingdom) {
		super(true);
		this.kingdom = kingdom;
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
