package com.songoda.kingdoms.objects.land;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

public class ChestSign {
	
	private final Set<UUID> owners = new HashSet<>();
	private Location location;
	private UUID owner;
	
	public ChestSign(Location location, UUID owner) {
		this.location = location;
		this.owner = owner;
	}
	
	public ChestSign(Location location, UUID owner, Collection<UUID> owners) {
		this.owners.addAll(owners);
		this.location = location;
		this.owner = owner;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Set<UUID> getOwners() {
		return owners;
	}
	
	public UUID getOwner() {
		return owner;
	}

}
