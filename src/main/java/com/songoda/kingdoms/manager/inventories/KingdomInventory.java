package com.songoda.kingdoms.manager.inventories;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public abstract class KingdomInventory {

	private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();
	protected final FileConfiguration configuration, inventories;
	protected final ConfigurationSection section;
	protected final Kingdoms instance;
	private final InventoryType type;
	private final int size;

	public KingdomInventory(InventoryType type, String path, int size) {
		this(type, path, null, size);
	}

	public KingdomInventory(InventoryType type, String path, ConfigurationSection section, int size) {
		this.size = size;
		this.type = type;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.inventories = instance.getConfiguration("inventories").get();
		this.section = section == null ? inventories.getConfigurationSection("inventories." + path) : section;
	}

	public void open(KingdomPlayer kingdomPlayer) {
		Inventory inventory = createInventory(kingdomPlayer);
		build(inventory, kingdomPlayer);
		openInventory(inventory, kingdomPlayer.getPlayer());
	}

	protected void openInventory(Inventory inventory, Player player) {
		player.openInventory(inventory);
		instance.getManager(InventoryManager.class).opening(player.getUniqueId(), this);
	}

	protected Inventory createInventory(KingdomPlayer kingdomPlayer) {
		String title = new MessageBuilder(false, "title")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(section)
				.get();
		Inventory inventory;
		if (type == InventoryType.CHEST)
			inventory = instance.getServer().createInventory(null, size, title);
		else
			inventory = instance.getServer().createInventory(null, type, title);
		return inventory;
	}

	protected abstract void build(Inventory inventory, KingdomPlayer kingdomPlayer);

	protected void setAction(int slot, Consumer<InventoryClickEvent> consummer) {
		actions.put(slot, consummer);
	}

	public Optional<Consumer<InventoryClickEvent>> getAction(int slot) {
		return Optional.ofNullable(actions.get(slot));
	}

	protected void reopen(KingdomPlayer kingdomPlayer) {
		open(kingdomPlayer);
	}

}
