package com.songoda.kingdoms.inventories;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.songoda.kingdoms.manager.inventories.KingdomInventory;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ItemStackBuilder;

public class ConfirmationMenu extends KingdomInventory {

	public ConfirmationMenu() {
		super(InventoryType.HOPPER, "confirmation", 69);
	}

	@Override
	public void build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		inventory.setItem(1, new ItemStackBuilder(inventories.getConfigurationSection("confirm.accept")).build());
		inventory.setItem(3, new ItemStackBuilder(inventories.getConfigurationSection("confirm.cancel")).build());
	}

}
