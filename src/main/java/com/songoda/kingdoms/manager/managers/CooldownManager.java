package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class CooldownManager extends Manager {

	private final static Map<String, KingdomCooldown> cooldowns = new HashMap<>();
	
	public CooldownManager() {
		super("cooldown", false);
	}

	public boolean isInCooldown(OfflineKingdom kingdom, String name) {
		if (getTimeLeft(kingdom, name) > 0) {
			return true;
		} else {
			stop(kingdom, name);
			return false;
		}
	}

	public long getTimeLeft(OfflineKingdom kingdom, String name) {
		Optional<KingdomCooldown> optional = getCooldown(kingdom, name);
		long remaining = -1;
		if (optional.isPresent()) {
			KingdomCooldown cooldown = optional.get();
			if (cooldown != null && kingdom != null) {
				long now = System.currentTimeMillis();
				long cooldownTime = cooldown.start;
				long totalTime = cooldown.seconds;
				int r = (int) (now - cooldownTime) / 1000;
				remaining = (r - totalTime) * (-1);
			}
		}
		return remaining;
	}

	private Optional<KingdomCooldown> getCooldown(OfflineKingdom kingdom, String name) {
		return Optional.ofNullable(cooldowns.get(kingdom.getName() + name));
	}

	private void stop(OfflineKingdom kingdom, String name) {
		cooldowns.remove(kingdom.getName() + name);
	}

	public static class KingdomCooldown {

		private final OfflineKingdom kingdom;
		private final long seconds;
		private final String name;
		private long start;

		public KingdomCooldown(OfflineKingdom kingdom, String name, long seconds) {
			this.kingdom = kingdom;
			this.seconds = seconds;
			this.name = name;
		}

		public void start() {
			this.start = System.currentTimeMillis();
			cooldowns.put(kingdom.getName() + name, this);
		}
	 
	}
	
	@Override
	public void initalize() {}

	@Override
	public void onDisable() {
		cooldowns.clear();
	}

}
