package com.songoda.kingdoms.manager.inventories;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.utils.Utils;

public class InventoryManager extends Manager {

	private final Map<UUID, KingdomInventory> opened = new HashMap<>();
	private final Set<KingdomInventory> inventories = new HashSet<>();
	
	public InventoryManager() {
		super("inventory", true);
	}
	
	public void start() {
		Utils.getClassesOf(instance, instance.getPackageName() + ".inventories", KingdomInventory.class).forEach(clazz -> {
			try {
				KingdomInventory inventory = clazz.newInstance();
				inventories.add(inventory);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public <C extends KingdomInventory> C getInventory(Class<C> inventory) {
		return (C) inventories.parallelStream()
				.filter(kingdomInventory -> kingdomInventory.getClass().equals(inventory))
				.findFirst().orElseGet(() -> {
					KingdomInventory kingdomInventory;
					try {
						kingdomInventory = inventory.newInstance();
						inventories.add(kingdomInventory);
						return kingdomInventory;
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
					return null;
				});
	}

	public void opening(UUID uuid, KingdomInventory inventory) {
		opened.put(uuid, inventory);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		Player player = (Player) event.getWhoClicked();
		if (event.getCurrentItem() == null)
			return;
		UUID uuid = player.getUniqueId();
		if (!opened.containsKey(uuid))
			return;
		event.setCancelled(true);
		if (event.getRawSlot() >= event.getInventory().getSize())
			return;
		Optional<KingdomInventory> optional = Optional.ofNullable(opened.get(uuid));
		if (!optional.isPresent())
			return;
		KingdomInventory inventory = optional.get();
		Optional<Consumer<InventoryClickEvent>> consumer = inventory.getAction(event.getSlot());
		if (!consumer.isPresent())
			return;
		consumer.get().accept(event);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		if (opened.containsKey(uuid))
			opened.remove(uuid);
	}

	@Override
	public void onDisable() {
		opened.keySet().parallelStream()
				.map(uuid -> Bukkit.getPlayer(uuid))
				.filter(player -> player != null)
				.forEach(player -> player.closeInventory());
		opened.clear();
		inventories.clear();
	}

}
