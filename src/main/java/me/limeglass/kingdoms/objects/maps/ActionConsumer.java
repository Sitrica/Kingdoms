package me.limeglass.kingdoms.objects.maps;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.manager.managers.TeleportManager;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.utils.LocationUtils;
import me.limeglass.kingdoms.utils.MessageBuilder;

public enum ActionConsumer {

	COMMAND((land, kingdomPlayer) -> {
		throw new UnsupportedOperationException("The COMMAND ActionConsumer may not be accessed this way, you need to use the #executeCommand(Player, String) method");
	}) {
		@Override
		public void executeCommand(Player player, String command) {
			player.performCommand(command);
		}
	},
	TELEPORT((land, kingdomPlayer) -> {
		Structure structure = land.getStructure();
		Optional<OfflineKingdom> landKingdom = land.getKingdomOwner();
		new MessageBuilder("messages.teleport")
				.fromConfiguration(Kingdoms.getInstance().getConfiguration("map").get())
				.replace("%chunk%", LocationUtils.chunkToString(land.getChunk()))
				.setKingdom(landKingdom.isPresent() ? landKingdom.get() : null)
				.setPlaceholderObject(kingdomPlayer)
				.send(kingdomPlayer);
		Location location = LocationUtils.getLowestBlockIn(land.getChunk());
		if (structure != null)
			location = structure.getLocation().add(0, 1, 0);
		Kingdoms.getInstance().getManager(TeleportManager.class).teleport(kingdomPlayer, location);
	});

	private final BiConsumer<Land, KingdomPlayer> consumer;

	ActionConsumer(BiConsumer<Land, KingdomPlayer> consumer) {
		this.consumer = consumer;
	}

	public void accept(Land land, KingdomPlayer kingdomPlayer) {
		consumer.accept(land, kingdomPlayer);
	}

	public void executeCommand(Player player, String command) {}

}
