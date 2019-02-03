package com.songoda.kingdoms.objects.land;

import com.songoda.kingdoms.constants.StructureType;
import com.songoda.kingdoms.objects.KingdomPlayer;

public class Crystal extends Structure {
	
	private String chunkProtected;

	public Crystal(SimpleLocation loc, StructureType type) {
		super(loc, type);
	}
	
	public Crystal(SimpleLocation loc, StructureType type, String chunkProtected) {
		super(loc, type);
		this.chunkProtected = chunkProtected;
		
	}
	
	public void openInventory(KingdomPlayer player){
		
	}
}
