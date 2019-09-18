package com.songoda.kingdoms.manager.managers;

import java.util.Optional;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.utils.Utils;

public class MiscUpgradesManager extends Manager {

	public MiscUpgradesManager() {
		super(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreeperExplosion(EntityExplodeEvent event) {
		if (event.getEntityType() != EntityType.CREEPER)
			return;
		Land land = instance.getManager(LandManager.class).getLandAt(event.getLocation());
		Optional<OfflineKingdom> kingdom = land.getKingdomOwner();
		if (!kingdom.isPresent())
			return;
		MiscUpgrade miscUpgrade = kingdom.get().getMiscUpgrades();
		if (miscUpgrade.hasAnticreeper())
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onCropTrample(PlayerInteractEvent event) {
		if (event.getAction() != Action.PHYSICAL)
			return;
		Block block = event.getClickedBlock();
		if (block.getType() != Utils.materialAttempt("FARMLAND", "SOIL"))
			return;
		Land land = instance.getManager(LandManager.class).getLandAt(block.getLocation());
		Optional<OfflineKingdom> kingdom = land.getKingdomOwner();
		if (!kingdom.isPresent())
			return;
		MiscUpgrade miscUpgrade = kingdom.get().getMiscUpgrades();
		if (miscUpgrade.hasAntiTrample())
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBombShard(BlockExplodeEvent event) {
		event.blockList().removeIf(block -> {
			Land land = instance.getManager(LandManager.class).getLandAt(block.getLocation());
			Optional<OfflineKingdom> kingdom = land.getKingdomOwner();
			if (!kingdom.isPresent())
				return false;
			MiscUpgrade miscUpgrade = kingdom.get().getMiscUpgrades();
			if (miscUpgrade.hasBombShards())
				return true;
			return false;
		});
	}

	@EventHandler(ignoreCancelled = true)
	public void onGlory(EntityDeathEvent event) {
		LivingEntity victim = event.getEntity();
		Player attacker = victim.getKiller();
		if (attacker == null)
			return;
		Land land = instance.getManager(LandManager.class).getLandAt(victim.getLocation());
		Optional<OfflineKingdom> kingdom = land.getKingdomOwner();
		if (!kingdom.isPresent())
			return;
		MiscUpgrade miscUpgrade = kingdom.get().getMiscUpgrades();
		if (miscUpgrade.hasGlory()) {
			int xp = event.getDroppedExp();
			event.setDroppedExp(xp * 3);
		}
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
