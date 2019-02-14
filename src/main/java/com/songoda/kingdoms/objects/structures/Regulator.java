package com.songoda.kingdoms.objects.structures;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;

import com.songoda.kingdoms.objects.land.Structure;
import com.songoda.kingdoms.objects.land.StructureType;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public class Regulator extends Structure {

	private final Set<OfflineKingdomPlayer> builders = new HashSet<>();
	private final Set<OfflineKingdomPlayer> interact = new HashSet<>();
	private boolean monsters = true;
	private boolean animals = true;

	public Regulator(Location location) {
		super(location, StructureType.REGULATOR);
	}
	
	//TODO come back to this unused
	/*protected Regulator(Location location, StructureType type, ArrayList<UUID> whoCanBuild, ArrayList<UUID> whoCanInteract, boolean allowMonsterSpawning, boolean allowAnimalSpawning) {
		super(location, type);
		this.monsters = allowMonsterSpawning;
		this.animals = allowAnimalSpawning;
	}*/

	public boolean canSpawnMonsters() {
		return monsters;
	}
	
	public void setMonstersSpawning(boolean monsters) {
		this.monsters = monsters;
	}

	public boolean canSpawnAnimals() {
		return animals;
	}
	
	public void setAnimalsSpawning(boolean animals) {
		this.animals = animals;
	}

	public Set<OfflineKingdomPlayer> getWhoCanBuild() {
		return builders;
	}

	public Set<OfflineKingdomPlayer> getWhoCanInteract() {
		return interact;
	}

}
