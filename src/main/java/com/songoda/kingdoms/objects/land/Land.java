package com.songoda.kingdoms.objects.land;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.objects.turrets.Turret;

public class Land {

	private final Set<ChestSign> signs = new HashSet<>();
	private final Set<Turret> turrets = new HashSet<>();
	private Structure structure;
	private final String world;
	private String kingdom;
	private final int x, z;
	private long claimTime;

	public Land(Chunk chunk) {
		this.x = chunk.getX();
		this.z = chunk.getZ();
		this.world = chunk.getWorld().getName();
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public String getKingdomName() {
		return kingdom;
	}

	public LandInfo toInfo() {
		return Kingdoms.getInstance().getManager(LandManager.class).getInfo(this);
	}

	public Chunk getChunk() {
		World world = getWorld();
		if (world == null)
			return null;
		return world.getChunkAt(x, z);
	}

	public World getWorld() {
		return Bukkit.getWorld(world);
	}

	public Long getClaimTime() {
		return claimTime;
	}

	public void setClaimTime(long claimTime) {
		this.claimTime = claimTime;
	}

	public Structure getStructure() {
		return structure;
	}

	public void setStructure(Structure structure) {
		this.structure = structure;
	}

	public boolean isNexus() {
		return structure != null ? structure.getType() == StructureType.NEXUS : false;
	}

	public boolean hasOwner() {
		return getKingdomOwner().isPresent();
	}

	public Optional<OfflineKingdom> getKingdomOwner() {
		if (kingdom == null)
			return Optional.empty();
		return Kingdoms.getInstance().getManager(KingdomManager.class).getOfflineKingdom(kingdom);
	}

	public void setKingdomOwner(String kingdom) {
		this.kingdom = kingdom;
	}

	public boolean hasTurret(Turret turret) {
		return turrets.contains(turret);
	}

	public void addTurret(Turret turret) {
		turrets.add(turret);
	}

	public void removeTurret(Turret turret) {
		turrets.remove(turret);
	}

	public Set<Turret> getTurrets() {
		return turrets;
	}

	public void addChestSign(ChestSign sign) {
		signs.add(sign);
	}

	public ChestSign getChestSign(Location location) {
		if (location == null)
			return null;
		for (ChestSign sign : signs) {
			Location signLocation = sign.getLocation();
			if (signLocation.equals(location))
				return sign;
			if (signLocation.distance(location) <= 0.9)
				return sign;
		}
		return null;
	}

	public void removeChestSign(Location location) {
		if (location == null)
			return;
		for (Iterator<ChestSign> iterator = signs.iterator(); iterator.hasNext();){
			ChestSign sign = iterator.next();
			Location signLocation = sign.getLocation();
			if (signLocation.equals(location))
				iterator.remove();
			if (signLocation.distance(location) <= 0.9)
				iterator.remove();
		}
	}

	public Set<Land> getSurrounding() {
		Set<Land> lands = new HashSet<>();
		World world = getWorld();
		if (world == null)
			return lands;
		LandManager landManager = Kingdoms.getInstance().getManager(LandManager.class);
		// North
		Chunk north = world.getChunkAt(x, z - 9);
		lands.add(landManager.getLand(north));
		// North-East
		Chunk northEast = world.getChunkAt(x + 9, z - 9);
		lands.add(landManager.getLand(northEast));
		// East
		Chunk east = world.getChunkAt(x + 9, z);
		lands.add(landManager.getLand(east));
		// South-East
		Chunk southEast = world.getChunkAt(x + 9, z + 9);
		lands.add(landManager.getLand(southEast));
		// South
		Chunk south = world.getChunkAt(x, z + 9);
		lands.add(landManager.getLand(south));
		// South-West
		Chunk southWest = world.getChunkAt(x - 9, z + 9);
		lands.add(landManager.getLand(southWest));
		// West
		Chunk west = world.getChunkAt(x - 9, z);
		lands.add(landManager.getLand(west));
		// North-West
		Chunk northWest = world.getChunkAt(x - 9, z - 9);
		lands.add(landManager.getLand(northWest));
		return lands;
	}

	/**
	 * Checks if this Land is worthy of being saved to a database.
	 * @return boolean If this chunk serves no purpose.
	 */
	public boolean isSignificant() {
		if (!turrets.isEmpty())
			return true;
		if (structure != null)
			return true;
		if (!signs.isEmpty())
			return true;
		if (kingdom != null)
			return true;
		if (claimTime > 0)
			return true;
		return false;
	}

	public Turret getTurret(Location location) {
		if (turrets.isEmpty())
			return null;
		for (Turret turret : turrets) {
			Location turretLocation = turret.getLocation();
			if (turretLocation.equals(location))
				return turret;
			if (turretLocation.distance(location) <= 0.9)
				return turret;
		}
		return null;
	}

}
