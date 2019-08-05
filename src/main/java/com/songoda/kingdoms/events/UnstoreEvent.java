package com.songoda.kingdoms.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class UnstoreEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Map<Integer, ItemStack> items = new HashMap<>();
	private final Inventory inventory;
	private final Player player;
	private boolean cancelled;
	
	public UnstoreEvent(Player player, Inventory inventory, Map<Integer, ItemStack> items) {
		this.inventory = inventory;
		this.items.putAll(items);
		this.player = player;
	}
	
	public UnstoreEvent(Player player, ItemStack item, Inventory inventory, Integer... slots) {
		this.inventory = inventory;
		this.player = player;
		for (int slot : slots)
			items.put(slot, item);
	}

	public UnstoreEvent(Player player, List<ItemStack> items, Inventory inventory, Integer... slots) {
		this.inventory = inventory;
		this.player = player;
		int i = 0;
		for (int slot : slots) {
			ItemStack item = items.get(i);
			if (item != null) {
				this.items.put(slot, item);
			}
			i++;
		}
	}

	public Map<Integer, ItemStack> getItems() {
		return items;
	}

	public Player getPlayer() {
		return player;
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

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
