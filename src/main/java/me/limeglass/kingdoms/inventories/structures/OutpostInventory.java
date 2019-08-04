package me.limeglass.kingdoms.inventories.structures;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.limeglass.kingdoms.manager.inventories.StructureInventory;
import me.limeglass.kingdoms.manager.managers.RankManager.Rank;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.placeholders.Placeholder;
import me.limeglass.kingdoms.utils.ItemStackBuilder;
import me.limeglass.kingdoms.utils.MessageBuilder;
import me.limeglass.kingdoms.utils.Utils;

public class OutpostInventory extends StructureInventory {

	public OutpostInventory() {
		super(InventoryType.HOPPER, "outpost", 69);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		int cost = structures.getInt("structures.outpost.experience-bottle-cost", 5);
		ItemStack xp1 = new ItemStackBuilder(section.getConfigurationSection("buy-xp1"))
				.setPlaceholderObject(kingdomPlayer)
				.replace("%cost%", cost)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(0, xp1);
		setAction(player.getUniqueId(), 0, event -> {
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
			reopen(kingdomPlayer);
		});
		int cost64 = cost * 64;
		ItemStack xp64 = new ItemStackBuilder(section.getConfigurationSection("buy-xp64"))
				.setPlaceholderObject(kingdomPlayer)
				.replace("%cost%", cost64)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(1, xp64);
		setAction(player.getUniqueId(), 1, event -> {
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
			reopen(kingdomPlayer);
		});
		return inventory;
	}

}
