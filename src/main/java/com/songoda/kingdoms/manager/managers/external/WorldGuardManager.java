package com.songoda.kingdoms.manager.managers.external;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.utils.Utils;

public class WorldGuardManager extends Manager {
	
	private final List<String> list = new ArrayList<>();
	private Method canBuild, getRegionManager;
	private boolean blacklist, enabled = true;
	private WorldGuardPlugin plugin;
	private WorldGuard worldGuard;
	private boolean version6;
	
	protected WorldGuardManager() {
		super("worldguard", false);
		if (!instance.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
			enabled = false;
			return;
		}
		blacklist = configuration.getBoolean("worldguard.list-is-blacklist", false);
		list.addAll(configuration.getStringList("worldguard.list"));
		if (!Utils.classExists("com.sk89q.worldguard.WorldGuard")) { // Assume WorldGuard 6
			try {
				plugin = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
				canBuild = plugin.getClass().getMethod("canBuild", Player.class, Location.class);
				canBuild.setAccessible(true);
				getRegionManager = plugin.getClass().getMethod("getRegionManager", World.class);
				getRegionManager.setAccessible(true);
				version6 = true;
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return;
		}
		worldGuard = WorldGuard.getInstance();
	}
	
	private boolean acceptsRegion(ProtectedRegion region) {
		if (blacklist)
			return !list.contains(region.getId());
		return list.contains(region.getId());
	}
	
	public boolean canBuild(Player player, Location location) {
		if (!enabled)
			return true;
		if (version6) {
			try {
				return (boolean) canBuild.invoke(plugin, player, location);
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				return false;
			}
		}
		RegionQuery query = worldGuard.getPlatform().getRegionContainer().createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
		return set.testState(plugin.wrapPlayer(player), Flags.BUILD);
	}
	
	public boolean isInRegion(Location location) {
		if (!enabled)
			return true;
		/*
		if (version6) {
			RegionManager manager;
			try {
				manager = (RegionManager) getRegionManager.invoke(plugin, location.getWorld());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				return false;
			}
			try {
				ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asVector(location));
				return set.size() > 0;
			} catch (IncompatibleClassChangeError | NullPointerException e) {
				for (ProtectedRegion region : manager.getRegions().values()) {
					boolean inside = region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
					if (inside)
						return true;
				}
			}
			return false;
		}
		*/
		RegionQuery query = worldGuard.getPlatform().getRegionContainer().createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
		Set<ProtectedRegion> regions = set.getRegions();
		return regions != null && regions.size() > 0;
	}
	
	public boolean canClaim(Location location) {
		if (!enabled)
			return true;
		/*
		if (version6) {
			RegionManager manager;
			try {
				manager = (RegionManager) getRegionManager.invoke(plugin, location.getWorld());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				return false;
			}
			try {
				ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asVector(location));
				for (ProtectedRegion region : set) {
					if (!acceptsRegion(region))
						return false;
				}
				return set.size() > 0;
			} catch (IncompatibleClassChangeError | NullPointerException e) {
				for (ProtectedRegion region : manager.getRegions().values()) {
					if (!acceptsRegion(region))
						return false;
					if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
						return true;
				}
			}
			return false;
		}
		*/
		RegionQuery query = worldGuard.getPlatform().getRegionContainer().createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
		return set.getRegions().parallelStream()
				.filter(region -> acceptsRegion(region))
				.findAny().isPresent();
	}

	@Override
	public void onDisable() {}

}
