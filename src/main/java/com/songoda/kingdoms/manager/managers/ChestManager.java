package com.songoda.kingdoms.manager.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.songoda.kingdoms.events.StoreEvent;
import com.songoda.kingdoms.events.UnstoreEvent;
import com.songoda.kingdoms.listeners.StoreListener;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestManager extends Manager {
	
	private final Map<Player, KingdomChest> viewing = new HashMap<>();
	
	protected ChestManager() {
		super("chest", true);
		instance.getServer().getPluginManager().registerEvents(new StoreListener(), instance);
	}
	
	public boolean openChest(KingdomPlayer kingdomPlayer, Kingdom kingdom) {
		KingdomChest chest = kingdom.getKingdomChest();
		Player player = kingdomPlayer.getPlayer();
		player.openInventory(chest.getInventory());
		viewing.put(player, chest);
		return true;
	}
	
	@EventHandler
	public void onChestClose(InventoryCloseEvent event) {
		viewing.remove(event.getPlayer());
	}
	
	@EventHandler
	public void onStore(StoreEvent event) {
		Optional<KingdomChest> chest = Optional.ofNullable(viewing.get(event.getPlayer()));
		if (!chest.isPresent())
			return;
		Inventory inventory = chest.get().getInventory();
		event.getItems().entrySet().forEach(entry -> inventory.setItem(entry.getKey(), entry.getValue()));
	}
	
	@EventHandler
	public void onUnstore(UnstoreEvent event) {
		Optional<KingdomChest> chest = Optional.ofNullable(viewing.get(event.getPlayer()));
		if (!chest.isPresent())
			return;
		Inventory inventory = chest.get().getInventory();
		event.getItems().values().forEach(item -> inventory.remove(item));
	}
	
	public static String serialize(KingdomChest chest) {
		YamlConfiguration configuration = new YamlConfiguration();
		Map<Integer, ItemStack> map = chest.getContents();
		configuration.set("inventory-slots", map.keySet());
		configuration.set("inventory-items", map.values());
		return configuration.saveToString();
	}
	
	@SuppressWarnings("unchecked")
	public static KingdomChest deserialize(OfflineKingdom kingdom, String serialized) {
		YamlConfiguration configuration = new YamlConfiguration();
		KingdomChest chest = new KingdomChest(kingdom);
		try {
			configuration.loadFromString(serialized);
			if (configuration.isSet("inv")) {
				Map<Integer, ItemStack> fixed = new HashMap<>();
				List<ItemStack> old = (List<ItemStack>) configuration.get("inv");
				int i = 0;
				for (ItemStack item : old) {
					fixed.put(i, item);
					i++;
				}
				chest.setContents(fixed);
			} else {
				Map<Integer, ItemStack> finished = new HashMap<>();
				Collection<Integer> slots = (Collection<Integer>) configuration.get("inventory-slots");
				List<ItemStack> items = (List<ItemStack>) configuration.get("inventory-items");
				for (int slot : slots)
					finished.put(slot, items.get(slot));
				chest.setContents(finished);
			}
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return chest;
		}
		return chest;
	}

	@Override
	public void onDisable() {
		viewing.clear();
	}

}
