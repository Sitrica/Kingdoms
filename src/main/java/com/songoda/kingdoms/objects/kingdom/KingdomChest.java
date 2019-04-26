package com.songoda.kingdoms.objects.kingdom;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.MessageBuilder;

public class KingdomChest {

	private final Map<Integer, ItemStack> contents = new HashMap<>();
	private final OfflineKingdom kingdom;
	private final Kingdoms instance;
	private Inventory inventory;
	private final String title;
	private int size = 27;

	public KingdomChest(OfflineKingdom kingdom) {
		this.kingdom = kingdom;
		this.instance = Kingdoms.getInstance();
		this.title = new MessageBuilder("inventories.nexus-chest.title")
				.fromConfiguration(instance.getConfiguration("inventories").get())
				.setKingdom(kingdom)
				.get();
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public OfflineKingdom getKingdom() {
		return kingdom;
	}

	public Map<Integer, ItemStack> getContents() {
		contents.clear();
		if (inventory == null)
			inventory = getInventory();
		inventory.forEach(itemstack -> contents.putAll(inventory.all(itemstack)));
		return contents;
	}

	public Inventory getInventory() {
		if (inventory != null)
			return inventory;
		inventory = Bukkit.createInventory(null, size, title);
		if (contents != null && !contents.isEmpty()) {
			for (Entry<Integer, ItemStack> entry : contents.entrySet())
				inventory.setItem(entry.getKey(), entry.getValue());
		}
		return inventory;
	}

	public void setContents(Map<Integer, ItemStack> contents) {
		this.contents.clear();
		this.contents.putAll(contents);
		inventory = null;
		getInventory();
	}

}
