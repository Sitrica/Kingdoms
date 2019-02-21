package com.songoda.kingdoms.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

	public static boolean hasEnough(Player player, Material material, int amount) {
		int amt = 0;
		for (ItemStack item : player.getInventory().all(material).values()) {
			if (item == null)
				continue;
			amt += item.getAmount();
			if (amt >= amount)
				return true;
		}
		return false;
	}
	
	public static void removeMaterial(Player player, Material material, int amount) {
		int amt = 0;
		amt += amount;
		for (ItemStack item : player.getInventory().all(material).values()) {
			if (item == null)
				continue;
			amt -= item.getAmount();
			player.getInventory().remove(item);
			player.updateInventory();
			if (amt <= 0)
				break;
		}
		if (amt < 0)
			player.getInventory().addItem(new ItemStack(material, amt * -1));
		player.updateInventory();
	}

}
