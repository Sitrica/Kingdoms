package com.songoda.kingdoms.events;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LandUnclaimEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private OfflineKingdom kingdom;
	private Land land;
	
	public LandUnclaimEvent(Land land, OfflineKingdom kingdom) {
		this.kingdom = kingdom;
		this.land = land;
	}
	
	public Land getLand() {
		return land;
	}
	
	public OfflineKingdom getKingdom() {
		return kingdom;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

}
