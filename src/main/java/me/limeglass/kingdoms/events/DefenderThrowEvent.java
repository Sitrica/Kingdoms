package me.limeglass.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class DefenderThrowEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final KingdomPlayer player;
	private final Defender defender;
	private final double strength;
	private boolean cancelled;

	public DefenderThrowEvent(Defender defender, KingdomPlayer player, double strength) {
		this.defender = defender;
		this.strength = strength;
		this.player = player;
	}

	public double getRange() {
		return strength;
	}

	public Defender getDefender() {
		return defender;
	}

	public KingdomPlayer getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
