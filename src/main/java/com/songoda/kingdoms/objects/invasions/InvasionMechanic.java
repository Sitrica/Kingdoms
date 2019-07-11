package com.songoda.kingdoms.objects.invasions;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.PlayerChangeChunkEvent;
import com.songoda.kingdoms.objects.Defender;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public abstract class InvasionMechanic implements Listener {

	private final SetMultimap<OfflineKingdom, Defender> defenders = MultimapBuilder.hashKeys().hashSetValues().build();
	private final String[] names;

	protected InvasionMechanic(String... names) {
		this.names = names;
	}

	public enum StopReason {

		DEFENDED, STOPPED, TIMEOUT, WIN;

		public static StopReason get(boolean complete) {
			return complete ? WIN : DEFENDED;
		}

	}

	/**
	 * Called when this mechanic is being initialized.
	 * 
	 * @param instance Kingdoms instance
	 * @return boolean true if successfully initialized.
	 */
	public abstract boolean initialize(Kingdoms instance);

	/**
	 * Called when this mechanic is being started on an Invasion.
	 * 
	 * @param starting The starting land this invasion was called in.
	 * @param invasion The invasion data and information.
	 * @return boolean true if can start this invasion.
	 */
	public abstract boolean start(Land starting, Invasion invasion);

	/**
	 * Called when a player not apart of the target Kingdom enters one of the target Kingdom's lands.
	 * <p>
	 * The land is still in possession of the target Kingdom when this is called.
	 * 
	 * @param event The PlayerChangeChunkEvent event involved in this call.
	 * @param kingdomPlayer A player that walked into the land that is not apart of the target Kingdom.
	 * @param land The land the player walked into.
	 */
	public abstract void onMoveIntoLand(PlayerChangeChunkEvent event, KingdomPlayer kingdomPlayer, Land land);

	/**
	 * Called when a player not apart of the target Kingdom interacts within the target Kingdom's lands.
	 * <p>
	 * The land is still in possession of the target Kingdom when this is called.
	 * 
	 * @param event The PlayerInteractEvent event involved in this call.
	 * @param kingdomPlayer The player interacting within the Land.
	 * @param land The Land the event was part of.
	 */
	public abstract void onInteract(PlayerInteractEvent event, KingdomPlayer kingdomPlayer, Land land);

	/**
	 * Called when a player not apart of the target Kingdom breaks a block in the target Kingdom's lands.
	 * <p>
	 * The land is still in possession of the target Kingdom when this is called.
	 * 
	 * @param event The BlockBreakEvent event involved in this call.
	 * @param kingdomPlayer The player breaking the block.
	 * @param land The Land the block was broken in.
	 */
	public abstract void onBlockBreak(BlockBreakEvent event, KingdomPlayer kingdomPlayer, Land land);

	/**
	 * Called when a player not apart of the target Kingdom breaks the Nexus in the target Kingdom's lands.
	 * <p>
	 * The land is still in possession of the target Kingdom when this is called.
	 * 
	 * @param event The BlockBreakEvent event involved in this call.
	 * @param kingdomPlayer The player breaking the nexus.
	 * @param land The Land the block was broken in.
	 */
	public abstract void onNexusBreak(BlockBreakEvent event, KingdomPlayer kingdomPlayer, Land land);

	/**
	 * Called when an entity within the target Kingdom's land is damaged by another entity.
	 * <p>
	 * The land is still in possession of the target Kingdom when this is called.
	 * 
	 * @param event The EntityDamageByEntityEvent event involved in this call.
	 * @param land The Land the event was part of.
	 */
	public abstract void onDamage(EntityDamageByEntityEvent event, Land land);

	/**
	 * Checks that the mechanic is still alive and working.
	 * 
	 * @param invasion The Invasion involved in this check.
	 * @return boolean If false this invasion will be stopped.
	 */
	public abstract boolean update(Invasion invasion);

	/**
	 * Called when an invasion stops.
	 * 
	 * @param reason The StopReason as to why this invasion stopped.
	 * @param invasion The Invasion that is being stopped.
	 */
	public abstract void onInvasionStop(StopReason reason, Invasion invasion);

	public String[] getNames() {
		return names;
	}

	public Optional<OfflineKingdom> getDefenderOwner(Entity entity) {
		UUID uuid = entity.getUniqueId();
		return defenders.entries().parallelStream()
				.filter(entry -> entry.getValue().getFirst().equals(uuid))
				.map(entry -> entry.getKey())
				.findFirst();
	}

	/**
	 * Get the Defender object of an Entity.
	 * 
	 * @param entity The entity to check.
	 * @return Defender object if the entity is a Defender.
	 */
	public Optional<Defender> getDefender(Entity entity) {
		UUID uuid = entity.getUniqueId();
		return defenders.entries().parallelStream()
				.map(entry -> entry.getValue())
				.filter(defender -> defender.getFirst().equals(uuid))
				.findFirst();
	}

	/**
	 * Check if an Entity is a Kingdom's defender.
	 * 
	 * @param entity The entity to check.
	 * @return boolean If the entity is a Defender.
	 */
	public boolean isDefender(Entity entity) {
		UUID entityUUID = entity.getUniqueId();
		return defenders.values().stream().anyMatch(element -> element.getFirst().equals(entityUUID));
	}

	/**
	 * @param kingdom The OfflineKingdom which has these defenders.
	 * @return Grab the Zombie Defenders that this mechanic has spawned.
	 */
	public Set<Defender> getDefenders(OfflineKingdom kingdom) {
		return defenders.get(kingdom);
	}

	/**
	 * Call this when an entity dies and it's expected to be a defender.
	 */
	public void death(LivingEntity entity) {
		getDefender(entity).ifPresent(defender -> defenders.remove(defender.getOwner(), defender));
	}

	public void stopInvasion(StopReason reason, Invasion invasion) {
		invasion.setCompleted(true);
		if (reason == StopReason.WIN)
			invasion.setWon(true);
		onInvasionStop(reason, invasion);
		OfflineKingdom target = invasion.getTarget();
		getDefenders(target).parallelStream()
				.map(defender -> defender.getDefender())
				.filter(optional -> optional.isPresent())
				.forEach(optional -> optional.get().remove());
		defenders.removeAll(target);
	}

	/**
	 * Spawn a main defender for this Invasion. This defender will be handled via Kingdoms.
	 * 
	 * @param location The location to spawn the defender at.
	 * @param invasion The Invasion this defender is connected to.
	 */
	public void spawnDefender(Location location, Invasion invasion) {
		Kingdoms instance = Kingdoms.getInstance();
		OfflineKingdom target = invasion.getTarget();
		KingdomPlayer instigator = invasion.getInstigator();
		Zombie entity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		Defender defender = new Defender(entity.getUniqueId(), invasion);
		defenders.put(target, defender);
		startChampionCountdown(entity);
		FileConfiguration configuration = instance.getConfig();
		int value = configuration.getInt("invading.defender.health", 2048);
		String name = new MessageBuilder("invading.defenders-name")
				.setPlaceholderObject(instigator)
				.setKingdom(target)
				.get();
		Material helmet = Utils.materialAttempt(configuration.getString("invading.defender.helmet", "PUMPKIN"), "PUMPKIN");
		entity.setBaby(false);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);
		entity.setTarget(instigator.getPlayer());
		entity.getEquipment().setBootsDropChance(0);
		entity.getEquipment().setHelmetDropChance(0);
		entity.getEquipment().setLeggingsDropChance(0);
		entity.getEquipment().setChestplateDropChance(0);
		DeprecationUtils.setItemInHandDropChance(entity, 0);
		entity.getEquipment().setHelmet(new ItemStack(helmet));

		// Start applying upgrades.
		DefenderInfo info = defender.getDefenderInfo();
		int health = info.getHealth() > value ? value : info.getHealth();
		DeprecationUtils.setMaxHealth(entity, health);
		double amount = info.getResistance() / 100f;
		DeprecationUtils.setKnockbackResistance(entity, amount);

		// Armour
		ItemStack armor = new ItemStack(Material.DIAMOND_CHESTPLATE);
		if (info.getArmor() > 0)
			armor.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, info.getArmor() - 1);
		entity.getEquipment().setChestplate(armor);
		Enchantment depth = DeprecationUtils.getEnchantment("DEPTH_STRIDER");
		if (info.getAqua() > 0 && depth != null) {
			ItemStack boots = entity.getEquipment().getBoots();
			if (boots == null || boots.getType() == Material.AIR) {
				entity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
				boots = entity.getEquipment().getBoots();
			}
			boots.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
			boots.addUnsafeEnchantment(depth, 10);
		}

		// Weapon
		int weapon = info.getWeapon();
		switch (weapon) {
			case 0:
				DeprecationUtils.setItemInMainHand(entity, null);
				break;
			case 1:
				Material material = Utils.materialAttempt("WOODEN_SWORD", "WOOD_SWORD");
				DeprecationUtils.setItemInMainHand(entity, new ItemStack(material));
				break;
			case 2:
				DeprecationUtils.setItemInMainHand(entity,  new ItemStack(Material.STONE_SWORD));
				break;
			case 3:
				DeprecationUtils.setItemInMainHand(entity,  new ItemStack(Material.IRON_SWORD));
				break;
			case 4:
				DeprecationUtils.setItemInMainHand(entity,  new ItemStack(Material.DIAMOND_SWORD));
				break;
			default:
				ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
				sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, weapon - 4);
				DeprecationUtils.setItemInMainHand(entity, sword);
				break;
		}
	}

	private void startChampionCountdown(Monster champion) {
		if (Utils.methodExists(Monster.class, "setInvulnerable")) {
			champion.setInvulnerable(true);
			champion.setAI(false);
			Bukkit.getScheduler().runTaskLater(Kingdoms.getInstance(), () -> {
				champion.setInvulnerable(false);
				champion.setAI(true);
			}, 40L);
		} else {
			champion.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 255));
			champion.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 255));
		}
	}

}
