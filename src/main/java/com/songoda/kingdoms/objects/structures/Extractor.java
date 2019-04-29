package com.songoda.kingdoms.objects.structures;

import java.util.Optional;

import org.bukkit.Location;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class Extractor extends Structure {
	
	private final LandManager landManager;
	private final long delay, reward;
	private long next;

	public Extractor(OfflineKingdom kingdom, Location location) {
		this(kingdom, location, System.currentTimeMillis());
	}
	
	public Extractor(OfflineKingdom kingdom, Location location, long next) {
		super(kingdom, location, StructureType.EXTRACTOR);
		String interval = configuration.getString("structures.extractor.reward-delay", "24 hours");
		this.reward = configuration.getLong("structures.extractor.reward-amount", 50);
		this.landManager = instance.getManager("land", LandManager.class);
		this.delay = IntervalUtils.getInterval(interval);
		this.next = next;
	}
	
	public void resetTime() {
		this.next = System.currentTimeMillis();
	}
	
	public boolean isReady() {
		return getTimeLeft() <= 0;
	}
	
	public long getTimeToNextCollection() {
		return next;
	}
	
	public long getTimeLeft() {
		long time;
		long now = System.currentTimeMillis();
		long totalTime = delay * 60;
		int r = (int) (now - next) / 1000;
		time = (r - totalTime) * (-1);
		return time / 60;
	}
	
	public void collect(KingdomPlayer player) {
		if (!isReady())
			return;
		next = System.currentTimeMillis();
		Land land = landManager.getLand(location.getChunk());
		Optional<OfflineKingdom> landKingdom = land.getKingdomOwner();
		if (!landKingdom.isPresent())
			return;
		Kingdom kingdom = landKingdom.get().getKingdom();
		if (kingdom == null)
			return;
		kingdom.setResourcePoints(kingdom.getResourcePoints() + reward);
		new MessageBuilder("structures.extractor-collection")
				.replace("%amount%", reward)
				.setKingdom(kingdom)
				.send(kingdom.getOnlinePlayers());
	}

}
