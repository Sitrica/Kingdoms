package me.limeglass.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.manager.Manager;
import me.limeglass.kingdoms.objects.Relation;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.maps.ActionInfo;
import me.limeglass.kingdoms.objects.maps.MapElement;
import me.limeglass.kingdoms.objects.maps.RelationOptions.RelationAction;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.utils.ListMessageBuilder;
import me.limeglass.kingdoms.utils.LocationUtils;
import me.limeglass.kingdoms.utils.MessageBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MapManager extends Manager {

	private final FileConfiguration map;

	public MapManager() {
		super(true);
		this.map = instance.getConfiguration("map").get();
	}

	public void displayMap(KingdomPlayer kingdomPlayer, boolean structures) {
//		Map<Integer, Integer> calculate = new HashMap<>();
//		for (int vertical = 0; vertical < map.getInt("configure.height", 8); vertical++) {
//			int x = vertical - 4;
//			for (int horizontal = 0; horizontal <= map.getInt("configure.width", 24); horizontal++) {
//				int z = horizontal - 12;
//				Chunk entry = chunk.getWorld().getChunkAt(originX + x, originZ + z);
//				Land land = landManager.getLand(entry);
//			}
//		}
		Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
			List<TextComponent> rows = new ArrayList<>();
			Player player = kingdomPlayer.getPlayer();
			Chunk chunk = kingdomPlayer.getChunkAt();
			int originX = chunk.getX();
			int originZ = chunk.getZ();
			LandManager landManager = instance.getManager(LandManager.class);
			player.sendMessage("");
			for (int vertical = 0; vertical < map.getInt("configure.height", 8); vertical++) {
				int x = vertical - 4;
				TextComponent row = new TextComponent();
				for (int horizontal = 0; horizontal <= map.getInt("configure.width", 24); horizontal++) {
					int z = horizontal - 12;
					// Get land.
					Chunk entry = chunk.getWorld().getChunkAt(originX + x, originZ + z);
					Land land = landManager.getLand(entry);
					// Get MapElement.
					MapElement element = getElement(kingdomPlayer, land, structures);
					if (x == 0 && z == 0)
						element = MapElement.YOU;
					Relation relation = Relation.getRelation(land, kingdomPlayer.getKingdom());
					Optional<OfflineKingdom> landKingdom = land.getKingdomOwner();
					// Get Icon.
					MessageBuilder builder = element.getIcon(relation)
							.replace("%chunk%", LocationUtils.chunkToString(land.getChunk()))
							.setKingdom(landKingdom.isPresent() ? landKingdom.get(): null)
							.replace("%player%", player.getName());
					TextComponent component = new TextComponent(builder.get());
					// Get hover.
					Optional<ListMessageBuilder> hover = element.getHover(relation);
					if (hover.isPresent())
						component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.get()
								.replace("%chunk%", LocationUtils.chunkToString(land.getChunk()))
								.setKingdom(landKingdom.isPresent() ? landKingdom.get(): null)
								.replace("%player%", player.getName())
								.get()
								.stream()
								.map(text -> new TextComponent(text))
								.toArray(TextComponent[]::new)));
					// Get ClickEvent
					Optional<RelationAction> action = element.getRelationAction(relation);
					if (action.isPresent()) {
						instance.getActions().add(kingdomPlayer, new ActionInfo(land.toInfo(), player.getUniqueId(), action.get()));
						ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kingdomsaction");
						component.setClickEvent(clickEvent);
					}
					// Add as a row.
					row.addExtra(component);
				}
				rows.add(row);
			}
			rows.forEach(component -> player.spigot().sendMessage(component));
			player.sendMessage("");
			//TODO legend.
		});
	}

	private MapElement getElement(KingdomPlayer kingdomPlayer, Land land, boolean structures) {
		Structure structure = land.getStructure();
		if (!structures || structure == null)
			return MapElement.LAND;
		return MapElement.fromStructure(structure.getType());
	}

//	public void displayMapOld(Player player, boolean revealStructures) {
//		Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
//			List<String> compass = AsciiCompass.getAsciiCompass(AsciiCompass.getCardinalDirection(player), ChatColor.YELLOW, ChatColor.AQUA + "");
//			String[] row = {"", "", "", "", "", "", "", ""};
//			String compass1 = compass.get(0);
//			String compass2 = compass.get(1);
//			String compass3 = compass.get(2);
//			String cck = ChatColor.AQUA + "Unoccupied";
//			KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
//			Kingdom kingdom = kingdomPlayer.getKingdom();
//			Chunk chunk = kingdomPlayer.getChunkAt();
//			Land land = landManager.getLand(chunk);
//			Optional<OfflineKingdom> optional = land.getKingdomOwner();
//			if (optional.isPresent() && kingdom != null) {
//				OfflineKingdom landKingdom = optional.get();
//				if (kingdom.isAllianceWith(kingdom))
//					cck = ChatColor.LIGHT_PURPLE + kingdom.getName();
//				else if (landKingdom.equals(kingdom))
//					cck = ChatColor.GREEN + kingdom.getName();
//				else if (kingdom.isEnemyWith(kingdom))
//					cck = ChatColor.RED + kingdom.getName();
//				else
//					cck = ChatColor.GRAY + kingdom.getName();
//			}
//			int originX = chunk.getX();
//			int originZ = chunk.getZ();
//			player.sendMessage("");
//			String youIcon = new MessageBuilder(false, "map.icons.you").get();
//			for (int xc = 0; xc < 8; xc++) {
//				int x = xc - 4;
//				for (int zc = 0; zc <= 24; zc++) {
//					int z = zc - 12;
//					Chunk schunk = chunk.getWorld().getChunkAt(originX + x, originZ + z);
//					String schunkcolor = mapIdentifyChunk(schunk, kingdomPlayer, revealStructures);
//					if (x == 0 && z == 0)
//						schunkcolor = ChatColor.WHITE + youIcon;
//					row[xc] += schunkcolor;
//					if (xc == 0 && zc == 24)
//						row[xc] = row[xc] + ChatColor.LIGHT_PURPLE + new MessageBuilder(false, "map.key-header").setPlaceholderObject(kingdomPlayer);
//					if (xc == 1 && zc == 24)
//						row[xc] = row[xc] + ChatColor.LIGHT_PURPLE + "   " + youIcon + " = " + new MessageBuilder(false, "map.you").setPlaceholderObject(kingdomPlayer)
//						+ "				 " + StructureType.NEXUS.getMapIcon() + " = " + new MessageBuilder(false, "map.nexus").setPlaceholderObject(kingdomPlayer);
//					if (xc == 2 && zc == 24)
//						row[xc] = row[xc] + ChatColor.LIGHT_PURPLE + "   " + StructureType.POWERCELL.getMapIcon() + " = " + new MessageBuilder(false, "map.powercell").setPlaceholderObject(kingdomPlayer).get()
//						+ " " + StructureType.OUTPOST.getMapIcon() + " = " + new MessageBuilder(false, "map.outpost").setPlaceholderObject(kingdomPlayer).get();
//					if (xc == 3 && zc == 24)
//						row[xc] = row[xc] + "   " + ChatColor.WHITE + compass1;
//					if (xc == 4 && zc == 24)
//						row[xc] = row[xc] + "   " + ChatColor.WHITE + compass2 + ChatColor.AQUA + "	  [" + cck + ChatColor.AQUA + "]";
//					if (xc == 5 && zc == 24)
//						row[xc] = row[xc] + "   " + ChatColor.WHITE + compass3;
//					if (xc == 6 && zc == 24)
//						row[xc] = row[xc] + "   " + ChatColor.GREEN
//								+ new MessageBuilder(false, "map.kingdom").setPlaceholderObject(kingdomPlayer).get() + "   "
//								+ ChatColor.WHITE + new MessageBuilder(false, "map.you").setPlaceholderObject(kingdomPlayer).get();
//					if (xc == 7 && zc == 24)
//						row[xc] = row[xc] + "   " + ChatColor.RED + new MessageBuilder(false, "map.enemies").setPlaceholderObject(kingdomPlayer).get()
//								+ "   " + ChatColor.AQUA + new MessageBuilder(false, "map.unoccupied").setPlaceholderObject(kingdomPlayer).get();
//					if (xc == 8 && zc == 24)
//						row[xc] = row[xc] + "   " + ChatColor.LIGHT_PURPLE + new MessageBuilder(false, "map.alliance").setPlaceholderObject(kingdomPlayer).get()
//								+ "   " + ChatColor.GRAY + new MessageBuilder(false, "map.unidentified").setPlaceholderObject(kingdomPlayer).get();
//				}
//				player.sendMessage(row[xc]);
//			}
//			player.sendMessage("");
//		});
//	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
