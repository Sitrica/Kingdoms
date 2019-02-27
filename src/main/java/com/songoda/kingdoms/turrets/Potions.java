package com.songoda.kingdoms.turrets;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.songoda.kingdoms.utils.Utils;

public class Potions {
	
	private final Set<PotionEffect> effects = new HashSet<>();
	private final boolean iconExists;
	
	public Potions(String input) {
		this.iconExists = Utils.methodExists(PotionEffect.class, "hasIcon");
		for (String potion : input.split(":")) {
			String[] values = potion.split(";");
			if (values.length < 5)
				return;
			PotionEffectType type = PotionEffectType.HARM;
			try {
				type = PotionEffectType.getByName(values[0]);
			} catch (Exception e) {
				type = PotionEffectType.HARM;
			}
			int duration = getInt(values[1], 60);
			int amplifier = getInt(values[2], 1);
			boolean ambient = getBoolean(values[3], false);
			boolean particles = getBoolean(values[4], true);
			if (iconExists && values.length > 5) {
				boolean toast = getBoolean(values[5], true);
				effects.add(new PotionEffect(type, duration, amplifier, ambient, particles, toast));
				continue;
			}
			effects.add(new PotionEffect(type, duration, amplifier, ambient, particles));
		}
	}
	
	public Potions(ConfigurationSection section) {
		this.iconExists = Utils.methodExists(PotionEffect.class, "hasIcon");
		for (String node : section.getKeys(false)) {
			section = section.getConfigurationSection(node);
			PotionEffectType type = PotionEffectType.HARM;
			try {
				type = PotionEffectType.getByName(section.getString("potion", "HARM"));
			} catch (Exception e) {
				type = PotionEffectType.HARM;
			}
			int amplifier = section.getInt("amplifier", 1);
			int duration = section.getInt("duration", 60);
			boolean ambient = section.getBoolean("ambient", false);
			boolean particles = section.getBoolean("particles", false);
			if (iconExists) {
				boolean toast = section.getBoolean("toast", false);
				effects.add(new PotionEffect(type, duration, amplifier, ambient, particles, toast));
				continue;
			}
			effects.add(new PotionEffect(type, duration, amplifier, ambient, particles));
		}
	}
	
	private int getInt(String value, int def) {
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return def;
		}
	}
	
	private boolean getBoolean(String value, boolean def) {
		try {
			return Boolean.valueOf(value);
		} catch (Exception e) {
			return def;
		}
	}

	public Set<PotionEffect> getPotionEffects() {
		return effects;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		effects.forEach(effect -> {
			String type = effect.getType() + ";";
			String duration = effect.getDuration() + ";";
			String amplifier = effect.getAmplifier() + ";";
			String ambient = effect.isAmbient() + ";";
			String particles = effect.hasParticles() + ";";
			if (iconExists) {
				String icon = effect.hasIcon() + "";
				builder.append(type + duration + amplifier + ambient + particles + icon + ":");
			} else {
				String string = type + duration + amplifier + ambient + particles;
				int index = string.lastIndexOf(";");
				if (index >= 0)
					string = string.substring(0, index);
				builder.append(string + ":");
			}
		});
		return builder.toString();
	}
	
}
