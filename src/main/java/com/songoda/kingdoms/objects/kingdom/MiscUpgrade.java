package com.songoda.kingdoms.objects.kingdom;

import java.util.HashMap;
import java.util.Map;

public class MiscUpgrade {
	
	private final Map<MiscUpgradeType, Boolean> enabled = new HashMap<>();
	private final Map<MiscUpgradeType, Boolean> bought = new HashMap<>();
	private final OfflineKingdom kingdom;
	
	public MiscUpgrade(OfflineKingdom kingdom) {
		this.kingdom = kingdom;
	}
	
	public OfflineKingdom getKingdom() {
		return kingdom;
	}

	public boolean hasBought(MiscUpgradeType upgrade) {
		if (upgrade == null)
			return false;
		return bought.getOrDefault(upgrade, false);
	}
	
	public void setBought(MiscUpgradeType upgrade, boolean bought) {
		this.bought.put(upgrade, bought);
	}
	
	public boolean isEnabled(MiscUpgradeType upgrade) {
		if (upgrade == null)
			return false;
		if (!hasBought(upgrade))
			return false;
		return enabled.getOrDefault(upgrade, false);
	}

	public void setEnabled(MiscUpgradeType upgrade, boolean enabled) {
		this.enabled.put(upgrade, enabled);
	}

	/**
	 * Soil in land cannot be trampled.
	 */
	public boolean hasAntiTrample() {
		return isEnabled(MiscUpgradeType.ANTI_TRAMPLE);
	}

	public void setAntitrample(boolean antiTrample) {
		setBought(MiscUpgradeType.ANTI_TRAMPLE, antiTrample);
	}

	/**
	 * No land damage nor any member damage.
	 */
	public boolean hasAnticreeper() {
		return isEnabled(MiscUpgradeType.ANTI_CREEPER);
	}

	public void setAnticreeper(boolean antiCreeper) {
		setBought(MiscUpgradeType.ANTI_CREEPER, antiCreeper);
	}

	/**
	 * Spawn two light armor defenders with weapons on Nexus.
	 */
	public boolean hasNexusGuard() {
		return isEnabled(MiscUpgradeType.NEXUS_GUARD);
	}

	public void setNexusguard(boolean nexusGuard) {
		setBought(MiscUpgradeType.NEXUS_GUARD, nexusGuard);
	}
	
	/**
	 * No land damage, 5 damage to non-members around.
	 */
	public boolean hasBombShards() {
		return isEnabled(MiscUpgradeType.BOMB_SHARDS);
	}

	public void setBombshards(boolean bombShards) {
		setBought(MiscUpgradeType.BOMB_SHARDS, bombShards);
	}
	
	/**
	 * Apply strength 1 for 10 seconds to defender and guards.
	 */
	public boolean hasInsanity() {
		return isEnabled(MiscUpgradeType.INSANITY);
	}

	public void setInsanity(boolean insanity) {
		setBought(MiscUpgradeType.INSANITY, insanity);
	}

	/**
	 * x3 experience on kills.
	 */
	public boolean hasGlory() {
		return isEnabled(MiscUpgradeType.GLORY);
	}

	public void setGlory(boolean glory) {
		setBought(MiscUpgradeType.GLORY, glory);
	}

}
