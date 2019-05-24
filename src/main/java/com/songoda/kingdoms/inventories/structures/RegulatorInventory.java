package com.songoda.kingdoms.inventories.structures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.manager.inventories.ScrollerInventoryOLD;
import com.songoda.kingdoms.manager.inventories.ScrollerManager;
import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.objects.structures.Regulator;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.utils.ItemStackBuilder;

public class RegulatorInventory extends StructureInventory {

	private final ScrollerManager scrollerManager;

	public RegulatorInventory() {
		super(InventoryType.HOPPER, "regulator", 69);
		this.scrollerManager = instance.getManager("scroller", ScrollerManager.class);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		throw new UnsupportedOperationException("This method should not be called, use openRegulatorMenu(Land, KingdomPlayer)");
	}

	public void openRegulatorMenu(Land land, KingdomPlayer kingdomPlayer) {
		Structure structure = land.getStructure();
		if (structure == null || !(structure instanceof Regulator))
			return;
		Inventory inventory = createInventory(kingdomPlayer);
		Player player = kingdomPlayer.getPlayer();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Regulator regulator = (Regulator) structure;
		ConfigurationSection viewer = inventories.getConfigurationSection("inventories.regulator-viewer");
		ItemStack build = new ItemStackBuilder(section.getConfigurationSection("build"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(0, build);
		setAction(player.getUniqueId(), 0, event -> {
			List<ItemStack> items = new ArrayList<ItemStack>();
			for (OfflineKingdomPlayer offlineKingdomPlayer : kingdom.getMembers()) {
				if (offlineKingdomPlayer.equals(kingdomPlayer))
					continue;
				if (regulator.getWhoCanBuild().contains(offlineKingdomPlayer)) {
					items.add(new ItemStackBuilder(viewer.getConfigurationSection("can-build"))
							.fromConfiguration(inventories)
							.setPlaceholderObject(offlineKingdomPlayer)
							.setKingdom(kingdom)
							.build());
				} else {
					items.add(new ItemStackBuilder(viewer.getConfigurationSection("cannot-build"))
							.fromConfiguration(inventories)
							.setPlaceholderObject(offlineKingdomPlayer)
							.setKingdom(kingdom)
							.build());
				}
			}
			int size = Math.round(items.size() / 9);
			if (size > 6)
				size = 6;
			scrollerManager.registerScroller(new ScrollerInventoryOLD(items, size * 9, viewer.getString("build-title", "&4Click to edit build permissions"), kingdomPlayer.getPlayer()));
		});
		ItemStack interact = new ItemStackBuilder(section.getConfigurationSection("interact"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(1, interact);
		setAction(player.getUniqueId(), 1, event -> {
			List<ItemStack> items = new ArrayList<ItemStack>();
			for (OfflineKingdomPlayer offlineKingdomPlayer : kingdom.getMembers()) {
				if (offlineKingdomPlayer.equals(kingdomPlayer))
					continue;
				if (regulator.getWhoCanInteract().contains(offlineKingdomPlayer)) {
					items.add(new ItemStackBuilder(viewer.getConfigurationSection("can-interact"))
							.fromConfiguration(inventories)
							.setPlaceholderObject(offlineKingdomPlayer)
							.setKingdom(kingdom)
							.build());
				} else {
					items.add(new ItemStackBuilder(viewer.getConfigurationSection("cannot-interact"))
							.fromConfiguration(inventories)
							.setPlaceholderObject(offlineKingdomPlayer)
							.setKingdom(kingdom)
							.build());
				}
			}
			int size = Math.round(items.size() / 9);
			if (size > 6)
				size = 6;
			ScrollerInventoryOLD scroller = new ScrollerInventoryOLD(items, size * 9, viewer.getString("interact-title", "&4Click to edit interact permissions"), kingdomPlayer.getPlayer());
			scroller.setAction(click -> {
				ItemStack item = click.getCurrentItem();
				String name = item.getItemMeta().getDisplayName();
				ItemStack can = new ItemStackBuilder(viewer.getConfigurationSection("can-interact"))
						.fromConfiguration(inventories)
						.setKingdom(kingdom)
						.build();
				if (can.getType() == item.getType()) {
					regulator.getWhoCanBuild().add(kingdomPlayer);
					ItemMeta meta = can.getItemMeta();
					meta.setDisplayName(name);
					can.setItemMeta(meta);
					click.getClickedInventory().setItem(click.getSlot(), can);
				} else {
					regulator.getWhoCanBuild().remove(kingdomPlayer);
					ItemStack cannot = new ItemStackBuilder(viewer.getConfigurationSection("cannot-interact"))
							.fromConfiguration(inventories)
							.setKingdom(kingdom)
							.build();
					ItemMeta meta = cannot.getItemMeta();
					meta = cannot.getItemMeta();
					meta.setDisplayName(name);
					cannot.setItemMeta(meta);
					click.getClickedInventory().setItem(click.getSlot(), cannot);
				}
			});
			scrollerManager.registerScroller(scroller);
		});
		if (structures.getBoolean("structures.regulator.allow-monster-spawn-disabling", true)) {
			ItemStackBuilder monsters = new ItemStackBuilder(section.getConfigurationSection("monsters-can-spawn"))
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom);
			if (!regulator.canSpawnMonsters())
				monsters.setConfigurationSection(section.getConfigurationSection("monsters-cannot-spawn"));
			inventory.setItem(2, monsters.build());
			setAction(player.getUniqueId(), 2, event -> {
				regulator.setMonstersSpawning(!regulator.canSpawnMonsters());
				openRegulatorMenu(land, kingdomPlayer);
			});
		}
		if (structures.getBoolean("structures.regulator.allow-animal-spawn-disabling", true)) {
			ItemStackBuilder animals = new ItemStackBuilder(section.getConfigurationSection("animals-can-spawn"))
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom);
			if (!regulator.canSpawnAnimals())
				animals.setConfigurationSection(section.getConfigurationSection("animals-cannot-spawn"));
			inventory.setItem(3, animals.build());
			setAction(player.getUniqueId(), 3, event -> {
				regulator.setAnimalsSpawning(!regulator.canSpawnAnimals());
				openRegulatorMenu(land, kingdomPlayer);
			});
		}
		openInventory(inventory, player);
	}

}
