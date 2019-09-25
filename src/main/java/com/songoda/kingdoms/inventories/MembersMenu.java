package com.songoda.kingdoms.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.inventories.PagesInventory;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class MembersMenu extends PagesInventory {

	public MembersMenu() {
		super("members", 45);
	}

	@Override
	public List<PageItem> getItems(KingdomPlayer kingdomPlayer) {
		List<PageItem> items = new ArrayList<>();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return items;
		for (OfflineKingdomPlayer player : instance.getManager(RankManager.class).sortByRanks(kingdom.getMembers())) {
			ItemStack item = new ItemStackBuilder("inventories.members.player-item")
					.replace("%gui-format%", getPrefix(kingdomPlayer))
					.fromConfiguration(inventories)
					.setPlaceholderObject(player)
					.setKingdom(kingdom)
					.build();
			items.add(new PageItem(item, event -> {
				if (kingdom.getOwner().isPresent() && player.equals(kingdom.getOwner().get())) {
					new MessageBuilder("kingdoms.owner-may-not-be-modified")
							.setPlaceholderObject(player)
							.setKingdom(kingdom)
							.send(kingdomPlayer);
					return;
				}
				Rank rank = player.getRank();
				if (event.isLeftClick()) {
					Rank next = kingdom.getNextRank(rank);
					if (next.isEqualTo(kingdom.getHighestRank())) {
						new MessageBuilder("kingdoms.owner-transfer-command-only")
								.setPlaceholderObject(player)
								.setKingdom(kingdom)
								.send(kingdomPlayer);
						return;
					}
					if (next.isHigherThan(rank) && player.equals(kingdomPlayer)) {
						new MessageBuilder("kingdoms.cant-promote-self")
								.setPlaceholderObject(player)
								.setKingdom(kingdom)
								.send(kingdomPlayer);
						return;
					}
					player.setRank(next);
				} else if (event.isRightClick())
					player.setRank(kingdom.getPreviousRank(rank));
				else
					return;
				reopen(kingdomPlayer);
			}));
		}
		return items;
	}

	@Override
	protected Consumer<InventoryClickEvent> getBackAction(KingdomPlayer kingdomPlayer) {
		return event -> instance.getManager(InventoryManager.class).getInventory(NexusInventory.class).open(kingdomPlayer);
	}

	private String getPrefix(KingdomPlayer kingdomPlayer) {
		Rank rank = kingdomPlayer.getRank();
		FileConfiguration ranks = instance.getConfiguration("ranks").get();
		MessageBuilder builder = new MessageBuilder(false, "gui-prefix")
				.replace("%chatcolor%", rank.getChatColor().toString())
				.replace("%prefix%", rank.getPrefix(kingdomPlayer))
				.replace("%color%", rank.getColor().toString())
				.withPlaceholder(kingdomPlayer, new SimplePlaceholder("%player%") {
					@Override
					public String get() {
						if (ranks.getBoolean("use-display-name", false))
							return kingdomPlayer.getPlayer().getDisplayName();
						else
							return kingdomPlayer.getName();
					}
				})
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(ranks);
		return builder.get();
	}

}
