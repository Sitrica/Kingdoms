package me.limeglass.kingdoms.manager.managers.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import me.limeglass.kingdoms.events.PlayerChangeChunkEvent;
import me.limeglass.kingdoms.manager.ExternalManager;
import me.limeglass.kingdoms.manager.managers.LandManager;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.LocationUtils;

public class DynmapManager extends ExternalManager {
	
	private final String base = "<div>"
			+ "This land is owned by: <span style=\"font-weight:bold;color:black\">%kingdomName% </span><br>"
			+ "<span style=\"font-weight:italic;color:red\">%king% </span><br>"
			+ "<span style=\"font-weight:italic;color:red\">%membercount% </span><br>"
			+ "<span style=\"font-weight:italic;color:red\">%resourcepoints% </span><br>"
			+ "<span style=\"font-weight:bold;color:black\">Members: </span><br>"
			+ "</div>";
	private LandManager landManager;
	private DynmapAPI dynmap;
	
	public DynmapManager() {
		super("dynmap", true);
		Server server = instance.getServer();
		PluginManager pluginManager = server.getPluginManager();
		if (!pluginManager.isPluginEnabled("dynmap"))
			return;
		dynmap = (DynmapAPI) pluginManager.getPlugin("dynmap");
		if (dynmap == null)
			return;
		MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("com/songoda/kingdoms");
		if (set != null)
			set.deleteMarkerSet();
		this.landManager = instance.getManager("land", LandManager.class);
		server.getScheduler().runTaskTimerAsynchronously(instance, () -> landManager.getLoadedLand().forEach(entry -> consumer.accept(entry.getKey())), 0, 20);
	}

	@Override
	public boolean isEnabled() {
		return dynmap != null;
	}

	public void update(Chunk chunk) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> consumer.accept(chunk));
	}

	private final Consumer<Chunk> consumer = new Consumer<Chunk>() {
		@Override
		public void accept(Chunk chunk) {
			MarkerAPI marker = dynmap.getMarkerAPI();
			MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("com/songoda/kingdoms");
			if (set == null)
				set = marker.createMarkerSet("com/songoda/kingdoms", "com/songoda/kingdoms", null, true);
			AreaMarker amarker;
			Land land = landManager.getLand(chunk);
			Optional<OfflineKingdom> optional = land.getKingdomOwner();
			String chunkString = LocationUtils.chunkToString(chunk);
			if (!optional.isPresent()) {
				amarker = set.findAreaMarker(chunkString);
				if (amarker != null)
					amarker.deleteMarker();
				return;
			}
			OfflineKingdom landKingdom = optional.get();
			List<Double> arrX = new ArrayList<Double>();
			List<Double> arrZ = new ArrayList<Double>();

			arrX.add(((chunk.getX() << 4) | 0) - 0.5);
			arrZ.add(((chunk.getZ() << 4) | 0) - 0.5);

			arrX.add(((chunk.getX() << 4) | 0) - 0.5);
			arrZ.add(((chunk.getZ() << 4) | 15) + 0.5);

			arrX.add(((chunk.getX() << 4) | 15) + 0.5);
			arrZ.add(((chunk.getZ() << 4) | 15) + 0.5);

			arrX.add(((chunk.getX() << 4) | 15) + 0.5);
			arrZ.add(((chunk.getZ() << 4) | 0) - 0.5);

			amarker = set.findAreaMarker(chunkString);
			if (amarker == null)
				amarker = set.createAreaMarker(chunkString, landKingdom.getName(), false, chunk.getWorld().getName(), toPrimitive(arrX), toPrimitive(arrZ), true);
			
			amarker.setLineStyle(0, 0, landKingdom.getDynmapColor());
			amarker.setFillStyle(0.4, landKingdom.getDynmapColor());
			amarker.setDescription(" " + getKingdomDescription(landKingdom));
			amarker.setCornerLocations(toPrimitive(arrX), toPrimitive(arrZ));
		}
	};

	private double[] toPrimitive(List<Double> array) {
		if (array == null)
			return null;
		if (array.size() == 0)
			return new double[] {};
		double[] result = new double[array.size()];
		for (int i = 0; i < array.size(); i++)
			result[i] = array.get(i).doubleValue();
		return result;
	}

	private String getKingdomDescription(OfflineKingdom offlineKingdom) {
		String description = base;
		description = description.replace("%kingdomName%", offlineKingdom.getName());
		description = description.replace("%king%", "King: " + offlineKingdom.getOwner().getName());
		description = description.replace("%membercount%", "Number of Members: " + offlineKingdom.getMembers().size());
		description = description.replace("%resourcepoints%", "ResourcePoints: " + offlineKingdom.getResourcePoints());
		if (offlineKingdom.isOnline()) {
			for (KingdomPlayer kingdomPlayer : offlineKingdom.getKingdom().getOnlinePlayers()) {
				description += "<span style=\"font-weight:italic;color:black\">" + kingdomPlayer.getPlayer().getName() + "</span><br>";
			}
		}
		return description;
	}

	public void removeClaimMarker(Chunk chunk) {
		MarkerAPI marker = dynmap.getMarkerAPI();
		MarkerSet set = marker.getMarkerSet("com/songoda/kingdoms");
		if (set == null)
			return;
		AreaMarker amarker = set.findAreaMarker(LocationUtils.chunkToString(chunk));
		if (amarker != null)
			amarker.deleteMarker();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkChange(PlayerChangeChunkEvent event) {
		int radius = 1;
		Chunk center = event.getToChunk();
		for (int x = -radius;x <= radius; x++) {
			for (int z = -radius; z<=radius; z++) {
				update(center.getWorld().getChunkAt(center.getX() + x, center.getZ() + z));
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		update(event.getChunk());
	}

	@Override
	public void onDisable() {}
	
}
