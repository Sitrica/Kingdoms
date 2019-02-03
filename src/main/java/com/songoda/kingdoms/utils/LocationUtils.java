package com.songoda.kingdoms.utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils {

	public static String locationToString(Location location) {
		if (location == null)
			return "";
		String world = location.getWorld().getName();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float pitch = location.getPitch();
		float yaw = location.getYaw();
		return world + " , " + x + " , " + y + " , " + z + " , " + pitch + " , " + yaw;
	}

	//world , 839.0 , 66.0 , -728.0 , 0.0 , 0.0
	public static Location stringToLocation(String string) {
		if (string == null)
			return null;
		String[] split = string.replaceAll(" ", "").split(",");
		if (split.length != 4 && split.length != 6)
			return null;
		World world = Bukkit.getWorld(split[0]);
		if (world == null)
			return null;
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		Location location = new Location(world, x, y, z);
		if (split.length == 4) {
			float pitch = Float.parseFloat(split[4]);
			location.setPitch(pitch);
			float yaw = Float.parseFloat(split[5]);
			location.setYaw(yaw);
		}
		return location;
	}
	
	//world , x, z
	public static Chunk stringToChunk(String string) {
		if (string == null)
			return null;
		String[] split = string.replaceAll(" ", "").split(",");
		if (split.length != 3)
			return null;
		World world = Bukkit.getWorld(split[0]);
		if (world == null)
			return null;
		int x = Integer.parseInt(split[1]);
		int z = Integer.parseInt(split[2]);
		return world.getChunkAt(x, z);
	}
	
	public static String chunkToString(Chunk chunk) {
		if (chunk == null)
			return null;
		String world = chunk.getWorld().getName();
		return world + " , " + chunk.getX() + " , " + chunk.getZ();
	}

}
