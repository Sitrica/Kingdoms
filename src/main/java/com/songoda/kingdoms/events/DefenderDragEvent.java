package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.invasions.Invasion;

public class DefenderDragEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Invasion invasion;
	private final Entity defender;
	private final double range;
	private boolean cancelled;

	public DefenderDragEvent(Entity defender, Invasion invasion, double range) {
		this.defender = defender;
		this.invasion = invasion;
		this.range = range;
	}

	public double getRange() {
		return range;
	}
	
	public Entity getDefender() {
		return defender;
	}

	public Invasion getInvasion() {
		return invasion;
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
