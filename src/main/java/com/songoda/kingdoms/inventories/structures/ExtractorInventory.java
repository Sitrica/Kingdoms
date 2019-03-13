package com.songoda.kingdoms.inventories.structures;

import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Extractor;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.ItemStackBuilder;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ExtractorInventory extends StructureInventory {
	
	private final long amount, time;
	
	public ExtractorInventory() {
		super(InventoryType.HOPPER, "extractor", 1);
		FileConfiguration structures = instance.getConfiguration("structures").get();
		this.amount = structures.getInt("structures.extractor.reward-amount", 50);
		String interval = structures.getString("structures.extractor.reward-delayt", "24 hours");
		this.time = IntervalUtils.getInterval(interval);
	}
	
	@Override
	public void openInventory(KingdomPlayer kingdomPlayer) {
		throw new UnsupportedOperationException("This method should not be called, use openExtractorMenu(Extractor, KingdomPlayer)");
	}

	public void openExtractorMenu(Extractor extractor, KingdomPlayer kingdomPlayer) {
		boolean ready = extractor.isReady();
		long timeLeft = extractor.getTimeLeft();
		if (timeLeft > time)
			extractor.resetTime();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		ConfigurationSection section = inventories.getConfigurationSection("inventories.extractor");
		ItemStackBuilder collectBuilder = new ItemStackBuilder(section.getConfigurationSection("collect-item"))
				.setKingdom(kingdom != null ? kingdom : null)
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.replace("%time%", timeLeft)
				.replace("%amount%", amount);
		ConfigurationSection timeSection = section.getConfigurationSection("time-item");
		ItemStackBuilder timeBuilder = new ItemStackBuilder(timeSection)
				.glowingIf(() -> {
					if (timeSection.getBoolean("glowing-collect", false))
						return ready;
					return false;
				})
				.setKingdom(kingdom != null ? kingdom : null)
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.replace("%time%", timeLeft)
				.replace("%amount%", amount);
		if (section.getBoolean("use-filler", true)) {
			ItemStack filler = new ItemStackBuilder(section.getConfigurationSection("filler-item-collect"))
					.setKingdom(kingdom != null ? kingdom : null)
					.setPlaceholderObject(kingdomPlayer)
					.fromConfiguration(inventories)
					.replace("%time%", timeLeft)
					.replace("%amount%", amount)
					.build();
			if (!ready)
				filler = new ItemStackBuilder(section.getConfigurationSection("filler-item-cant-collect"))
						.setKingdom(kingdom != null ? kingdom : null)
						.setPlaceholderObject(kingdomPlayer)
						.fromConfiguration(inventories)
						.replace("%time%", timeLeft)
						.replace("%amount%", amount)
						.build();
			for (int i = 0; i < inventory.getType().getDefaultSize(); i++)
				inventory.setItem(i, filler);
		}
		if (ready)
			inventory.setItem(2, collectBuilder.build());
		else
			inventory.setItem(2, timeBuilder.build());
		Player player = kingdomPlayer.getPlayer();
		openInventory(player);
		setAction(2, event -> extractor.collect(kingdomPlayer));
		new BukkitRunnable() {
			@Override
			public void run() {
				if (ready)
					inventory.setItem(2, collectBuilder.build());
				else
					inventory.setItem(2, timeBuilder.build());
				if (!player.getOpenInventory().getTopInventory().equals(inventory)) { //TODO needs testing.
					cancel();
				}
			}
		}.runTaskTimer(instance, 0, 1);
	}

}
