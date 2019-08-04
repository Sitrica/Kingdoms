package me.limeglass.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;

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
