package com.songoda.kingdoms.objects.invasions.mechanics;

import java.util.Optional;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.manager.managers.GuardsManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class DefaultInvasion extends InvasionMechanic {

	public DefaultInvasion() {
		super("default");
	}

	@Override
	public void onMoveIntoLand(PlayerChangeChunkEvent event, KingdomPlayer kingdomPlayer, Land land) {
		if (!Kingdoms.getInstance().getConfig().getBoolean("invading.invading-deny-chunk-change", true))
			return;
		//InvadingManager invadingManager = Kingdoms.getInstance().getManager(InvadingManager.class);
		Optional<LandInfo> invading = kingdomPlayer.getInvadingLand();
		if (!invading.isPresent())
			return;
		if (invading.get().equals(land.toInfo())) {
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

	//TODO make this whole thing work.
	public void command() {
		// 200 bonus health for Nexus defense.
		Structure structure = land.getStructure();
		if (structure != null) {
			if (structure.getType() == StructureType.NEXUS) {
				Land nexusLand = landManager.getLand(structure.getLocation().getChunk());
				if (land.equals(nexusLand)) {
					DeprecationUtils.setMaxHealth(entity, entity.getHealth() + 200);
					if (kingdom.getMiscUpgrades().hasNexusGuard())
						instance.getManager(GuardsManager.class).spawnNexusGuard(location, kingdom, challenger);
				}
			} else if (structure.getType() == StructureType.POWERCELL) {
				if (kingdom.getMiscUpgrades().hasInsanity() && MiscUpgradeType.INSANITY.isEnabled())
					entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000, 1));
			}
		}
	}

	@Override
	public void onInteract(PlayerInteractEvent event, KingdomPlayer kingdomPlayer, Land land) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event, KingdomPlayer kingdomPlayer, Land land) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNexusBreak(BlockBreakEvent event, KingdomPlayer kingdomPlayer, Land land) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDamage(EntityDamageByEntityEvent event, Land land) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean start(Land starting, Invasion invasion) {
		//target.sendAnnouncement(null, Kingdoms.getLang().getString("Command_Invade_Warning", kp.getLang()), true);
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
		String setting = Kingdoms.getInstance().getConfig().getString("invading.mechanics.default.max-time", "50 minutes");
		return System.currentTimeMillis() - invasion.getStartingTime() < IntervalUtils.getMilliseconds(setting);
	}

	@Override
	public void onInvasionStop(StopReason reason, Invasion invasion) {
		// TODO Auto-generated method stub
		
	}

}
