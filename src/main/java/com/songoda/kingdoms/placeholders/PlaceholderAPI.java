package com.songoda.kingdoms.placeholders;

import java.util.Optional;

import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPI extends PlaceholderExpansion {

	private final Kingdoms instance;

	/**
	 * Since we register the expansion inside our own plugin, we
	 * can simply use this method here to get an instance of our
	 * plugin.
	 *
	 * @param plugin The instance of our plugin.
	 */
	public PlaceholderAPI(Kingdoms instance) {
		this.instance = instance;
	}

	/**
	 * Because this is an internal class,
	 * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
	 * PlaceholderAPI is reloaded.
	 *
	 * @return true to persist through reloads
	 */
	@Override
	public boolean persist() {
		return true;
	}

	/**
	 * Because this is a internal class, this check is not needed
	 * and we can simply return {@code true}
	 *
	 * @return Always true since it's an internal class.
	 */
	@Override
	public boolean canRegister() {
		return true;
	}

	/**
	 * The name of the person who created this expansion should go here.
	 * <br>For convenience do we return the author from the plugin.yml
	 * 
	 * @return The name of the author as a String.
	 */
	@Override
	public String getAuthor() {
		return instance.getDescription().getAuthors().toString();
	}

	/**
	 * The placeholder identifier should go here.
	 * <br>This is what tells PlaceholderAPI to call our onRequest 
	 * method to obtain a value if a placeholder starts with our 
	 * identifier.
	 * <br>This must be unique and can not contain % or _
	 *
	 * @return The identifier in {@code %<identifier>_<value>%} as String.
	 */
	@Override
	public String getIdentifier() {
		return "kingdoms";
	}

	/**
	 * This is the version of the expansion.
	 * <br>You don't have to use numbers, since it is set as a String.
	 *
	 * For convenience do we return the version from the plugin.yml
	 *
	 * @return The version as a String.
	 */
	@Override
	public String getVersion() {
		return instance.getDescription().getVersion();
	}

	/**
	 * This is the method called when a placeholder with our identifier 
	 * is found and needs a value.
	 * <br>We specify the value identifier in this method.
	 * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
	 *
	 * @param player A {@link org.bukkit.Player Player}.
	 * @param identifier A String containing the identifier/value.
	 *
	 * @return possibly-null String of the requested identifier.
	 */
	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		PlayerManager playerManager = instance.getManager(PlayerManager.class);
		if (player == null)
			return new MessageBuilder("plugin.placeholder-api.player-null")
					.fromConfiguration(instance.getConfig())
					.get();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		switch (identifier) {
			case "kingdom":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.kingdom")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "hasKingdom":
				return (kingdom != null) + "";
			case "rp":
			case "points":
			case "resourcePoints":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.resource-points")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "land":
			case "landAt":
				Land land = kingdomPlayer.getLandAt();
				return new MessageBuilder("plugin.placeholder-api.land-at")
						.replace("%land%", LocationUtils.chunkToString(land.getChunk()))
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "members":
			case "offlinemembers":
			case "offlineMembers":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.offline-members")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "onlinemembers":
			case "onlineMembers":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.online-members")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "onlinecount":
			case "onlineCount":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.online-count")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "offlinecount":
			case "offlineCount":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.offline-count")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "king":
			case "owner":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.owner")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
			case "rank":
				if (kingdom == null)
					return new MessageBuilder("plugin.placeholder-api.no-kingdom")
							.fromConfiguration(instance.getConfig())
							.setPlaceholderObject(kingdomPlayer)
							.get();
				return new MessageBuilder("plugin.placeholder-api.rank")
						.fromConfiguration(instance.getConfig())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.get();
		}
		if (!instance.getConfig().getBoolean("plugin.placeholder-api.kingdom-placeholders", true))
			return null;
		if (!DefaultPlaceholders.hasInitalized())
			DefaultPlaceholders.initalize();
		Optional<Placeholder<?>> placeholder = Placeholders.getPlaceholder(identifier);
		if (placeholder.isPresent()) {
			String value = placeholder.get().replace_i(kingdomPlayer);
			if (value != null)
				return value;
			if (kingdom == null)
				return null;
			return placeholder.get().replace_i(kingdom);
		}
		return null;
	}

}
