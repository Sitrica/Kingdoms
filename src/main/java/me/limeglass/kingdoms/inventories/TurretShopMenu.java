package me.limeglass.kingdoms.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import me.limeglass.kingdoms.inventories.structures.NexusInventory;
import me.limeglass.kingdoms.manager.inventories.InventoryManager;
import me.limeglass.kingdoms.manager.inventories.PagesInventory;
import me.limeglass.kingdoms.manager.managers.TurretManager;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.turrets.TurretType;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class TurretShopMenu extends PagesInventory {

	public TurretShopMenu() {
		super("turret-shop", 18);
	}

	@Override
	protected List<PageItem> getItems(KingdomPlayer kingdomPlayer) {
		List<PageItem> items = new ArrayList<>();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return items;
		Player player = kingdomPlayer.getPlayer();
		for (TurretType type : instance.getManager(TurretManager.class).getTypes()) {
			if (!type.isEnabled())
				continue;
			items.add(new PageItem(type.build(kingdom, true), event -> {
				long cost = type.getCost();
				if (kingdom.getResourcePoints() < cost) {
					new MessageBuilder("turrets.cannot-afford")
							.replace("%turret%", type.getTitle())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				PlayerInventory playersInventory = player.getInventory();
				if (playersInventory.firstEmpty() == -1) {
					new MessageBuilder("turrets.inventory-full-purchase")
							.replace("%turret%", type.getTitle())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				new MessageBuilder("turrets.purchase")
						.replace("%turret%", type.getTitle())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
				kingdom.subtractResourcePoints(cost);
				playersInventory.addItem(type.build(kingdom, false));
			}));
		}
		return items;
	}

	@Override
	protected Consumer<InventoryClickEvent> getBackAction(KingdomPlayer kingdomPlayer) {
		return event -> instance.getManager(InventoryManager.class).getInventory(NexusInventory.class).open(kingdomPlayer);
	}

}
