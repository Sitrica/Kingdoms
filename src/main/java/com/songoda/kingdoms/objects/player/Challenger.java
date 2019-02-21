package com.songoda.kingdoms.objects.player;

import org.bukkit.entity.LivingEntity;

import com.songoda.kingdoms.objects.land.Land;

public interface Challenger {
	
	public LivingEntity getOpponent();
	
	public void setOpponent(LivingEntity opponent);
	
	public Land getInvadingLand();
	
	public boolean isInvading();
	
	public void setInvadingLand(Land land);

}
