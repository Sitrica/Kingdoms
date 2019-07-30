package com.songoda.kingdoms.objects;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class Defender extends DoubleObject<UUID, Invasion> {

	private boolean nexus;

	public Defender(UUID uuid, Invasion invasion, boolean nexus) {
		super(uuid, invasion);
		this.nexus = nexus;
	}

	public Optional<LivingEntity> getDefender() {
		return Optional.ofNullable((LivingEntity) Bukkit.getEntity(getFirst()));
	}

	public DefenderInfo getDefenderInfo() {
		return getInvasion().getTarget().getDefenderInfo();
	}

	public void setNexus(boolean nexus) {
		this.nexus = nexus;
	}

	public OfflineKingdom getOwner() {
		return getSecond().getTarget();
	}

	public boolean isNexusDefender() {
		return nexus;
	}

	public Invasion getInvasion() {
		return getSecond();
	}

}
