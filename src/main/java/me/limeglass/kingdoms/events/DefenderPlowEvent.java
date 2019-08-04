package me.limeglass.kingdoms.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.kingdoms.objects.invasions.Invasion;

public class DefenderPlowEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Invasion invasion;
	private final Entity defender;
	private final Block block;
	private boolean cancelled;

	public DefenderPlowEvent(Entity defender, Invasion invasion, Block block) {
		this.defender = defender;
		this.invasion = invasion;
		this.block = block;
	}

	public Block getBlock() {
		return block;
	}
	
	public Entity getDefender() {
		return defender;
	}

	public Invasion getInvasion() {
		return invasion;
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
