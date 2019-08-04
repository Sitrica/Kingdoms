package me.limeglass.kingdoms.objects.invasions;

import java.lang.reflect.ParameterizedType;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.PlayerChangeChunkEvent;
import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.DeprecationUtils;
import me.limeglass.kingdoms.utils.MessageBuilder;
import me.limeglass.kingdoms.utils.Utils;

public abstract class InvasionMechanic<M extends InvasionTrigger> implements Listener {

	private final SetMultimap<OfflineKingdom, Defender> defenders = MultimapBuilder.hashKeys().hashSetValues().build();
	private final Class<M> trigger;
	private final String[] names;

	@SuppressWarnings("unchecked")
	protected InvasionMechanic(boolean listener, String... names) {
		this.trigger = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.names = names;
		if (listener)
			Bukkit.getPluginManager().registerEvents(this, Kingdoms.getInstance());
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
	 * Called when a player not apart of the target Kingdom breaks a block in the target Kingdom's lands.
	 * <p>
	 * The land is still in possession of the target Kingdom when this is called.
	 * 
	 * @param event The BlockBreakEvent event involved in this call.
	 * @param kingdomPlayer The player breaking the block.
	 * @param land The Land the block was broken in.
	 * @param invasion The Invasion at this land.
	 */
	public abstract void onBlockBreak(BlockBreakEvent event, KingdomPlayer kingdomPlayer, Land land, Invasion invasion);

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
	 * Called when a defender dies.
	 * 
	 * @param event The EntityDeathEvent event involved in this call.
	 * @param defender Defender that died.
	 */
	public abstract void onDefenderDeath(EntityDeathEvent event, Defender defender);

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
	 * Called when a player triggers a invade claim on a land.
	 * @param <A>
	 * 
	 * @param trigger The InvasionTrigger event involved in this call.
	 * @param kingdomPlayer The player calling the invade on the land.
	 */
	protected abstract void onInvade(M trigger, KingdomPlayer kingdomPlayer);

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

	@SuppressWarnings("unchecked")
	public <A extends InvasionTrigger> boolean callInvade(A trigger, KingdomPlayer kingdomPlayer) {
		if (!this.trigger.equals(trigger.getClass()))
			return false;
		onInvade((M) trigger, kingdomPlayer);
		return true;
	}

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
	 * @param nexus If this defender is the nexus defender.
	 */
	public Defender spawnDefender(Location location, Invasion invasion, boolean nexus) {
		Kingdoms instance = Kingdoms.getInstance();
		OfflineKingdom target = invasion.getTarget();
		KingdomPlayer instigator = invasion.getInstigator();
		Zombie entity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		Defender defender = new Defender(entity.getUniqueId(), invasion, nexus);
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
		return defender;
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
