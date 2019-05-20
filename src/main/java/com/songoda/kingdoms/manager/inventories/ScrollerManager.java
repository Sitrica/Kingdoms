package com.songoda.kingdoms.manager.inventories;

import com.songoda.kingdoms.manager.Manager;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ScrollerManager extends Manager {

	private final Set<ScrollerInventory> scrollers = new HashSet<>();

	public ScrollerManager() {
		super( true);
	}

	public void registerScroller(ScrollerInventory scroller) {
		this.scrollers.add(scroller);
	}

	@EventHandler(ignoreCancelled = true)
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		scrollers.parallelStream()
				.filter(scroller -> scroller.getUsers().contains(player))
				.forEach(scroller -> {
					event.setCancelled(true);
					if (event.getSlot() == scroller.getSize()) {
						scroller.nextPage(player);
					} else if (event.getSlot() == scroller.getSize() - 8) {
						scroller.previousPage(player);
					} else {
						scroller.runAction(event);
					}
				});
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {
		scrollers.clear();
	}

}
