package com.songoda.kingdoms.inventories;

import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.inventories.KingdomInventory;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class MiscUpgradeMenu extends KingdomInventory {

	public MiscUpgradeMenu() {
		super(InventoryType.CHEST, "defender-upgrades", 18);
	}

	@Override
	protected Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		// Should almost never happen.
		if (kingdom == null)
			return inventory;
		FileConfiguration miscUpgrades = Kingdoms.getInstance().getConfiguration("misc-upgrades").get();
		MiscUpgrade info = kingdom.getMiscUpgrades();
		Player player = kingdomPlayer.getPlayer();
		UUID uuid = player.getUniqueId();
		int slot = 0;
		for (MiscUpgradeType upgrade : MiscUpgradeType.values()) {
			if (!upgrade.isEnabled())
				continue;
			ItemStack item = upgrade.getItemStackBuilder().setKingdom(kingdom).build();
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			boolean bought = info.hasBought(upgrade);
			if (bought)
				lore.addAll(new ListMessageBuilder(false, "misc-upgrades.bought", miscUpgrades)
						.replace("%upgrade%", upgrade.name())
						.replace("%cost%", upgrade.getCost())
						.setKingdom(kingdom)
						.get());
			else
				lore.addAll(new ListMessageBuilder(false, "misc-upgrades.shop", miscUpgrades)
						.replace("%upgrade%", upgrade.name())
						.replace("%cost%", upgrade.getCost())
						.setKingdom(kingdom)
						.get());
			meta.setLore(lore);
			item.setItemMeta(meta);
			inventory.setItem(slot, item);
			setAction(uuid, slot, event -> {
				long cost = upgrade.getCost();
				if (bought) {
					new MessageBuilder("upgrades.already-bought")
							.replace("%upgrade%", upgrade.name())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				if (kingdom.getResourcePoints() < cost) {
					new MessageBuilder("upgrades.misc-cannot-afford")
							.replace("%upgrade%", upgrade.name())
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", cost)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				kingdom.subtractResourcePoints(cost);
				info.setBought(upgrade, true);
				new MessageBuilder("upgrades.purchase-misc")
						.replace("%upgrade%", upgrade.name())
						.setPlaceholderObject(kingdomPlayer)
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
