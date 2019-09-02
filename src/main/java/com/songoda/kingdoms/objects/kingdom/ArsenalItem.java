package com.songoda.kingdoms.objects.kingdom;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public enum ArsenalItem {

	TURRET_BREAKER("turret-breaker"),
	SIEGE_ROCKET("siege-rocket");

	private final ConfigurationSection section;
	private final FileConfiguration arsenal;
	private final String title, meta;
	private final Kingdoms instance;
	private final Material material;
	private final boolean enabled;
	private final int cost;

	private ArsenalItem(String node) {
		this.instance = Kingdoms.getInstance();
		this.arsenal = instance.getConfiguration("arsenal-items").get();
		this.section = arsenal.getConfigurationSection("arsenal-items." + node);
		this.material = Utils.materialAttempt(section.getString("material", "LEVER"), "LEVER");
		this.title = Formatting.color(section.getString("title"));
		this.meta = section.getString("material-meta", "");
		this.enabled = section.getBoolean("enabled", true);
		this.cost = section.getInt("cost", 100);
	}

	public ListMessageBuilder getDescription() {
		return new ListMessageBuilder(false, "description", section)
				.replace("%time%", section.isSet("time") ? section.getString("time") : "")
				.replace("%cost%", cost);
	}

	public int getCost() {
		return cost;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Material getMaterial() {
		return material;
	}

	public ItemStack build(boolean shop) {
		ItemStack itemstack = new ItemStack(material);
		ItemMeta itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(Formatting.color(title));
		List<String> lores = new ArrayList<>();
		boolean spacing = arsenal.getBoolean("arsenal-items.spacing", false);
		if (spacing)
			lores.add(" ");
		getDescription().get().forEach(line -> lores.add(line));
		if (shop) {
			lores.add(new MessageBuilder(false, "arsenal-items.formats.cost")
					.fromConfiguration(arsenal)
					.replace("%cost%", cost)
					.get());
		}
		if (spacing)
			lores.add(" ");
		itemmeta.setLore(lores);
		itemstack.setItemMeta(DeprecationUtils.setupItemMeta(itemmeta, meta));
		return itemstack;
	}

}
