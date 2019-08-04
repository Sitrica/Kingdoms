package me.limeglass.kingdoms.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.limeglass.kingdoms.inventories.structures.NexusInventory;
import me.limeglass.kingdoms.manager.inventories.InventoryManager;
import me.limeglass.kingdoms.manager.inventories.PagesInventory;
import me.limeglass.kingdoms.manager.managers.RankManager;
import me.limeglass.kingdoms.manager.managers.RankManager.Rank;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.player.OfflineKingdomPlayer;
import me.limeglass.kingdoms.placeholders.SimplePlaceholder;
import me.limeglass.kingdoms.utils.ItemStackBuilder;
import me.limeglass.kingdoms.utils.MessageBuilder;

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
		RankManager rankManager = instance.getManager(RankManager.class);
		for (OfflineKingdomPlayer player : instance.getManager(RankManager.class).sortByRanks(kingdom.getMembers())) {
			ItemStack item = new ItemStackBuilder("inventories.members.player-item")
					.replace("%gui-format%", getPrefix(kingdomPlayer))
					.fromConfiguration(inventories)
					.setPlaceholderObject(player)
					.setKingdom(kingdom)
					.build();
			items.add(new PageItem(item, event -> {
				if (player.equals(kingdom.getOwner())) {
					new MessageBuilder("kingdoms.owner-may-not-be-modified")
							.setPlaceholderObject(player)
							.setKingdom(kingdom)
							.send(kingdomPlayer);
					return;
				}
				Rank rank = player.getRank();
				if (event.isLeftClick()) {
					Rank next = kingdom.getNextRank(rank);
					if (next.isEqualTo(rankManager.getOwnerRank())) {
						new MessageBuilder("kingdoms.owner-transfer-command-only")
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
