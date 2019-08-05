package com.songoda.kingdoms.objects.structures;

import org.bukkit.Location;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.objects.land.Land;

public class WarpPad extends Structure {

	private String name;

	public WarpPad(String kingdom, Location location, String name) {
		super(kingdom, location, StructureType.WARPPAD);
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static class Warp { 

		private final Location location;
		private String name;

		public Warp(String name, Location location) {
			this.location = location;
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Location getLocation() {
			return location;
		}

		public Land getLand() {
			return Kingdoms.getInstance().getManager(LandManager.class).getLandAt(location);
		}

	}

}
