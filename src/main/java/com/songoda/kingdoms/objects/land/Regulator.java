package com.songoda.kingdoms.objects.land;

import java.util.ArrayList;
import java.util.UUID;

import com.songoda.kingdoms.constants.StructureType;

public class Regulator extends Structure {
	
	private boolean allowMonsterSpawning = true;
	private boolean allowAnimalSpawning = true;
	private ArrayList<UUID> whoCanBuild = new ArrayList<UUID>();
	private ArrayList<UUID> whoCanInteract = new ArrayList<UUID>();

	public Regulator(SimpleLocation loc, StructureType type) {
		super(loc, type);
	}
	
	public Regulator(SimpleLocation loc, StructureType type, ArrayList<UUID> whoCanBuild, ArrayList<UUID> whoCanInteract, boolean allowMonsterSpawning, boolean allowAnimalSpawning) {
		super(loc, type);
		this.whoCanBuild = whoCanBuild;
		this.whoCanInteract = whoCanInteract;
		this.allowMonsterSpawning = allowMonsterSpawning;
		this.allowAnimalSpawning = allowAnimalSpawning;
	}

	public boolean isAllowMonsterSpawning() {
		return allowMonsterSpawning;
	}

	public boolean isAllowAnimalSpawning() {
		return allowAnimalSpawning;
	}

	public ArrayList<UUID> getWhoCanBuild() {
		return whoCanBuild;
	}

	public ArrayList<UUID> getWhoCanInteract() {
		return whoCanInteract;
	}

	public void setAllowMonsterSpawning(boolean allowMonsterSpawning) {
		this.allowMonsterSpawning = allowMonsterSpawning;
	}

	public void setAllowAnimalSpawning(boolean allowAnimalSpawning) {
		this.allowAnimalSpawning = allowAnimalSpawning;
	}

	public void setWhoCanBuild(ArrayList<UUID> whoCanBuild) {
		this.whoCanBuild = whoCanBuild;
	}

	public void setWhoCanInteract(ArrayList<UUID> whoCanInteract) {
		this.whoCanInteract = whoCanInteract;
	}

}
