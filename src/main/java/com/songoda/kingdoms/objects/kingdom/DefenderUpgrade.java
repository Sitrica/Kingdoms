package com.songoda.kingdoms.objects.kingdom;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

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
		this.max = section.getInt("max-level", -1);
		this.value = section.getInt("value", 1);
		this.cost = section.getInt("cost", 10);
	}

	public int getCostAt(int level) {
		if (level < 1)
			return cost;
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

	public ItemStack build(OfflineKingdom kingdom) {
		int level = kingdom.getDefenderInfo().getUpgradeLevel(this);
		ItemStack itemstack = new ItemStackBuilder(section)
				.replace("%current%", level * value)
				.replace("%cost%", getCostAt(level))
				.replace("%enabled%", enabled)
				.replace("%value%", value)
				.replace("%level%", level)
				.replace("%max%", max)
				.setKingdom(kingdom)
				.build();
		if (section.isSet("store-lore")) {
			ItemMeta meta = itemstack.getItemMeta();
			List<String> lores = meta.getLore();
			lores.addAll(new ListMessageBuilder(false, "store-lore", section)
					.replace("%current%", level * value)
					.replace("%cost%", getCostAt(level))
					.replace("%enabled%", enabled)
					.replace("%value%", value)
					.replace("%level%", level)
					.replace("%max%", max)
					.setKingdom(kingdom)
					.get());
			if (level > 0)
				lores.add(new MessageBuilder(false, "current", section)
						.replace("%current%", level * value)
						.replace("%cost%", getCostAt(level))
						.replace("%enabled%", enabled)
						.replace("%value%", value)
						.replace("%level%", level)
						.replace("%max%", max)
						.setKingdom(kingdom)
						.get());
			meta.setLore(lores);
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ATTRIBUTES);
			itemstack.setItemMeta(meta);
		}
		return itemstack;
	}

}
