package com.songoda.kingdoms.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.ActionbarManager;
import com.songoda.kingdoms.objects.StringList;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.placeholders.Placeholders;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;

public class MessageBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private final Set<KingdomPlayer> kingdomPlayers = new HashSet<>();
	private final List<CommandSender> senders = new ArrayList<>();
	private Object defaultPlaceholderObject;
	private ConfigurationSection section;
	private OfflineKingdom kingdom;
	private KingdomPlayer self;
	private String complete;
	private String[] nodes;
	private boolean prefix;

	/**
	 * Creates a MessageBuilder with the defined nodes..
	 * 
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public MessageBuilder(String... nodes) {
		this.prefix = true;
		this.nodes = nodes;
	}

	/**
	 * Creates a MessageBuilder from the defined ConfigurationSection.
	 * 
	 * @param node The configuration nodes from the ConfigurationSection.
	 * @param section The ConfigurationSection to read from.
	 */
	public MessageBuilder(boolean prefix, String node, ConfigurationSection section) {
		this(prefix, node);
		this.section = section;
	}

	/**
	 * Creates a MessageBuilder with the defined nodes, and if it should contain the prefix.
	 * 
	 * @param prefix The boolean to enable or disable prefixing this message.
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public MessageBuilder(boolean prefix, String... nodes) {
		this.prefix = prefix;
		this.nodes = nodes;
	}

	public MessageBuilder addPlaceholders(Map<Placeholder<?>, Object> existing) {
		placeholders.putAll(existing);
		return this;
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Collection<KingdomPlayer> to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toKingdomPlayers(Collection<? extends KingdomPlayer> players) {
		this.kingdomPlayers.addAll(players);
		return this;
	}

	/**
	 * Set the senders to send this message to.
	 *
	 * @param senders The CommandSenders to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toSenders(CommandSender... senders) {
		this.senders.addAll(Sets.newHashSet(senders));
		return this;
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Players... to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toPlayers(Player... players) {
		this.senders.addAll(Sets.newHashSet(players));
		return this;
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Collection<Player> to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toPlayers(Collection<? extends Player> players) {
		this.senders.addAll(players);
		return this;
	}

	/**
	 * Add a placeholder to the MessageBuilder.
	 * 
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
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
	public MessageBuilder fromConfiguration(ConfigurationSection section) {
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
	public <T> MessageBuilder replace(String syntax, Collection<T> collection, Function<T, String> mapper) {
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
	public MessageBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Set the configuration nodes from messages.yml
	 *
	 * @param nodes The nodes to use.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder setNodes(String... nodes) {
		this.nodes = nodes;
		return this;
	}

	/**
	 * Add a player to ignore this message, useful when sending to multiple players.
	 * 
	 * @param self The KingdomPlayer to ignore this message.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder ignoreSelf(KingdomPlayer self) {
		this.self = self;
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}

	/**
	 * Set the Kingdom option to be used for placeholders later.
	 * 
	 * @param kingdom The OfflineKingdom to set as.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder setKingdom(OfflineKingdom kingdom) {
		this.kingdom = kingdom;
		return this;
	}

	/**
	 * Sends the message as an actionbar to the defined players.
	 * 
	 * @param players the players to send to
	 */
	public void sendActionbar(Player... players) {
		toPlayers(players).sendActionbar();
	}

	/**
	 * Sends the message as a title to the defined players.
	 * 
	 * @param players the players to send to
	 */
	public void sendTitle(Player... players) {
		toPlayers(players).sendTitle();
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
	 * Sends the final product of the builder.
	 */
	public void send(CommandSender... senders) {
		toSenders(senders).send();
	}

	/**
	 * Completes and returns the final product of the builder.
	 */
	public String get() {
		Kingdoms instance = Kingdoms.getInstance();
		if (section == null)
			section = instance.getConfiguration("messages").orElse(instance.getConfig());
		if (prefix)
			complete = Formatting.messagesPrefixed(section, nodes);
		else
			complete = Formatting.messages(section, nodes);
		complete = applyPlaceholders(complete);
		return complete;
	}

	private String applyPlaceholders(String input) {
		// Registered Placeholders
		for (Entry<Placeholder<?>, Object> entry : placeholders.entrySet()) {
			Placeholder<?> placeholder = entry.getKey();
			for (String syntax : placeholder.getSyntaxes()) {
				if (!input.toLowerCase().contains(syntax.toLowerCase()))
					continue;
				syntax = syntax.replace(Pattern.quote("\\"), "");
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else {
					String replacement = placeholder.replace_i(entry.getValue());
					if (replacement != null)
						input = input.replaceAll(Pattern.quote(syntax), replacement);
				}
			}
		}
		// Default Placeholders
		for (Placeholder<?> placeholder : Placeholders.getPlaceholders()) {
			for (String syntax : placeholder.getSyntaxes()) {
				if (!input.toLowerCase().contains(syntax.toLowerCase()))
					continue;
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else if (defaultPlaceholderObject != null && placeholder.getType().isAssignableFrom(defaultPlaceholderObject.getClass())) {
					String replacement = placeholder.replace_i(defaultPlaceholderObject);
					if (replacement != null)
						input = input.replaceAll(Pattern.quote(syntax), replacement);
				}
				if (kingdom != null && placeholder.getType().isAssignableFrom(OfflineKingdom.class)) {
					String replacement = placeholder.replace_i(kingdom);
					if (replacement != null)
						input = input.replaceAll(Pattern.quote(syntax), replacement);
				}
			}
		}
		// This allows users to insert new lines into their lores.
		int i = section.getInt("kingdoms.new-lines", 4); //The max about of new lines users are allowed.
		while (input.contains("%newline%") || input.contains("%nl%")) {
			input = input.replaceAll(Pattern.quote("%newline%"), "\n");
			input = input.replaceAll(Pattern.quote("%nl%"), "\n");
			i--;
			if (i <= 0)
				break;
		}
		return input;
	}

	/**
	 * Sends the final product of the builder as a title if the players using toPlayers are set.
	 * 
	 * WARNING: The title method needs to have the following as a configuration, this is special.
	 * title:
	 * 	  enabled: false
	 * 	  title: "&2Example"
	 * 	  subtitle: "&5&lColors work too."
	 * 	  fadeOut: 20
	 * 	  fadeIn: 20
	 * 	  stay: 200
	 */
	public void sendTitle() {
		Kingdoms instance = Kingdoms.getInstance();
		if (section == null)
			section = instance.getConfiguration("messages").orElse(instance.getConfig());
		if (nodes.length != 1)
			return;
		if (!section.getBoolean(nodes[0] + ".enabled", false))
			return;
		String subtitle = section.getString(nodes[0] + ".subtitle", "");
		String title = section.getString(nodes[0] + ".title", "");
		int fadeOut = section.getInt(nodes[0] + ".fadeOut", 10);
		int fadeIn = section.getInt(nodes[0] + ".fadeIn", 10);
		int stay = section.getInt(nodes[0] + ".stay", 20);
		title = applyPlaceholders(title).replaceAll("\n", "");
		subtitle = applyPlaceholders(subtitle).replaceAll("\n", "");
		Player[] players = senders.parallelStream()
				.filter(sender -> sender instanceof Player)
				.toArray(Player[]::new);
		if (senders != null && senders.size() > 0)
			new Title.Builder()
					.subtitle(subtitle)
					.fadeOut(fadeOut)
					.fadeIn(fadeIn)
					.title(title)
					.stay(stay)
					.send(players);
	}

	/**
	 * Sends the final product of the builder as an actionbar if the players using toPlayers are set.
	 */
	public void sendActionbar() {
		get();
		complete = complete.replaceAll("\n", "");
		ActionbarManager actionbar = Kingdoms.getInstance().getManager(ActionbarManager.class);
		if (senders != null && senders.size() > 0) {
			for (CommandSender sender : senders) {
				if (sender instanceof Player)
					actionbar.sendActionBar((Player)sender, complete);
			}
		}
	}

	/**
	 * Sends the final product of the builder if the senders are set.
	 */
	public void send() {
		get();
		if (!kingdomPlayers.isEmpty()) {
			senders.addAll(kingdomPlayers.parallelStream()
					.map(player -> player.getPlayer())
					.collect(Collectors.toSet()));
		}
		if (!senders.isEmpty()) {
			for (CommandSender sender : senders) {
				if (self != null && sender instanceof Player)
					if (self.getUniqueId().equals(((Player)sender).getUniqueId()))
						continue;
				sender.sendMessage(complete);
			}
		}
	}

	@Override
	public String toString() {
		return get();
	}

}
