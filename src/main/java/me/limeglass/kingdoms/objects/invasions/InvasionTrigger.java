package me.limeglass.kingdoms.objects.invasions;

import java.util.UUID;

import me.limeglass.kingdoms.manager.managers.LandManager.LandInfo;

public abstract class InvasionTrigger {

	private final Invasion invasion;
	private final LandInfo land;
	private final UUID player;

	protected InvasionTrigger(Invasion invasion, LandInfo info, UUID player) {
		this.invasion = invasion;
		this.player = player;
		this.land = info;
	}

	public Invasion getInvasion() {
		return invasion;
	}

	public LandInfo getLandInfo() {
		return land;
	}

	public UUID getPlayerUUID() {
		return player;
	}

}
