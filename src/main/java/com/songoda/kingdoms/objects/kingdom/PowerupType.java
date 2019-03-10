package com.songoda.kingdoms.objects.kingdom;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public enum PowerupType {
	
	DAMAGE_REDUCTION("damage-reduction"),
	REGENERATION_BOOST("regeneration"),
	DAMAGE_BOOST("damage-boost"),
	ARROW_BOOST("arrow-damage");
	
	private final ConfigurationSection section;
	private final boolean enabled;
	private final int cost, max;
	
	private PowerupType(String node) {
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("powerups").get();
		this.section = configuration.getConfigurationSection("powerups." + node);
		this.enabled = section.getBoolean("enabled", true);
		this.cost = section.getInt("cost", 10);
		this.max = section.getInt("max", 0);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public long getCost() {
		return cost;
	}
	
	public long getMax() {
		return max;
	}
	
	public ItemStackBuilder getItemStackBuilder() {
		return new ItemStackBuilder(section)
				.replace("%cost%", cost)
				.replace("%max%", max);
	}

}
