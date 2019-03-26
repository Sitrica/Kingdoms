package com.songoda.kingdoms.manager.managers.external;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.utils.Utils;
import com.songoda.kingdoms.worldguard.WorldGuard6;
import com.songoda.kingdoms.worldguard.WorldGuard7;
import com.songoda.kingdoms.worldguard.WorldGuardKingdoms;

public class WorldGuardManager extends Manager implements WorldGuardKingdoms {

	private final List<String> list = new ArrayList<>();
	private WorldGuardKingdoms worldGuard;
	private boolean blacklist;

	protected WorldGuardManager() {
		super("worldguard", false);
		if (!instance.getServer().getPluginManager().isPluginEnabled("WorldGuard"))
			return;
		blacklist = configuration.getBoolean("worldguard.list-is-blacklist", false);
		list.addAll(configuration.getStringList("worldguard.list"));
		if (!Utils.classExists("com.sk89q.worldguard.WorldGuard")) { // Assume WorldGuard 6
			worldGuard = new WorldGuard6(instance, list, blacklist);
			return;
		}
		worldGuard = new WorldGuard7(instance, list, blacklist);
	}

	@Override
	public void onDisable() {}

	@Override
	public boolean canClaim(Location location) {
		if (worldGuard == null)
			return false;
		return worldGuard.canClaim(location);
	}

	@Override
	public boolean isInRegion(Location location) {
		if (worldGuard == null)
			return false;
		return worldGuard.isInRegion(location);
	}

	@Override
	public boolean canBuild(Player player, Location location) {
		if (worldGuard == null)
			return false;
		return worldGuard.canBuild(player, location);
	}

}
