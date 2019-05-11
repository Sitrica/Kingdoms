package com.songoda.kingdoms.objects.structures;

import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class Structure {

	protected final FileConfiguration configuration;
	private final KingdomManager kingdomManager;
	protected final StructureType type;
	protected final Location location;
	protected final Kingdoms instance;
	private final String kingdom;

	public Structure(OfflineKingdom kingdom, Location location, StructureType type) {
		this(kingdom.getName(), location, type);
	}

	public Structure(String kingdom, Location location, StructureType type) {
		Validate.notNull(location);
		this.instance = Kingdoms.getInstance();
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.configuration = instance.getConfig();
		this.location = location;
		this.kingdom = kingdom;
		this.type = type;
	}

	public OfflineKingdom getKingdom() {
		Optional<OfflineKingdom> optional = kingdomManager.getOfflineKingdom(kingdom);
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

}
