package me.limeglass.kingdoms.inventories.structures;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import me.limeglass.kingdoms.manager.inventories.StructureInventory;
import me.limeglass.kingdoms.objects.kingdom.ArsenalItem;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class ArsenalInventory extends StructureInventory {

	private final FileConfiguration arsenal;
	
	public ArsenalInventory() {
		super(InventoryType.CHEST, "arsenal", 9);
		this.arsenal = instance.getConfiguration("arsenal-items").get();
	}
	
	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		int i = 0;
		for (ArsenalItem item : ArsenalItem.values()) {
			if (!item.isEnabled())
				continue;
			inventory.setItem(i, item.build(true));
			setAction(player.getUniqueId(), i, event -> {
					Kingdom kingdom = kingdomPlayer.getKingdom();
					if (kingdom == null)
						return;
					int cost = item.getCost();
					if (kingdom.getResourcePoints() - cost < 0) {
						new MessageBuilder("messages.not-enough-points")
								.setPlaceholderObject(kingdomPlayer)
								.fromConfiguration(arsenal)
								.replace("%cost%", cost)
								.setKingdom(kingdom)
								.send(player);
						return;
					}
					if (player.getInventory().firstEmpty() == -1) {
						new MessageBuilder("messages.inventory-full")
								.setPlaceholderObject(kingdomPlayer)
								.fromConfiguration(arsenal)
								.replace("%cost%", cost)
								.setKingdom(kingdom)
								.send(player);
						return;
					}
					kingdom.subtractResourcePoints(cost);
					player.getInventory().addItem(item.build(false));
					new MessageBuilder("messages.bought-turret-breaker")
							.setPlaceholderObject(kingdomPlayer)
							.fromConfiguration(arsenal)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					reopen(kingdomPlayer);
				}
			);
			i++;
		}
		return inventory;
	}

}
