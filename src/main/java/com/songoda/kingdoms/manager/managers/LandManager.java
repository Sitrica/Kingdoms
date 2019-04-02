package com.songoda.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.events.LandClaimEvent;
import com.songoda.kingdoms.events.LandLoadEvent;
import com.songoda.kingdoms.events.LandUnclaimEvent;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.manager.managers.external.DynmapManager;
import com.songoda.kingdoms.manager.managers.external.WorldGuardManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class LandManager extends Manager {
	
	private final List<String> unclaiming = new ArrayList<>(); //TODO test if this is even required. It's a queue to avoid claiming and removing at same time.
	private final Map<Chunk, Land> lands = new HashMap<>();
	private final Set<String> forbidden = new HashSet<>();
	private Optional<WorldGuardManager> worldGuardManager;
	private Optional<CitizensManager> citizensManager;
	private Optional<DynmapManager> dynmapManager;
	private VisualizerManager visualizerManager;
	private StructureManager structureManager;
	private KingdomManager kingdomManager;
	private PlayerManager playerManager;
	private BukkitTask autoSaveThread;
	private WorldManager worldManager;
	private Database<Land> database;
	private LandManager landManager;

	public LandManager() {
		super("land", true, "serializer");
		this.forbidden.addAll(configuration.getStringList("kingdoms.forbidden-inventories"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initalize() {
		this.worldGuardManager = instance.getExternalManager("worldguard", WorldGuardManager.class);
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.visualizerManager = instance.getManager("visualizer", VisualizerManager.class);
		this.structureManager = instance.getManager("structure", StructureManager.class);
		this.dynmapManager = instance.getExternalManager("dynmap", DynmapManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(Land.class);
		else
			database = getSQLiteDatabase(Land.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, saveTask, 0, IntervalUtils.getInterval(interval) * 20);
		}
		initLands();
		if (configuration.getBoolean("taxes.enabled", false)) {
			String timeString = configuration.getString("taxes.interval", "2 hours");
			long time = IntervalUtils.getInterval(timeString) * 20;
			int amount = configuration.getInt("taxes.amount", 10);
			Bukkit.getScheduler().scheduleAsyncRepeatingTask(instance, new Runnable() {
				@Override
				public void run() {
					Kingdoms.debugMessage("Land taxes executing...");
					new MessageBuilder("taxes.take")
							.toPlayers(Bukkit.getOnlinePlayers())
							.replace("%interval%", timeString)
							.replace("%amount%", amount)
							.send();
					boolean disband = configuration.getBoolean("taxes.disband-cant-afford", false);
					for (Land land : Collections.unmodifiableMap(lands).values()) {
						OfflineKingdom kingdom = land.getKingdomOwner();
						if (kingdom != null) {
							long resourcePoints = kingdom.getResourcePoints();
							if (resourcePoints < amount && disband) {
								new MessageBuilder("taxes.disband")
										.toPlayers(Bukkit.getOnlinePlayers())
										.replace("%amount%", amount)
										.setKingdom(kingdom)
										.send();
								kingdomManager.deleteKingdom(kingdom);
								return;
							}
							kingdom.setResourcePoints(resourcePoints - amount);
							if (configuration.getBoolean("taxes.reverse", false))
								kingdom.setResourcePoints(resourcePoints + (amount * 2));
						}
					}
				}
			}, time, time);
		}
	}
	
	private final Runnable saveTask = new Runnable() {
		@Override
		public void run() {
			Kingdoms.debugMessage("Starting Land Save");
			int i = 0;
			Set<String> saved = new HashSet<>();
			for (Chunk chunk : getLoadedLand()) {
				String name = LocationUtils.chunkToString(chunk);
				if (saved.contains(name))
					continue;
				Kingdoms.debugMessage("Saving land: " + name);
				Land land = getLand(chunk);
				OfflineKingdom kingdom = land.getKingdomOwner();
				if (kingdom == null && land.getTurrets().size() <= 0 && land.getStructure() == null) {
					database.delete(name);
					saved.add(name);
					i++;
					continue;
				}
				try{
					database.save(name, land);
					saved.add(name);
					i++;
				} catch (Exception e) {
					Bukkit.getLogger().severe("[Kingdoms] Failed autosave for land at: " + name);
				}
			}
			Kingdoms.debugMessage("Saved [" + i + "] lands");
		}
	};

	private void initLands() {
		Set<String> keys = database.getKeys();
		for (String name : keys) {
			// Old data
			if (name.equals("LandData") || name.endsWith("_temp"))
				continue;
			Kingdoms.debugMessage("Loading land: " + name);
			try{
				Land land = database.get(name);
				Chunk chunk = LocationUtils.stringToChunk(name);
				if (chunk == null)
					continue;
				OfflineKingdom kingdom = land.getKingdomOwner();
				if (kingdom != null) {
					Kingdoms.consoleMessage("Land data [" + name + "] is corrupted! ignoring...");
					Kingdoms.consoleMessage("The land owner is [" + kingdom.getName() + "] but no such kingdom with the name exists");
				}
				LandLoadEvent event = new LandLoadEvent(land);
				Bukkit.getPluginManager().callEvent(new LandLoadEvent(land));
				if (!!event.isCancelled() && !lands.containsKey(chunk))
					lands.put(chunk, land);
			} catch(Exception e) {
				Kingdoms.consoleMessage("Land data [" + name + "] is corrupted! ignoring...");
				if (instance.getConfig().getBoolean("debug", true))
					e.printStackTrace();
			}
		}
		database.delete("LandData");
		Kingdoms.consoleMessage("Total of [" + getLoadedLand().size() + "] lands are initialized");
	}

	/**
	 * @return Set<Chunk> of all loaded land locations.
	 */
	public Set<Chunk> getLoadedLand() {
		return Collections.unmodifiableMap(lands).keySet();
	}
	
	public Land getLandAt(Location location) {
		return getLand(location.getChunk());
	}

	/**
	 * Load land if exist; create if not exist.
	 * 
	 * @param chunk Chunk of land to get from.
	 * @return Land even if not loaded.
	 */
	public Land getLand(Chunk chunk) {
		if (chunk == null)
			return null;
		String name = LocationUtils.chunkToString(chunk);
		Kingdoms.debugMessage("Fetching info for land: " + name);
		Land land = lands.get(chunk);
		if (land == null) {
			land = new Land(chunk);
			if (!lands.containsKey(chunk))
				lands.put(chunk, land);
		}
		return land;
	}
	
	public void playerClaimLand(KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld())) {
			new MessageBuilder("claiming.world-disabled")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("claiming.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canClaim()) {
			new MessageBuilder("kingdoms.permissions-too-low")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canClaim()), new Placeholder<Optional<Rank>>("%rank%") {
						@Override
						public String replace(Optional<Rank> rank) {
							if (rank.isPresent())
								return rank.get().getName();
							return "(Not attainable)";
						}
					})
					.setKingdom(kingdom)
					.send(player);
			return;
		}
		if (worldGuardManager.isPresent())
			if (!worldGuardManager.get().canClaim(player.getLocation())) {
				new MessageBuilder("claiming.worldguard").send(player);
				return;
			}
		Land land = getLand(player.getLocation().getChunk());
		Chunk chunk = land.getChunk();
		String chunkString = LocationUtils.chunkToString(chunk);
		if (land.getKingdomOwner() != null) {
			OfflineKingdom landKingdom = land.getKingdomOwner();
			if (landKingdom.getUniqueId().equals(kingdom.getUniqueId())) {
				new MessageBuilder("claiming.already-owned")
						.replace("%chunk%", chunkString)
						.replace("%kingdom%", landKingdom.getName())
						.send(player);
				return;
			}
			new MessageBuilder("claiming.already-claimed")
					.replace("%chunk%", LocationUtils.chunkToString(land.getChunk()))
					.replace("%kingdom%", landKingdom.getName())
					.send(player);
			return;
		}
		Set<Land> claims = kingdomPlayer.getClaims();
		int max = kingdom.getPermissions(kingdomPlayer.getRank()).getMaximumClaims();
		if (max > 0 && claims.size() >= max) {
			new MessageBuilder("claiming.max-user-claims")
					.replace("%amount%", max)
					.setKingdom(kingdom)
					.send(player);
		}
		int maxClaims = configuration.getInt("claiming.maximum-claims", -1);
		if (maxClaims > 0 && kingdom.getClaims().size() >= maxClaims) {
			new MessageBuilder("claiming.max-claims")
					.setKingdom(kingdom)
					.replace("%amount%", maxClaims)
					.send(player);
			return;
		}
		// Check if it's the Kingdoms first claim.
		if (kingdom.getClaims().isEmpty()) {
			new MessageBuilder("claiming.first-claim")
					.replace("%chunk%", chunkString)
					.setKingdom(kingdom)
					.send(player);
			if (kingdom.getSpawn() == null) {
				Location location = player.getLocation();
				kingdom.setSpawn(location);
				kingdom.setUsedFirstClaim(true);
				new MessageBuilder("commands.spawn-set")
						.replace("%location%", LocationUtils.locationToString(location))
						.setKingdom(kingdom)
						.send(player);
			}
		} else {
			if (configuration.getBoolean("claiming.land-must-be-connected", false)) {
				boolean connected = false;
				World world = player.getWorld();
				for (int x = -1; x <= 1; x++) {
					for (int z = -1; z <= 1; z++) {
						if (x == 0 && z == 0)
							continue;
						Chunk c = world.getChunkAt(chunk.getX() + x, chunk.getZ() + z);
						Land adjustment = getLand(c);
						OfflineKingdom owner = adjustment.getKingdomOwner();
						if (owner != null) {
							if(owner.getUniqueId().equals(kingdom.getUniqueId())){
								connected = true;
								break;
							}
						}
					}
				}
				if (!connected) {
					new MessageBuilder("claiming.must-be-connected")
							.setKingdom(kingdom)
							.send(player);
					return;
				}
			}
			int cost = configuration.getInt("claiming.cost", 5);
			if (!kingdomPlayer.hasAdminMode() && kingdom.getResourcePoints() < cost) {
				new MessageBuilder("claiming.need-resourcepoints")
						.replace("%needed%", cost - kingdom.getResourcePoints())
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
				return;
			} else if (kingdomPlayer.hasAdminMode())
				new MessageBuilder("claiming.admin-claim")
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
			else {
				kingdom.setResourcePoints(kingdom.getResourcePoints() - cost);
				new MessageBuilder("claiming.success")
						.replace("%cost%", cost)
						.setKingdom(kingdom)
						.send(player);
			}
		}
		claimLand(kingdom, chunk);
		visualizerManager.visualizeLand(kingdomPlayer, chunk);
	}

	/**
	 * Claim a new land. This does not check if chunk is already occupied.
	 * 
	 * @param chunk Chunk location
	 * @param kingdom Kingdom owner
	 */
	public void claimLand(Kingdom kingdom, Chunk... chunks) {
		for (Chunk chunk : chunks) {
			Land land = getLand(chunk);
			LandClaimEvent event = new LandClaimEvent(land, kingdom);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				continue;
			kingdom.addClaim(land);
			land.setClaimTime(new Date().getTime());
			land.setKingdomOwner(kingdom);
			String name = LocationUtils.chunkToString(land.getChunk());
			database.save(name, land);
			if (dynmapManager.isPresent())
				dynmapManager.get().update(chunk);
		}
	}

	/**
	 * Unclaim the land. This does not check if chunk is occupied.
	 * 
	 * @param chunk Chunk to unclaim
	 * @param kingdom Kingdom whom is unclaiming.
	 */
	public void unclaimLand(OfflineKingdom kingdom, Chunk... chunks) {
		for (Chunk chunk : chunks) {
			Land land = getLand(chunk);
			if (land.getKingdomOwner() == null) {
				continue;
			}
			if (land.getKingdomOwner().equals(kingdom)) {
				LandUnclaimEvent event = new LandUnclaimEvent(land, kingdom);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled())
					continue;
				kingdom.removeClaim(land);
				land.setClaimTime(0L);
				land.setKingdomOwner(null);
				String name = LocationUtils.chunkToString(land.getChunk());
				database.save(name, null);
				if (land.getStructure() != null) {
					// Sync back to server.
					Bukkit.getScheduler().runTask(instance, new Runnable() {
						@Override
						public void run() {
							structureManager.breakStructureAt(land);
						}
					});
				}
				if (dynmapManager.isPresent())
					dynmapManager.get().update(chunk);
			}
		}
	}

	/**
	 * Unclaims ALL existing land in database
	 * Use at own risk.
	 */
	public void unclaimAllExistingLand() {
		kingdomManager.getKingdoms().forEach(kingdom -> unclaimAllLand(kingdom));
	}
	
	/**
	 * Unclaim all lands thatbelong to kingdom.
	 * 
	 * @param kingdom Kingdom owner
	 * @return number of lands unclaimed
	 */
	public int unclaimAllLand(OfflineKingdom kingdom) {
		String name = kingdom.getName();
		if (unclaiming.contains(name))
			return -1;
		unclaiming.add(name);
		Stream<Land> stream = getLoadedLand().parallelStream()
				.map(chunk -> getLand(chunk))
				.filter(land -> land.getKingdomOwner() != null)
				.filter(land -> land.getKingdomOwner().equals(kingdom));
		long count = stream.count();
		kingdom.getMembers().forEach(player -> player.onKingdomLeave());
		stream.forEach(land -> unclaimLand(kingdom, land.getChunk()));
		unclaiming.remove(name);
		return (int)count;
	}
	
	public boolean isConnectedToNexus(Land land) {
		OfflineKingdom offlineKingdom = land.getKingdomOwner();
		if (offlineKingdom == null)
			return false;
		Kingdom kingdom = offlineKingdom.getKingdom();
		Location nexusLocation = kingdom.getNexusLocation();
		if (nexusLocation == null)
			return false;
		Land nexus = getLand(nexusLocation.getChunk());
		return getAllConnectingLand(nexus).contains(land);
	}

	/**
	 * Unclaim all lands not connected to the kingdom, and with no structures. TODO It checks for only structures though...
	 * 
	 * @param kingdom Kingdom owner
	 * @return number of lands unclaimed
	 */
	public int unclaimDisconnectedLand(Kingdom kingdom) {
		String name = kingdom.getName();
		if (unclaiming.contains(name))
			return -1;
		unclaiming.add(name);
		Set<Land> connected = new HashSet<>();
		getLoadedLand().parallelStream()
				.map(chunk -> getLand(chunk))
				.filter(land -> land.getStructure() != null)
				.filter(land -> land.getKingdomOwner() != null)
				.filter(land -> land.getKingdomOwner().getUniqueId().equals(kingdom.getUniqueId()))
				.forEach(land -> connected.addAll(getAllConnectingLand(land)));
		Stream<Land> stream = getLoadedLand().parallelStream()
				.map(chunk -> getLand(chunk))
				.filter(land -> land.getKingdomOwner() != null)
				.filter(land -> land.getKingdomOwner().getUniqueId().equals(kingdom.getUniqueId()))
				.filter(land -> !connected.contains(land));
		long count = stream.count();
		stream.forEach(land -> unclaimLand(kingdom, land.getChunk()));
		unclaiming.remove(name);
		return (int)count;
	}
	
	public Set<Land> getConnectingLand(Land center, Collection<Land> checked) {
		return center.getSurrounding().parallelStream()
				.filter(land -> land.getKingdomOwner() != null)
				.filter(land -> land.getKingdomOwner().getUniqueId().equals(center.getKingdomOwner().getUniqueId()))
				.collect(Collectors.toSet());
	}
	
	public Set<Land> getOutwardLands(Collection<Land> surroundings, Collection<Land> checked) {
		Set<Land> connected = new HashSet<>();
		for (Land land : surroundings) {
			for (Land furtherSurroundings : land.getSurrounding()) {
				if (surroundings.contains(furtherSurroundings))
					continue;
				if (checked.contains(furtherSurroundings))
					continue;
				if (connected.contains(furtherSurroundings))
					continue;
				connected.add(furtherSurroundings);
			}
		}
		return connected;
	}

	public Set<Land> getAllConnectingLand(Land center) {
		Set<Land> connected = new HashSet<>();
		Set<Land> checked = new HashSet<>();
		connected.add(center);
		Set<Land> outwards = getOutwardLands(getConnectingLand(center, checked), checked);
		boolean check = true;
		while (check) {
			check = false;
			Set<Land> newOutwards = new HashSet<>();
			for (Land land : outwards) {
				Kingdoms.debugMessage("Checking Claim: " + LocationUtils.chunkToString(land.getChunk()));
				if (checked.contains(land))
					continue;
				checked.add(land);
				if (land.getKingdomOwner() == null)
					continue;
				if (!land.getKingdomOwner().getUniqueId().equals(center.getKingdomOwner().getUniqueId()))
					continue;
				connected.add(land);
				newOutwards.add(land);
				check = true;
			}
			outwards = getOutwardLands(newOutwards, checked);
		}
		return connected;
	}
	
	private boolean isForbidden(Material material) {
		boolean contains = configuration.getBoolean("kingdoms.forbidden-contains", true);
		for (String name : forbidden) {
			if (contains) {
				if (material.name().endsWith(name)) {
					return true;
				}
			} else {
				Material attempt = Utils.materialAttempt(material.name(), "LEGACY_" + material);
				if (attempt != null && attempt == material) {
					return true;
				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onKingdomInteract(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block block = event.getClickedBlock();
		if (block == null)
			return;
		if (configuration.getBoolean("kingdoms.open-other-kingdom-inventories", false))
			return;
		Player player = event.getPlayer();
		// Testing if the player is eating at a block.
		if (player.isSneaking() && !isForbidden(block.getType())) {
			ItemStack item;
			try {
				item = player.getItemInHand();
			} catch (Exception e) {
				item = player.getInventory().getItemInMainHand();
			}
			// When the deprecated getItemInHand() method gets removed use this instead of this try and catch.
			//ItemStack item = player.getInventory().getItemInMainHand();
			if (item == null)
				return;
			if (item.getType() != Material.ARMOR_STAND && item.getType() != Material.ITEM_FRAME) {
				return;
			}
		}
		Location location = block.getLocation();
		Land land = getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (kingdomPlayer.hasAdminMode())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-interact-land-no-kingdom").send(player);
		} else {
			if (!kingdom.getUniqueId().equals(landKingdom.getUniqueId())) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.cannot-interact-land")
						.replace("%playerkingdom%", kingdom.getName())
						.setKingdom(landKingdom)
						.send(player);
				return;
			}
			if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuild()) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.rank-too-low-build")
						.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuild()), new Placeholder<Optional<Rank>>("%rank%") {
							@Override
							public String replace(Optional<Rank> rank) {
								if (rank.isPresent())
									return rank.get().getName();
								return "(Not attainable)";
							}
						})
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			Structure structure = land.getStructure();
			if (structure != null && structure.getType() == StructureType.NEXUS) {
				if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildInNexus()) {
					event.setCancelled(true);
					new MessageBuilder("kingdoms.rank-too-low-nexus-build")
							.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildInNexus()), new Placeholder<Optional<Rank>>("%rank%") {
								@Override
								public String replace(Optional<Rank> rank) {
									if (rank.isPresent())
										return rank.get().getName();
									return "(Not attainable)";
								}
							})
							.setKingdom(kingdom)
							.send(player);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractAtEntityEvent event) {
		Location location = event.getRightClicked().getLocation();
		Land land = getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (kingdomPlayer.hasAdminMode())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-interact-land-no-kingdom").send(player);
		} else {
			if (!kingdom.getUniqueId().equals(landKingdom.getUniqueId())) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.cannot-interact-land")
						.replace("%playerkingdom%", kingdom.getName())
						.setKingdom(landKingdom)
						.send(player);
				return;
			}
			if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuild()) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.rank-too-low-build")
						.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canClaim()), new Placeholder<Optional<Rank>>("%rank%") {
							@Override
							public String replace(Optional<Rank> rank) {
								if (rank.isPresent())
									return rank.get().getName();
								return "(Not attainable)";
							}
						})
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			Structure structure = land.getStructure();
			if (structure != null && structure.getType() == StructureType.NEXUS) {
				if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildInNexus()) {
					event.setCancelled(true);
					new MessageBuilder("kingdoms.rank-too-low-nexus-build")
							.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildInNexus()), new Placeholder<Optional<Rank>>("%rank%") {
								@Override
								public String replace(Optional<Rank> rank) {
									if (rank.isPresent())
										return rank.get().getName();
									return "(Not attainable)";
								}
							})
							.setKingdom(kingdom)
							.send(player);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
				return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (kingdomPlayer.isAutoClaiming()){
			Bukkit.getScheduler().scheduleSyncDelayedTask(Kingdoms.getInstance(), new Runnable() {
				public void run() {
					playerClaimLand(kingdomPlayer);
				}
			}, 1L);
		}
	}

	@EventHandler
	public void onBucketOnUnoccupied(PlayerBucketEvent event) {
		Block block = event.getBlockClicked();
		World world = block.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		if (!worldManager.canBuildInUnoccupied(world))
			return;
		Location location = block.getRelative(event.getBlockFace()).getLocation();
		Land land = getLand(location.getChunk());
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		OfflineKingdom kingdom = land.getKingdomOwner();
		if (kingdom == null && !kingdomPlayer.hasAdminMode()) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-build-unoccupied-land")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send(kingdomPlayer);
		}
	}

	@EventHandler
	public void onBreakUnoccupied(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Block block = event.getBlock();
		World world = block.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		if (!worldManager.canBuildInUnoccupied(world))
			return;
		Location location = block.getLocation();
		Land land = getLand(location.getChunk());
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		OfflineKingdom kingdom = land.getKingdomOwner();
		if (kingdom == null && !kingdomPlayer.hasAdminMode()) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-build-unoccupied-land")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send(kingdomPlayer);
		}
	}

	@EventHandler
	public void onBuildUnoccupied(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		Block block = event.getBlock();
		World world = block.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		if (!worldManager.canBuildInUnoccupied(world))
			return;
		Location location = block.getLocation();
		Land land = getLand(location.getChunk());
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		OfflineKingdom kingdom = land.getKingdomOwner();
		if (kingdom == null && !kingdomPlayer.hasAdminMode()) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-build-unoccupied-land")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send(kingdomPlayer);
		}
	}


	@EventHandler
	public void onBreakArmorStandOrFrame(EntityDamageByEntityEvent event) {
		if (event.isCancelled())
			return;
		Entity entity = event.getEntity();
		World world = entity.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		EntityType type = entity.getType();
		if (type != EntityType.ARMOR_STAND && type != EntityType.ITEM_FRAME)
			return;
		if (!(event.getDamager() instanceof Player))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer((Player)event.getDamager());
		if (kingdomPlayer.hasAdminMode())
			return;
		Location location = entity.getLocation();
		Land land = getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null){
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-interact-land-no-kingdom").send(kingdomPlayer);
		} else {
			if (!kingdom.getUniqueId().equals(landKingdom.getUniqueId())) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.cannot-interact-land")
						.replace("%playerkingdom%", kingdom.getName())
						.setKingdom(landKingdom)
						.send(kingdomPlayer);
				return;
			}
			Structure structure = land.getStructure();
			if (structure != null && structure.getType() == StructureType.NEXUS) {
				if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildInNexus()) {
					event.setCancelled(true);
					new MessageBuilder("kingdoms.rank-too-low-nexus-build")
							.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildInNexus()), new Placeholder<Optional<Rank>>("%rank%") {
								@Override
								public String replace(Optional<Rank> rank) {
									if (rank.isPresent())
										return rank.get().getName();
									return "(Not attainable)";
								}
							})
							.setKingdom(kingdom)
							.send(kingdomPlayer);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onBreakInOtherKingdom(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Block block = event.getBlock();
		World world = block.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		Location location = block.getLocation();
		Land land = getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		if (kingdomPlayer.hasAdminMode())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-interact-land-no-kingdom").send(kingdomPlayer);
		} else {
			if (!kingdom.getUniqueId().equals(landKingdom.getUniqueId())) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.cannot-interact-land")
						.replace("%playerkingdom%", kingdom.getName())
						.setKingdom(landKingdom)
						.send(kingdomPlayer);
				return;
			}
			Structure structure = land.getStructure();
			if (structure != null && structure.getType() == StructureType.NEXUS) {
				if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildInNexus()) {
					event.setCancelled(true);
					new MessageBuilder("kingdoms.rank-too-low-nexus-build")
							.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildInNexus()), new Placeholder<Optional<Rank>>("%rank%") {
								@Override
								public String replace(Optional<Rank> rank) {
									if (rank.isPresent())
										return rank.get().getName();
									return "(Not attainable)";
								}
							})
							.setKingdom(kingdom)
							.send(kingdomPlayer);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onBucketInOtherKingdom(PlayerBucketEvent event) {
		Location location = event.getBlockClicked().getLocation();
		Land land = getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		if (kingdomPlayer.hasAdminMode())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-interact-land-no-kingdom").send(kingdomPlayer);
		} else {
			if (!kingdom.getUniqueId().equals(landKingdom.getUniqueId())) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.cannot-interact-land")
						.replace("%playerkingdom%", kingdom.getName())
						.setKingdom(landKingdom)
						.send(kingdomPlayer);
				return;
			}
		}
	}

	@EventHandler
	public void onPlaceInOtherKingdom(BlockPlaceEvent event) {
		Location location = event.getBlock().getLocation();
		Land land = landManager.getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		if (kingdomPlayer.hasAdminMode())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			event.setCancelled(true);
			new MessageBuilder("kingdoms.cannot-interact-land-no-kingdom").send(kingdomPlayer);
		} else {
			if (!kingdom.getUniqueId().equals(landKingdom.getUniqueId())) {
				event.setCancelled(true);
				new MessageBuilder("kingdoms.cannot-interact-land")
						.replace("%playerkingdom%", kingdom.getName())
						.setKingdom(landKingdom)
						.send(kingdomPlayer);
				return;
			}
			Structure structure = land.getStructure();
			if (structure != null && structure.getType() == StructureType.NEXUS) {
				if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildInNexus()) {
					event.setCancelled(true);
					new MessageBuilder("kingdoms.rank-too-low-nexus-build")
							.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildInNexus()), new Placeholder<Optional<Rank>>("%rank%") {
								@Override
								public String replace(Optional<Rank> rank) {
									if (rank.isPresent())
										return rank.get().getName();
									return "(Not attainable)";
								}
							})
							.setKingdom(kingdom)
							.send(kingdomPlayer);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onFlowIntoKingdom(BlockFromToEvent event) {
		if (!configuration.getBoolean("kingdoms.disable-liquid-flow-into", false))
			return;
		OfflineKingdom from = getLand(event.getBlock().getLocation().getChunk()).getKingdomOwner();
		OfflineKingdom to = getLand(event.getToBlock().getLocation().getChunk()).getKingdomOwner();
		if (from.getUniqueId() == null && to.getUniqueId() != null) {
			event.setCancelled(true);
		} else if (!from.getUniqueId().equals(to.getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onDisable() {
		if (autoSaveThread != null)
			autoSaveThread.cancel();
		Kingdoms.debugMessage("Saving lands to database...");
		try {
			saveTask.run();
		} catch (Exception e) {
			Kingdoms.consoleMessage("MySQL connection failed! Saving to file database");
			database = getMySQLDatabase(Land.class);
			saveTask.run();
		}
		lands.clear();
	}

}
