package com.songoda.kingdoms.objects.maps;

import java.util.Optional;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;

public enum Relation {

	ALLIANCE,
	ENEMY,
	NONE,
	OWN,
	YOU;

	public static Relation getRelation(Land land, OfflineKingdom kingdom) {
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (optional.isPresent() && kingdom != null) {
			OfflineKingdom landKingdom = optional.get();
			if (kingdom.equals(landKingdom))
				return OWN;
			else if (kingdom.isAllianceWith(landKingdom))
				return ALLIANCE;
			else if (kingdom.isEnemyWith(landKingdom))
				return ENEMY;
		}
		return NONE;
	}

	public String getColorFor(MapElement element) {
		return element.getRelationColor(this);
	}

}
