package com.songoda.kingdoms.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public abstract class KingdomInventory {
	
	protected final Map<Integer, Runnable> actions = new HashMap<>();
	protected final InventoryManager inventoryManager;
	protected final FileConfiguration configuration;
	protected final Inventory inventory;
	protected final Kingdoms instance;
	
	public KingdomInventory(String name, int size) {
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.inventoryManager = instance.getManager("inventory", InventoryManager.class);
		this.inventory = instance.getServer().createInventory(null, size * 9, name);
	}
	
	public abstract void openInventory(KingdomPlayer player);
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public void setAction(int slot, Runnable runnable) {
		actions.put(slot, runnable);
	}
	
	public void openInventory(Player player) {
		player.openInventory(inventory);
		inventoryManager.opening(player.getUniqueId(), this);
	}
	
	public Runnable getAction(int slot) {
		if (!actions.containsKey(slot))
			return null;
		return actions.get(slot);
	}

}
