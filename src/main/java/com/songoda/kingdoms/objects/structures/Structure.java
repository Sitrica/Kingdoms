package com.songoda.kingdoms.objects.structures;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;

public class Structure {

	protected final FileConfiguration configuration;
	protected final StructureType type;
	protected final Location location;
	protected final Kingdoms instance;

	public Structure(Location location, StructureType type) {
		Validate.notNull(location);
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.location = location;
		this.type = type;
	}
	
	public StructureType getType() {
		return type;
	}
	
	public Location getLocation() {
		return location;
	}

}
