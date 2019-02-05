package com.songoda.kingdoms.objects.player;

import org.bukkit.Chunk;
import org.bukkit.entity.LivingEntity;

public interface Challenger {
	
	public LivingEntity getOpponent();
	
	public void setOpponent(LivingEntity opponent);
	
	public Chunk getInvadingChunk();
	
	public void setInvadingChunk(Chunk chunk);

}
