package me.limeglass.kingdoms.objects.kingdom;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.utils.ItemStackBuilder;

public enum MiscUpgradeType {
	
	ANTI_CREEPER("anti-creeper"),
	ANTI_TRAMPLE("anti-trample"),
	NEXUS_GUARD("nexus-guard"),
	BOMB_SHARDS("bomb-shards"),
	INSANITY("insanity"),
	GLORY("glory");
	
	private final ConfigurationSection section;
	private final boolean enabled, def;
	private final int cost, max;
	
	private MiscUpgradeType(String node) {
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("misc-upgrades").get();
		this.section = configuration.getConfigurationSection("misc-upgrades." + node);
		this.enabled = section.getBoolean("enabled", true);
		this.def = section.getBoolean("default", true);
		this.cost = section.getInt("cost", 10);
		this.max = section.getInt("max", 0);
	}
	
	public boolean isDefault() {
		return def;
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
