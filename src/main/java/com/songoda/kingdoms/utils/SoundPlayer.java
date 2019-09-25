package com.songoda.kingdoms.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;

public class SoundPlayer {

	private final Set<KingdomSound> sounds = new HashSet<>();

	public SoundPlayer(String node) {
		this(Kingdoms.getInstance().getConfiguration("sounds").get().getConfigurationSection(node));
	}

	public SoundPlayer(ConfigurationSection section) {
		if (!section.getBoolean("enabled", true))
			return;
		if (!section.isConfigurationSection("sounds")) {
			Kingdoms.debugMessage("There was no 'sounds' configuration section at " + section.getCurrentPath() + ".sounds");
			return;
		}
		section = section.getConfigurationSection("sounds");
		for (String node : section.getKeys(false)) {
			this.sounds.add(new KingdomSound(section.getConfigurationSection(node), "CLICK"));
		}
	}

	public SoundPlayer(Collection<KingdomSound> sounds) {
		this.sounds.addAll(sounds);
	}

	private List<KingdomSound> getSorted() {
		return sounds.parallelStream()
				.sorted(Comparator.comparing(KingdomSound::getDelay))
				.collect(Collectors.toList());
	}

	public void playAt(Location... locations) {
		if (sounds.isEmpty())
			return;
		Kingdoms instance = Kingdoms.getInstance();
		for (KingdomSound sound : getSorted()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				@Override
				public void run() {
					sound.playAt(locations);
				}
			}, sound.getDelay());
		}
	}

	public void playTo(Collection<Player> players) {
		playTo(players.stream().toArray(size -> new Player[size]));
	}

	public void playTo(Player... player) {
		if (sounds.isEmpty())
			return;
		Kingdoms instance = Kingdoms.getInstance();
		for (KingdomSound sound : getSorted()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				@Override
				public void run() {
					sound.playTo(player);
				}
			}, sound.getDelay());
		}
	}

}
