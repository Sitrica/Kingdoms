package com.songoda.kingdoms;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldManager implements Listener {

	private Set<World> worlds = new HashSet<>();
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		worlds.add(world);
	}
	@EventHandler
	public void onWorldUnload(WorldLoadEvent event) {
		World world = event.getWorld();
		worlds.remove(world);
	}
	
}
