package me.limeglass.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.kingdom.Kingdom;

public class KingdomLoadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Kingdom kingdom;

	public KingdomLoadEvent(Kingdom kingdom) {
		this.kingdom = kingdom;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
