package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class SoldierTurretManager extends Manager {
	
	private final Set<Soldier> soldiers = new HashSet<>();
	private final GuardsManager guardsManager;

	public SoldierTurretManager() {
		super("soldier-turret", false);
		this.guardsManager = instance.getManager("guards", GuardsManager.class);
	}
	
	public Zombie spawnSoldier(Kingdom owner, Location location, Location origin, int damage, Player target) {
		soldiers.parallelStream()
				.filter(soldier -> soldier.getSpawnLocation().equals(location))
				.forEach(soldier -> soldier.getZombie().remove());
		Zombie soldier = location.getWorld().spawn(location, Zombie.class);
		soldier.setMetadata(guardsManager.GUARD_KINGDOM, new FixedMetadataValue(instance, owner.getUniqueId()));
		soldier.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 2));
		soldier.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2));
		DeprecationUtils.setItemInMainHand(soldier, new ItemStack(Material.IRON_SWORD));
		soldier.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
		DeprecationUtils.setItemInHandDropChance(soldier, 0);
		soldier.getEquipment().setHelmetDropChance(0.0f);
		soldier.setCustomName(new MessageBuilder("kingdoms.soldier-name")
				.setPlaceholderObject(target)
				.setKingdom(owner)
				.get());
		soldier.setCustomNameVisible(true);
		soldier.setTarget(target);
		soldier.setBaby(false);
		if (damage > -1)
			soldier.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 99999, damage));
		soldiers.add(new Soldier(soldier, location, target));
		guardsManager.getTargets().put(soldier, target);
		return soldier;
	}
	
	public Set<Soldier> getSoldiers() {
		return soldiers;
	}
	
	public class Soldier {
		
		private final LivingEntity target;
		private final Location spawn;
		private final Zombie zombie;
		
		public Soldier(Zombie zombie, Location spawn, LivingEntity target) {
			this.zombie = zombie;
			this.target = target;
			this.spawn = spawn;
		}
		
		public Zombie getZombie() {
			return zombie;
		}
		
		public LivingEntity getTarget() {
			return target;
		}
		
		public Location getSpawnLocation() {
			return spawn;
		}
		
	}
	
	@Override
	public void onDisable() {
		soldiers.clear();
	}

}
