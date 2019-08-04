package me.limeglass.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class DefenderFocusEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final KingdomPlayer player;
	private final Defender defender;
	private boolean cancelled;

	public DefenderFocusEvent(Defender defender, KingdomPlayer player) {
		this.defender = defender;
		this.player = player;
	}

	public Defender getDefender() {
		return defender;
	}

	public KingdomPlayer getFocusedPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
