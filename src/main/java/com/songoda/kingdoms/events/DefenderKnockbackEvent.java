package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.Defender;

public class DefenderKnockbackEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Defender defender;
	private final Entity damager;
	private boolean cancelled;
	
	public DefenderKnockbackEvent(Defender defender, Entity damager) {
		this.defender = defender;
		this.damager = damager;
	}
	
	/**
	 * @return The entity that is causing the knockback to be cancelled.
	 */
	public Entity getDamager() {
		return damager;
	}

	public Defender getDefender() {
		return defender;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Cancel the cancellation of the knockback.
	 * 
	 * @param cancelled If the event should be cancelled.
	 */
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
