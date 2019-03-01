package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class DefenderDamageMaxedEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Entity attacker, defender;
	private final OfflineKingdom kingdom;
	private final double dealt;
	private boolean cancelled;
	private final int limit;

	public DefenderDamageMaxedEvent(OfflineKingdom kingdom, Entity defender, Entity attacker, int limit, double dealt) {
		this.defender = defender;
		this.attacker = attacker;
		this.kingdom = kingdom;
		this.limit = limit;
		this.dealt = dealt;
	}

	public int getLimit() {
		return limit;
	}

	public Entity getDefender() {
		return defender;
	}
	
	public Entity getAttacker() {
		return attacker;
	}

	public double getDamageDealt() {
		return dealt;
	}

	public OfflineKingdom getDefenderKingdom() {
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
	
}
