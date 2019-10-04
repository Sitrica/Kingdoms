package com.songoda.kingdoms.objects.invasions.mechanics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.managers.GuardsManager;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.Defender;
import com.songoda.kingdoms.objects.Pair;
import com.songoda.kingdoms.objects.invasions.CommandTrigger;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.SoundPlayer;

public class DefaultInvasion extends InvasionMechanic<CommandTrigger> {

	private final Map<UUID, Pair<LandInfo, Defender>> invading = new HashMap<>(); //UUID is a player
	private final Map<KingdomPlayer, Long> combat = new HashMap<>();
	private final boolean combatLog, kill;

	public DefaultInvasion() {
		super(true, "default");
		Kingdoms instance = Kingdoms.getInstance();
		FileConfiguration configuration = instance.getConfig();
		kill = configuration.getBoolean("invading.mechanics.default.combat-log.kill-player", false);
		combatLog = configuration.getBoolean("invading.mechanics.default.combat-log.enabled", false);
		if (combatLog) {
			long time = IntervalUtils.getInterval(configuration.getString("invading.mechanics.default.combat-log.time", "60 seconds"));
			Bukkit.getScheduler().runTaskTimer(instance, () -> {
				if (combat.isEmpty())
					return;
				Iterator<Entry<KingdomPlayer, Long>> iterator = combat.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<KingdomPlayer, Long> entry = iterator.next();
					if (System.currentTimeMillis() - entry.getValue() <= time)
						continue;
					iterator.remove();
					new MessageBuilder("invading.combat-log-over")
							.setPlaceholderObject(entry.getKey())
							.send(entry.getKey());
				}
			}, 0, 20);
		}
	}

	/**
	 * Grab invasion infomation from the player's UUID.
	 * 
	 * @param uuid Player uuid to check for.
	 * @return All the required information if found.
	 */
	public Optional<Pair<LandInfo, Defender>> getInvading(UUID uuid) {
		return invading.entrySet().parallelStream()
				.filter(entry -> entry.getKey().equals(uuid))
				.map(entry -> entry.getValue())
				.findFirst();
	}

	@Override
	public void onMoveIntoLand(PlayerChangeChunkEvent event, KingdomPlayer kingdomPlayer, Land land) {
		if (!Kingdoms.getInstance().getConfig().getBoolean("invading.invading-deny-chunk-change", true))
			return;
		Optional<Pair<LandInfo, Defender>> invading = getInvading(kingdomPlayer.getUniqueId());
		if (!invading.isPresent())
			return;
		LandInfo info = invading.get().getFirst();
		if (info.equals(land.toInfo())) {
			event.setPush(true);
			event.setCancelled(true);
			new MessageBuilder("invading.invading-deny-chunk-change")
					.replace("%chunkFrom%", LocationUtils.chunkToString(event.getFromChunk()))
					.replace("%chunkTo%", LocationUtils.chunkToString(event.getToChunk()))
					.setKingdom(kingdomPlayer.getKingdom())
					.setPlaceholderObject(kingdomPlayer)
					.send(kingdomPlayer);
		}
	}

	@Override
	public void onInvade(CommandTrigger trigger, KingdomPlayer kingdomPlayer) {
		Land land = trigger.getLandInfo().get();
		OfflineKingdom kingdom = land.getKingdomOwner().get();
		Structure structure = land.getStructure();
		Player player = kingdomPlayer.getPlayer();
		Location location = player.getLocation();
		Defender defender = spawnDefender(location, trigger.getInvasion(), false);
		if (structure != null) {
			Optional<LivingEntity> optional = defender.getDefender();
			LivingEntity entity = optional.get();
			if (structure.getType() == StructureType.NEXUS) {
				defender.setNexus(true);
				DeprecationUtils.setMaxHealth(entity, entity.getHealth() + 200);
				if (kingdom.getMiscUpgrades().hasNexusGuard())
					Kingdoms.getInstance().getManager(GuardsManager.class).spawnNexusGuard(location, kingdom, kingdomPlayer);
			} else if (structure.getType() == StructureType.POWERCELL) {
				if (kingdom.getMiscUpgrades().hasInsanity() && MiscUpgradeType.INSANITY.isEnabled())
					entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000, 1));
			}
		}
		UUID uuid = player.getUniqueId();
		invading.put(uuid, Pair.of(trigger.getLandInfo(), defender));
	}

	@Override
	public void onDefenderDeath(EntityDeathEvent event, Defender defender) {
		Iterator<Entry<UUID, Pair<LandInfo, Defender>>> iterator = invading.entrySet().iterator();
		Invasion invasion = defender.getInvasion();
		String attacking = invasion.getAttacking().getName(); //So we don't need to get from the cache every iterate.
		LandManager landManager = Kingdoms.getInstance().getManager(LandManager.class);
		while (iterator.hasNext()) {
			Entry<UUID, Pair<LandInfo, Defender>> entry = iterator.next();
			Pair<LandInfo, Defender> object = entry.getValue();
			Defender search = object.getSecond();
			if (!search.getFirst().equals(defender.getFirst())) //Compare UUID's
				continue;
			// Set owner of Land
			Land land = object.getFirst().get();
			landManager.unclaimLand(land);
			landManager.getLand(object.getFirst()).setKingdomOwner(attacking);
			iterator.remove();
		}
		check(defender);
	}

	private void check(Defender defender) {
		if (defender == null)
			return;
		Invasion invasion = defender.getInvasion();
		FileConfiguration configuration = Kingdoms.getInstance().getConfig();
		if (defender.isNexusDefender() && configuration.getBoolean("invading.defender.defender-death-ends-invasion")) {
			stopInvasion(StopReason.WIN, invasion);
			return;
		}
		if (invasion.getTarget().getClaims().isEmpty())
			stopInvasion(StopReason.WIN, invasion);
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event, KingdomPlayer kingdomPlayer, Land land, Invasion invasion) {}

	@Override
	public void onDamage(EntityDamageByEntityEvent event, Land land) {
		if (!combatLog)
			return;
		Optional<Defender> defender = getDefender(event.getEntity());
		if (!defender.isPresent())
			return;
		Entity attacker = event.getDamager();
		if (!(attacker instanceof Player))
			return;
		KingdomPlayer kingdomPlayer = Kingdoms.getInstance().getManager(PlayerManager.class).getKingdomPlayer((Player)attacker);
		combat.put(kingdomPlayer, System.currentTimeMillis());
		new MessageBuilder("invading.combat-logged")
				.setKingdom(defender.get().getOwner())
				.setPlaceholderObject(kingdomPlayer)
				.send(kingdomPlayer);
	}

	@Override
	public void onInvasionStop(StopReason reason, Invasion invasion) {
		Set<KingdomPlayer> senders = Sets.newHashSet(invasion.getAttacking().getOnlinePlayers());
		OfflineKingdom defender = invasion.getTarget();
		getDefenders(defender).forEach(defenderEntity -> defenderEntity.getDefender().ifPresent(entity -> entity.remove()));
		switch (reason) {
			case DEFENDED:
				if (defender.isOnline())
					senders.addAll(defender.getKingdom().getOnlinePlayers());
				new MessageBuilder("invading.invasion-defended")
						.replace("%attacker%", invasion.getAttacking().getName())
						.replace("%target%", invasion.getTarget().getName())
						.setKingdom(invasion.getTarget())
						.send(senders);
				break;
			case STOPPED:
				if (defender.isOnline())
					senders.addAll(defender.getKingdom().getOnlinePlayers());
				new MessageBuilder("invading.invasion-stopped")
						.replace("%attacker%", invasion.getAttacking().getName())
						.replace("%target%", invasion.getTarget().getName())
						.setKingdom(invasion.getTarget())
						.send(senders);
				break;
			case TIMEOUT:
				if (defender.isOnline())
					senders.addAll(defender.getKingdom().getOnlinePlayers());
				new MessageBuilder("invading.invasion-timeout")
						.replace("%attacker%", invasion.getAttacking().getName())
						.replace("%target%", invasion.getTarget().getName())
						.setKingdom(invasion.getTarget())
						.send(senders);
				break;
			case WIN:
				new MessageBuilder("invading.invasion-ended-attacker")
						.setKingdom(invasion.getTarget())
						.send(invasion.getAttacking().getOnlinePlayers());
				new SoundPlayer("invading.win").playAt(invasion.getTarget().getNexusLocation());
				OfflineKingdom defenders = invasion.getTarget();
				if (defenders.isOnline())
					new MessageBuilder("invading.invasion-ended-defenders")
							.setKingdom(invasion.getAttacking())
							.send(defenders.getKingdom().getOnlinePlayers());
				defender.setSpawn(null);
				defender.setNexusLocation(null);
				if (Kingdoms.getInstance().getConfig().getBoolean("invading.mechanics.default.disband-on-loss", false))
					Kingdoms.getInstance().getManager(KingdomManager.class).deleteKingdom(defender.getName());
				break;
		}
	}

	@Override
	public boolean start(Land starting, Invasion invasion) {
		new MessageBuilder("invading.invasion-started-attacker")
				.setKingdom(invasion.getTarget())
				.send(invasion.getAttacking().getOnlinePlayers());
		OfflineKingdom defenders = invasion.getTarget();
		if (defenders.isOnline())
			new MessageBuilder("invading.invasion-started-defenders")
					.setKingdom(invasion.getAttacking())
					.send(defenders.getKingdom().getOnlinePlayers());
		return true;
	}

	// Default Mechanic should always be present.
	@Override
	public boolean initialize(Kingdoms instance) {
		return true;
	}

	@Override
	public boolean update(Invasion invasion) {
		// Timeout system
		if (invasion.getTarget().getClaims().isEmpty())
			stopInvasion(StopReason.WIN, invasion);
		String setting = Kingdoms.getInstance().getConfig().getString("invading.mechanics.default.max-time", "50 minutes");
		return System.currentTimeMillis() - invasion.getStartingTime() > IntervalUtils.getMilliseconds(setting);
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event, KingdomPlayer player) {
		Iterator<KingdomPlayer> iterator = combat.keySet().iterator();
		while (iterator.hasNext()) {
			KingdomPlayer kingdomPlayer = iterator.next();
			if (!kingdomPlayer.equals(player))
				continue;
			Optional<Pair<LandInfo, Defender>> information = getInvading(kingdomPlayer.getUniqueId());
			if (!information.isPresent())
				continue;
			stopInvasion(StopReason.STOPPED, information.get().getSecond().getSecond());
			if (kill) {
				Player bukkitPlayer = kingdomPlayer.getPlayer();
				bukkitPlayer.damage(bukkitPlayer.getHealth() + 1);
			}
		}
	}

}
