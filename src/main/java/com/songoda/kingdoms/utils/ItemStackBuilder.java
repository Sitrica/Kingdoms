package com.songoda.kingdoms.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.placeholders.Placeholders;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;

public class ItemStackBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private FileConfiguration configuration;
	private Object defaultPlaceholderObject;
	private ConfigurationSection section;
	private final Kingdoms instance;
	private OfflineKingdom kingdom;
	private boolean glowing;

	/**
	 * Creates a ItemStackBuilder with the defined nodes..
	 * 
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public ItemStackBuilder(String node) {
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.section = configuration.getConfigurationSection(node);
	}

	/**
	 * Creates a ItemStackBuilder with the defined nodes..
	 * 
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public ItemStackBuilder(ConfigurationSection section) {
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.section = section;
	}

	/**
	 * Add a placeholder to the ItemStackBuilder.
	 * 
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
		this.defaultPlaceholderObject = placeholderObject;
		placeholders.put(placeholder, placeholderObject);
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object.
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Set the configuration to read from, by default is the config.yml
	 * 
	 * @param configuration The FileConfiguration to read from.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder fromConfiguration(ConfigurationSection section) {
		this.section = section;
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder glowingIf(Supplier<Boolean> glowing) {
		this.glowing = glowing.get();
		return this;
	}

	/**
	 * Set the section to read from.
	 * 
	 * @param section The ConfigurationSection to read from.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder setConfigurationSection(ConfigurationSection section) {
		this.section = section;
		return this;
	}

	/**
	 * Set the Kingdom option to be used for placeholders later.
	 * 
	 * @param kingdom The OfflineKingdom to set as.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder setKingdom(OfflineKingdom kingdom) {
		this.kingdom = kingdom;
		return this;
	}

	private String applyPlaceholders(String input) {
		// Registered Placeholders
		for (Entry<Placeholder<?>, Object> entry : placeholders.entrySet()) {
			Placeholder<?> placeholder = entry.getKey();
			for (String syntax : placeholder.getSyntaxes()) {
				if (!input.toLowerCase().contains(syntax.toLowerCase()))
					continue;
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else {
					input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(entry.getValue()));
				}
			}
		}
		// Default Placeholders
		for (Placeholder<?> placeholder : Placeholders.getPlaceholders()) {
			for (String syntax : placeholder.getSyntaxes()) {
				if (!input.toLowerCase().contains(syntax.toLowerCase()))
					continue;
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else if (defaultPlaceholderObject != null) {
					if (placeholder.getType().isAssignableFrom(defaultPlaceholderObject.getClass()))
						input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(defaultPlaceholderObject));
				}
				if (kingdom != null) {
					if (placeholder.getType().isAssignableFrom(OfflineKingdom.class))
						input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(kingdom));
				}
			}
		}
		return input;
	}

	/**
	 * Sends the final product of the builder.
	 */
	public ItemStack build() {
		if (section == null) {
			Kingdoms.consoleMessage("A configuration node is formatted incorrectly.");
			return null;
		}
		String title = section.getString("title", "");
		title = applyPlaceholders(title);
		Material material = Utils.materialAttempt(section.getString("material", "STONE"), "STONE");
		ItemStack itemstack = new ItemStack(material);
		ItemMeta meta = itemstack.getItemMeta();
		meta.setDisplayName(Formatting.color(title));
		List<String> lores = section.getStringList("lore");
		if (lores == null || lores.isEmpty())
			lores = section.getStringList("description");
		if (lores != null && !lores.isEmpty()) {
			meta.setLore(lores.parallelStream()
					.map(lore -> applyPlaceholders(lore))
					.map(lore -> Formatting.color(lore))
					.collect(Collectors.toList()));
		}
		DeprecationUtils.setupItemMeta(itemstack, section.getString("material-meta", ""));
		if (section.getBoolean("glowing", false) || glowing) {
			if (material == Material.BOW) {
				itemstack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 69);
			} else {
				itemstack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 69);
			}
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		itemstack.setItemMeta(meta);
		return itemstack;
	}

}
