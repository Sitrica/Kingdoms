package com.songoda.kingdoms.events;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NexusMoveEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Location location, previousNexus;
	private final KingdomPlayer player;
	private final Land land, previous;
	private final Kingdom kingdom;
	private boolean cancelled;
	private String message;

	public NexusMoveEvent(KingdomPlayer player, Kingdom kingdom, Location location, Location previousNexus, Land land, Land previous) {
		this.message = Kingdoms.getInstance().getConfiguration("messages").get().getString("structures.nexus-move-cancelled");
		this.previousNexus = previousNexus;
		this.location = location;
		this.previous = previous;
		this.kingdom = kingdom;
		this.player = player;
		this.land = land;
	}

	public Land getLand() {
		return land;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}

	public Land getPreviousLand() {
		return previous;
	}
	
	public Location getPlaceLocation() {
		return location;
	}
	
	public Location getPreviousNexus() {
		return previousNexus;
	}

	public KingdomPlayer getKingdomPlayer() {
		return player;
	}
	
	public String getMessage() {
		if (message == null)
			return "&cYou're not allowed to move the nexus.";
		return message;
	}
	
	/**
	 * Set the message of the cancellation move.
	 * &1 &l color support.
	 * 
	 * @param message The message to change too.
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
