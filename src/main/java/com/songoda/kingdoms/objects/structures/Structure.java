package com.songoda.kingdoms.objects.structures;

import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

public class Structure {

	protected final FileConfiguration configuration;
	protected final StructureType type;
	protected final Location location;
	protected final Kingdoms instance;
	private final String kingdom;

	public Structure(String kingdom, Location location, StructureType type) {
		Validate.notNull(location);
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.location = location;
		this.kingdom = kingdom;
		this.type = type;
	}

	public OfflineKingdom getKingdom() {
		Optional<OfflineKingdom> optional = instance.getManager(KingdomManager.class).getOfflineKingdom(kingdom);
		if (!optional.isPresent())
			return null;
		return optional.get();
	}

	public StructureType getType() {
		return type;
	}

	public Location getLocation() {
		return location;
	}

	public Land getLand() {
		return instance.getManager(LandManager.class).getLandAt(location);
	}

}
