package com.songoda.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.LandManager.ChunkDaddy;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.objects.Pair;
import com.songoda.kingdoms.objects.Relation;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.maps.ActionInfo;
import com.songoda.kingdoms.objects.maps.MapElement;
import com.songoda.kingdoms.objects.maps.RelationOptions.RelationAction;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MapManager extends Manager {

	private final AsyncLoadingCache<ChunkDaddy, Optional<Land>> cache = Caffeine.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.buildAsync(new CacheLoader<ChunkDaddy, Optional<Land>>() {
					public Optional<Land> load(ChunkDaddy daddy) {
						if (daddy == null)
							return null;
						return instance.getManager(LandManager.class).getLand(daddy);
					}
			});
	private final FileConfiguration map;

	public MapManager() {
		super(true);
		this.map = instance.getConfiguration("map").get();
	}

	public void displayMap(KingdomPlayer kingdomPlayer, boolean structures) {

		// Setup values
		int height = map.getInt("configure.height", 8);
		int width = map.getInt("configure.width", 24);
		Player player = kingdomPlayer.getPlayer();
		Chunk start = player.getLocation().getChunk();
		String world = start.getWorld().getName();
		int originX = start.getX();
		int originZ = start.getZ();
		int x = originX - (height / 2);
		int z = originZ - (width / 2);

		// Grab all lands and cache their request.
		List<ChunkDaddy> daddies = new ArrayList<>();
		for (int vertical = 0; vertical < height; vertical++)
			// Need to be less than or equal.
			for (int horizontal = 0; horizontal <= width; horizontal++)
				daddies.add(new ChunkDaddy(x + vertical, z + horizontal, world));
		List<Pair<ChunkDaddy, Optional<Land>>> lands = daddies.parallelStream()
				.map(daddy -> {
					try {
						return Pair.of(daddy, cache.get(daddy).get(10, TimeUnit.SECONDS));
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						Kingdoms.debugMessage("Skipping chunk daddy " + daddy.getX() + ", " + daddy.getZ());
						return null;
					}
				})
				.filter(pair -> pair != null)
				.collect(Collectors.toList());

		// Define the spots to display in chat.
		int vertical = 0, horizontal = 0;
		Table<Integer, Integer, Pair<ChunkDaddy, Optional<Land>>> table = HashBasedTable.create();
		for (Pair<ChunkDaddy, Optional<Land>> pair : lands) {
			table.put(vertical, horizontal, pair);
			horizontal = horizontal + 1;
			if (horizontal > width) {
				vertical = vertical + 1;
				horizontal = 0;
			}
		}

		// Grab the element icons of the map
		List<TextComponent> rows = new ArrayList<>();
		player.sendMessage("");
		for (int row = 0; row < table.rowKeySet().size(); row++) {
			TextComponent rowComponent = new TextComponent();
			for (Pair<ChunkDaddy, Optional<Land>> pair : table.row(row).values()) {
				Optional<Land> landOptional = pair.getSecond();
				MapElement element = MapElement.LAND;
				ChunkDaddy daddy = pair.getFirst();
				if (daddy.getX() == originX && daddy.getZ() == originZ)
					element = MapElement.YOU;
				TextComponent component = new TextComponent();
				Relation relation = Relation.NEUTRAL;
				Optional<OfflineKingdom> placeholderKingdom = Optional.empty();
				String chunk = LocationUtils.daddyToString(daddy);
				Optional<LandInfo> actionInfo = Optional.empty();
				if (!landOptional.isPresent() || element == MapElement.YOU) {
					MessageBuilder builder = element.getIcon()
							.setKingdom(kingdomPlayer.getKingdom() != null ? kingdomPlayer.getKingdom() : null)
							.setPlaceholderObject(kingdomPlayer)
							.replace("%chunk%", chunk)
							.replace("%player%", player.getName());
					component = new TextComponent(element.getNoRelationColor() + builder.get());
				} else {
					Land land = landOptional.get();
					element = getElement(kingdomPlayer, land, structures);
					relation = Relation.getRelation(land, kingdomPlayer.getKingdom());				
					Optional<OfflineKingdom> landKingdom = land.getKingdomOwner();
					placeholderKingdom = landKingdom;
					MessageBuilder builder = element.getIcon(relation)
							.setKingdom(landKingdom.isPresent() ? landKingdom.get(): null)
							.replace("%player%", player.getName())
							.replace("%chunk%", chunk);
					component = new TextComponent(element.getRelationColor(relation) + builder.get());
					actionInfo = Optional.of(land.toInfo());
				}
				Optional<ListMessageBuilder> hover = element.getHover(relation);
				if (hover.isPresent()) {
					TextComponent hoverComponent = new TextComponent();
					List<String> list = hover.get()
							.setKingdom(placeholderKingdom.isPresent() ? placeholderKingdom.get(): null)
							.replace("%player%", player.getName())
							.replace("%chunk%", chunk)
							.get();
					for (int i = 0; i < list.size(); i++)
						if (i == list.size() - 1)
							hoverComponent.addExtra(list.get(i));
						else
							hoverComponent.addExtra(list.get(i) + "\n");
					component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] {hoverComponent}));
				}
				Optional<RelationAction> action = element.getRelationAction(relation);
				if (action.isPresent() && actionInfo.isPresent()) {
					instance.getActions().add(kingdomPlayer, new ActionInfo(actionInfo.get(), player.getUniqueId(), action.get()));
					ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kingdomsaction");
					component.setClickEvent(clickEvent);
				}
				rowComponent.addExtra(component);
			}
			rows.add(rowComponent);
		}

		// Send map in chat.
		rows.forEach(component -> player.spigot().sendMessage(component));
		player.sendMessage("");
		//TODO legend.
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
