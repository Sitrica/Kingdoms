package com.songoda.kingdoms.objects.structures;

import org.bukkit.Location;

import com.songoda.kingdoms.objects.land.Land;

public class WarpPad extends Structure {

	private final String name;
	private final Land land;

	public WarpPad(Location location, String name, Land land) {
		super(location, StructureType.WARPPAD);
		this.land = land;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Land getLand() {
		return land;
	}

}
