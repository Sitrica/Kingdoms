package me.limeglass.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.Relation;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;

public class KingdomAllegianceEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Relation oldRelation, newRelation;
	private final OfflineKingdom kingdom, target;

	public KingdomAllegianceEvent(OfflineKingdom kingdom, OfflineKingdom target, Relation oldRelation, Relation newRelation) {
		this.newRelation = newRelation;
		this.oldRelation = oldRelation;
		this.kingdom = kingdom;
		this.target = target;
	}

	public OfflineKingdom getAlliancedKingdom() {
		return target;
	}

	public OfflineKingdom getKingdom() {
		return kingdom;
	}

	public Relation getNewRelation() {
		return newRelation;
	}

	public Relation getOldRelation() {
		return oldRelation;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
