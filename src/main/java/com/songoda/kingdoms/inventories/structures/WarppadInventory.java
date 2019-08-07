package com.songoda.kingdoms.inventories.structures;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.manager.managers.RenameManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.WarpPad.Warp;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class WarppadInventory extends StructureInventory {

	public WarppadInventory() {
		super(InventoryType.CHEST, "warp-pad", 54);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Player player = kingdomPlayer.getPlayer();
		int slot = 0;
		for (Warp warp : kingdom.getWarps()) {
			if (warp == null)
				continue;
			Land land = warp.getLand();
			if (land == null)
				continue;
			Structure structure = land.getStructure();
			if (structure == null)
				continue;
			Location location = structure.getLocation();
			if (location == null)
				continue;
			inventory.setItem(slot, new ItemStackBuilder(section.getConfigurationSection("warp-item"))
					.replace("%location%", LocationUtils.locationToString(location))
					.setPlaceholderObject(kingdomPlayer)
					.replace("%warp%", warp.getName())
					.setKingdom(kingdom)
					.build());
			setAction(inventory, player.getUniqueId(), slot, event -> {
				Kingdoms.debugMessage("test click warppad");
				if (event.isLeftClick()) {
					player.teleport(location.add(0, 0.3, 0));
					player.closeInventory();
				} else {
					new MessageBuilder("structures.rename-warp")
							.replace("%location%", LocationUtils.locationToString(location))
							.setPlaceholderObject(kingdomPlayer)
							.replace("%warp%", warp.getName())
							.setKingdom(kingdom)
							.send(player);
					instance.getManager(RenameManager.class).rename(player, chat -> {
						String message = chat.getMessage();
						new MessageBuilder("structures.rename-success")
								.replace("%location%", LocationUtils.locationToString(location))
								.setPlaceholderObject(kingdomPlayer)
								.replace("%old%", warp.getName())
								.replace("%new%", message)
								.setKingdom(kingdom)
								.send(player);
						warp.setName(message);
					});
				}
			});
		}
		return inventory;
	}

}
