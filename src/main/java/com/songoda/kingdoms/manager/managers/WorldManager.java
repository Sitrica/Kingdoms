package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

import com.songoda.kingdoms.manager.Manager;

public class WorldManager extends Manager {
	
	private final Set<String> unoccupied = new HashSet<>();
	private final boolean whitelist, whitelistUnoccupied;
	private final Set<World> worlds = new HashSet<>();
	private final Set<String> names = new HashSet<>();
	
	protected WorldManager() {
		super("world", true);
		this.unoccupied.addAll(configuration.getStringList("worlds.worlds-with-no-building-in-unoccupied"));
		this.whitelistUnoccupied = configuration.getBoolean("worlds.unoccupied-list-is-whitelist", true);
		this.whitelist = configuration.getBoolean("worlds.list-is-whitelist", true);
		this.names.addAll(configuration.getStringList("worlds.list"));
		this.names.add("KingdomsConquest");
	}
	
	public boolean acceptsWorld(World world) {
		String name = world.getName();
		if (whitelist) {
			if (!names.contains(name))
				return false;
		} else {
			if (names.contains(name))
				return false;
		}
		return true;
	}
	
	public boolean canBuildInUnoccupied(World world) {
		String name = world.getName();
		if (whitelistUnoccupied) {
			if (!unoccupied.contains(name))
				return false;
		} else {
			if (unoccupied.contains(name))
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
