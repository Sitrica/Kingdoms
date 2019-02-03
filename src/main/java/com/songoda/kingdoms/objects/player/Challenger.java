package com.songoda.kingdoms.objects.player;

import com.songoda.kingdoms.constants.land.SimpleChunkLocation;
import org.bukkit.entity.Entity;

public interface Challenger {
	public Entity getChampionPlayerFightingWith();
	public void setChampionPlayerFightingWith(Entity champion);
	public ChunkLocation getFightZone();
	public void setInvadingChunk(ChunkLocation loc);
}
