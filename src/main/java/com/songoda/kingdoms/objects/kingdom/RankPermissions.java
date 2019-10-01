package com.songoda.kingdoms.objects.kingdom;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;

/**
 * When adding new Permissions, you need to update the following to:
 * 		- ranks.yml
 * 		- inventories.yml (node inventories.permissions.X)
 * 		- RankPermissionsSerializer.class
 */
public class RankPermissions {

	private boolean claiming, unclaiming, invade, nexus, alliance, turrets, spawn, chest, invite, broadcast, useSpawn, build, nexusBuild, structures, protectedChests, xp, permissions, kick, enemy, lore;
	private final FileConfiguration configuration;
	private final String node;
	private final Rank rank;
	private int max;

	public RankPermissions(@NotNull Rank rank) {
		assert rank != null;
		this.rank = rank;
		this.configuration = Kingdoms.getInstance().getConfiguration("ranks").get();
		this.node = rank.getConfigurationNode() + ".default-permissions";
		this.permissions = getDefaultValue(node + ".edit-permissions");
		this.max = configuration.getInt(node + ".max-claims", -1);
		this.nexusBuild = getDefaultValue(node + ".nexus-build");
		this.structures = getDefaultValue(node + ".structures");
		this.unclaiming = getDefaultValue(node + ".unclaiming");
		this.broadcast = getDefaultValue(node + ".broadcast");
		this.nexus = getDefaultValue(node + ".nexus-access");
		this.useSpawn = getDefaultValue(node + ".use-spawn");
		this.chest = getDefaultValue(node + ".chest-access");
		this.claiming = getDefaultValue(node + ".claiming");
		this.alliance = getDefaultValue(node + ".alliance");
		this.spawn = getDefaultValue(node + ".set-spawn");
		this.turrets = getDefaultValue(node + ".turrets");
		this.invade = getDefaultValue(node + ".invade");
		this.invite = getDefaultValue(node + ".invite");
		this.build = getDefaultValue(node + ".build");
		this.kick = getDefaultValue(node + ".enemy");
		this.kick = getDefaultValue(node + ".kick");
		this.lore = getDefaultValue(node + ".lore");
	}

	public boolean getDefaultValue(String node) {
		return configuration.getBoolean(node, true);
	}

	public boolean canAccessProtected() {
		return protectedChests;
	}

	public void setProtectedAccess(boolean protectedChests) {
		this.protectedChests = protectedChests;
	}

	public boolean canEditPermissions() {
		return permissions;
	}

	public void setEditPermissions(boolean permissions) {
		this.permissions = permissions;
	}

	public boolean canBuildStructures() {
		return structures;
	}

	public void setBuildStructures(boolean structures) {
		this.structures = structures;
	}

	public boolean canBuildInNexus() {
		return nexusBuild;
	}

	public void setNexusBuild(boolean nexusBuild) {
		this.nexusBuild = nexusBuild;
	}

	public boolean canUnclaim() {
		return unclaiming;
	}

	public void setUnclaiming(boolean unclaiming) {
		this.unclaiming = unclaiming;
	}

	public boolean canBroadcast() {
		return broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

	public boolean hasNexusAccess() {
		return nexus;
	}

	public void setNexusAccess(boolean nexus) {
		this.nexus = nexus;
	}

	public boolean canClaim() {
		return claiming;
	}

	public void setClaiming(boolean claiming) {
		this.claiming = claiming;
	}

	public boolean hasChestAccess() {
		return chest;
	}

	public void setChestAccess(boolean chest) {
		this.chest = chest;
	}

	public boolean canGrabExperience() {
		return xp;
	}

	public void setGrabExperience(boolean xp) {
		this.xp = xp;
	}

	public boolean canUseSpawn() {
		return useSpawn;
	}

	public void setUseSpawn(boolean useSpawn) {
		this.useSpawn = useSpawn;
	}

	public boolean canAlliance() {
		return alliance;
	}

	public void setAlliance(boolean alliance) {
		this.alliance = alliance;
	}

	public boolean canEnemy() {
		return enemy;
	}

	public void setEnemy(boolean enemy) {
		this.enemy = enemy;
	}

	public boolean canUseTurrets() {
		return turrets;
	}

	public void setTurrets(boolean turrets) {
		this.turrets = turrets;
	}

	public boolean canInvite() {
		return invite;
	}

	public void setInvite(boolean invite) {
		this.invite = invite;
	}

	public int getMaximumClaims() {
		return max;
	}

	public void setMaximumClaims(int max) {
		this.max = max;
	}

	public boolean canInvade() {
		return invade;
	}

	public void setInvade(boolean invade) {
		this.invade = invade;
	}

	public boolean canSetSpawn() {
		return spawn;
	}

	public void setSpawn(boolean spawn) {
		this.spawn = spawn;
	}

	public boolean canBuild() {
		return build;
	}

	public void setBuild(boolean build) {
		this.build = build;
	}

	public boolean canSetLore() {
		return lore;
	}

	public void setLore(boolean lore) {
		this.lore = lore;
	}

	public boolean canKick() {
		return kick;
	}

	public void setKick(boolean kick) {
		this.kick = kick;
	}

	public Rank getRank() {
		return rank;
	}

}
