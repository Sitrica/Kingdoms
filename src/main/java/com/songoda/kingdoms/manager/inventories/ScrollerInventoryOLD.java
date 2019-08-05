package com.songoda.kingdoms.manager.inventories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ItemStackBuilder;

public class ScrollerInventoryOLD {

	private final List<Inventory> pages = new ArrayList<>();
	private final Set<Player> users = new HashSet<>();
	private Consumer<InventoryClickEvent> consumer;
	private final ScrollerManager scrollerManager;
	private FileConfiguration configuration;
	private final Kingdoms instance;
	private final int size, total;
	private int current = 0;

	public ScrollerInventoryOLD(List<ItemStack> items, int size, String name, Player player) {
		if (size == 9)
			size = 18;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfiguration("inventories").get();
		this.scrollerManager = instance.getManager(ScrollerManager.class);
		this.size = size;
		if (name.length() > 32)
			name = name.substring(0, 32);
		Inventory page = getBlankPage(name);
		for (int i = 0; i < items.size(); i++) {
			if (page.firstEmpty() == size - 8) {
				pages.add(page);
				page = getBlankPage(name);
				page.addItem(items.get(i));
			} else {
				page.addItem(items.get(i));
			}
		}
		pages.add(page);
		this.total = pages.size();
		users.add(player);
		scrollerManager.registerScroller(this);
	}

	private Inventory getBlankPage(String name) {
		Inventory page = Bukkit.createInventory(null, size, name);
		if (current >= total)
			page.setItem(size, new ItemStackBuilder("scroller.next-item")
					.replace("%previous%", current - 1)
					.fromConfiguration(configuration)
					.replace("%total%", total)
					.replace("%next%", current + 1)
					.build());
		if (current > 0)
			page.setItem(size - 8, new ItemStackBuilder("scroller.previous-item")
					.replace("%previous%", current - 1)
					.fromConfiguration(configuration)
					.replace("%total%", total)
					.replace("%next%", current + 1)
					.build());
		return page;
	}

	public void fromConfiguration(FileConfiguration configuration) {
		this.configuration = configuration;
	}

	public void nextPage(Player player) {
		if (!users.contains(player))
			return;
		if (current >= pages.size() - 1)
			return;
		current++;
		player.openInventory(pages.get(current));
	}

	public void previousPage(Player player) {
		if (!users.contains(player))
			return;
		if (current > 0) {
			current--;
			player.openInventory(pages.get(current));
		}
	}

	public void setAction(Consumer<InventoryClickEvent> consumer) {
		this.consumer = consumer;
	}

	public void runAction(InventoryClickEvent event) {
		if (consumer != null)
			consumer.accept(event);
	}

	public int getSize() {
		return size;
	}

	public Set<Player> getUsers() {
		return users;
	}

	public void openInventory(KingdomPlayer kingdomPlayer) {
		kingdomPlayer.getPlayer().openInventory(pages.get(current));
	}

}
