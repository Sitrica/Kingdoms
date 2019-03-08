package com.songoda.kingdoms.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class UnstoreEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Inventory inventory;
	private final ItemStack cursor;
	private final Player player;
	private boolean cancelled;
	private final int slot;
	
	public UnstoreEvent(Player player, ItemStack cursor, Inventory inventory, int slot) {
		this.inventory = inventory;
		this.player = player;
		this.cursor = cursor;
		this.slot = slot;
	}

	public int getSlot() {
		return slot;
	}

	public Player getPlayer() {
		return player;
	}
	
	public ItemStack getCursor() {
		return cursor;
	}

	public Inventory getInventory() {
		return inventory;
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
