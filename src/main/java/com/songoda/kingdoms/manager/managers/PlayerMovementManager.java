package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.Turret;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerMovementManager extends Manager {

	private final List<String> worlds = new ArrayList<>();
	private final FileConfiguration configuration;
	private final LandManager landManager;
	private final Kingdoms instance;
	private final boolean whitelist;
	
	public PlayerMovementManager() {
		super(true);
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.whitelist = configuration.getBoolean("worlds.list-is-whitelist", true);
		this.landManager = instance.getManager("land", LandManager.class).orElseCreate();
		List<String> temp = configuration.getStringList("worlds.list");
		if (temp == null || temp.isEmpty()) {
			worlds.add("world");
			return;
		}
		worlds.addAll(temp);
	}
	
	private boolean checkWorld(World world) {
		if (whitelist) {
			if (!worlds.contains(world.getName()))
				return false;
		} else {
			if (worlds.contains(world.getName()))
				return false;
		}
		return true;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (ExternalManager.isCitizen(player))
			return;
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			@Override
			public void run() {
				if (!player.isOnline() || !player.isValid())
					return;
				PlayerChangeChunkEvent event = new PlayerChangeChunkEvent(player, null, player.getLocation().getChunk());
				Bukkit.getPluginManager().callEvent(event);
			}
		}, 5);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (ExternalManager.isCitizen(player))
			return;
		Location from = event.getFrom();
		Location to = event.getTo();
		if (from.getChunk().equals(to.getChunk()))
			return;
		PlayerChangeChunkEvent change = new PlayerChangeChunkEvent(player, null, to.getChunk());
		Bukkit.getPluginManager().callEvent(change);
		if (change.isCancelled())
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		World world = player.getWorld();
		if (checkWorld(world))
			return;
		if (ExternalManager.isCitizen(player))
			return;
		Location from = event.getFrom();
		Location to = event.getTo();
		Chunk chunkFrom = from.getChunk();
		Chunk chunkTo = to.getChunk();
		int centerX = chunkTo.getX(), centerZ = chunkTo.getZ();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				Chunk next = world.getChunkAt(centerX + x, centerZ + z);
				Optional<Land> optional = landManager.getLandAt(next);
				if (optional.isPresent()) {
					Land land = optional.get();
					List<Turret> turrets = land.getTurrets();
					for (Turret turret : turrets) {
						//attempt to get the closest player to the turret
						Location turretLocation = turret.getLocation();
						Player closest = null;
						double closestDistance = 0.0;
						for (Player p : getNearbyPlayers(turretLocation, turret.getType().getRange())) {
							double distance = p.getLocation().distance(turretLocation);
							if (distance > closestDistance) {
								closest = p;
								closestDistance = distance;
							}
						}
						if (closest != null)
							turret.fire(closest);
					}
				}
			}
		}
		if (chunkTo != chunkFrom) {
			//Check if player is in a fightzone
			boolean invadingDeny = configuration.getBoolean("invading-deny-chunk-change", true);
			if (GameManagement.getPlayerManager().getSession(player).getFightZone() != null && invadingDeny) {
				// Direction from to to.
				Vector vector = from.toVector().subtract(to.toVector()).normalize().multiply(2);
				// This used to be teleport to player.getLocation().add(vector)
				// Changed to velocity because I think pushing them back with an animation looks better.
				player.setVelocity(vector);
				player.setFallDistance(0);
				event.setCancelled(true);
				new MessageBuilder("kingdoms.invading.invading-deny-chunk-change")
						.replace("%chunkFrom%", LocationUtils.chunkToString(chunkFrom))
						.replace("%chunkTo%", LocationUtils.chunkToString(chunkTo))
						.replace("%kingdom%", kingdom)
						.replace("%land%", kingdom)
						.replace("%player%", player.getName())
						.send(player);
				return;
			}
			PlayerChangeChunkEvent change = new PlayerChangeChunkEvent(player, chunkFrom, chunkTo);
			Bukkit.getServer().getPluginManager().callEvent(change);
			if (change.isCancelled()) {
				player.setFallDistance(0);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onChunkChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		World world = player.getWorld();
		if (checkWorld(world))
			return;
		//TODO add configuration option to ignore or not regions.
		if (ExternalManager.isInRegion(player.getLocation()))
			return;
		KingdomPlayer kingdomPlayer = GameManagement.getPlayerManager().getSession(player);
		//TODO Should not be possible to get null
		if (kingdomPlayer == null)
			return;
		Chunk chunkFrom = event.getFromChunk();
		Chunk chunkTo = event.getToChunk();
		Land landTo = landManager.getOrLoadLand(chunkTo);
		if (chunkFrom != null) {
			Land landFrom = landManager.getOrLoadLand(chunkFrom);
			UUID fromOwner = landFrom.getOwnerUUID();
			UUID toOwner = landTo.getOwnerUUID();
			if (fromOwner == null && toOwner == null)
				return;
			else if (fromOwner.equals(toOwner))
				return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				if (landTo.getOwnerUUID() == null) {
					MessageBuilder message = new MessageBuilder(false, "map.unoccupied-land.actionbar")
							.replace("%chunk%", LocationUtils.chunkToString(chunkTo))
							.replace("%world%", world.getName())
							.setPlaceholderObject(player);
					if(Config.getConfig().getBoolean("showLandEnterMessage"))
						kp.sendMessage(ChatColor.DARK_GREEN + Kingdoms.getLang().getString("Map_Unoccupied", kp.getLang()));
					return;
				}
			}
		});
		new Thread(new Runnable() {
			@Override
			public void run(){
	
			Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(landTo.getOwnerUUID());
	
			ChatColor color;
			if(kp.getKingdom() == null){
				color = ChatColor.WHITE;
			}
			else if(kp.getKingdom().equals(kingdom)){
				color = ChatColor.GREEN;
			}
			else if(kp.getKingdom().isAllianceWith(kingdom)){
				color = ChatColor.YELLOW;
			}
			else if(kp.getKingdom().isEnemyWith(kingdom)){
				color = ChatColor.RED;
			}
			else{
				color = ChatColor.WHITE;
			}
	
			String lore = "";
			String titleLore = "";
			if(kingdom.isNeutral())
				lore += ChatColor.GREEN + " (" + Kingdoms.getLang().getString("Misc_Neutral", kp.getLang()) + ")";
			if(kingdom != null && kingdom.getKingdomLore() != null){
				lore += ChatColor.WHITE + " - " + color + kingdom.getKingdomLore();
				titleLore = kingdom.getKingdomLore();
			}
	
			ExternalManager.sendTitleBar(e.getPlayer(), color + landTo.getOwner(), titleLore);
	
	
			ExternalManager.sendActionBar(e.getPlayer(), ChatColor.BOLD + "" + color + ChatColor.BOLD + landTo.getOwner());
	
			if(Config.getConfig().getBoolean("showLandEnterMessage"))
				kp.sendMessage(color + landTo.getOwner() + lore);
			}
		}).start();
	}

	private List<Player> getNearbyPlayers(Location loc, double distance){
	double distanceSquared = distance * distance;
	List<Player> list = new ArrayList<>();
	for(Player p : Bukkit.getOnlinePlayers()){
		if(!p.getWorld().equals(loc.getWorld())){
		continue;
		}
		if(p.getLocation().distanceSquared(loc) < distanceSquared){
		list.add(p);
		}
	}
	return list;
	}


	@Override
	public void onDisable(){

	}

}
