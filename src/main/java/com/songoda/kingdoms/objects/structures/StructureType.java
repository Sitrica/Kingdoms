package com.songoda.kingdoms.objects.structures;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum StructureType {
	
	SHIELD_BATTERY("shield-battery", "shieldbattery"), // Second string is for old metadata on strucutres.
	SIEGE_ENGINE("siege-engine", "siegeengine"), // Second string is for old metadata on strucutres.
	POWERCELL("powercell"),
	EXTRACTOR("extractor"),
	REGULATOR("regulator"),
	WARPPAD("warp-pad"),
	OUTPOST("outpost"),
	ARSENAL("arsenal"),
	NEXUS("nexus"),
	RADAR("radar");
	
	private final List<String> description = new ArrayList<>();
	private final List<String> additional = new ArrayList<>();
	private final Material material, item;
	private final String title, metadata;
	private final boolean enabled;
	private ItemStack itemstack;
	private final long cost;
	
	StructureType(String node) {
		this(node, null);
	}
	
	StructureType(String node, String metadata) {
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("structures").get();
		ConfigurationSection section = configuration.getConfigurationSection("structures." + node);
		this.item = Utils.materialAttempt(section.getString("inventory-material", "MUSIC_DISC_WAIT"), "RECORD_3");
		this.material = Utils.materialAttempt(section.getString("material"), "REDSTONE_BLOCK");
		this.additional.addAll(configuration.getStringList("structures.additional-lore"));
		this.description.addAll(section.getStringList("description"));
		this.enabled = section.getBoolean("enabled", true);
		this.cost = section.getLong("cost", 0);
		this.title = section.getString("name");
		if (metadata == null) { 
			this.metadata = node;
			return;
		}
		this.metadata = metadata;
	}
	
	public Material getBlockMaterial() {
		return material;
	}
	
	public Material getItemMaterial() {
		return item;
	}
	
	public List<String> getDescription() {
		return description.parallelStream()
				.map(string -> Formatting.color(string))
				.collect(Collectors.toList());
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public String getTitle() {
		return Formatting.color(title);
	}
	
	public long getCost() {
		return cost;
	}
	
	public String getMetaData() {
		return metadata.toLowerCase();
	}
	
	public ItemStack build() {
		if (itemstack != null)
			return itemstack;
		itemstack = new ItemStack(item);
		ItemMeta meta = itemstack.getItemMeta();
		meta.setDisplayName(Formatting.color(title));
		List<String> lores = new ArrayList<>();
		getDescription().forEach(line -> lores.add(line));
		additional.forEach(lore -> lores.add(Formatting.color(lore)));
		meta.setLore(lores);
		itemstack.setItemMeta(meta);
		return itemstack;
	}

}
