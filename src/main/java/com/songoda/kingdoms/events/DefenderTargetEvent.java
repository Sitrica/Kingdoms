package com.songoda.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.Defender;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class DefenderTargetEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Defender defender;
	private KingdomPlayer target;
	private boolean cancelled;

	public DefenderTargetEvent(Defender defender, KingdomPlayer target) {
		this.defender = defender;
		this.target = target;
	}

	public Defender getDefender() {
		return defender;
	}

	public KingdomPlayer getTarget() {
		return target;
	}

	public void setTarget(KingdomPlayer target) {
		this.target = target;
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
