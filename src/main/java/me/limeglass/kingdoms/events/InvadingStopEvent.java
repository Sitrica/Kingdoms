package me.limeglass.kingdoms.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.invasions.InvasionMechanic.StopReason;

public class InvadingStopEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final StopReason reason;
	private final Invasion invasion;

	public InvadingStopEvent(StopReason reason, Invasion invasion) {
		this.invasion = invasion;
		this.reason = reason;
	}

	public Invasion getInvasion() {
		return invasion;
	}

	public StopReason getReason() {
		return reason;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
