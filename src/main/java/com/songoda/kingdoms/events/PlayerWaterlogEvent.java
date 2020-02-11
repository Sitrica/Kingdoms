package com.songoda.kingdoms.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerWaterlogEvent extends PlayerBucketEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	public PlayerWaterlogEvent(Player who, Block blockClicked, BlockFace blockFace, Material bucket, ItemStack itemInHand) {
		super(who, blockClicked, blockFace, bucket, itemInHand);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
