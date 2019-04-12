package com.songoda.kingdoms.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.placeholders.Placeholders;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;

public class ListMessageBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private final Set<KingdomPlayer> kingdomPlayers = new HashSet<>();
	private final List<CommandSender> senders = new ArrayList<>();
	private FileConfiguration configuration;
	private Object defaultPlaceholderObject;
	private final Kingdoms instance;
	private OfflineKingdom kingdom;
	private final String node;
	private boolean prefix;
	
	/**
	 * Creates a ListMessageBuilder with the defined node.
	 * 
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public ListMessageBuilder(String node) {
		this(true, node);
	}
	
	/**
	 * Creates a ListMessageBuilder with the defined nodes, and if it should contain the prefix.
	 * 
	 * @param prefix The boolean to enable or disable prefixing this message.
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public ListMessageBuilder(boolean prefix, String node) {
		this.instance = Kingdoms.getInstance();
		this.prefix = prefix;
		this.node = node;
	}
	
	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Collection<KingdomPlayer> to send the message to.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder toKingdomPlayers(Collection<? extends KingdomPlayer> players) {
		this.kingdomPlayers.addAll(players);
		return this;
	}
	
	/**
	 * Set the senders to send this message to.
	 *
	 * @param senders The CommandSenders to send the message to.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder toSenders(CommandSender... senders) {
		this.senders.addAll(Sets.newHashSet(senders));
		return this;
	}
	
	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Players... to send the message to.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder toPlayers(Player... players) {
		this.senders.addAll(Sets.newHashSet(players));
		return this;
	}
	
	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Collection<Player> to send the message to.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder toPlayers(Collection<? extends Player> players) {
		this.senders.addAll(players);
		return this;
	}
	
	/**
	 * Add a placeholder to the MessageBuilder.
	 * 
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
		this.defaultPlaceholderObject = placeholderObject;
		placeholders.put(placeholder, placeholderObject);
		return this;
	}
	
	/**
	 * Set the configuration to read from, by default is the messages.yml
	 * 
	 * @param configuration The FileConfiguration to read from.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder fromConfiguration(FileConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}
	
	/**
	 * Created a single replacement and ignores the placeholder object.
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}
	
	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}
	
	/**
	 * Set the Kingdom option to be used for placeholders later.
	 * 
	 * @param kingdom The OfflineKingdom to set as.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder setKingdom(OfflineKingdom kingdom) {
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
	 * Sends the final product of the builder.
	 */
	public void send(CommandSender... senders) {
		toSenders(senders).send();
	}
	
	/**
	 * Completes and returns the final product of the builder.
	 */
	public List<String> get() {
		List<String> list = new ArrayList<>();
		if (configuration == null)
			configuration = instance.getConfiguration("messages").orElse(instance.getConfig());
		boolean usedPrefix = false;
		for (String string : configuration.getStringList(node)) {
			if (prefix && !usedPrefix) {
				string = applyPlaceholders(string);
				string = Formatting.getPrefix() + " " + Formatting.color(string);
				usedPrefix = true;
			} else {
				string = Formatting.color(string);
			}
			list.add(string);
		}
		return list;
	}
	
	private String applyPlaceholders(String input) {
		// Default Placeholders
		for (Placeholder<?> placeholder : Placeholders.getPlaceholders()) {
			for (String syntax : placeholder.getSyntaxes()) {
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else if (defaultPlaceholderObject != null) {
					if (placeholder.getType().isAssignableFrom(defaultPlaceholderObject.getClass()))
						input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(defaultPlaceholderObject));
				}
				if (kingdom != null) {
					if (placeholder.getType().isAssignableFrom(OfflineKingdom.class))
						input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(kingdom));
				}
			}
		}
		// Registered Placeholders
		for (Entry<Placeholder<?>, Object> entry : placeholders.entrySet()) {
			Placeholder<?> placeholder = entry.getKey();
			for (String syntax : placeholder.getSyntaxes()) {
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else {
					input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(entry.getValue()));
				}
			}
		}
		// This allows users to insert new lines into their lores.
		int i = configuration.getInt("kingdoms.new-lines", 4); //The max about of new lines users are allowed.
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
	 * Sends the final product of the builder if the senders are set.
	 */
	public void send() {
		List<String> list = get();
		if (!kingdomPlayers.isEmpty()) {
			senders.addAll(kingdomPlayers.parallelStream()
					.map(player -> player.getPlayer())
					.collect(Collectors.toSet()));
		}
		if (senders.isEmpty())
			return;
		for (CommandSender sender : senders)
			list.forEach(message -> sender.sendMessage(message));
	}
	
}
