package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.List;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.StructureType;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

public class WarpPadManager extends Manager {
	
	static {
		registerManager("warppad", new WarpPadManager());
	}
	
	private static HashMap<OfflineKingdom, List<Land>> pads = new HashMap<>();
	
	protected WarpPadManager() {
		super(false);
		Kingdoms isntance = Kingdoms.getInstance();
		LandManager landManager = isntance.getManager("land", LandManager.class);
		landManager.getLoadedLand().parallelStream()
				.map(chunk -> landManager.getLand(chunk))
				.forEach(land -> checkLoad(land));
	}
	
	public void checkLoad(Land land){
		if (land.getStructure() != null) {
			StructureType type = land.getStructure().getType();
			if (type == StructureType.OUTPOST || type == StructureType.NEXUS || type == StructureType.WARPPAD) {
				if (land.getKingdomOwner() != null) {
					addLand(land.getKingdomOwner(), land);
				}
			}
		}
	}	
	
	public void removeLand(OfflineKingdom kingdom, Land land) {
		if (!pads.containsKey(kingdom))
			return;
		pads.get(kingdom).remove(land);
	}
	
	public void addLand(OfflineKingdom kingdom, Land land) {
		if (!pads.containsKey(kingdom))
			return;
		List<Land> lands = pads.get(kingdom);
		if (!lands.contains(land))
			lands.add(land);
		pads.put(kingdom, lands);
	}
	
	public List<Land> getOutposts(OfflineKingdom kingdom) {
		return pads.get(kingdom);
	}

	@Override
	public void onDisable() {
		pads.clear();
	}

}
