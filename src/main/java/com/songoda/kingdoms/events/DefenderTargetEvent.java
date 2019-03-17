package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class DefenderTargetEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Entity defender, target;
	private final OfflineKingdom kingdom;
	private boolean cancelled;

	public DefenderTargetEvent(OfflineKingdom kingdom, Entity defender, Entity target) {
		this.defender = defender;
		this.kingdom = kingdom;
		this.target = target;
	}
	
	public Entity getTarget() {
		return target;
	}
	
	public Entity getDefender() {
		return defender;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
