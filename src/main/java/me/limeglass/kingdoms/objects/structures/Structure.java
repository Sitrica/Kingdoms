package me.limeglass.kingdoms.objects.structures;

import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.manager.managers.KingdomManager;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;

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

}
