package com.songoda.kingdoms.inventories;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.inventories.KingdomInventory;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class StructureShopMenu extends KingdomInventory {

	public StructureShopMenu() {
		super(InventoryType.CHEST, "structure-shop", 18);
	}

	@Override
	protected Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		// Should almost never happen.
		if (kingdom == null)
			return inventory;
		Player player = kingdomPlayer.getPlayer();
		UUID uuid = player.getUniqueId();
		int slot = 0;
		for (StructureType type : StructureType.values()) {
			if (!type.isEnabled() || type == StructureType.NEXUS)
				continue;
			inventory.setItem(slot, type.buildShopItem());
			setAction(uuid, slot, event -> {
				long cost = type.getCost();
				if (kingdom.getResourcePoints() < cost) {
					new MessageBuilder("structures.cannot-afford")
							.replace("%structure%", type.getTitle())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				PlayerInventory playersInventory = player.getInventory();
				if (playersInventory.firstEmpty() == -1) {
					new MessageBuilder("structures.inventory-full-purchase")
							.replace("%structure%", type.getTitle())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				new MessageBuilder("structures.purchase")
						.replace("%structure%", type.getTitle())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
				kingdom.subtractResourcePoints(cost);
				playersInventory.addItem(type.build());
			});
			slot++;
		}
		inventory.setItem(slot, new ItemStackBuilder("scroller.back-item")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build());
		setAction(uuid, slot, event -> instance.getManager(InventoryManager.class).getInventory(NexusInventory.class).open(kingdomPlayer));
		return inventory;
	}

}
