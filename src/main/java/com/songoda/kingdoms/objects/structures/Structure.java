package com.songoda.kingdoms.objects.structures;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;

public class Structure {

	protected final StructureType type;
	protected final Location location;

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
