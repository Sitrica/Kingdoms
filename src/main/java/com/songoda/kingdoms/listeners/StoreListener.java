package com.songoda.kingdoms.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.events.StoreEvent;
import com.songoda.kingdoms.events.UnstoreEvent;

/**
 * 
 * @author eyesniper2
 * With permission
 * https://github.com/eyesniper2/skRayFall/blob/master/src/main/java/net/rayfall/eyesniper2/skrayfall/general/events/StoreListener.java
 *
 */
public class StoreListener implements Listener {

	private final Set<Player> unstorePossible = new HashSet<>();
	private final Set<Player> storePossible = new HashSet<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStoringFilter(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (!check(event.getAction(), InventoryAction.SWAP_WITH_CURSOR, InventoryAction.COLLECT_TO_CURSOR, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_ONE))
			return;
		InventoryView view = event.getView();
		Inventory top = view.getTopInventory();
		Inventory inventory = event.getClickedInventory();
		if (view.getBottomInventory().getType() != InventoryType.PLAYER)
			return;
		InventoryType type = top.getType();
		if (!isAcceptableInventory(type))
			return;
		// If store.
		if (inventory.getType() == InventoryType.PLAYER) {
			if (!storePossible.contains(player)) {
				storePossible.add(player);
				unstorePossible.remove(player);
			}
		// If unstore.
		} else if (!unstorePossible.contains(player)) {
			unstorePossible.add(player);
			storePossible.remove(player);
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		InventoryView view = event.getView();
		Player player = (Player) event.getPlayer();
		if (view.getBottomInventory().getType() != InventoryType.PLAYER)
			return;
		if (isAcceptableInventory(view.getTopInventory().getType())) {
			storePossible.remove(player);
			unstorePossible.remove(player);
		}
	}

	@EventHandler
	public void onStore(InventoryClickEvent event) {
		InventoryAction action = event.getAction();
		Player player = (Player) event.getWhoClicked();
		InventoryType type = event.getClickedInventory().getType();
		ItemStack cursor = event.getCursor().clone();
		if (check(action, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL, InventoryAction.SWAP_WITH_CURSOR)) {
			if (!isAcceptableInventory(type))
				return;
			if (!storePossible.contains(player))
				return;
			if (event.isLeftClick()) {
				StoreEvent store = new StoreEvent(player, cursor, event.getInventory(), event.getSlot());
				Bukkit.getPluginManager().callEvent(store);
				if (store.isCancelled()) {
					event.setCancelled(true);
					return;
				}
				storePossible.remove(player);
			} else {
				cursor.setAmount(1);
				StoreEvent store = new StoreEvent(player, cursor, event.getInventory(), event.getSlot());
				Bukkit.getPluginManager().callEvent(store);
				if (store.isCancelled())
					event.setCancelled(true);
			}
		} else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			if (event.isShiftClick() && type == InventoryType.PLAYER) {
				InventoryView view = event.getView();
				InventoryType topType = view.getTopInventory().getType();
				if (view.getBottomInventory().getType() != InventoryType.PLAYER)
					return;
				if (!isAcceptableInventory(topType))
					return;
				StoreEvent store = new StoreEvent(player, cursor, event.getInventory(), event.getSlot());
				Bukkit.getPluginManager().callEvent(store);
				if (store.isCancelled()) {
					event.setCancelled(true);
					return;
				}
				storePossible.remove(player);
			}
		}
	}

	@EventHandler
	public void onStoreDrag(InventoryDragEvent event) {
		Inventory inventory = event.getInventory();
		InventoryType type = inventory.getType();
		Player player = (Player) event.getWhoClicked();
		InventoryView view = event.getView();
		Inventory top = view.getTopInventory();
		if (isAcceptableInventory(type)) {
			if (storePossible.contains(player)) {
				StoreEvent store = new StoreEvent(player, inventory, event.getNewItems());
				Bukkit.getPluginManager().callEvent(store);
				if (store.isCancelled())
					event.setCancelled(true);
			}
		}
		if (view.getBottomInventory().getType() == InventoryType.PLAYER) {
			if (!isAcceptableInventory(top.getType()))
				return;
			if (!unstorePossible.contains(player))
				return;
			if (!isAcceptableInventory(inventory.getType()))
				return;
			int chestSize = view.getTopInventory().getSize();
			int num = 0;
			ItemStack temp = new ItemStack(Material.AIR, 0);
			for (int i : event.getNewItems().keySet()) {
				if (i >= chestSize) {
					ItemStack item = event.getNewItems().get(i);
					if (num == 0)
						temp = item.clone();
					if (view.getBottomInventory().getItem(view.convertSlot(i)) != null) {
						num += item.getAmount() - view.getBottomInventory().getItem(view.convertSlot(i)).getAmount();
					} else {
						num += item.getAmount();
					}
				}
			}
			if (temp.getType() == Material.AIR)
				return;
			temp.setAmount(num);
			Set<Integer> slots = event.getInventorySlots();
			UnstoreEvent unstore = new UnstoreEvent(player, temp, inventory, slots.toArray(new Integer[slots.size()]));
			Bukkit.getPluginManager().callEvent(unstore);
			if (unstore.isCancelled())
				event.setCancelled(true);
		}

	}

	@EventHandler
	public void onUnstore(InventoryClickEvent event) {
		InventoryAction action = event.getAction();
		Player player = (Player) event.getWhoClicked();
		InventoryView view = event.getView();
		ItemStack cursor = event.getCursor().clone();
		if (check(action, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL, InventoryAction.SWAP_WITH_CURSOR)) {
			Inventory clicked = event.getClickedInventory();
			if (clicked.getType() != InventoryType.PLAYER)
				return;
			if (event.isLeftClick() && unstorePossible.contains(player)) {
				UnstoreEvent unstore = new UnstoreEvent(player, cursor, event.getInventory());
				Bukkit.getPluginManager().callEvent(unstore);
				if (unstore.isCancelled()) {
					event.setCancelled(true);
					return;
				}
				unstorePossible.remove(player);
			}
			if (event.isRightClick() && unstorePossible.contains(player)) {
				cursor.setAmount(1);
				UnstoreEvent unstore = new UnstoreEvent(player, cursor, event.getInventory());
				Bukkit.getPluginManager().callEvent(unstore);
				if (unstore.isCancelled())
					event.setCancelled(true);
			}
		} else if (check(action, InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ONE_SLOT)) {
			if (!isAcceptableInventory(view.getTopInventory().getType()))
				return;
			if (view.getBottomInventory().getType() != InventoryType.PLAYER)
				return;
			if (event.isLeftClick() && unstorePossible.contains(player)) {
				UnstoreEvent unstore = new UnstoreEvent(player, cursor, event.getInventory());
				Bukkit.getPluginManager().callEvent(unstore);
				if (unstore.isCancelled()) {
					event.setCancelled(true);
					return;
				}
				unstorePossible.remove(player);
			}
			if (event.isRightClick() && unstorePossible.contains(player)) {
				cursor.setAmount(1);
				UnstoreEvent unstore = new UnstoreEvent(player, cursor, event.getInventory());
				Bukkit.getPluginManager().callEvent(unstore);
				if (unstore.isCancelled())
					event.setCancelled(true);
			}
		} else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			if (view.getBottomInventory().getType() != InventoryType.PLAYER)
				return;
			if (!isAcceptableInventory(view.getTopInventory().getType()))
				return;
			if (event.isShiftClick() && isAcceptableInventory(event.getClickedInventory().getType())) {
				UnstoreEvent unstore = new UnstoreEvent(player, cursor, event.getInventory());
				Bukkit.getPluginManager().callEvent(unstore);
				if (unstore.isCancelled())
					event.setCancelled(true);
			}
		}
	}
	
	private <T> boolean check(T action, @SuppressWarnings("unchecked") T... actions) {
		return Sets.newHashSet(actions).contains(action);
	}
	
	private boolean isAcceptableInventory(InventoryType type) {
		return (check(type, InventoryType.CHEST, InventoryType.ENDER_CHEST, InventoryType.HOPPER, InventoryType.DISPENSER, InventoryType.DROPPER));
	}

}
