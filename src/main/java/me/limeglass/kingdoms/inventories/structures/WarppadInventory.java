package me.limeglass.kingdoms.inventories.structures;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.limeglass.kingdoms.manager.inventories.StructureInventory;
import me.limeglass.kingdoms.manager.managers.RenameManager;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.objects.structures.WarpPad;
import me.limeglass.kingdoms.utils.ItemStackBuilder;
import me.limeglass.kingdoms.utils.LocationUtils;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class WarppadInventory extends StructureInventory {

	public WarppadInventory() {
		super(InventoryType.CHEST, "warp-pad", 54);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Player player = kingdomPlayer.getPlayer();
		int slot = 0;
		for (WarpPad warp : kingdom.getWarps()) {
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
			ItemStack item = new ItemStackBuilder(section.getConfigurationSection("warp-item"))
					.replace("%location%", LocationUtils.locationToString(location))
					.setPlaceholderObject(kingdomPlayer)
					.replace("%warp%", warp.getName())
					.setKingdom(kingdom)
					.build();
			inventory.setItem(slot, item);
			setAction(player.getUniqueId(), slot, event -> {
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
