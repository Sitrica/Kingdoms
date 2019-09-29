package com.songoda.kingdoms.objects.maps;

import java.util.Optional;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.Relation;
import com.songoda.kingdoms.objects.maps.RelationOptions;
import com.songoda.kingdoms.objects.maps.RelationOptions.RelationAction;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public enum MapElement {

	POWERCELL("powercell"),
	EXTRACTOR("extractor"),
	WARPPAD("warp-pad"),
	OUTPOST("outpost"),
	ARSENAL("arsenal"),
	RADAR("radar"),
	NEXUS("nexus"),
	LAND("land"),
	YOU("you");

	private final RelationOptions alliance, enemy, own, neutral;
	private final MessageBuilder legend, icon;
	private final String node;

	MapElement(String node) {
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("map").get();
		ConfigurationSection section = configuration.getConfigurationSection("elements." + node);
		if (section.isConfigurationSection("relations")) {
			alliance = new RelationOptions(node, Relation.ALLIANCE);
			neutral = new RelationOptions(node, Relation.NEUTRAL);
			enemy = new RelationOptions(node, Relation.ENEMY);
			own = new RelationOptions(node, Relation.OWN);
		} else {
			alliance = new RelationOptions(node);
			neutral = new RelationOptions(node);
			enemy = new RelationOptions(node);
			own = new RelationOptions(node);
		}
		legend = new MessageBuilder(false, "legend-message", section);
		icon = new MessageBuilder(false, "icon", section);
		this.node = node;
	}

	public String getNode() {
		return node;
	}

	public static MapElement fromStructure(StructureType structure) {
		switch (structure) {
			case ARSENAL:
				return ARSENAL;
			case EXTRACTOR:
				return EXTRACTOR;
			case NEXUS:
				return NEXUS;
			case OUTPOST:
				return OUTPOST;
			case POWERCELL:
				return POWERCELL;
			case RADAR:
				return RADAR;
			case WARPPAD:
				return WARPPAD;
		}
		return YOU;
	}

	public MessageBuilder getIcon() {
		return icon;
	}

	public MessageBuilder getIcon(Relation relation) {
		return icon.replace("%relation%", relation == null ? getRelationColor(relation) : neutral.getColor());
	}

	public String getNoRelationColor() {
		return neutral.getColor();
	}

	public String getRelationColor(Relation relation) {
		switch (relation) {
			case ALLIANCE:
				return alliance.getColor();
			case ENEMY:
				return enemy.getColor();
			case OWN:
				return own.getColor();
			case NEUTRAL:
			default:
				return neutral.getColor();
		}
	}

	public Optional<ListMessageBuilder> getHover(Relation relation) {
		switch (relation) {
			case ALLIANCE:
				return alliance.getHover();
			case ENEMY:
				return enemy.getHover();
			case OWN:
				return own.getHover();
			case NEUTRAL:
			default:
				return neutral.getHover();
		}
	}

	public MessageBuilder getLegend() {
		return legend;
	}

	public Optional<RelationAction> getRelationAction(Relation relation) {
		switch (relation) {
			case ALLIANCE:
				return Optional.ofNullable(alliance.getAction());
			case ENEMY:
				return Optional.ofNullable(enemy.getAction());
			case OWN:
				return Optional.ofNullable(own.getAction());
			case NEUTRAL:
			default:
				return Optional.ofNullable(neutral.getAction());
		}
	}

}
