package com.songoda.kingdoms.objects.player;

import java.util.Queue;

import org.bukkit.Location;

public interface Markable {
	public Queue<Location> getLastMarkedChunk();
	public void setMarkDisplaying(boolean bool);
	public boolean isMarkDisplaying();
	public Long getLastDisplayTime();
	public void setLastDisplayTime(Long time);
}
