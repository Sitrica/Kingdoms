package com.songoda.kingdoms.objects.kingdom;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.ListMessageBuilder;

public enum DefenderUpgrade {

	REINFORCEMENTS("reinforcements"),
	MEGA_HEALTH("mega-health"),
	RESISTANCE("resistance"),
	DEATH_DUEL("death-duel"),
	DAMAGE_CAP("damage-cap"),
	STRENGTH("strength"),
	WEAPON("weapon"),
	HEALTH("health"),
	FOCUS("focus"),
	THROW("throw"),
	SPEED("speed"),
	ARMOR("armor"),
	AQUA("aqua"),
	DRAG("drag"),
	THOR("thor"),
	PLOW("plow");

	private final int max, value, cost, multiplier;
	private final ConfigurationSection section;
	private final boolean enabled;

	private DefenderUpgrade(String node) {
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("defender-upgrades").get();
		this.section = configuration.getConfigurationSection("upgrades." + node);
		this.multiplier = section.getInt("cost-multiplier", 0);
		this.enabled = section.getBoolean("enabled", false);
		this.max = section.getInt("max-level", 1);
		this.value = section.getInt("value", 1);
		this.cost = section.getInt("cost", 10);
	}

	public int getCostAt(int level) {
		return cost + (level * multiplier);
	}

	public int getCostMultiplier() {
		return multiplier;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getMaxLevel() {
		return max;
	}

	public int getValue() {
		return value;
	}

	public int getCost() {
		return cost;
	}

	public ItemStack build(OfflineKingdom kingdom, boolean shop) {
		ItemStack itemstack = new ItemStackBuilder(section)
				.replace("%enabled%", enabled)
				.replace("%value%", value)
				.setKingdom(kingdom)
				.build();
		if (shop && section.isConfigurationSection("store-lore")) {
			ItemMeta meta = itemstack.getItemMeta();
			meta.setLore(new ListMessageBuilder(false, "store-lore", section)
					.replace("%enabled%", enabled)
					.replace("%value%", value)
					.setKingdom(kingdom)
					.get());
			itemstack.setItemMeta(meta);
		}
		return itemstack;
	}

}
