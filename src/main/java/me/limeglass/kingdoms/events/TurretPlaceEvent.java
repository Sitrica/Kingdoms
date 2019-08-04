package me.limeglass.kingdoms.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.turrets.Turret;

public class TurretPlaceEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final KingdomPlayer player;
	private final Kingdom owner;
	private final Turret turret;
	private boolean cancelled;
	private final Land land;

	public TurretPlaceEvent(Land land, Turret turret, KingdomPlayer player) {
		this(land, turret, player, null);
	}

	public TurretPlaceEvent(Land land, Turret turret, KingdomPlayer player, Kingdom owner) {
		this.turret = turret;
		this.player = player;
		this.owner = owner;
		this.land = land;
	}

	public Land getLand() {
		return land;
	}

	public Turret getTurret() {
		return turret;
	}

	public Kingdom getKingdomOwner() {
		return owner;
	}

	public KingdomPlayer getKingdomPlayer() {
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
