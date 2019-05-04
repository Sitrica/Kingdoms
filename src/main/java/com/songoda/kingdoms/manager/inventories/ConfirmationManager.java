package com.songoda.kingdoms.manager.inventories;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.songoda.kingdoms.inventories.ConfirmationMenu;
import com.songoda.kingdoms.manager.Manager;

public class ConfirmationManager extends Manager implements Listener {

	private final Map<Player, Consumer<Boolean>> waiting = new HashMap<>();
	private InventoryManager inventoryManager;

	public ConfirmationManager() {
		super("confirmation", true);
	}

	public void openConfirmation(Player player, Consumer<Boolean> consumer) {
		inventoryManager.getInventory(ConfirmationMenu.class).openInventory(player);
		waiting.put(player, consumer);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		Optional<Consumer<Boolean>> consumer = waiting.entrySet().stream()
				.filter(entry -> entry.getKey().equals(player))
				.map(entry -> entry.getValue())
				.findFirst();
		if (!consumer.isPresent())
			return;
		event.setCancelled(true);
		int slot = event.getSlot();
		if (slot != 1 && slot != 3)
			return;
		if (slot == 1)
			consumer.get().accept(true);
		else if (slot == 3)
			consumer.get().accept(false);
		waiting.remove(player);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		waiting.remove(event.getPlayer());
	}

	@Override
	public void onDisable() {}

	@Override
	public void initalize() {}

}
