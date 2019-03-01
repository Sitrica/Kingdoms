package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class DefenderFocusEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom kingdom;
	private final KingdomPlayer player;
	private final Entity defender;
	private boolean cancelled;

	public DefenderFocusEvent(OfflineKingdom kingdom, Entity defender, KingdomPlayer player) {
		this.defender = defender;
		this.kingdom = kingdom;
		this.player = player;
	}

	public Entity getDefender() {
		return defender;
	}
	
	public KingdomPlayer getFocusedPlayer() {
		return player;
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
