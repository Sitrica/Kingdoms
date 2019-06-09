package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.songoda.kingdoms.events.StoreEvent;
import com.songoda.kingdoms.events.UnstoreEvent;
import com.songoda.kingdoms.listeners.StoreListener;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class ChestManager extends Manager {

	private final Map<UUID, KingdomChest> viewing = new HashMap<>();

	public ChestManager() {
		super(true);
		instance.getServer().getPluginManager().registerEvents(new StoreListener(), instance);
	}

	public boolean openChest(KingdomPlayer kingdomPlayer, Kingdom kingdom) {
		KingdomChest chest = kingdom.getKingdomChest();
		Player player = kingdomPlayer.getPlayer();
		player.openInventory(chest.getInventory());
		viewing.put(player.getUniqueId(), chest);
		return true;
	}

	@EventHandler
	public void onChestClose(InventoryCloseEvent event) {
		viewing.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onStore(StoreEvent event) {
		Optional<KingdomChest> chest = Optional.ofNullable(viewing.get(event.getPlayer().getUniqueId()));
		if (!chest.isPresent())
			return;
		Inventory inventory = chest.get().getInventory();
		event.getItems().entrySet().forEach(entry -> inventory.setItem(entry.getKey(), entry.getValue()));
	}

	@EventHandler
	public void onUnstore(UnstoreEvent event) {
		Optional<KingdomChest> chest = Optional.ofNullable(viewing.get(event.getPlayer().getUniqueId()));
		if (!chest.isPresent())
			return;
		Inventory inventory = chest.get().getInventory();
		event.getItems().values().forEach(item -> inventory.remove(item));
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {
		viewing.clear();
	}

}
