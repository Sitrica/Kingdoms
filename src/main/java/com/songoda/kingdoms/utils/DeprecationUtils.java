package com.songoda.kingdoms.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

@SuppressWarnings("deprecation")
public class DeprecationUtils {

	public static void setupOldSkull(BlockState state) {
		MaterialData data = state.getData();
		data.setData((byte) 1);
		state.setData(data);
		state.update(true);
	}

	public static ItemStack getItemInMainHand(Player player) {
		try {
			return player.getItemInHand();
		} catch (Exception e) {
			return player.getInventory().getItemInMainHand();
		}
	}

	public static void setItemInMainHand(Player player, ItemStack item) {
		try {
			player.setItemInHand(item);
		} catch (Exception e) {
			player.getInventory().setItemInMainHand(item);
		}
	}

	public static OfflinePlayer getSkullOwner(String input) {
		if (Utils.isUUID(input))
			return Bukkit.getOfflinePlayer(Utils.getUniqueId(input));
		else
			return Bukkit.getOfflinePlayer(input);
	}

	public static ItemStack setupItemMeta(ItemStack itemstack, String meta) {
		ItemMeta itemMeta = itemstack.getItemMeta();
		if (itemMeta instanceof PotionMeta) {
			PotionMeta potionMeta = (PotionMeta) itemMeta;
			PotionType type;
			try {
				type = PotionType.valueOf(meta);
			} catch (Exception e) {
				type = PotionType.SPEED;
			}
			potionMeta.setBasePotionData(new PotionData(type));
			itemstack.setItemMeta(potionMeta);
		}
		if (itemMeta instanceof SkullMeta) {
			SkullMeta skullMeta = (SkullMeta) itemMeta;
			skullMeta.setOwningPlayer(getSkullOwner(meta));
			itemstack.setItemMeta(skullMeta);
		}
		if (itemMeta instanceof TropicalFishBucketMeta) {
			TropicalFishBucketMeta fishMeta = (TropicalFishBucketMeta) itemMeta;
			String[] metas = meta.split(":");
			if (metas.length < 2)
				return itemstack;
			Pattern pattern;
			try {
				pattern = Pattern.valueOf(metas[1]);
			} catch (Exception e) {
				pattern = Pattern.BETTY;
			}
			fishMeta.setPattern(pattern);
			DyeColor color;
			try {
				color = DyeColor.valueOf(metas[0]);
			} catch (Exception e) {
				color = DyeColor.GREEN;
			}
			fishMeta.setBodyColor(color);
			itemstack.setItemMeta(fishMeta);
		}
		if (itemMeta instanceof SpawnEggMeta) {
			SpawnEggMeta eggMeta = (SpawnEggMeta) itemMeta;
			EntityType entity;
			try {
				entity = EntityType.valueOf(meta);
			} catch (Exception e) {
				entity = EntityType.ZOMBIE;
			}
			eggMeta.setSpawnedType(entity);
			itemstack.setItemMeta(eggMeta);
		}
		if (itemMeta instanceof LeatherArmorMeta) {
			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
			Color color;
			String[] colors = meta.split(":");
			if (colors.length < 3)
				return itemstack;
			int r = Integer.parseInt(colors[0]);
			int g = Integer.parseInt(colors[1]);
			int b = Integer.parseInt(colors[2]);
			try {
				color = Color.fromBGR(r, g, b);
			} catch (Exception e) {
				color = Color.RED;
			}
			leatherMeta.setColor(color);
			itemstack.setItemMeta(leatherMeta);
		}
		if (itemMeta instanceof BannerMeta) {
			BannerMeta bannerMeta = (BannerMeta) itemMeta;
			DyeColor color;
			try {
				color = DyeColor.valueOf(meta);
			} catch (Exception e) {
				color = DyeColor.RED;
			}
			bannerMeta.setBaseColor(color);
			itemstack.setItemMeta(bannerMeta);
		}
		return itemstack;
	}

}
