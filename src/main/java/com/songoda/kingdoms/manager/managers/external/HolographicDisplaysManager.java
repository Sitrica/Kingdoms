package com.songoda.kingdoms.manager.managers.external;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.internal.BackendAPI;
import com.gmail.filoghost.holographicdisplays.api.placeholder.PlaceholderReplacer;
import com.gmail.filoghost.holographicdisplays.placeholder.RelativePlaceholder;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.Formatting;

public class HolographicDisplaysManager extends Manager {

	static {
		registerManager("holographic-displays", new HolographicDisplaysManager());
	}

	private BackendAPI holographic;
	private final boolean enabled;

	protected HolographicDisplaysManager() {
		super(false);
		enabled = instance.getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
		if (!enabled)
			return;
		holographic = BackendAPI.getImplementation();
		PlayerManager playerManager = instance.getManager("player", PlayerManager.class);
		LandManager landManager = instance.getManager("land", LandManager.class);
		FileConfiguration messages = instance.getConfiguration("messages").get();
		RelativePlaceholder.register(new RelativePlaceholder("{kingdom}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdom.getName() : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{kingdom-rank}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdomPlayer.getRank().getName() : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{points}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdom.getResourcePoints() + "" : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{claims}") {
			@Override
			public String getReplacement(Player player) {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom", "No Kingdom"));
				return kingdom != null ? kingdom.getClaims().size() + "" : notFound;
			}
		});
		RelativePlaceholder.register(new RelativePlaceholder("{kingdom-at}") {
			@Override
			public String getReplacement(Player player) {
				OfflineKingdom kingdom = landManager.getLandAt(player.getLocation()).getKingdomOwner();
				String notFound = Formatting.color(messages.getString("messages.holographic-displays.no-kingdom-at", "No Kingdom"));
				return kingdom != null ? kingdom.getName() : notFound;
			}
		});
	}

	/**
	 * @return boolean if HolographicDisplays is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Resets and removes all the placeholders registered by Kingdoms. This is useful
	 * when you have configurable placeholders and you want to remove all of them.
	 */
	public void unregisterPlaceholders() {
		holographic.unregisterPlaceholders(instance);
	}

	/**
	 * @return Holograms created by Kingdoms. Collection is a copy
	 * and modifying it has no effect on the holograms.
	 */
	public Collection<Hologram> getHolograms() {
		return holographic.getHolograms(instance);
	}

	/**
	 * Checks if an Entity is part of a hologram.
	 * 
	 * @param entity Entity to check.
	 * @return boolean if the entity is a part of a hologram.
	 */
	public boolean isHologramEntity(Entity entity) {
		return holographic.isHologramEntity(entity);
	}

	/**
	 * Creates a hologram at the given location.
	 * 
	 * @param source Location where the hologram will appear.
	 * @return Hologram that is created.
	 */
	public Hologram createHologram(Location source) {
		return holographic.createHologram(instance, source);
	}

	/**
	 * @return Collection of placeholders registered by Kingdoms.
	 */
	public Collection<String> getRegisteredPlaceholders() {
		return holographic.getRegisteredPlaceholders(instance);
	}
	
	/**
	 * Unregister a placeholder created by Kingdoms.
	 * 
	 * @param placeholder Placeholder to remove
	 * @return boolean if found and removed, false otherwise.
	 */
	public boolean unregisterPlaceholder(String placeholder) {
		return holographic.unregisterPlaceholder(instance, placeholder);
	}
	
	/**
	 * Registers a new placeholder that can be used in holograms created with commands.
	 * With this method, you can basically expand the core of HolographicDisplays.
	 * 
	 * @param placeholder the text that the placeholder will be associated to (e.g.: "{onlinePlayers}")
	 * @param refreshRate the refresh rate of the placeholder, in seconds. Keep in mind that the minimum is 0.1 seconds, and that will be rounded to tenths of seconds
	 * @param replacer the implementation that will return the text to replace the placeholder, where the update() method is called every <b>refreshRate</b> seconds
	 * @return true if the registration was successful, false if it was already registered.
	 */
	public boolean registerPlaceholder(String placeholder, double refreshRate, PlaceholderReplacer replacer) {
		return holographic.registerPlaceholder(instance, placeholder, refreshRate, replacer);
	}
	
	@Override
	public void onDisable() {}

}
