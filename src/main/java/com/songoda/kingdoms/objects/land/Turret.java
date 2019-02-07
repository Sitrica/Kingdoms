package com.songoda.kingdoms.objects.land;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.utils.LocationUtils;
import com.songoda.kingdoms.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Calendar;

public class Turret {

	private final Location location;
	private long cooldown = 0;
	private TurretType type;

	public Turret(Location location, TurretType type) {
		this.location = location;
		this.type = type;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setType(TurretType type){
		this.type = type;
	}

	public boolean isValid(){
		if (type == null)
			return false;
		if (location == null)
			return false;
		Block block = location.getBlock();
		if (!block.getRelative(0, -1, 0).getType().toString().endsWith("FENCE"))
			return false;
		Material check = Utils.materialAttempt("SKELETON_SKULL", "SKULL");
		return block.getType() == check;
	}

	public static Turret fromString(String string) {
		String[] split = string.split(":");
		if (split.length != 2) {
			Kingdoms.debugMessage("Invailed turret data: " + string);
			return null;
		}
		Location loc = LocationUtils.stringToLocation(split[0]);
		TurretType type = TurretType.valueOf(split[1]);
		return new Turret(loc, type);
	}

	public void destroy() {
		Chunk chunk = location.getWorld().getChunkAt((int)location.getX(), (int)location.getZ());
		Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
		location.getBlock().setType(Material.AIR);
		land.getTurrets().remove(this);
	}
	
	public void breakTurret(){
		location.getWorld().dropItem(location, type.getTurretDisk());
		destroy();
	}

	public void fireAt(Player target) {
		Land land = GameManagement.getLandManager().getOrLoadLand(loc.toSimpleChunk());
		Kingdom shooter = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
		Calendar calender = Calendar.getInstance();
		if (cooldown > calender.getTimeInMillis())
			return;
		if (target == null)
			return;
		if (!TurretUtil.canBeTarget(this, target))
			return;
		KingdomTurretFireEvent event = new KingdomTurretFireEvent(location, type, target, shooter);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			calender.add(Calendar.SECOND, type.getFireCD());
			cooldownExpire = calender.getTimeInMillis();
			target = event.getTarget();
			switch(type) {
				case ARROW:
					TurretUtil.shootArrow(shooter, target.getLocation(), loc.toLocation(), false, false, type.getDamage());
					break;
				case FLAME:
					TurretUtil.shootArrow(shooter, target.getLocation(), loc.toLocation(), false, true, type.getDamage());
					break;
				case HEALING:
					TurretUtil.healEffect((Player) target, type.getDamage());
					if(shooter.getTurretUpgrades().isImprovedHeal())
					TurretUtil.regenHealEffect((Player) target, (float) type.getDamage() / 2);
					break;
				case HEATBEAM:
					TurretUtil.heatbeamAttack(target, loc.toLocation(), type.getDamage(), shooter.getTurretUpgrades().isUnrelentingGaze());
					break;
				case HELLFIRE:
					TurretUtil.shootArrow(shooter, target.getLocation(), loc.toLocation(), true, false, type.getDamage());
					break;
				case MINE_CHEMICAL:
					int dur = 100;
					if(shooter.getTurretUpgrades().isVirulentPlague()) dur = 200;
					target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
					target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
					target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dur, type.getDamage()));
					destroy();
					break;
				case MINE_PRESSURE:
					loc.toLocation().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), type.getDamage(), false, false);
					if(shooter.getTurretUpgrades().isConcentratedBlast())
					loc.toLocation().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) (type.getDamage() * 0.5), false, false);
					destroy();
					break;
				case PSIONIC:
					TurretUtil.psionicEffect(target, type.getDamage(), shooter.getTurretUpgrades().isVoodoo());
					break;
				case SOLDIER:
					GameManagement.getSoldierTurretManager().turretSpawnSoldier(shooter, target.getLocation(), loc.toLocation(), type.getDamage(), (Player) target);
					break;
			}
		}
	}

}
