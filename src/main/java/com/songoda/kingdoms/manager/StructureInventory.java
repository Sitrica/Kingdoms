package com.songoda.kingdoms.manager;

import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.Formatting;

public abstract class StructureInventory extends KingdomInventory {
	
	public StructureInventory(String structure, int size) {
		super(getStructureTitle(structure), size);
	}
	
	public static String getStructureTitle(String structure) {
		FileConfiguration structures = Kingdoms.getInstance().getConfiguration("structures").get();
		String title = structures.getString("structures." + structure + ".title");
		return Formatting.color(title);
	}

}
