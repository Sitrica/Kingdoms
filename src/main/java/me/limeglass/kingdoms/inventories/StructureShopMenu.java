package me.limeglass.kingdoms.inventories;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import me.limeglass.kingdoms.inventories.structures.NexusInventory;
import me.limeglass.kingdoms.manager.inventories.InventoryManager;
import me.limeglass.kingdoms.manager.inventories.KingdomInventory;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.structures.StructureType;
import me.limeglass.kingdoms.utils.ItemStackBuilder;
import me.limeglass.kingdoms.utils.MessageBuilder;

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
