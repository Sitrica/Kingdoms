package com.songoda.kingdoms.worldguard;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.songoda.kingdoms.Kingdoms;

public class WorldGuard7 implements WorldGuardKingdoms {

	private final Set<String> set = new HashSet<>();
	private final WorldGuardPlugin plugin;
	private final WorldGuard worldGuard;
	private final boolean blacklist;

	public WorldGuard7(Kingdoms instance, Collection<String> worlds, boolean blacklist) {
		this.blacklist = blacklist;
		this.set.addAll(worlds);
		plugin = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
		worldGuard = WorldGuard.getInstance();
	}

	private boolean acceptsRegion(ProtectedRegion region) {
		if (blacklist)
			return !set.contains(region.getId());
		return set.contains(region.getId());
	}

	@Override
	public boolean canBuild(Player player, Location location) {
		RegionQuery query = worldGuard.getPlatform().getRegionContainer().createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
		return set.testState(plugin.wrapPlayer(player), Flags.BUILD);
	}

	@Override
	public boolean isInRegion(Location location) {
		RegionQuery query = worldGuard.getPlatform().getRegionContainer().createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
		Set<ProtectedRegion> regions = set.getRegions();
		return regions != null && regions.size() > 0;
	}

	@Override
	public boolean canClaim(Location location) {
		RegionQuery query = worldGuard.getPlatform().getRegionContainer().createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
		return set.getRegions().parallelStream()
				.filter(region -> acceptsRegion(region))
				.findAny().isPresent();
	}

}
