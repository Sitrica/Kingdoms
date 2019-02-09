package com.songoda.kingdoms.objects.land;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class Land {

	private final Set<ChestSign> signs = new HashSet<>();
	private final Set<Turret> turrets = new HashSet<>();
	private final LandManager landManager;
	private final Kingdoms instance;
	private OfflineKingdom kingdom;
	private Structure structure;
	private final Chunk chunk;
	private long claimTime;
	
	public Land(Chunk chunk) {
		this.chunk = chunk;
		this.instance = Kingdoms.getInstance();
		this.landManager = instance.getManager("land", LandManager.class);
	}
	
	public Chunk getChunk() {
		return chunk;
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
	
	public OfflineKingdom getKingdomOwner() {
		return kingdom;
	}
	
	public void setKingdomOwner(OfflineKingdom kingdom) {
		this.kingdom = kingdom;
	}
	
	public boolean hasTurret(Turret turret) {
		return turrets.contains(turret);
	}

	public void addTurret(Turret turret) {
		turrets.add(turret);
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
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0)
					continue;
				Chunk location = chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z);
				lands.add(landManager.getLand(location));
			}
		}
		return lands;
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
