package me.limeglass.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;

import me.limeglass.kingdoms.objects.Defender;

public class DefenderDeathEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final EntityDeathEvent event;
	private final Defender defender;
	
	public DefenderDeathEvent(Defender defender, EntityDeathEvent event) {
		this.defender = defender;
		this.event = event;
	}

	public Defender getDefender() {
		return defender;
	}

	public EntityDeathEvent getEvent() {
		return event;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
