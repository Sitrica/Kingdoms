package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class DefenderDragEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom kingdom;
	private final KingdomPlayer player;
	private final Entity defender;
	private final double range;
	private boolean cancelled;

	public DefenderDragEvent(OfflineKingdom kingdom, Entity defender, KingdomPlayer player, double range) {
		this.defender = defender;
		this.kingdom = kingdom;
		this.player = player;
		this.range = range;
	}

	public double getRange() {
		return range;
	}
	
	public Entity getDefender() {
		return defender;
	}

	public KingdomPlayer getPlayer() {
		return player;
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
