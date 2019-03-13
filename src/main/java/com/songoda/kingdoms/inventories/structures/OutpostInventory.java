package com.songoda.kingdoms.inventories.structures;

import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class OutpostInventory extends StructureInventory {

	protected OutpostInventory() {
		super(InventoryType.HOPPER, "outpost", 69);
	}

	@Override
	public void openInventory(KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		ConfigurationSection section = inventories.getConfigurationSection("inventories.outpost");
		int cost = structures.getInt("structures.outpost.experience-bottle-cost", 5);
		ItemStack xp1 = new ItemStackBuilder(section.getConfigurationSection("buy-xp1"))
				.setPlaceholderObject(kingdomPlayer)
				.replace("%cost%", cost)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(0, xp1);
		setAction(0, event -> {
			if (!kingdom.getPermissions(kingdomPlayer.getRank()).canGrabExperience()) {
				new MessageBuilder("kingdoms.rank-too-low-grab-experience")
						.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canGrabExperience()), new Placeholder<Optional<Rank>>("%rank%") {
							@Override
							public String replace(Optional<Rank> rank) {
								if (rank.isPresent())
									return rank.get().getName();
								return "(Not attainable)";
							}
						})
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (player.getInventory().firstEmpty() <= 0) {
				new MessageBuilder("messages.inventory-full")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (cost > kingdom.getResourcePoints()) {
				new MessageBuilder("structures.outpost.experience-bottle-not-enough")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			kingdom.subtractResourcePoints(cost);
			player.getInventory().addItem(new ItemStack(Utils.materialAttempt("EXPERIENCE_BOTTLE", "EXP_BOTTLE")));
		});
		int cost64 = cost * 64;
		ItemStack xp64 = new ItemStackBuilder(section.getConfigurationSection("buy-xp64"))
				.setPlaceholderObject(kingdomPlayer)
				.replace("%cost%", cost64)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(1, xp64);
		setAction(1, event -> {
			if (!kingdom.getPermissions(kingdomPlayer.getRank()).canGrabExperience()) {
				new MessageBuilder("kingdoms.rank-too-low-grab-experience")
						.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canGrabExperience()), new Placeholder<Optional<Rank>>("%rank%") {
							@Override
							public String replace(Optional<Rank> rank) {
								if (rank.isPresent())
									return rank.get().getName();
								return "(Not attainable)";
							}
						})
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (player.getInventory().firstEmpty() <= 0) {
				new MessageBuilder("messages.inventory-full")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (cost64 > kingdom.getResourcePoints()) {
				new MessageBuilder("messages.not-enough-points")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost64)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			kingdom.subtractResourcePoints(cost64);
			player.getInventory().addItem(new ItemStack(Utils.materialAttempt("EXPERIENCE_BOTTLE", "EXP_BOTTLE"), 64));
		});
		ItemStack r = new ItemStack(Material.HAY_BLOCK);
		ItemMeta rm = r.getItemMeta();
		rm.setDisplayName(Kingdoms.getLang().getString("Guis_ResourcePoints_Title", kp.getLang()));
		ArrayList rl = new ArrayList();
		rl.add(Kingdoms.getLang().getString("Guis_ResourcePoints_Desc", kp.getLang()));
		rl.add(Kingdoms.getLang().getString("Guis_ResourcePoints_Count", kp.getLang()).replaceAll("%amount%", ""+kingdom.getResourcepoints()));
		rm.setLore(LoreOrganizer.organize(rl));
		r.setItemMeta(rm);

		gui.getInventory().setItem(8, r);
		openInventory(kingdomPlayer);
	}

}
