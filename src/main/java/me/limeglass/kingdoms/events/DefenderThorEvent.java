package me.limeglass.kingdoms.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class DefenderThorEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineKingdom kingdom;
	private final KingdomPlayer player;
	private final Entity defender;
	private final double damage;
	private boolean cancelled;

	public DefenderThorEvent(OfflineKingdom kingdom, Entity defender, KingdomPlayer player) {
		this.damage = player.getKingdom().getDefenderInfo().getThor() + 2;
		this.defender = defender;
		this.kingdom = kingdom;
		this.player = player;
	}

	public double getDamage() {
		return damage;
	}
	
	public Entity getDefender() {
		return defender;
	}

	public KingdomPlayer getPlayer() {
		return player;
	}

	public OfflineKingdom getKingdom() {
		return kingdom;
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
