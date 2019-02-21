package com.songoda.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class InvadingSurrenderEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final KingdomPlayer challenger;
	private final OfflineKingdom defender;
	private final Land land;

	public InvadingSurrenderEvent(KingdomPlayer challenger, OfflineKingdom defender, Land land) {
		this.challenger = challenger;
		this.defender = defender;
		this.land = land;
	}

	public Land getLand() {
		return land;
	}

	public OfflineKingdom getDefender() {
		return defender;
	}

	public KingdomPlayer getChallenger() {
		return challenger;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
