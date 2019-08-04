package me.limeglass.kingdoms.objects.turrets;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

public class HealthInfo {
	
	private final Random random = new Random();
	private final int health, chance;
	private final boolean useChance;
	
	public HealthInfo(ConfigurationSection section) {
		this.useChance = section.getBoolean("chance.enabled", false);
		this.chance = section.getInt("chance.percent", 80);
		this.health = section.getInt("health", 2);
	}

	public boolean isChance() {
		return useChance;
	}
	
	public int getHealth() {
		return health;
	}
	
	public boolean chance() {
		return random.nextInt(100) < chance;
	}
	
}
