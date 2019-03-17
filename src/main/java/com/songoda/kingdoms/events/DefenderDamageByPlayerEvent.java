package com.songoda.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class DefenderDamageByPlayerEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom kingdom;
	private final KingdomPlayer player;
	private final Entity defender;
	private final double damage;
	private boolean cancelled;

	public DefenderDamageByPlayerEvent(OfflineKingdom kingdom, Entity defender, KingdomPlayer player, double damage) {
		this.defender = defender;
		this.kingdom = kingdom;
		this.player = player;
		this.damage = damage;
	}

	public double getDamage() {
		return damage;
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
