package com.songoda.kingdoms.placeholders;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.placeholder.RelativePlaceholder;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.Formatting;

public class HologramPlaceholders {

	public HologramPlaceholders(Kingdoms instance) {
		PlayerManager playerManager = instance.getManager("player", PlayerManager.class);
		LandManager landManager = instance.getManager("land", LandManager.class);
		FileConfiguration messages = instance.getConfiguration("messages").get();
		RelativePlaceholder.register(new RelativePlaceholder("{kingdom}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdom.getName() : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{kingdom-rank}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdomPlayer.getRank().getName() : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{points}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdom.getResourcePoints() + "" : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{claims}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdom.getClaims().size() + "" : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{kingdom-at}") {
			@Override
			public String getReplacement(Player player) {
				Optional<OfflineKingdom> kingdom = landManager.getLandAt(player.getLocation()).getKingdomOwner();
				String string = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom-at", "No Kingdom"));
				return kingdom.isPresent() ? kingdom.get().getName() : string;
			}
		});
	}
	
}
