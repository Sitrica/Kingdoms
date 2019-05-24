package com.songoda.kingdoms.inventories.structures;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.SiegeEngineManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.SiegeEngine;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.ItemStackBuilder;

public class SiegeEngineInventory extends StructureInventory {

	public SiegeEngineInventory() {
		super(InventoryType.CHEST, "siege-engine", 27);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		throw new UnsupportedOperationException("This method should not be called, use openSiegeMenu(Land, KingdomPlayer)");
	}

	public void openSiegeMenu(Land engineLand, KingdomPlayer kingdomPlayer) {
		Structure structure = engineLand.getStructure();
		if (structure == null)
			return;
		Inventory inventory = createInventory(kingdomPlayer);
		SiegeEngine engine = (SiegeEngine) structure;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Player player = kingdomPlayer.getPlayer();
		Location location = engine.getLocation();
		Land land = instance.getManager(LandManager.class).getLand(location.getChunk());
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (!optional.isPresent())
			return;
		SiegeEngineManager siegeEngineManager = instance.getManager(SiegeEngineManager.class);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				//No diagonal Firing.
				if (x != 0 && z != 0)
					continue;
				ItemStack item;
				if (x == 0 && z == 0) {
					item = new ItemStackBuilder(section.getConfigurationSection("location"))
							.setPlaceholderObject(kingdomPlayer)
							.replace("x", location.getBlockX())
							.replace("y", location.getBlockY())
							.replace("z", location.getBlockZ())
							.fromConfiguration(inventories)
							.setKingdom(kingdom)
							.build();
				} else {
					item = new ItemStackBuilder(section.getConfigurationSection("unoccupied"))
							.replace("%time%", IntervalUtils.getSeconds(engine.getCooldownTimeLeft()))
							.setPlaceholderObject(kingdomPlayer)
							.replace("x", location.getBlockX())
							.replace("y", location.getBlockY())
							.replace("z", location.getBlockZ())
							.fromConfiguration(inventories)
							.setKingdom(kingdom)
							.build();
					if (!optional.isPresent()) {
						item = new ItemStackBuilder(section.getConfigurationSection("no-target"))
								.replace("%time%", IntervalUtils.getSeconds(engine.getCooldownTimeLeft()))
								.setPlaceholderObject(kingdomPlayer)
								.replace("x", location.getBlockX())
								.replace("y", location.getBlockY())
								.replace("z", location.getBlockZ())
								.fromConfiguration(inventories)
								.setKingdom(kingdom)
								.build();
					} else {
						OfflineKingdom landKingdom = optional.get();
						if (!engine.isReady()) {
							item = new ItemStackBuilder(section.getConfigurationSection("reloading"))
									.replace("%time%", IntervalUtils.getSeconds(engine.getCooldownTimeLeft()))
									.setPlaceholderObject(kingdomPlayer)
									.replace("x", location.getBlockX())
									.replace("y", location.getBlockY())
									.replace("z", location.getBlockZ())
									.fromConfiguration(inventories)
									.setKingdom(landKingdom)
									.build();
						} else if (kingdom.equals(landKingdom) || kingdom.getAllies().contains(landKingdom)) {
							item = new ItemStackBuilder(section.getConfigurationSection("friendly"))
									.replace("%time%", IntervalUtils.getSeconds(engine.getCooldownTimeLeft()))
									.setPlaceholderObject(kingdomPlayer)
									.replace("x", location.getBlockX())
									.replace("y", location.getBlockY())
									.replace("z", location.getBlockZ())
									.fromConfiguration(inventories)
									.setKingdom(landKingdom)
									.build();
						} else {
							int cost = structures.getInt("cost-per-shot", 10);
							item = new ItemStackBuilder(section.getConfigurationSection("ready"))
									.replace("%time%", IntervalUtils.getSeconds(engine.getCooldownTimeLeft()))
									.setPlaceholderObject(kingdomPlayer)
									.replace("x", location.getBlockX())
									.replace("y", location.getBlockY())
									.replace("z", location.getBlockZ())
									.fromConfiguration(inventories)
									.replace("%cost%", cost)
									.setKingdom(landKingdom)
									.build();
							setAction(player.getUniqueId(), (1 + x) + (9 * (z + 1)), event -> {
								player.closeInventory();
								siegeEngineManager.fireSiegeEngine(engine, land, kingdom, landKingdom);
							});
						}
					}
				}
				inventory.setItem((1 + x) + (9 * (z + 1)), item);
			}
			openInventory(inventory, player);
		}
		inventory.setItem(8, new ItemStackBuilder("inventories.resource-points")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build());
	}

}
