package com.songoda.kingdoms.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.Formatting;

public abstract class StructureInventory extends KingdomInventory {
	
	public StructureInventory(InventoryType type, String structure, int size) {
		super(type, getStructureTitle(structure), size);
	}
	
	public static String getStructureTitle(String structure) {
		FileConfiguration structures = Kingdoms.getInstance().getConfiguration("structures").get();
		String title = structures.getString("structures." + structure + ".title");
		return Formatting.color(title);
	}
	
	public String getTitle(String structure) {
		return getStructureTitle(structure);
	}

}
