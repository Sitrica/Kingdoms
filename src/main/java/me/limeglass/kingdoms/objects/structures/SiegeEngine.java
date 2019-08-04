package me.limeglass.kingdoms.objects.structures;

import org.bukkit.Location;

import me.limeglass.kingdoms.utils.IntervalUtils;

public class SiegeEngine extends Structure {
	
	private final long cooldown;
	private long time;
	
	public SiegeEngine(String kingdom, Location location) {
		this(kingdom, location, System.currentTimeMillis());
	}
	
	public SiegeEngine(String kingdom, Location location, long time) {
		super(kingdom, location, StructureType.SIEGE_ENGINE);
		String interval = configuration.getString("structures.siege-engine.cooldown", "60 seconds");
		this.cooldown = IntervalUtils.getInterval(interval);
		this.time = time;
	}
	
	public void resetCooldown() {
		this.time = System.currentTimeMillis();
	}
	
	public boolean isReady() {
		return getCooldownTimeLeft() <= 0;
	}
	
	public long getCooldownTimeLeft() {
		long left = -1;
		long now = System.currentTimeMillis();
		long totalTime = cooldown * 60;
		int r = (int) (now - time) / 1000;
		left = (r - totalTime) * (-1);
		return left / 60;
	}

}
