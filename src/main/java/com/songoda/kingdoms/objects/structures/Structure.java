package com.songoda.kingdoms.objects.structures;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class Structure {

	protected final FileConfiguration configuration;
	private final OfflineKingdom kingdom;
	protected final StructureType type;
	protected final Location location;
	protected final Kingdoms instance;

	public Structure(OfflineKingdom kingdom, Location location, StructureType type) {
		Validate.notNull(location);
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.location = location;
		this.kingdom = kingdom;
		this.type = type;
	}

	public OfflineKingdom getKingdom() {
		return kingdom;
	}

	public StructureType getType() {
		return type;
	}

	public Location getLocation() {
		return location;
	}

}
