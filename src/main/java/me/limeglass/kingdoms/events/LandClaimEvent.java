package me.limeglass.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.land.Land;

public class LandClaimEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Kingdom kingdom;
	private Land land;
	
	public LandClaimEvent(Land land, Kingdom kingdom) {
		this.kingdom = kingdom;
		this.land = land;
	}
	
	public Land getLand() {
		return land;
	}
	
	public Kingdom getKingdom() {
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
