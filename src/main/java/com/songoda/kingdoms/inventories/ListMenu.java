package com.songoda.kingdoms.inventories;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.inventories.KingdomInventory;
import com.songoda.kingdoms.manager.inventories.PagesInventory;
import com.songoda.kingdoms.manager.inventories.SearchManager;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class ListMenu extends KingdomInventory {

	public ListMenu() {
		super(InventoryType.HOPPER, "list.main", 69);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		UUID uuid = player.getUniqueId();
		inventory.setItem(0, new ItemStackBuilder(section.getConfigurationSection("all")).build());
		setAction(inventory, uuid, 0, event -> new ListSortingMenu("all").open(kingdomPlayer));
		inventory.setItem(1, new ItemStackBuilder(section.getConfigurationSection("nearby"))
				.replace("%radius%", configuration.getDouble("commands.list-command-radius", 150))
				.build());
		setAction(inventory, uuid, 1, event -> new ListSortingMenu("nearby").open(kingdomPlayer));
		inventory.setItem(2, new ItemStackBuilder(section.getConfigurationSection("powerful")).build());
		setAction(inventory, uuid, 2, event -> new ListSortingMenu("powerful").open(kingdomPlayer));
		inventory.setItem(3, new ItemStackBuilder(section.getConfigurationSection("alphabetical")).build());
		setAction(inventory, uuid, 3, event -> new ListSortingMenu("alphabetical").open(kingdomPlayer));
		inventory.setItem(4, new ItemStackBuilder(section.getConfigurationSection("search")).build());
		setAction(inventory, uuid, 4, event -> instance.getManager(SearchManager.class).openSearch(kingdomPlayer, result -> {
			if (result == null)
				return;
			Bukkit.dispatchCommand(player, "k info " + result);
		}));
		return inventory;
	}

	private class ListSortingMenu extends PagesInventory {

		private final static String ALPHABETICAL = "alphabetical";
		private final static String POWERFUL = "powerful";
		private final static String NEARBY = "nearby";
		private final static String ALL = "all";
		private final String VALUE;

		public ListSortingMenu(String node) {
			super("list." + node, 9 * 6);
			VALUE = node;
		}

		@Override
		protected List<PageItem> getItems(KingdomPlayer kingdomPlayer) {
			Comparator<OfflineKingdom> comparator = Comparator.comparing(OfflineKingdom::isOnline, Comparator.reverseOrder());
			switch (VALUE) {
				case ALL:
					break;
				case NEARBY:
					Location location = kingdomPlayer.getLocation();
					comparator = new Comparator<OfflineKingdom>() {
				        @Override
				        public int compare(OfflineKingdom kingdom, OfflineKingdom other) {
				        	if (kingdom.getSpawn() == null)
				        		return -1;
				            return Double.compare(location.distance(kingdom.getSpawn()), location.distance(other.getSpawn()));
				        }
				    };
					break;
				case POWERFUL:
					comparator = Comparator.comparing(OfflineKingdom::getResourcePoints);
					break;
				case ALPHABETICAL:
					comparator = Comparator.comparing(OfflineKingdom::getName);
					break;
			}
			double max = configuration.getDouble("commands.list-command-radius", 150);
			return instance.getManager(KingdomManager.class).getOfflineKingdoms().parallelStream()
					.sorted(comparator)
					.filter(kingdom -> {
						if (!kingdomPlayer.getKingdom().equals(kingdom))
							return true;
						return configuration.getBoolean("commands.list-command-contains-own", true);
					})
					.map(kingdom -> {
						Player player = kingdomPlayer.getPlayer();
						ItemStack itemstack = new ItemStackBuilder(section.getConfigurationSection("item"))
								.withPlaceholder(kingdom, new Placeholder<OfflineKingdom>("%distance%") {
									@Override
									public Object replace(OfflineKingdom object) {
										double distance = kingdomPlayer.getLocation().distance(kingdom.getSpawn());
										if (distance >= max)
											return new MessageBuilder(section.getString("greater", ">%max%"))
													.setPlaceholderObject(kingdomPlayer)
													.replace("%max%", max)
													.get();
										return new DecimalFormat(section.getString("decimal-format", "###.##")).format(distance);
									}
								})
								.replace("%player%", player.getName())
								.glowingIf(() -> kingdom.isOnline())
								.setKingdom(kingdom)
								.build();
						return new PageItem(itemstack, event -> Bukkit.dispatchCommand(player, "k info " + kingdom.getName()));
					})
					.collect(Collectors.toList());
		}

		@Override
		protected Consumer<InventoryClickEvent> getBackAction(KingdomPlayer kingdomPlayer) {
			return event -> instance.getManager(InventoryManager.class).getInventory(ListMenu.class).open(kingdomPlayer);
		}

	}

}
