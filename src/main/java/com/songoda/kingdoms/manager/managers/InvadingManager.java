package com.songoda.kingdoms.manager.managers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.events.DefenderKnockbackEvent;
import com.songoda.kingdoms.events.InvadingStopEvent;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.Defender;
import com.songoda.kingdoms.objects.invasions.CommandTrigger;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic;
import com.songoda.kingdoms.objects.invasions.InvasionTrigger;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic.StopReason;
import com.songoda.kingdoms.objects.invasions.mechanics.DefaultInvasion;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.SoundPlayer;
import com.songoda.kingdoms.utils.Utils;

public class InvadingManager extends Manager {

	private InvasionMechanic<? extends InvasionTrigger> mechanic;
	private final Set<Invasion> invasions = new HashSet<>();
	private Optional<CitizensManager> citizensManager;
	private final Random random = new Random();

	public InvadingManager() {
		super(true);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void initalize() {
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		String name = instance.getConfig().getString("invasions.mechanics.type", "default");
		List<Class<InvasionMechanic>> classes = Utils.getClassesOf(instance, instance.getPackageName() + ".objects.invasions.mechanics", InvasionMechanic.class);
		for (Class<InvasionMechanic> clazz : classes) {
			InvasionMechanic<?> mechanic;
			try {
				mechanic = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				continue;
			}
			if (mechanic == null || !mechanic.initialize(instance))
				continue;
			if (!Arrays.stream(mechanic.getNames()).anyMatch(type -> name.equalsIgnoreCase(type)))
				continue;
			this.mechanic = mechanic;
		}
		if (mechanic == null)
			mechanic = new DefaultInvasion();
		long interval = IntervalUtils.getInterval(configuration.getString("invading.mechanics.heartbeat", "1 minute"));
		instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, () -> {
			invasions.parallelStream()
					.filter(invasion -> !mechanic.update(invasion))
					.forEach(invasion -> stopInvasion(StopReason.TIMEOUT, invasion));
		}, 0, interval);
	}

	public boolean hasInvadedAllLands(Invasion invasion) {
		return invasion.getTarget().getClaims().isEmpty();
	}

	/**
	 * @return Set<Invasion> All the current invasions happening.
	 */
	public Set<Invasion> getInvasions() {
		return invasions;
	}

	/**
	 * @return The current InvasionMechanic that the configuration defines.
	 */
	public InvasionMechanic<? extends InvasionTrigger> getInvasionMechanic() {
		return mechanic;
	}

	/**
	 * Checks if there is an invasion at a land, and then return the invasion at it.
	 * 
	 * @param land The land to check.
	 * @return Optional<Invasion> if an invasion is present.
	 */
	public Set<Invasion> getInvasionAt(Land land) {
		return invasions.parallelStream()
				.filter(invasion -> invasion.getInvadingLands().stream().anyMatch(info -> info.equals(land.toInfo())))
				.collect(Collectors.toSet());
	}

	/**
	 * Grab all invasions of the KingdomPlayer's target Kingdom and that of the KingdomPlayer.
	 * 
	 * @param kingdomPlayer The KingdomPlayer to search for invasions on.
	 * @return Set<Invasion> all invasions found connected to the searched KingdomPlayer.
	 */
	public Set<Invasion> getAllInvasions(KingdomPlayer kingdomPlayer) {
		Set<Invasion> found = Sets.newHashSet(getTargetInvasionsOn(kingdomPlayer));
		found.addAll(getInvasionsOn(kingdomPlayer));
		return found;
	}

	/**
	 * Grab all invasions of the KingdomPlayer's target Kingdom.
	 * 
	 * @param kingdomPlayer The KingdomPlayer to search for invasions on.
	 * @return Set<Invasion> all invasions on the KingdomPlayer's Kingdom target Kingdom.
	 */
	public Set<Invasion> getTargetInvasionsOn(KingdomPlayer kingdomPlayer) {
		Set<Invasion> found = new HashSet<>();
		getInvasionsOn(kingdomPlayer).parallelStream()
				.map(invasion -> invasion.getTarget())
				.forEach(target -> found.addAll(getInvasionsOn(target)));
		return found;
	}

	/**
	 * Grab all invasions on a KingdomPlayer's Kingdom.
	 * 
	 * @param kingdomPlayer The KingdomPlayer to search for invasions on.
	 * @return Set<Invasion> all invasions on the target KingdomPlayer's Kingdom.
	 */
	public Set<Invasion> getInvasionsOn(KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return Sets.newHashSet();
		return getInvasionsOn(kingdom);
	}

	/**
	 * Grab all invasions on a OfflineKingdom.
	 * 
	 * @param target The OfflineKingdom to search for invasions on.
	 * @return Set<Invasion> all invasions on the target OfflineKingdom.
	 */
	public Set<Invasion> getInvasionsOn(OfflineKingdom target) {
		return invasions.parallelStream()
				.filter(invasion -> invasion.getTarget().equals(target))
				.collect(Collectors.toSet());
	}

	public boolean isDefender(Entity entity) {
		return mechanic.isDefender(entity);
	}

	/**
	 * Start an invasion between OfflineKingdom and Instigator.
	 *
	 * @param land The starting Land the command was called.
	 * @param challenger KingdomPlayer who challenges the defender
	 * @return Entity instance of defender.
	 */
	public void startInvasion(Land land, OfflineKingdom kingdom, KingdomPlayer instigator) {
		Invasion invasion = new Invasion(instigator, kingdom);
		if (!mechanic.start(land, invasion))
			return;
		Player player = instigator.getPlayer();
		player.setGameMode(GameMode.SURVIVAL);
		invasions.add(invasion);
		mechanic.callInvade(new CommandTrigger(invasion, land.toInfo(), instigator), instigator);
	}

	public void stopInvasion(StopReason reason, Invasion invasion) {
		mechanic.stopInvasion(reason, invasion);
		stopInvasion(reason, invasion.getTarget());
	}

	/**
	 * Stops an invasion on the target OfflineKingdom.
	 *
	 * @param target The target OfflineKingdom to have all invasions stop .
	 */
	public void stopInvasion(StopReason reason, OfflineKingdom target) {
		Iterator<Invasion> iterator = invasions.iterator();
		while (iterator.hasNext()) {
			Invasion invasion = iterator.next();
			if (!invasion.getTarget().equals(target))
				continue;
			Bukkit.getPluginManager().callEvent(new InvadingStopEvent(reason, invasion));
			invasion.finish(true);
			iterator.remove();
		}
	}

	@EventHandler
	public void onChunkInvasionChange(PlayerChangeChunkEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		Land land = instance.getManager(LandManager.class).getLand(event.getFromChunk());
		Optional<Invasion> invasion = getInvasionsOn(kingdom).parallelStream()
				.filter(inv -> inv.getInvadingLands().stream().anyMatch(info -> info.equals(land.toInfo())))
				.findFirst();
		if (!invasion.isPresent())
			return;
		mechanic.onMoveIntoLand(event, kingdomPlayer, land);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
				return;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		if (!kingdom.isOnline()) //TODO might not actual stop the invasion due to async catch up.
			stopInvasion(StopReason.DEFENDED, kingdom);
		// Stop invasions where this player was appart of.
		getInvasionsOn(kingdom).parallelStream()
				.filter(invasion -> {
					invasion.removeInvading(kingdomPlayer);
					return !invasion.getAttacking().isOnline();
				})
				.forEach(invasion -> stopInvasion(StopReason.DEFENDED, invasion.getAttacking()));
	}

	/**
	 * @param event EntityDamageEvent so the event can be cancelled.
	 */
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType() != EntityType.PLAYER)
			return;
		Player player = (Player) entity;
		if (event.getFinalDamage() < player.getHealth())
			return;
		ConfigurationSection section = configuration.getConfigurationSection("invading.player-deaths");
		if (!section.getBoolean("enabled", true))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
				return;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		ConfigurationSection sounds = instance.getConfiguration("sounds").get();
		sounds.getConfigurationSection("invading.death-cancelled");
		SoundPlayer sound = new SoundPlayer(sounds);
		avoid : if (!section.getBoolean("cancel-defending-deaths", false)) {
			if (section.getBoolean("cancel-only-player-attacks", true)) {
				if (!(event instanceof EntityDamageByEntityEvent))
					break avoid;
				if (((EntityDamageByEntityEvent)event).getDamager().getType() != EntityType.PLAYER)
					break avoid;
			}
			Optional<OfflineKingdom> landOwner = kingdomPlayer.getLandAt().getKingdomOwner();
			if (landOwner.isPresent() && landOwner.get().equals(kingdom)) {
				sound.playAt(player.getLocation());
				event.setCancelled(true);
				return;
			}
		}
		for (Invasion invasion : getAllInvasions(kingdomPlayer)) {
			if (invasion.getTarget().isMember(kingdomPlayer)) { // Defending member death.
				long interval = IntervalUtils.getMilliseconds(section.getString("defending.death-cooldown", "30 seconds"));
				if (interval > 0) {
					if (System.currentTimeMillis() - invasion.getLastDeathTime() < interval) {
						sound.playAt(player.getLocation());
						invasion.resetDeathTime();
						event.setCancelled(true);
						continue;
					}
					invasion.resetDeathTime();
				}
				if (section.getBoolean("defending.stop", false)) {
					if (section.getBoolean("defending.death-counter", true)) {
						if (invasion.getDeathCount() < section.getInt("defending.deaths", 5))
							continue;
						invasion.addDeath();
					}
					stopInvasion(StopReason.DEFENDED, invasion);
				}
			} else if (invasion.getAttacking().isMember(kingdomPlayer)) {// Attacking member death.
				if (section.getBoolean("attacking.resets-or-stop", true)) { // Reset
					invasion.getInvolved().parallelStream()
							.filter(involved -> {
								if (!section.getBoolean("attacking.applies-to-all-invasions", false) && !involved.getKingdom().equals(kingdom))
									return false;
								if (section.getBoolean("attacking.progress-resets-all", false))
									return true;
								else if (kingdomPlayer.equals(involved))
									return true;
								return false;
							})
							.forEach(involved -> invasion.removeInvading(involved));
				} else { // Stop
					if (section.getBoolean("attacking.applies-to-all-invasions", false)) {
						stopInvasion(StopReason.DEFENDED, invasion);
						continue;
					} else if (invasion.getAttacking().equals(kingdom)) {
						stopInvasion(StopReason.DEFENDED, invasion);
					}
				}
			}
		}
	}

	@EventHandler
	public void onEnterVehicle(VehicleEnterEvent event) {
		if (isDefender(event.getEntered()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Land land = instance.getManager(LandManager.class).getLandAt(event.getBlock().getLocation());
		if (!land.hasOwner())
			return;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(event.getPlayer());
		getInvasionAt(land).parallelStream().forEach(invasion -> mechanic.onBlockBreak(event, kingdomPlayer, land, invasion));
	}

	@EventHandler
	public void onBreak(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		Land land = instance.getManager(LandManager.class).getLandAt(victim.getLocation());
		if (!land.hasOwner())
			return;
		getInvasionAt(land).forEach(invasion -> mechanic.onDamage(event, land));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandWhileFight(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Set<Invasion> current = getInvasionsOn(kingdomPlayer);
		if (current.isEmpty())
			return;
		String command = event.getMessage();
		if (command.equalsIgnoreCase("/k ff") || command.equalsIgnoreCase("/kingdoms ff") || command.equalsIgnoreCase("/kingdom ff")) {
			if (player.hasPermission("kingdoms.surrender") || player.hasPermission("kingdoms.player"))
				return;
		}
		if (command.equalsIgnoreCase("/k forfeit") || command.equalsIgnoreCase("/kingdoms forfeit") || command.equalsIgnoreCase("/kingdom forfeit")) {
			if (player.hasPermission("kingdoms.surrender") || player.hasPermission("kingdoms.player"))
				return;
		}
		if (configuration.getStringList("commands.allowed-during-invasion").contains(command))
			return;
		new MessageBuilder("kingdoms.defender-command-blocked")
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProjectileKnockback(EntityDamageByEntityEvent event) {
		DamageCause cause = event.getCause();
		if (cause != DamageCause.ENTITY_ATTACK && cause != DamageCause.PROJECTILE)
			return;
		Entity entity = event.getEntity();
		Optional<Defender> optional = mechanic.getDefender(entity);
		if (!optional.isPresent())
			return;
		Defender defender = optional.get();
		DefenderInfo info = defender.getDefenderInfo();
		int resistance = info.getResistance();
		if (resistance <= 0)
			return;
		if (random.nextInt(100) <= resistance) {
			DefenderKnockbackEvent knockbackEvent = new DefenderKnockbackEvent(defender, event.getDamager());
			Bukkit.getPluginManager().callEvent(knockbackEvent);
			if (knockbackEvent.isCancelled())
				return;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> entity.setVelocity(new Vector()), 1);
		}
	}

	@Override
	public void onDisable() {}

}
