package com.songoda.kingdoms.inventories;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.inventories.KingdomInventory;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.DefenderUpgrade;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

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
			inventory.setItem(slot, upgrade.build(kingdom, true));
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
				int newLevel = level++;
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
