package com.songoda.kingdoms.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.songoda.core.compatibility.ClientVersion;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.BackgroundType;
import com.songoda.core.gui.GuiManager;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.CoreManager;
import com.songoda.kingdoms.objects.StringList;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;

public class PopupBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private final Set<KingdomPlayer> kingdomPlayers = new HashSet<>();
	private final List<Player> players = new ArrayList<>();
	private Object defaultPlaceholderObject;
	private ConfigurationSection section;
	private OfflineKingdom kingdom;
	private KingdomPlayer self;
	private String node;

	/**
	 * Creates a PopupBuilder from the defined ConfigurationSection.
	 * 
	 * @param section The ConfigurationSection to read from.
	 */
	public PopupBuilder(ConfigurationSection section) {
		this.section = section;
	}

	public PopupBuilder(String node) {
		this.node = node;
	}

	/**
	 * Set the players to send this popup to.
	 *
	 * @param senders The Collection<KingdomPlayer> to send the message to.
	 * @return The PopupBuilder for chaining.
	 */
	public PopupBuilder toKingdomPlayers(Collection<? extends KingdomPlayer> players) {
		this.kingdomPlayers.addAll(players);
		return this;
	}

	/**
	 * Set the players to send this popup to.
	 *
	 * @param senders The Players... to send the message to.
	 * @return The PopupBuilder for chaining.
	 */
	public PopupBuilder toPlayers(Player... players) {
		this.players.addAll(Sets.newHashSet(players));
		return this;
	}

	/**
	 * Set the players to send this pop to.
	 *
	 * @param senders The Collection<Player> to send the message to.
	 * @return The PopupBuilder for chaining.
	 */
	public PopupBuilder toPlayers(Collection<? extends Player> players) {
		this.players.addAll(players);
		return this;
	}

	/**
	 * Add a placeholder to the PopupBuilder.
	 * 
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The MessageBuilder for chaining.
	 */
	public PopupBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
		this.defaultPlaceholderObject = placeholderObject;
		placeholders.put(placeholder, placeholderObject);
		return this;
	}

	/**
	 * Set the configuration to read from, by default is the messages.yml
	 * 
	 * @param configuration The FileConfiguration to read from.
	 * @return The MessageBuilder for chaining.
	 */
	public PopupBuilder fromConfiguration(ConfigurationSection section) {
		this.section = section;
		return this;
	}

	/**
	 * Created a list replacement and ignores the placeholder object.
	 * @param <T>
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The MessageBuilder for chaining.
	 */
	public <T> PopupBuilder replace(String syntax, Collection<T> collection, Function<T, String> mapper) {
		replace(syntax, new StringList(collection, mapper).toString());
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object.
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The MessageBuilder for chaining.
	 */
	public PopupBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Add a player to ignore this message, useful when sending to multiple players.
	 * 
	 * @param self The KingdomPlayer to ignore this message.
	 * @return The MessageBuilder for chaining.
	 */
	public PopupBuilder ignoreSelf(KingdomPlayer self) {
		this.self = self;
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The MessageBuilder for chaining.
	 */
	public PopupBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}

	/**
	 * Set the Kingdom option to be used for placeholders later.
	 * 
	 * @param kingdom The OfflineKingdom to set as.
	 * @return The MessageBuilder for chaining.
	 */
	public PopupBuilder setKingdom(OfflineKingdom kingdom) {
		this.kingdom = kingdom;
		return this;
	}

	/**
	 * Sends the final product of the builder.
	 */
	public void send(Collection<KingdomPlayer> players) {
		toKingdomPlayers(Sets.newHashSet(players)).send();
	}

	/**
	 * Sends the final product of the builder.
	 */
	public void send(KingdomPlayer... players) {
		send(Sets.newHashSet(players));
	}

	/**
	 * Sends the final product of the builder if the senders are set.
	 */
	@SuppressWarnings("static-access")
	public void send() {
		Kingdoms instance = Kingdoms.getInstance();
		if (section == null)
			section = instance.getConfiguration("messages").orElse(instance.getConfig());
		if (node != null)
			section = section.getConfigurationSection(node);
		if (section == null)
			return;
		if (!section.getBoolean("enabled", true))
			return;
		if (!kingdomPlayers.isEmpty()) {
			players.addAll(kingdomPlayers.parallelStream()
					.map(player -> player.getPlayer())
					.collect(Collectors.toSet()));
		}
		if (players.isEmpty())
			return;
		String message = new MessageBuilder(false, "message", section)
				.setPlaceholderObject(defaultPlaceholderObject)
				.addPlaceholders(placeholders)
				.setKingdom(kingdom)
				.get();
		BackgroundType background = Optional.ofNullable(BackgroundType.valueOf(section.getString("background", "ADVENTURE")))
				.orElse(BackgroundType.ADVENTURE);
		CompatibleMaterial material = CompatibleMaterial.getMaterial(section.getString("material", "ARROW"), CompatibleMaterial.ARROW);
		GuiManager guiManager = instance.getManager(CoreManager.class).getGuiManager();
		boolean title = section.getBoolean("no-title", true);
		for (Player player : players) {
			if (self != null && self.getUniqueId().equals(player.getUniqueId()))
				continue;
			// TODO Songoda Fix soon. Improper static access.
			// Check if the setting for players to not have a title sent under 1.12 is set.
			if (!ClientVersion.getClientVersion(player).isServerVersionAtLeast(ServerVersion.V1_12) && title)
				continue;
			guiManager.showPopup(player, message, material, background);
		}
	}

}
