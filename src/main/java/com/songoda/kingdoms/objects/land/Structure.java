package com.songoda.kingdoms.objects.land;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;

import com.songoda.kingdoms.objects.StructureType;

public class Structure {

	private final StructureType type;
	private final Location location;

	public Structure(Location location, StructureType type) {
		Validate.notNull(location);
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
