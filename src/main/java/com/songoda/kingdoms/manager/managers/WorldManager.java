package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

import com.songoda.kingdoms.manager.Manager;

public class WorldManager extends Manager {

	static {
		registerManager("world", new WorldManager());
	}
	
	private final Set<World> worlds = new HashSet<>();
	private final Set<String> names = new HashSet<>();
	private final boolean whitelist;
	
	protected WorldManager() {
		super(true);
		this.whitelist = configuration.getBoolean("worlds.list-is-whitelist", true);
		this.names.addAll(configuration.getStringList("worlds.list"));
	}
	
	public boolean acceptsWorld(World world) {
		if (whitelist) {
			if (!names.contains(world.getName()))
				return false;
		} else {
			if (names.contains(world.getName()))
				return false;
		}
		return true;
	}
	
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
	
	@Override
	public void onDisable() {
		worlds.clear();
		names.clear();
	}
	
}
