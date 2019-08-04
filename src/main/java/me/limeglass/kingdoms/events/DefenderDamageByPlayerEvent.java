package me.limeglass.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class DefenderDamageByPlayerEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final KingdomPlayer player;
	private final Defender defender;
	private boolean cancelled;
	private double damage;

	public DefenderDamageByPlayerEvent(Defender defender, KingdomPlayer player, double damage) {
		this.defender = defender;
		this.player = player;
		this.damage = damage;
	}

	public double getDamage() {
		return damage;
	}
	
	public Defender getDefender() {
		return defender;
	}

	public KingdomPlayer getPlayer() {
		return player;
	}

	public void setDamage(double damage) {
		this.damage = damage;
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
