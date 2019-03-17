package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.bukkit.util.Materials;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class GuardsManager extends Manager {

	static {
		registerManager("guards", new GuardsManager());
	}
	
	private final Map<Monster, Player> targets = new HashMap<>();
	private final String GUARD_NAME;
	
	protected GuardsManager() {
		super(false);
		GUARD_NAME = Kingdoms.getLang().getString("Soldier_Name");
		instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, () -> {
			Iterator<Entry<Monster, Player>> iterator = targets.entrySet().iterator();
		    while (iterator.hasNext()) {
		    	Entry<Monster, Player> entry = iterator.next();
		    	Monster guard = entry.getKey();
				Entity target = entry.getValue();
				if (guard.isDead() || !guard.isValid() || target.isDead() || !target.isValid()) {
					iterator.remove();
					guard.remove();
					Iterator<Entry<Location, Soldier>> soldierIt = GameManagement.getSoldierTurretManager().soldiers.entrySet().iterator();
					while (soldierIt.hasNext()) {
						Soldier soldier = soldierIt.next().getValue();
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
	
	public Entity spawnNexusGuard(Location location, Kingdom owner, KingdomPlayer target) {
		Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);		
		zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
		zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
		zombie.setBaby(false);
		zombie.getEquipment().setItemInHandDropChance(0.0f);
		if (target != null)
			zombie.setTarget(target.getPlayer());
		zombie.setCustomName(GUARD_NAME);
		zombie.setCustomNameVisible(true);
		if(owner != null) zombie.setMetadata("kingdom+" +owner.getKingdomName(), new FixedMetadataValue(Kingdoms.getInstance(), ""));
		int weapon = owner.getChampionInfo().getWeapon();
		if(weapon == 0){
			zombie.getEquipment().setItemInHand(null);
		}else if(weapon == 1){
			zombie.getEquipment().setItemInHand(new ItemStack(Materials.WOODEN_SWORD.parseMaterial()));
		}else if(weapon == 2){
			zombie.getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
		}else if(weapon == 3){
			zombie.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD));
		}else if(weapon == 4){
			zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
		}else if(weapon > 4){
			ItemStack diasword = new ItemStack(Material.DIAMOND_SWORD);
			diasword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, weapon - 4);
			
			zombie.getEquipment().setItemInHand(diasword);
		}
		if (owner != null) {
			int speed = owner.getChampionInfo().getSpeed();
				if(speed > 0){
					zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, speed - 1));
				}
		}
		if (target != null)
			targets.put(zombie, target.getPlayer());
		return zombie;
	}
	
	public Entity spawnSiegeBreaker(Location location, Kingdom owner, KingdomPlayer target) {
		Creeper creeper = (Creeper) location.getWorld().spawnEntity(location, EntityType.CREEPER);
		creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2));
		if (target != null)
			creeper.setTarget(target.getPlayer());
		creeper.setCustomName(GUARD_NAME);
		creeper.setCustomNameVisible(true);
		if (owner != null)
			creeper.setMetadata("kingdom+" + owner.getName(), new FixedMetadataValue(instance, ""));
		creeper.setPowered(true);
		if (target != null)
			targets.put(creeper, target.getPlayer());
		return creeper;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		String name = event.getEntity().getCustomName();
		if (name == null)
			return;
		if (name.equals(GUARD_NAME) && event.getCause() == DamageCause.ENTITY_EXPLOSION)
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		String name = event.getEntity().getCustomName();
		if (name == null)
			return;
		if (name.equals(GUARD_NAME)) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

	@EventHandler
	public void onSoldierEnterVehicle(VehicleEnterEvent event) {
		String name = event.getEntered().getCustomName();
		if (name == null)
			return;
		if (name.equals(GUARD_NAME))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onTarget(EntityTargetEvent event) {
		Entity target = event.getTarget();
		if (target == null)
			return;
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
	public void onDisable() {}

}
