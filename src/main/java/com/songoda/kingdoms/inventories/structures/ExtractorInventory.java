package com.songoda.kingdoms.inventories.structures;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Extractor;
import com.songoda.kingdoms.utils.ItemStackBuilder;

public class ExtractorInventory extends StructureInventory {

	public ExtractorInventory() {
		super(InventoryType.HOPPER, "extractor", 69);
	}

	@Override
	public Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer) {
		throw new UnsupportedOperationException("This method should not be called, use openExtractorMenu(Extractor, KingdomPlayer)");
	}

	public void openExtractorMenu(Extractor extractor, KingdomPlayer kingdomPlayer) {
		if (extractor == null)
			return;
		Inventory inventory = createInventory(kingdomPlayer);
		openExtractorMenu(inventory, extractor, kingdomPlayer);
	}

	public void openExtractorMenu(Inventory inventory, Extractor extractor, KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		long minutes = extractor.getMinutes();
		boolean ready = extractor.isReady();
		long amount = extractor.getReward();
		ConfigurationSection section = this.section;
		ConfigurationSection timeSection = section.getConfigurationSection("time-item");
		if (section.getBoolean("use-filler", true)) {
			ItemStack filler = new ItemStackBuilder(section.getConfigurationSection("filler-item-collect"))
					.setKingdom(kingdom != null ? kingdom : null)
					.setPlaceholderObject(kingdomPlayer)
					.replace("%time%", minutes)
					.replace("%amount%", amount)
					.build();
			if (!ready)
				filler = new ItemStackBuilder(section.getConfigurationSection("filler-item-cant-collect"))
						.setKingdom(kingdom != null ? kingdom : null)
						.setPlaceholderObject(kingdomPlayer)
						.replace("%time%", minutes)
						.replace("%amount%", amount)
						.build();
			for (int i = 0; i < inventory.getType().getDefaultSize(); i++)
				inventory.setItem(i, filler);
		}
		Player player = kingdomPlayer.getPlayer();
		openInventory(inventory, player);
		BukkitTask task = new BukkitRunnable() {
			@Override
			public void run() {
				if (ready)
					inventory.setItem(2, new ItemStackBuilder(section.getConfigurationSection("collect-item"))
							.setKingdom(kingdom != null ? kingdom : null)
							.setPlaceholderObject(kingdomPlayer)
							.replace("%amount%", amount)
							.replace("%time%", minutes)
							.build());
				else
					inventory.setItem(2, new ItemStackBuilder(timeSection)
							.glowingIf(() -> {
								if (timeSection.getBoolean("glowing-collect", false))
									return ready;
								return false;
							})
							.setKingdom(kingdom != null ? kingdom : null)
							.setPlaceholderObject(kingdomPlayer)
							.replace("%amount%", amount)
							.replace("%time%", minutes)
							.build());
				if (!player.getOpenInventory().getTopInventory().equals(inventory)) { //TODO needs testing.
					cancel();
				}
			}
		}.runTaskTimer(instance, 0, 20 * 30); // 30 seconds
		setAction(player.getUniqueId(), 2, event -> {
			extractor.collect(kingdomPlayer);
			openExtractorMenu(inventory, extractor, kingdomPlayer);
			task.cancel();
		});
	}

}
