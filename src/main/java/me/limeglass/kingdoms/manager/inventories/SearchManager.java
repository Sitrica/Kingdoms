package me.limeglass.kingdoms.manager.inventories;

import java.util.function.Consumer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.limeglass.kingdoms.manager.Manager;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.ItemStackBuilder;

public class SearchManager extends Manager {

	public SearchManager() {
		super(true);
	}

	public void openSearch(KingdomPlayer kingdomPlayer, Consumer<String> consumer) {
		Player player = kingdomPlayer.getPlayer();
		FileConfiguration inventories = instance.getConfiguration("inventories").get();
		ItemStack search = new ItemStackBuilder(inventories.getConfigurationSection("search.search-item"))
				.setPlaceholderObject(kingdomPlayer)
				.build();
		new AnvilMenu(search, player, consumer);
	}

	@Override
	public void onDisable() {}

	@Override
	public void initalize() {}

}
