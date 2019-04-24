package com.songoda.kingdoms.objects.structures;

import org.bukkit.Location;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

public class WarpPad extends Structure {

	private final Land land;
	private String name;

	public WarpPad(OfflineKingdom kingdom, Location location, String name, Land land) {
		super(kingdom, location, StructureType.WARPPAD);
		this.land = land;
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Land getLand() {
		return land;
	}

}
