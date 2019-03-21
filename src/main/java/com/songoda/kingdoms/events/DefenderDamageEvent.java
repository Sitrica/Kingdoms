package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class DefenderDamageEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final DefenderDamageCause cause;
	private final OfflineKingdom kingdom;
	private final Entity defender;
	private final double damage;
	private boolean cancelled;

	public enum DefenderDamageCause {
		PLAYER,
		TURRET,
		POTION
	}
	
	public DefenderDamageEvent(OfflineKingdom kingdom, Entity defender, double damage, DefenderDamageCause cause) {
		this.defender = defender;
		this.kingdom = kingdom;
		this.damage = damage;
		this.cause = cause;
	}

	public double getDamage() {
		return damage;
	}

	public Entity getDefender() {
		return defender;
	}

	public OfflineKingdom getKingdom() {
		return kingdom;
	}

	public DefenderDamageCause getCause() {
		return cause;
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
