package com.songoda.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.Defender;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class DefenderDamageMaxedEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final KingdomPlayer attacker;
	private final Defender defender;
	private final double dealt;
	private boolean cancelled;
	private final int limit;

	public DefenderDamageMaxedEvent(Defender defender, KingdomPlayer attacker, int limit, double dealt) {
		this.defender = defender;
		this.attacker = attacker;
		this.limit = limit;
		this.dealt = dealt;
	}

	public int getLimit() {
		return limit;
	}

	public Defender getDefender() {
		return defender;
	}

	public double getDamageDealt() {
		return dealt;
	}

	public KingdomPlayer getAttacker() {
		return attacker;
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
