package com.songoda.kingdoms.objects;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.Utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public enum StructureType {
	SHIELD_BATTERY("shield-battery"),
	SIEGE_ENGINE("siege-engine"),
	POWERCELL("powercell"),
	EXTRACTOR("extractor"),
	REGULATOR("regulator"),
	WARPPAD("warp-pad"),
	OUTPOST("outpost"),
	ARSENAL("arsenal"),
	NEXUS("nexus"),
	RADAR("radar");
	
	private final String title, description;
	private final Material material, disk;
	private final boolean enabled;
	private final long cost;
	
	private StructureType(String node) {
		ConfigurationSection section = Kingdoms.getInstance().getConfig().getConfigurationSection("structures." + node);
		this.disk = Utils.materialAttempt(section.getString("inventory-material"), "RECORD_3");
		this.material = Utils.materialAttempt(section.getString("material"), "REDSTONE_BLOCK");
		this.description = section.getString("description");
		this.enabled = section.getBoolean("enabled", true);
		this.cost = section.getLong("cost", 0);
		this.title = section.getString("name");
	}
	
	public String getDescription() {
		return description;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public Material getDisk() {
		return disk;
	}

	public String getTitle() {
		return title;
	}
	
	public long getCost() {
		return cost;
	}
	
	public String getMetaData() {
		return toString().toLowerCase();
	}

}
