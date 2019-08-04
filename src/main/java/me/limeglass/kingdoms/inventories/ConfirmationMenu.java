package me.limeglass.kingdoms.inventories;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import me.limeglass.kingdoms.manager.inventories.KingdomInventory;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.ItemStackBuilder;

public class ConfirmationMenu extends KingdomInventory {

	public ConfirmationMenu() {
		super(InventoryType.HOPPER, "confirmation", 69);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		inventory.setItem(1, new ItemStackBuilder(inventories.getConfigurationSection("confirm.accept")).build());
		inventory.setItem(3, new ItemStackBuilder(inventories.getConfigurationSection("confirm.cancel")).build());
		return inventory;
	}

}
