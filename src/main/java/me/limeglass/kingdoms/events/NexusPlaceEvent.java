package me.limeglass.kingdoms.events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class NexusPlaceEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final KingdomPlayer player;
	private final Location location;
	private final Kingdom kingdom;
	private boolean cancelled;
	private final Land land;

	public NexusPlaceEvent(KingdomPlayer player, Kingdom kingdom, Location location, Land land) {
		this.location = location;
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

	public Location getPlaceLocation() {
		return location;
	}

	public KingdomPlayer getKingdomPlayer() {
		return player;
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
