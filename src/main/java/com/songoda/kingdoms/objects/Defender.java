package com.songoda.kingdoms.objects;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class Defender extends DoubleObject<UUID, Invasion> {

	public Defender(UUID uuid, Invasion invasion) {
		super(uuid, invasion);
	}

	public Optional<LivingEntity> getDefender() {
		return Optional.ofNullable((LivingEntity) Bukkit.getEntity(getFirst()));
	}

	public DefenderInfo getDefenderInfo() {
		return getInvasion().getTarget().getDefenderInfo();
	}

	public OfflineKingdom getOwner() {
		return getSecond().getTarget();
	}

	public Invasion getInvasion() {
		return getSecond();
	}

}
