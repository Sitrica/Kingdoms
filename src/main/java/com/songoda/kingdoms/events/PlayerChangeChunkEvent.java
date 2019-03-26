package com.songoda.kingdoms.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerChangeChunkEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Chunk from, to;
	private final Player player;
	private boolean cancelled;

	public PlayerChangeChunkEvent(Player player, Chunk from, Chunk to) {
		this.player = player;
		this.from = from;
		this.to = to;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Chunk getToChunk() {
		return to;
	}

	public Chunk getFromChunk() {
		return from;
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
