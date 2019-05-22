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

	SIEGE_ENGINE("siege-engine"),
	POWERCELL("powercell"),
	EXTRACTOR("extractor"),
	REGULATOR("regulator"),
	WARPPAD("warp-pad"),
	OUTPOST("outpost"),
	ARSENAL("arsenal"),
	RADAR("radar"),
	NEXUS("nexus"),
	LAND("land"),
	YOU("you");

	private final RelationOptions alliance, enemy, own, none;
	private final MessageBuilder legend, icon;

	MapElement(String node) {
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("map").get();
		ConfigurationSection section = configuration.getConfigurationSection("elements." + node);
		if (section.isConfigurationSection("relations")) {
			alliance = new RelationOptions(section.getConfigurationSection("relations.alliance"));
			enemy = new RelationOptions(section.getConfigurationSection("relations.enemy"));
			none = new RelationOptions(section.getConfigurationSection("relations.other"));
			own = new RelationOptions(section.getConfigurationSection("relations.own"));
		} else {
			alliance = new RelationOptions(section);
			enemy = new RelationOptions(section);
			none = new RelationOptions(section);
			own = new RelationOptions(section);
		}
		legend = new MessageBuilder(false, "legend-message", section);
		icon = new MessageBuilder(false, "icon", section);
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
			case REGULATOR:
				return REGULATOR;
			case SIEGE_ENGINE:
				return SIEGE_ENGINE;
			case WARPPAD:
				return WARPPAD;
		}
		return YOU;
	}

	public MessageBuilder getIcon(Relation relation) {
		if (relation == null)
			return icon;
		return icon.replace("%relation%", relation.getColorFor(this));
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
				return none.getHover();
		}
	}

	public MessageBuilder getLegend() {
		return legend;
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
				return none.getColor();
		}
	}

	public Optional<RelationAction> getRelationAction(Relation relation) {
		switch (relation) {
			case ALLIANCE:
				if (alliance.getAction() == null)
					return Optional.empty();
				return Optional.of(alliance.getAction());
			case ENEMY:
				if (enemy.getAction() == null)
					return Optional.empty();
				return Optional.of(enemy.getAction());
			case OWN:
				if (own.getAction() == null)
					return Optional.empty();
				return Optional.of(own.getAction());
			case NEUTRAL:
			default:
				if (none.getAction() == null)
					return Optional.empty();
				return Optional.of(none.getAction());
		}
	}

}
