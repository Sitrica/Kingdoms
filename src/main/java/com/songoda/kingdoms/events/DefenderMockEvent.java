package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class DefenderMockEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom kingdom;
	private boolean cancelled, event;
	private final Entity defender;
	private int range;

	public DefenderMockEvent(OfflineKingdom kingdom, Entity defender, int range) {
		this.defender = defender;
		this.kingdom = kingdom;
		this.range = range;
	}

	public OfflineKingdom getDefenderKingdom() {
		return kingdom;
	}

	public Entity getDefender() {
		return defender;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	/**
	 * Check the cancellation of the mocking event.
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Set the cancellation of the mocking event.
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	/**
	 * Check the cancellation of the block placing event allocated to this mock event.
	 */
	public boolean isEventCancelled() {
		return event;
	}

	/**
	 * Set the cancellation of the block placing event allocated to this mock event.
	 */
	public void setEventCancelled(boolean event) {
		this.event = event;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
