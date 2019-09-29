package com.songoda.kingdoms.objects.maps;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.Relation;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.ListMessageBuilder;

public class RelationOptions {

	private final String color, name;
	private ListMessageBuilder hover;
	private RelationAction action;

	public RelationOptions(String name, Relation relation) {
		this.name = name;
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("map").get();
		ConfigurationSection section = null;
		String node = "elements." + name;
		if (relation != null) {
			section = configuration.getConfigurationSection(node + ".relations." + relation.name().toLowerCase(Locale.US));
		} else {
			section = configuration.getConfigurationSection(node);
		}
		color = section.getString("color", "&f");
		if (section.isSet("action")) {
			String[] split = section.getString("action", "").split(":");
			RelationAction action;
			if (split.length > 1) {
				ActionConsumer consumer = ActionConsumer.valueOf(split[0].toUpperCase(Locale.US));
				if (consumer != ActionConsumer.COMMAND) // Formatted incorrectly
					return;
				String command = split[1];
				if (split.length > 2)
					command = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
				action = new RelationAction(ActionConsumer.COMMAND, command);
			} else {
				ActionConsumer consumer = ActionConsumer.valueOf(split[0].toUpperCase(Locale.US));
				action = new RelationAction(consumer);
			}
			this.action = action;
		}
		if (section.isList("hover-message")) {
			hover = new ListMessageBuilder(false, "hover-message", section);
		} else {
			if (configuration.isList(node + ".hover-message")) {
				hover = new ListMessageBuilder(false, node + ".hover-message").fromConfiguration(configuration);
			} else if (configuration.isList(node + ".default-hover-message")) {
				hover = new ListMessageBuilder(false, node + ".default-hover-message").fromConfiguration(configuration);
			}
		}
	}

	public RelationOptions(String name) {
		this(name, null);
	}

	public String getElementName() {
		return name;
	}

	public Optional<ListMessageBuilder> getHover() {
		if (hover == null)
			return Optional.empty();
		return Optional.of(hover.replace("%relation%", getColor()));
	}

	public RelationAction getAction() {
		return action;
	}

	public String getColor() {
		return Formatting.color(color);
	}

	public class RelationAction {

		private final ActionConsumer consumer;
		private final String command;

		RelationAction(ActionConsumer consumer) {
			this(consumer, null);
		}

		RelationAction(ActionConsumer consumer, String command) {
			this.consumer = consumer;
			this.command = command;
		}

		public void execute(Land land, KingdomPlayer kingdomPlayer) {
			if (consumer == ActionConsumer.COMMAND) {
				Optional<OfflineKingdom> landKingdom = land.getKingdomOwner();
				String kingdom = Kingdoms.getInstance().getConfiguration("map").get().getString("messages.no-kingdom", "(No Kingdom)");
				if (landKingdom.isPresent())
					kingdom = landKingdom.get().getName();
				consumer.executeCommand(kingdomPlayer.getPlayer(), command
						.replace("%player%", kingdomPlayer.getName())
						.replace("%x%", land.getX() + "")
						.replace("%z%", land.getZ() + "")
						.replace("%kingdom%", kingdom));
				return;
			}
			consumer.accept(land, kingdomPlayer);
		}

		public String getCommand() {
			return command;
		}

	}

}
