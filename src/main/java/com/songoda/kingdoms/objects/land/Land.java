package com.songoda.kingdoms.objects.land;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;

import com.songoda.kingdoms.Kingdoms;

public class Land {

	private final Set<ChestSign> signs = new HashSet<>();
	private final Set<Turret> turrets = new HashSet<>();
	private Structure structure;
	private long claimTime;
	private Chunk chunk;
	private UUID owner;
	
	public Land(Chunk chunk) {
		this.chunk = chunk;
	}
	
	public void setStructure(Structure structure) {
		this.structure = structure;
	}
	
	public Structure getStructure() {
		return structure;
	}

	public Chunk getChunk() {
		return chunk;
	}
	
	public Set<Land> getSurrounding() {
		Kingdoms instance = Kingdoms.getInstance();
		Set<Land> lands = new HashSet<Land>();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0)
					continue;
				ChunkLocation location = new ChunkLocation(chunk.getWorld(), chunk.getX() + x, chunk.getZ() + z);
				//TODO
				lands.add(instance.getManagers().getLandManager().getOrLoadLand(location));
			}
		}
		return lands;
	}
	
	public Turret getTurret(Location location) {
		if (turrets.isEmpty())
			return null;
		for (Turret turret : turrets) {
			if (turret.getLocation().equals(location))
				return turret;
		}
		return null;
	}

	public void addTurret(Turret turret) {
		//2016-08-11
		if(turrets == null)
			turrets = new ArrayList<Turret>();
		
		if(turrets.contains(turret)) return;
		
		turrets.add(turret);
		//save();
	}
	
	public boolean hasTurret(Turret turret){
		//2016-08-11
		if(turrets == null)
			turrets = new ArrayList<Turret>();
		
		return turrets.contains(turret);
	}
	
	public List<Turret> getTurrets(){
		return turrets;
	}
	
	/**
	 * 
	 * @return Stringname of owning kingdom. Returns null if no owner
	 * @deprecated use getOwnerUUID() instead
	 */
	public String getOwner() {
		if (owner != null)
			return GameManagement.getKingdomManager().getOrLoadKingdom(owner).getKingdomName();
		return null;
	}
	
	public String getOwnerName() {
		return getOwner();
	}

	public void setOwner(String owner) {
		this.owner = GameManagement.getKingdomManager().getOrLoadKingdom(owner).getKingdomUuid();
		//save();
	}
	public UUID getOwnerUUID(){
		return owner;
	}
	public void setOwnerUUID(UUID uuid){
		this.owner = uuid;
	}

	public Long getClaimTime() {
		return claimTime;
	}

	public void setClaimTime(Long claimTime) {
		this.claimTime = claimTime;
		//if(owner != null || turrets.size() != 0) save();
	}
	
/*	public void autoSave(){
		if(owner == null && turrets.size() == 0) delete();
		else save();
	}*/

	public void addChestSign(KChestSign sign){
		//2016-08-11
		if(signs == null)
			signs = new ArrayList<KChestSign>();
		
		if(signs.contains(sign)) signs.remove(sign);
		
		signs.add(sign);
	}
	
	public KChestSign getChestSign(SimpleLocation loc) {
		if(loc == null) return null;
		
		//2016-08-11
		if(signs == null)
			return null;
		
		for(KChestSign sign : signs){
			if(sign.getLoc().equals(loc)) return sign;
		}
		return null;
	}
	
	public void removeChestSign(SimpleLocation loc){
		if(loc == null) return;
		
		//2016-08-11
		if(signs == null)
			return;
		
		for(Iterator<KChestSign> iter = signs.iterator();iter.hasNext();){
			KChestSign sign = iter.next();
			if(sign.getLoc().equals(loc)) {
				iter.remove();
				return;
			}
		}
	}

}
