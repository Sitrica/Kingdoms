package me.limeglass.kingdoms.inventories;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import me.limeglass.kingdoms.inventories.structures.NexusInventory;
import me.limeglass.kingdoms.manager.inventories.InventoryManager;
import me.limeglass.kingdoms.manager.inventories.KingdomInventory;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.objects.kingdom.DefenderUpgrade;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.ItemStackBuilder;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class DefenderUpgradeMenu extends KingdomInventory {

	public DefenderUpgradeMenu() {
		super(InventoryType.CHEST, "defender-upgrades", 27);
	}

	@Override
	protected Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		// Should almost never happen.
		if (kingdom == null)
			return inventory;
		DefenderInfo info = kingdom.getDefenderInfo();
		Player player = kingdomPlayer.getPlayer();
		UUID uuid = player.getUniqueId();
		int slot = 0;
		for (DefenderUpgrade upgrade : DefenderUpgrade.values()) {
			if (!upgrade.isEnabled())
				continue;
			inventory.setItem(slot, upgrade.build(kingdom));
			setAction(uuid, slot, event -> {
				int level = info.getUpgradeLevel(upgrade);
				long cost = upgrade.getCostAt(level);
				if (kingdom.getResourcePoints() < cost) {
					new MessageBuilder("upgrades.cannot-afford")
							.replace("%upgrade%", upgrade.name())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%level%", level)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				int max = upgrade.getMaxLevel();
				if (max == -1) {
					new MessageBuilder("upgrades.maxed-out")
							.replace("%upgrade%", upgrade.name())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%level%", level)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				} else if (level >= max) {
					new MessageBuilder("upgrades.level-max")
							.replace("%upgrade%", upgrade.name())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%level%", level)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				int newLevel = level + 1;
				kingdom.subtractResourcePoints(cost);
				info.setUpgradeLevel(upgrade, newLevel);
				new MessageBuilder("upgrades.purchase")
						.replace("%upgrade%", upgrade.name())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%newlevel%", newLevel)
						.replace("%level%", level)
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
				reopen(kingdomPlayer);
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
