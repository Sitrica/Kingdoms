package com.songoda.kingdoms.objects.structures;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class Extractor extends Structure {

	private final long delay, reward;
	private long last; // The last time this was reset

	public Extractor(String kingdom, Location location) {
		this(kingdom, location, System.currentTimeMillis());
	}

	public Extractor(String kingdom, Location location, long next) {
		super(kingdom, location, StructureType.EXTRACTOR);
		FileConfiguration structures = instance.getConfiguration("structures").get();
		String interval = structures.getString("structures.extractor.reward-delay", "24 hours");
		this.reward = structures.getLong("structures.extractor.reward-amount", 50);
		this.delay = IntervalUtils.getMilliseconds(interval);
		this.last = next;
	}

	public long getLastReset() {
		return last;
	}

	public void makeReady() {
		this.last = this.last - delay;
	}

	public void resetTime() {
		this.last = System.currentTimeMillis();
	}

	public boolean isReady() {
		return getMilliseconds() <= 0;
	}

	/**
	 * @return Time in milliseconds before reset.
	 */
	public long getMilliseconds() {
		long difference = System.currentTimeMillis() - last;
		return delay - difference;
	}

	public long getMinutes() {
		return (getMilliseconds() / 1000) / 60;
	}

	public long getReward() {
		return reward;
	}

	public void collect(KingdomPlayer kingdomPlayer) {
		if (!isReady())
			return;
		last = System.currentTimeMillis();
		Land land = instance.getManager(LandManager.class).getLand(location.getChunk());
		Optional<OfflineKingdom> landKingdom = land.getKingdomOwner();
		if (!landKingdom.isPresent())
			return;
		Kingdom kingdom = landKingdom.get().getKingdom();
		if (kingdom == null)
			return;
		kingdom.setResourcePoints(kingdom.getResourcePoints() + reward);
		new MessageBuilder("structures.extractor-collection")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%amount%", reward)
				.setKingdom(kingdom)
				.send(kingdom.getOnlinePlayers());
	}

}
