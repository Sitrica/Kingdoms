package com.songoda.kingdoms.manager.managers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.AsciiCompass;
import com.songoda.kingdoms.utils.MessageBuilder;

public class MapManager extends Manager {

	static {
		registerManager("map", new MapManager());
	}

	private final PlayerManager playerManager;
	private final LandManager landManager;

	protected MapManager() {
		super(true);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}

	public void displayMap(Player player, boolean revealStructures) {
		Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
			List<String> compass = AsciiCompass.getAsciiCompass(AsciiCompass.getCardinalDirection(player), ChatColor.YELLOW, ChatColor.AQUA + "");
			String[] row = {"", "", "", "", "", "", "", ""};
			String compass1 = compass.get(0);
			String compass2 = compass.get(1);
			String compass3 = compass.get(2);
			String cck = ChatColor.AQUA + "Unoccupied";
			KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
			Kingdom kingdom = kingdomPlayer.getKingdom();
			Chunk chunk = kingdomPlayer.getChunkAt();
			Land land = landManager.getLand(chunk);
			OfflineKingdom landKingdom = land.getKingdomOwner();
			if (landKingdom != null && kingdom != null) {
				if (kingdom.isAllianceWith(kingdom))
					cck = ChatColor.LIGHT_PURPLE + kingdom.getName();
				else if (landKingdom.equals(kingdom))
					cck = ChatColor.GREEN + kingdom.getName();
				else if (kingdom.isEnemyWith(kingdom))
					cck = ChatColor.RED + kingdom.getName();
				else
					cck = ChatColor.GRAY + kingdom.getName();
			}
			int originX = chunk.getX();
			int originZ = chunk.getZ();
			for (int xc = 0; xc < 8; xc++) {
				int x = xc - 4;
				for (int zc = 0; zc <= 24; zc++) {
					int z = zc - 12;
					Chunk schunk = chunk.getWorld().getChunkAt(originX + x, originZ + z);
					String schunkcolor = mapIdentifyChunk(schunk, kingdomPlayer, revealStructures);
					if (x == 0 && z == 0)
						schunkcolor = ChatColor.WHITE + "▣";
					row[xc] += schunkcolor;
					if (xc == 0 && zc == 24)
						row[xc] = row[xc] + ChatColor.LIGHT_PURPLE + "   ===========Key============";
					if (xc == 1 && zc == 24)
						row[xc] = row[xc] + ChatColor.LIGHT_PURPLE + "   ▣ = "
								+ new MessageBuilder("map.you").setPlaceholderObject(kingdomPlayer).get() + "				 □ = "
								+ new MessageBuilder("map.nexus").setPlaceholderObject(kingdomPlayer).get();
					if (xc == 2 && zc == 24)
						row[xc] = row[xc] + ChatColor.LIGHT_PURPLE + "   ▥ = "
								+ new MessageBuilder("map.powercell").setPlaceholderObject(kingdomPlayer).get() + " ▤ = "
								+ new MessageBuilder("map.outpost").setPlaceholderObject(kingdomPlayer).get();
					if (xc == 3 && zc == 24)
						row[xc] = row[xc] + "   " + ChatColor.WHITE + compass1;
					if (xc == 4 && zc == 24)
						row[xc] = row[xc] + "   " + ChatColor.WHITE + compass2 + ChatColor.AQUA + "	  [" + cck + ChatColor.AQUA + "]";
					if (xc == 5 && zc == 24)
						row[xc] = row[xc] + "   " + ChatColor.WHITE + compass3;
					if (xc == 6 && zc == 24)
						row[xc] = row[xc] + "   " + ChatColor.GREEN
								+ new MessageBuilder("map.kingdom").setPlaceholderObject(kingdomPlayer).get() + "   "
								+ ChatColor.WHITE + new MessageBuilder("map.you").setPlaceholderObject(kingdomPlayer).get();
					if (xc == 7 && zc == 24)
						row[xc] = row[xc] + "   " + ChatColor.RED + new MessageBuilder("map.enemies").setPlaceholderObject(kingdomPlayer).get()
								+ "   " + ChatColor.AQUA + new MessageBuilder("map.unoccupied").setPlaceholderObject(kingdomPlayer).get();
					if (xc == 8 && zc == 24)
						row[xc] = row[xc] + "   " + ChatColor.LIGHT_PURPLE + new MessageBuilder("map.alliance").setPlaceholderObject(kingdomPlayer).get()
								+ "   " + ChatColor.GRAY + new MessageBuilder("map.unidentified").setPlaceholderObject(kingdomPlayer).get();
				}
				player.sendMessage(row[xc]);
			}
			player.sendMessage(ChatColor.AQUA + "=======================================");
		});
	}

	private String mapIdentifyChunk(Chunk chunk, KingdomPlayer kingdomPlayer, boolean structures) {
		String icon = ChatColor.AQUA+"▩";
		
		Land land = landManager.getLand(chunk);
		Structure structure = land.getStructure();
		
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return icon;
		
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return ChatColor.GRAY + "▩";
		if (kingdom.equals(landKingdom)) {
			icon = ChatColor.GREEN + "▩";
			if (structure != null) {
				if (structure.getType() == StructureType.NEXUS)
					icon = ChatColor.GREEN + "□";
				else if (structure.getType() == StructureType.OUTPOST)
					icon = ChatColor.GREEN + "▤";
				else if (structure.getType() == StructureType.POWERCELL)
					icon = ChatColor.GREEN + "▥";
			}
		} else if (kingdom.isAllianceWith(landKingdom)) {
			icon = ChatColor.LIGHT_PURPLE + "▩";
			if (structure != null && structures) {
				if (structure.getType() == StructureType.NEXUS)
					icon = ChatColor.LIGHT_PURPLE + "□";
				else if(land.getStructure().getType() == StructureType.OUTPOST)
					icon = ChatColor.LIGHT_PURPLE + "▤";
				else if(land.getStructure().getType() == StructureType.POWERCELL)
					icon = ChatColor.LIGHT_PURPLE + "▥";
			}
		} else if (kingdom.isEnemyWith(landKingdom)) {
			icon = ChatColor.RED + "▩";
			if (structure != null && structures) {
				if (structure.getType() == StructureType.NEXUS)
					icon = ChatColor.RED + "□";
				else if (structure.getType() == StructureType.OUTPOST)
					icon = ChatColor.RED + "▤";
				else if (structure.getType() == StructureType.POWERCELL)
					icon = ChatColor.RED + "▥";
			}
		} else {
			icon = ChatColor.GRAY + "▩";
			if (structure != null && structures) {
				if (structure.getType() == StructureType.NEXUS)
					icon = ChatColor.GRAY + "□";
				else if (structure.getType() == StructureType.OUTPOST)
					icon = ChatColor.GRAY + "▤";
				else if (structure.getType() == StructureType.POWERCELL)
					icon = ChatColor.GRAY + "▥";
			}
		}
		return icon;
	}
	
	@Override
	public void onDisable() {}

}
