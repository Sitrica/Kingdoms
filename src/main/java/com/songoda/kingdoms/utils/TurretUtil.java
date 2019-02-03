package com.songoda.kingdoms.utils;

import com.songoda.kingdoms.constants.TurretType;
import com.songoda.kingdoms.manager.external.ExternalManager;
import com.songoda.kingdoms.manager.game.GameManagement;
import com.songoda.kingdoms.objects.KingdomPlayer;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.land.Turret;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.constants.TurretTargetType;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class TurretUtil {

	public static final String META_SHOOTER = "shooter";
	public static final String META_DAMAGE = "damage";

	public static final String turretDecal = ChatColor.GOLD + "Kingdoms Turret";

	//async it.
	public static boolean canBeTargeted(Turret turret, Player target) {
		if (target.isDead() || !target.isValid())
			return false;
		if (GameManagement.getChampionManager().isChampion(target))
			return false;
		Location location = turret.getLocation();
		if (!location.getWorld().equals(target.getWorld()))
			return false;
		if (target.getLocation().distanceSquared(location) > Math.pow(turret.getType().getRange(), 2))
			return false;
		TurretType type = turret.getType();
		UUID kingdomOwner = GameManagement.getLandManager().getOrLoadLand(location.toSimpleChunk()).getOwnerUUID();
		Kingdom landOwner = GameManagement.getKingdomManager().getOrLoadKingdom(kingdomOwner);
		Kingdom playerKingdom;
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(target.getUniqueId());
		playerKingdom = kp.getKingdom();
		if (kp.isTemp())
			return false;
		if (kp.isAdminMode())
			return false;
		if (kp.isVanishMode())
			return false;
		GameMode gamemode = kp.getPlayer().getGamemode();
		if (gamemode != GameMode.SURVIVAL && gamemode != GameMode.ADVENTURE)
			return false;
		if (type.getTargetTypes().contains(TurretTargetType.ALLY_PLAYERS)) {
			if (playerKingdom == null)
				return false;
			if (kingdomOwner.equals(playerKingdom.getKingdomUuid()))
				return true;
			return landOwner.isAllianceWith(playerKingdom);
		} else if (type.getTargetTypes().contains(TurretTargetType.ENEMY_PLAYERS)) {
			if (playerKingdom == null)
				return true;
			if (kingdomOwner.equals(playerKingdom.getKingdomUuid()))
				return false;
			return !landOwner.isAllianceWith(playerKingdom);
		}
		return false;
	}

	public static boolean canBeTarget(Kingdom kingdom, Entity target){
		if (!(target instanceof LivingEntity))
			return false;
		if (target.isDead() || !target.isValid())
			return false;
		if (ExternalManager.isCitizen(target))
			return false;
		Kingdom playerKingdom;
		if (GameManagement.getChampionManager().isChampion(target))
			return false;
		if (target instanceof Player) {
			KingdomPlayer kp = GameManagement.getPlayerManager().getSession(target.getUniqueId());
			playerKingdom = kp.getKingdom();
			if (kp.isTemp())
				return false;
			if (kp.isAdminMode())
				return false;
			if (kp.isVanishMode())
				return false;
			GameMode gamemode = kp.getPlayer().getGameMode();
			if (gamemode != GameMode.SURVIVAL && gamemode != GameMode.ADVENTURE)
				return false;
		} else if (target instanceof Wolf) {
			Wolf wolf = (Wolf) target;
			if (wolf.getOwner() != null) {
				OfflineKingdomPlayer okp = GameManagement.getPlayerManager().getOfflineKingdomPlayer(wolf.getOwner().getUniqueId());
				playerKingdom = GameManagement.getKingdomManager().getOrLoadKingdom(okp.getKingdomName());
			}
		}
		if (playerKingdom == null)
			return true;
		if (kingdom.getKingdomName().equals(playerKingdom.getKingdomName()))
			return false;
		return !kingdom.isAllianceWith(playerKingdom);
	}

	public static void shootArrow(Kingdom shooter, Location target, Location origin, boolean crit, boolean fire, double damage) {
		Vector to = target.clone().add(0.0D, 0.75D, 0.0D).toVector();
		Location fromLoc = origin.clone().add(0.5D, 1.0D, 0.5D);
		Vector from = fromLoc.toVector();
		Vector direction = to.subtract(from);
		direction.normalize();
		Kingdoms instance = Kingdoms.getInstance();
		Arrow arrow = origin.getWorld().spawnArrow(fromLoc, direction, 1.5F, 10);// speed,spread
		if (shooter != null)
			arrow.setMetadata(META_SHOOTER, new FixedMetadataValue(instance, shooter.getKingdomName()));
		if (crit)
			arrow.setCritical(crit);
		if (fire)
			arrow.setFireTicks(Integer.MAX_VALUE);
		arrow.setMetadata(META_DAMAGE, new FixedMetadataValue(instance, "" + damage));
	}

	public static void volley(Kingdom shooter, Location target, Location origin) {
		Location fromLoc = origin.clone().add(0.5D, 1.0D, 0.5D);
		int i = -2;
		while(i < 2) {
			Location toLoc = target.clone().add(randInt(-1, 1), randInt(0, 2), randInt(-1, 1));
			Vector to = toLoc.clone().add(0.0D, 0.75D, 0.0D).toVector();
			i++;
			Vector from = fromLoc.toVector();
			Vector direction = to.subtract(from);
			direction.normalize();
			Snowball snowball = (Snowball) origin.getWorld().spawnEntity(fromLoc, EntityType.SNOWBALL);
			snowball.setVelocity(direction);
			snowball.setFireTicks(Integer.MAX_VALUE);
			Kingdoms instance = Kingdoms.getInstance();
			snowball.setMetadata(META_SHOOTER, new FixedMetadataValue(instance, shooter.getKingdomName()));
			snowball.setMetadata("flamesnowballs", new FixedMetadataValue(instance, "fireballs"));
		}
	}

	public static void psionicEffect(Entity target, double damage, boolean isVoodoo) {
		if (!(target instanceof Damageable))
			return;

		Damageable d = (Damageable) target;

		double calcDamage = damage;
		if (d.getHealth() - damage < 6)
			calcDamage = d.getHealth() - 6;
		d.damage(calcDamage < 0 ? 0 : calcDamage);

		if (target instanceof LivingEntity) {
			if (target instanceof Player) {
				KingdomPlayer kp = GameManagement.getPlayerManager().getSession((Player) target);
				if (ExternalManager.sendActionBar(((Player) target),
						Kingdoms.getLang().getString("Turrets_Psi_Attack", kp.getLang()))) {
					target.sendMessage(Kingdoms.getLang().getString("Turrets_Psi_Attack", kp.getLang()));
				}
			}
			int dur = 10;
			if (isVoodoo)
				dur = 40;
			((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, dur, 6));
			((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 6));
		}
	}



	public static void heatbeamAttack(LivingEntity target, Location origin, double damage, boolean upgraded){
	heatbeamParticleEffect(target, origin);
	if(upgraded) damage += 3;
	target.damage((1.0 - getDamageReduced(target)) * damage);
	if(target instanceof Player){
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession((Player) target);
		if(ExternalManager.sendActionBar(((Player) target),
			Kingdoms.getLang().getString("Turrets_Heatbeam_Attack", kp.getLang()))){
		target.sendMessage(Kingdoms.getLang().getString("Turrets_Heatbeam_Attack", kp.getLang()));
		}
	}

	}

	public static void heatbeamParticleEffect(Entity target, Location origin){
		origin = origin.clone().add(0.5D, 1.0D, 0.5D);
		Location targetLoc = target.getLocation().clone().add(0, 0.5, 0);
		// 2017-05-04 -- distanceSquared is already always positive. Math.sqrt() seems to perform better than Math.abs()
		//							 I think Location.distance() does the exact same thing, but lets just do it manually as I'm not sure about that
		double distance = Math.sqrt(targetLoc.distanceSquared(origin));
		int distInt = (int) Math.ceil(distance);
	
		World world = origin.getWorld();
	
		List<Location> dustLocations = new ArrayList<Location>();
	
		// 2017-05-04
		for(int i = 0; i < distInt; i++){
			double delta = (i / 15.0D) * distance;
			double x = (1.0D - delta) * origin.getX() + delta * (targetLoc.getX() + 0.5D);
			double y = (1.0D - delta) * origin.getY() + delta * (targetLoc.getY() + 0.5D);
			double z = (1.0D - delta) * origin.getZ() + delta * (targetLoc.getZ() + 0.5D);
			dustLocations.add(new Location(world, x, y, z));
		}
		Effect effect = null;
		try {
			effect = Effect.valueOf("COLOURED_DUST");
		}catch (IllegalArgumentException e){
	
		}
		if(effect == null){
			effect = Effect.MOBSPAWNER_FLAMES;
		}
		final Location originCopy = origin.clone();
		for(Location loc : dustLocations){
			world.playEffect(loc, effect, 0);
		}
	}


	private static double getDamageReduced(LivingEntity livingEntity){
		EntityEquipment inv = livingEntity.getEquipment();
		ItemStack boots = inv.getBoots();
		ItemStack helmet = inv.getHelmet();
		ItemStack chest = inv.getChestplate();
		ItemStack pants = inv.getLeggings();
		double red = 0.0;
		if(helmet != null){
			if(helmet.getType() == Material.LEATHER_HELMET) red = red + 0.04;
			else if(helmet.getType() == Materials.GOLDEN_HELMET.parseMaterial()) red = red + 0.08;
			else if(helmet.getType() == Material.CHAINMAIL_HELMET) red = red + 0.08;
			else if(helmet.getType() == Material.IRON_HELMET) red = red + 0.08;
			else if(helmet.getType() == Material.DIAMOND_HELMET) red = red + 0.12;
		}
		//
		if(boots != null){
			if(boots.getType() == Material.LEATHER_BOOTS) red = red + 0.04;
			else if(boots.getType() == Materials.GOLDEN_BOOTS.parseMaterial()) red = red + 0.04;
			else if(boots.getType() == Material.CHAINMAIL_BOOTS) red = red + 0.04;
			else if(boots.getType() == Material.IRON_BOOTS) red = red + 0.08;
			else if(boots.getType() == Material.DIAMOND_BOOTS) red = red + 0.12;
		}
		//
		if(pants != null){
			if(pants.getType() == Material.LEATHER_LEGGINGS) red = red + 0.08;
			else if(pants.getType() == Materials.GOLDEN_LEGGINGS.parseMaterial()) red = red + 0.12;
			else if(pants.getType() == Material.CHAINMAIL_LEGGINGS) red = red + 0.16;
			else if(pants.getType() == Material.IRON_LEGGINGS) red = red + 0.20;
			else if(pants.getType() == Material.DIAMOND_LEGGINGS) red = red + 0.24;
		}
		//
		if(chest != null){
			if(chest.getType() == Material.LEATHER_CHESTPLATE) red = red + 0.12;
			else if(chest.getType() == Materials.GOLDEN_CHESTPLATE.parseMaterial()) red = red + 0.20;
			else if(chest.getType() == Material.CHAINMAIL_CHESTPLATE) red = red + 0.20;
			else if(chest.getType() == Material.IRON_CHESTPLATE) red = red + 0.24;
			else if(chest.getType() == Material.DIAMOND_CHESTPLATE) red = red + 0.32;
		}
		return red;
	}


	public static void healEffect(Player target, double amount){
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(target);
		int level = kp.getKingdom().getPowerUp().getRegenboost();
		double amt = amount * (1.0D + level / 100.0D);
	
		EntityRegainHealthEvent event = new EntityRegainHealthEvent(target, amt, RegainReason.CUSTOM);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		amt = event.getAmount();
		target.setHealth(target.getHealth() + amt > target.getMaxHealth() ? target.getMaxHealth()
			: target.getHealth() + amt);
	}

	private static final Map<UUID, Integer> healTasks = new ConcurrentHashMap<UUID, Integer>();

	public static void regenHealEffect(Player target, double amount){
		int i = Bukkit.getScheduler().scheduleSyncRepeatingTask(Kingdoms.getInstance(), new Runnable() {
			int timesRun = 0;
	
			@Override
			public void run(){
			timesRun++;
			if(timesRun >= 4){
				if(healTasks.get(target.getUniqueId()) != null)
				Bukkit.getScheduler().cancelTask(healTasks.remove(target.getUniqueId()));
				return;
			}
			healEffect(target, amount);
			}
	
		}, 0, 5L);
		healTasks.put(target.getUniqueId(), i);
	}

	public static int randInt(int min, int max) {

		Random rand = new Random();
	
		int randomNum = rand.nextInt((max - min) + 1) + min;
	
		return randomNum;
	}

}
