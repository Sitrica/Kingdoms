package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.SoldierTurretManager.Soldier;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class GuardsManager extends Manager {
	
	private final Map<Monster, Player> targets = new HashMap<>();
	public final String GUARD_KINGDOM = "kingdom-guard";
	private SoldierTurretManager soldierTurretManager;
	private KingdomManager kingdomManager;
	
	public GuardsManager() {
		super("guards", true);
		instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, () -> {
			Iterator<Entry<Monster, Player>> iterator = targets.entrySet().iterator();
		    while (iterator.hasNext()) {
		    	Entry<Monster, Player> entry = iterator.next();
		    	Monster guard = entry.getKey();
				Entity target = entry.getValue();
				if (guard.isDead() || !guard.isValid() || target.isDead() || !target.isValid()) {
					iterator.remove();
					guard.remove();
					Iterator<Soldier> soldierIt = soldierTurretManager.getSoldiers().iterator();
					while (soldierIt.hasNext()) {
						Soldier soldier = soldierIt.next();
						if (soldier.getZombie().getUniqueId().equals(guard.getUniqueId())) {
							soldierIt.remove();
							break;
						}
					}
					continue;
				}
				guard.setTarget((LivingEntity) target);
		    }
		}, 0, 20);
	}
	
	@Override
	public void initalize() {
		this.soldierTurretManager = instance.getManager("soldier-turret", SoldierTurretManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
	}
	
	public Map<Monster, Player> getTargets() {
		return targets;
	}
	
	public Optional<OfflineKingdom> getGuardKingdom(Metadatable metadatable) {
		if (!metadatable.hasMetadata(GUARD_KINGDOM))
			return Optional.empty();
		return metadatable.getMetadata(GUARD_KINGDOM).parallelStream()
				.filter(metadata -> metadata.getOwningPlugin().equals(instance))
				.map(metadata -> metadata.asString())
				.map(string -> UUID.fromString(string))
				.map(uuid -> kingdomManager.getOfflineKingdom(uuid))
				.filter(kingdom -> kingdom.isPresent())
				.map(optional -> optional.get())
				.findFirst();
	}
	
	public Entity spawnNexusGuard(Location location, OfflineKingdom owner, KingdomPlayer target) {
		if (owner == null)
			return null;
		Zombie zombie = location.getWorld().spawn(location, Zombie.class);
		zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
		zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
		DeprecationUtils.setItemInHandDropChance(zombie, 0);
		zombie.setBaby(false);
		if (target != null) {
			Player player = target.getPlayer();
			targets.put(zombie, player);
			zombie.setTarget(player);
		}
		zombie.setCustomName(new MessageBuilder("kingdoms.soldier-name")
				.setPlaceholderObject(target)
				.setKingdom(owner)
				.get());
		zombie.setCustomNameVisible(true);
		zombie.setMetadata(GUARD_KINGDOM, new FixedMetadataValue(instance, owner.getUniqueId()));
		DefenderInfo defenderInfo = owner.getDefenderInfo();
		int weapon = defenderInfo.getWeapon();
		if (weapon == 1)
			DeprecationUtils.setItemInMainHand(zombie, new ItemStack(Utils.materialAttempt("WOODEN_SWORD", "WOOD_SWORD")));
		else if( weapon == 2)
			DeprecationUtils.setItemInMainHand(zombie, new ItemStack(Material.STONE_SWORD));
		else if (weapon == 3)
			DeprecationUtils.setItemInMainHand(zombie, new ItemStack(Material.IRON_SWORD));
		else if (weapon == 4)
			DeprecationUtils.setItemInMainHand(zombie, new ItemStack(Material.DIAMOND_SWORD));
		else if (weapon > 4) {
			ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
			sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, weapon - 4);
			DeprecationUtils.setItemInMainHand(zombie, sword);
		}
		int speed = defenderInfo.getSpeed();
		if (speed > 0)
			zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, speed - 1));
		return zombie;
	}
	
	public Entity spawnSiegeBreaker(Location location, Kingdom owner, KingdomPlayer target) {
		if (owner == null)
			return null;
		Creeper creeper = (Creeper) location.getWorld().spawnEntity(location, EntityType.CREEPER);
		creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2));
		if (target != null) {
			Player player = target.getPlayer();
			targets.put(creeper, player);
			creeper.setTarget(player);
		}
		creeper.setMetadata(GUARD_KINGDOM, new FixedMetadataValue(instance, owner.getUniqueId()));
		creeper.setCustomName(new MessageBuilder("kingdoms.siege-breaker-name")
				.setPlaceholderObject(target)
				.setKingdom(owner)
				.get());
		creeper.setCustomNameVisible(true);
		creeper.setPowered(true);
		return creeper;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Optional<OfflineKingdom> kingdom = getGuardKingdom(event.getEntity());
		if (kingdom.isPresent() && event.getCause() == DamageCause.ENTITY_EXPLOSION)
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		Optional<OfflineKingdom> kingdom = getGuardKingdom(event.getEntity());
		if (kingdom.isPresent()) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

	@EventHandler
	public void onSoldierEnterVehicle(VehicleEnterEvent event) {
		Optional<OfflineKingdom> kingdom = getGuardKingdom(event.getEntered());
		if (kingdom.isPresent())
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onTarget(EntityTargetEvent event) {
		Entity target = event.getTarget();
		Entity entity = event.getEntity();
		Optional<Player> optional = targets.entrySet().parallelStream()
				.filter(entry -> entry.getKey().equals(entity))
				.map(entry -> entry.getValue())
				.findFirst();
		if (!optional.isPresent())
			return;
		Player player = optional.get();
		if (!target.equals(player)) {
			event.setCancelled(true);
			((Creature) entity).setTarget(player);
		}
	}

	@Override
	public void onDisable() {
		targets.clear();
	}

}
