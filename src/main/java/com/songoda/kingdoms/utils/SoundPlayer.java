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
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class SoundPlayer {

	private final Set<KingdomSound> sounds = new HashSet<>();
	private final Kingdoms instance;
	
	public SoundPlayer(ConfigurationSection section) {
		for (String node : section.getKeys(false)) {
			this.sounds.add(new KingdomSound(section.getConfigurationSection(node), "CLICK"));
		}
		this.instance = Kingdoms.getInstance();
	}
	
	public SoundPlayer(Collection<KingdomSound> sounds) {
		this.instance = Kingdoms.getInstance();
		this.sounds.addAll(sounds);
	}
	
	private List<KingdomSound> getSorted() {
		return sounds.parallelStream()
				.sorted(Comparator.comparing(KingdomSound::getDelay))
				.collect(Collectors.toList());
	}
	
	public void play(Collection<KingdomPlayer> players) {
		players.parallelStream()
				.map(player -> player.getPlayer())
				.forEach(player -> playTo(player));
	}
	
	public void playAt(Location... locations) {
		if (sounds.isEmpty())
			return;
		for (KingdomSound sound : getSorted()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				@Override
				public void run() {
					sound.playAt(locations);
				}
			}, sound.getDelay());
		}
	}
	
	public void playTo(Player... player) {
		if (sounds.isEmpty())
			return;
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
