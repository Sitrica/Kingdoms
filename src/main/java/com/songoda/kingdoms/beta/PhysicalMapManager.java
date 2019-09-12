package com.songoda.kingdoms.beta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class PhysicalMapManager extends Manager {

	private final Map<UUID, MapView> views = new HashMap<>();

	public PhysicalMapManager() {
		super(true);
	}

	public void createMap(KingdomPlayer player) {
		createMap(player.getPlayer());
	}

	public void createMap(Player player) {
		ItemStack map = new ItemStack(Material.FILLED_MAP);
		MapView view = Bukkit.createMap(player.getWorld());
		view.getRenderers().clear();
		// TODO make scale a valid number and check
		view.addRenderer(new KingdomMapRenderer(128));
		Kingdoms.debugMessage(view.getCenterX() + "");
		ItemMeta meta = map.getItemMeta();
		if (meta instanceof MapMeta) {
			MapMeta mapMeta = (MapMeta) meta;
			mapMeta.setMapView(view);
			map.setItemMeta(mapMeta);
		}
		player.getInventory().addItem(map);
		player.sendMap(view);
		views.put(player.getUniqueId(), view);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		//createMap(event.getPlayer());
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
