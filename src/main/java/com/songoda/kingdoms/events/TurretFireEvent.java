package com.songoda.kingdoms.events;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.turrets.Turret;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TurretFireEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom owner;
	private final Turret turret;
	private final Player target;
	private boolean cancelled;

	public TurretFireEvent(Turret turret, Player target, OfflineKingdom landKingdom) {
		this.turret = turret;
		this.target = target;
		this.owner = landKingdom;
	}
	
	public Turret getTurret() {
		return turret;
	}
	
	public Player getTarget() {
		return target;
	}
	
	public OfflineKingdom getKingdomOwner() {
		return owner;
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
