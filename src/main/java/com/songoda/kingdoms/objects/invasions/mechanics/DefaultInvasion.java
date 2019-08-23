package com.songoda.kingdoms.objects.invasions.mechanics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.managers.GuardsManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.objects.Defender;
import com.songoda.kingdoms.objects.DoubleObject;
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

public class DefaultInvasion extends InvasionMechanic<CommandTrigger> {

	private final Map<UUID, DoubleObject<LandInfo, Defender>> invading = new HashMap<>(); //UUID is a player

	public DefaultInvasion() {
		super(true, "default");
	}

	public Optional<DoubleObject<LandInfo, Defender>> getInvading(UUID uuid) {
		return invading.entrySet().parallelStream()
				.filter(entry -> entry.getKey().equals(uuid))
				.map(entry -> entry.getValue())
				.findFirst();
	}

	@Override
	public void onMoveIntoLand(PlayerChangeChunkEvent event, KingdomPlayer kingdomPlayer, Land land) {
		if (!Kingdoms.getInstance().getConfig().getBoolean("invading.invading-deny-chunk-change", true))
			return;
		Optional<DoubleObject<LandInfo, Defender>> invading = getInvading(kingdomPlayer.getUniqueId());
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
		invading.put(uuid, DoubleObject.of(trigger.getLandInfo(), defender));
	}

	@Override
	public void onDefenderDeath(EntityDeathEvent event, Defender defender) {
		Iterator<Entry<UUID, DoubleObject<LandInfo, Defender>>> iterator = invading.entrySet().iterator();
		Invasion invasion = defender.getInvasion();
		String attacking = invasion.getAttacking().getName(); //So we don't need to get from the cache every iterate.
		while (iterator.hasNext()) {
			Entry<UUID, DoubleObject<LandInfo, Defender>> entry = iterator.next();
			DoubleObject<LandInfo, Defender> object = entry.getValue();
			Defender search = object.getSecond();
			if (!search.getFirst().equals(defender.getFirst())) //Compare UUID's
				continue;
			// Set owner of Land
			object.getFirst().get().setKingdomOwner(attacking);
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
	public void onDamage(EntityDamageByEntityEvent event, Land land) {}

	@Override
	public void onInvasionStop(StopReason reason, Invasion invasion) {
		Set<KingdomPlayer> senders = Sets.newHashSet(invasion.getAttacking().getOnlinePlayers());
		OfflineKingdom defender = invasion.getTarget();
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
				OfflineKingdom defenders3 = invasion.getTarget();
				if (defenders3.isOnline())
					new MessageBuilder("invading.invasion-ended-defenders")
							.setKingdom(invasion.getAttacking())
							.send(defenders3.getKingdom().getOnlinePlayers());
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
		return System.currentTimeMillis() - invasion.getStartingTime() < IntervalUtils.getMilliseconds(setting);
	}

}
