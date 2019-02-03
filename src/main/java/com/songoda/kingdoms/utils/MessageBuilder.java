package com.songoda.kingdoms.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.placeholders.Placeholders;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;

public class MessageBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private final Set<CommandSender> senders = new HashSet<>();
	private FileConfiguration configuration;
	private Object defaultPlaceholderObject;
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
	 * Creates a MessageBuilder with the defined nodes, and if it should contain the prefix.
	 * 
	 * @param prefix The boolean to enable or disable prefixing this message.
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public MessageBuilder(boolean prefix, String... nodes) {
		this.prefix = prefix;
		this.nodes = nodes;
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
	public MessageBuilder fromConfiguration(FileConfiguration configuration) {
		this.configuration = configuration;
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
		placeholders.put(new SimplePlaceholder(syntax), replacement.toString());
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
		if (configuration == null)
			configuration = instance.getConfiguration("messages").orElse(instance.getConfig());
		if (prefix)
			complete = Formatting.messagesPrefixed(configuration, nodes);
		else
			complete = Formatting.messages(configuration, nodes);
		// Default Placeholders
		if (defaultPlaceholderObject != null) {
			for (Placeholder<?> placeholder : Placeholders.getPlaceholders()) {
				for (String syntax : placeholder.getSyntaxes()) {
					if (placeholder.getType().isAssignableFrom(defaultPlaceholderObject.getClass()))
						complete = complete.replaceAll(Pattern.quote(syntax), placeholder.replace_i(defaultPlaceholderObject));
				}
			}
		}
		// Registered Placeholders
		for (Entry<Placeholder<?>, Object> entry : placeholders.entrySet()) {
			Placeholder<?> placeholder = entry.getKey();
			for (String syntax : placeholder.getSyntaxes()) {
				complete = complete.replaceAll(Pattern.quote(syntax), placeholder.replace_i(entry.getValue()));
			}
		}
		return complete;
	}
	
	/**
	 * Sends the final product of the builder.
	 */
	public void send() {
		get();
		if (senders != null && senders.size() > 0) {
			for (CommandSender sender : senders) {
				sender.sendMessage(complete);
			}
		}
	}
	
}
