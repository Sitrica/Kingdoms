package com.songoda.kingdoms.manager.inventories;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;

import com.songoda.kingdoms.Kingdoms;

public abstract class StructureInventory extends KingdomInventory {

	protected final FileConfiguration structures;

	public StructureInventory(InventoryType type, String structure, int size) {
		super(type, structure, null, size);
		structures = Kingdoms.getInstance().getConfiguration("structures").get();
	}

}
