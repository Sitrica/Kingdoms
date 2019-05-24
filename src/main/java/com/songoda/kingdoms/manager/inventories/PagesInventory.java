package com.songoda.kingdoms.manager.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ItemStackBuilder;

public abstract class PagesInventory extends KingdomInventory {

	public PagesInventory(String path, int size) {
		super(InventoryType.CHEST, path, size);
	}

	public PagesInventory(String path, ConfigurationSection section, int size) {
		super(InventoryType.CHEST, path, section, size);
	}

	protected abstract List<PageItem> getItems(KingdomPlayer kingdomPlayer);

	/**
	 * Used when opening from a parent inventory.
	 * 
	 * @param kingdomPlayer The KingdomPlayer involved.
	 * @return Consumer<InventoryClickEvent> to open the parent inventory again.
	 */
	protected abstract Consumer<InventoryClickEvent> getBackAction(KingdomPlayer kingdomPlayer);

	@Override
	protected final Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		List<PageItem> items = getItems(kingdomPlayer);
		if (items == null || items.isEmpty())
			return inventory;
		Player player = kingdomPlayer.getPlayer();
		int rangeSize = range.getSlots().size();
		int total = (int) Math.ceil(items.size() / rangeSize);
		List<Inventory> pages = new ArrayList<>();
		UUID uuid = kingdomPlayer.getUniqueId();
		inventory = getBlankPage(kingdomPlayer, 1, total);
		Inventory page = inventory;
		for (int i = 0; i < items.size(); i++) {
			int slot = page.firstEmpty();
			PageItem item = items.get(i);
			if (slot == size - 8 || slot == -1) { // End of adding, add the back and next buttons.
				pages.add(page);
				Inventory next = getBlankPage(kingdomPlayer, pages.size() + 1, total);
				setAction(page, uuid, size, event -> player.openInventory(next));
				int previous = pages.size();
				setAction(page, uuid, size  - 8, event -> player.openInventory(pages.get(previous)));
				page = next;
				slot = page.firstEmpty();
				page.setItem(slot, item.getItem());
				setAction(page, uuid, slot, item.getConsumer());
			} else {
				page.setItem(slot, item.getItem());
				setAction(page, uuid, slot, item.getConsumer());
			}
		}
		pages.add(page);
		return pages.get(0);
	}

	/**
	 * Creates a blank page using the configuration section defined in the abstract class's constructor.
	 * 
	 * @param kingdomPlayer The KingdomPlayer receiving the inventory, used for placeholders.
	 * @param current The current page
	 * @param total The total amount of pages
	 * @return The Built inventory ready for items based on the configuration section item range.
	 */
	private Inventory getBlankPage(KingdomPlayer kingdomPlayer, int current, int total) {
		Inventory inventory = createInventory(kingdomPlayer);
		if (current < total)
			inventory.setItem(size, new ItemStackBuilder("scroller.next-item")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%previous%", current - 1)
					.fromConfiguration(inventories)
					.replace("%next%", current + 1)
					.replace("%total%", total)
					.build());
		Consumer<InventoryClickEvent> back = getBackAction(kingdomPlayer);
		if (current > 1 || back != null) {
			inventory.setItem(size - 9, new ItemStackBuilder("scroller.previous-item")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%previous%", current - 1)
					.fromConfiguration(inventories)
					.replace("%next%", current + 1)
					.replace("%total%", total)
					.build());
			if (current == 1 && back != null)
				setAction(inventory, kingdomPlayer.getUniqueId(), size - 9, back);
		}
		return inventory;
	}

	protected class PageItem {

		private final Consumer<InventoryClickEvent> consumer;
		private final ItemStack item;

		public PageItem(ItemStack item, Consumer<InventoryClickEvent> consumer) {
			this.consumer = consumer;
			this.item = item;
		}

		public Consumer<InventoryClickEvent> getConsumer() {
			return consumer;
		}

		public ItemStack getItem() {
			return item;
		}

	}

}
