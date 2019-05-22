package com.songoda.kingdoms.objects;

import java.util.Optional;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.maps.MapElement;

public enum Relation {

	ALLIANCE,
	NEUTRAL,
	ENEMY,
	OWN;

	public static Relation getRelation(OfflineKingdom kingdom, OfflineKingdom target) {
		if (kingdom.equals(target))
			return OWN;
		else if (kingdom.isEnemyWith(target))
			return ENEMY;
		else if (kingdom.isAllianceWith(target))
			return ALLIANCE;
		return NEUTRAL;
	}

	public static Relation getRelation(Land land, OfflineKingdom kingdom) {
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (optional.isPresent() && kingdom != null) {
			OfflineKingdom landKingdom = optional.get();
			if (kingdom.equals(landKingdom))
				return OWN;
			else if (kingdom.isEnemyWith(landKingdom))
				return ENEMY;
			else if (kingdom.isAllianceWith(landKingdom))
				return ALLIANCE;
		}
		return NEUTRAL;
	}

	public String getColorFor(MapElement element) {
		return element.getRelationColor(this);
	}

}
