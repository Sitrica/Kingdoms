package com.songoda.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class KingdomInvadeEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom target;
	private final KingdomPlayer invoker;
	private final Kingdom invader;
	private boolean cancelled;

	public KingdomInvadeEvent(KingdomPlayer invoker, Kingdom invader, OfflineKingdom target) {
		this.invoker = invoker;
		this.invader = invader;
		this.target = target;
	}

	public KingdomPlayer getInvoker() {
		return invoker;
	}

	public OfflineKingdom getTarget() {
		return target;
	}

	public Kingdom getInvader() {
		return invader;
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
