package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.events.DefenderDamageByPlayerEvent;
import com.songoda.kingdoms.events.DefenderDamageEvent;
import com.songoda.kingdoms.events.DefenderDamageEvent.DefenderDamageCause;
import com.songoda.kingdoms.events.DefenderDamageMaxedEvent;
import com.songoda.kingdoms.events.DefenderDragEvent;
import com.songoda.kingdoms.events.DefenderFocusEvent;
import com.songoda.kingdoms.events.DefenderKnockbackEvent;
import com.songoda.kingdoms.events.DefenderMockEvent;
import com.songoda.kingdoms.events.DefenderPlowEvent;
import com.songoda.kingdoms.events.DefenderStrengthEvent;
import com.songoda.kingdoms.events.DefenderTargetEvent;
import com.songoda.kingdoms.events.DefenderThorEvent;
import com.songoda.kingdoms.events.InvadingSurrenderEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.HologramBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class InvadingManager extends Manager {

	private final Map<OfflineKingdom, DefenderInfo> infos = new HashMap<>();
	private final Map<KingdomPlayer, Location> defenders = new HashMap<>();
	private final Map<UUID, OfflineKingdom> entities = new HashMap<>();
	private final Map<KingdomPlayer, UUID> fighting = new HashMap<>();
	private final Map<UUID, Integer> dragTasks = new HashMap<>();
	private final Map<UUID, Integer> thorTasks = new HashMap<>();
	private final Map<UUID, Integer> plowTasks = new HashMap<>();
	private final Map<Land, UUID> invading = new HashMap<>();
	private Optional<CitizensManager> citizensManager;
	private final FileConfiguration defenderUpgrades;
	private final Random random = new Random();
	private TurretManager turretManager;
	private PlayerManager playerManager;
	private GuardsManager guardsManager;
	private WorldManager worldManager;
	private LandManager landManager;

	public InvadingManager() {
		super("invading", true);
		this.defenderUpgrades = instance.getConfiguration("defender-upgrades").get();
	}

	@Override
	public void initalize() {
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.turretManager = instance.getManager("turret", TurretManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.guardsManager = instance.getManager("guards", GuardsManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}

	/**
	 * Check if the Land is being invaded.
	 * 
	 * @param land Land to check if is present.
	 * @return boolean If it's being invaded.
	 */
	public boolean isBeingInvaded(Land land) {
		return invading.containsKey(land);
	}

	/**
	 * Grabs all the land chunks current being invaded and the UUID of the entity defender on the land.
	 * 
	 * @return Map<Land, UUID> mapping the land being invaded to the UUID of the defender.
	 */
	public Map<Land, UUID> getInvadingChunks() {
		return invading;
	}

	/**
	 * Check if an Entity is a Kingdom's defender.
	 * 
	 * @param entity The entity to check.
	 * @return If the entity is a Defender.
	 */
	public boolean isDefender(Entity entity) {
		return entities.containsKey(entity.getUniqueId());
	}

	/**
	 * Grab the defender fighting with the KingdomPlayer.
	 * 
	 * @param kingdomPlayer The KingdomPlayer to check.
	 * @return The defender Entity instance.
	 */
	public Optional<Entity> getDefender(KingdomPlayer kingdomPlayer) {
		UUID uuid = fighting.get(kingdomPlayer);
		if (uuid == null)
			return Optional.empty();
		return Optional.ofNullable(Bukkit.getEntity(uuid));
	}

	/**
	 * Get the Kingdom that owns the defender's UUID
	 *
	 * @param uuid Entity's UUID of defender.
	 * @return owner Optional<OfflineKingdom>
	 */
	public Optional<OfflineKingdom> getDefenderOwner(Entity entity) {
		return Optional.ofNullable(entities.get(entity.getUniqueId()));
	}

	/**
	 * Get the KingdomPlayer challenger of the Entity UUID.
	 *
	 * @param uuid Entity's UUID of defender.
	 * @return challenger Optional<KingdomPlayer>
	 */
	public Optional<KingdomPlayer> getDefenderChallenger(Entity entity) {
		return fighting.entrySet().parallelStream()
				.filter(entry -> entry.getValue() == entity.getUniqueId())
				.map(entry -> entry.getKey())
				.findFirst();
	}

	/**
	 * Get the Kingdom defender's infomation.
	 *
	 * @param kingdom OfflineKingdom to search for.
	 * @return DefenderInfo
	 */
	public DefenderInfo getDefenderInfo(OfflineKingdom kingdom) {
		return Optional.ofNullable(infos.get(kingdom)).orElseGet(() -> {
			DefenderInfo defenderInfo = new DefenderInfo(kingdom);
			infos.put(kingdom, defenderInfo);
			return defenderInfo;
		});
	}

	/**
	 * Start an invasion between Kingdom and Challenger
	 *
	 * @param location Location to spawn the defender at.
	 * @param challenger KingdomPlayer who challenges the defender
	 * @return Entity instance of defender.
	 */
	public LivingEntity startInvasion(Location location, KingdomPlayer challenger) {
		Land land = landManager.getLand(location.getChunk());
		OfflineKingdom kingdom = land.getKingdomOwner();
		if (kingdom == null)
			return null;
		if (kingdom.isOnline()) {
			kingdom.getKingdom().getOnlinePlayers().forEach(player -> defenders.put(player, location));
		}
		Player player = challenger.getPlayer();
		player.setGameMode(GameMode.SURVIVAL);
		
		// Spawn defender zombie.
		Zombie defender = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		startChampionCountdown(defender);
		int value = configuration.getInt("invading.defender.health", 2048);
		String name = new MessageBuilder("invading.defenders-name")
				.setPlaceholderObject(challenger)
				.setKingdom(kingdom)
				.get();
		Material helmet = Utils.materialAttempt(configuration.getString("invading.defender.helmet", "PUMPKIN"), "PUMPKIN");
		defender.setBaby(false);
		defender.setTarget(player);
		defender.setCustomName(name);
		defender.setCustomNameVisible(true);
		defender.getEquipment().setBootsDropChance(0);
		defender.getEquipment().setHelmetDropChance(0);
		defender.getEquipment().setLeggingsDropChance(0);
		defender.getEquipment().setChestplateDropChance(0);
		DeprecationUtils.setItemInHandDropChance(defender, 0);
		defender.getEquipment().setHelmet(new ItemStack(helmet));
		
		UUID uuid = defender.getUniqueId();
		challenger.setInvadingLand(land);
		fighting.put(challenger, uuid);
		entities.put(uuid, kingdom);
		invading.put(land, uuid);
		
		// Start applying upgrades.
		DefenderInfo info = getDefenderInfo(kingdom);
		
		int health = info.getHealth() > value ? value : info.getHealth();
		DeprecationUtils.setMaxHealth(defender, health);
		double amount = info.getResistance() / 100f;
		DeprecationUtils.setKnockbackResistance(defender, amount);

		// 200 bonus health for Nexus defense
		Structure structure = land.getStructure();
		if (structure != null) {
			if (structure.getType() == StructureType.NEXUS) {
				Land nexusLand = landManager.getLand(structure.getLocation().getChunk());
				if (land.equals(nexusLand)) {
					DeprecationUtils.setMaxHealth(defender, defender.getHealth() + 200);
					if (kingdom.getMiscUpgrades().hasNexusGuard()) {
						guardsManager.spawnNexusGuard(location, kingdom, challenger);
					}
				}
			} else if (structure.getType() == StructureType.POWERCELL) {
				if (kingdom.getMiscUpgrades().hasInsanity() && MiscUpgradeType.INSANITY.isEnabled())
					defender.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000, 1));
			}
		}

		ItemStack armor = new ItemStack(Material.DIAMOND_CHESTPLATE);
		if (info.getArmor() > 0)
			armor.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, info.getArmor() - 1);
		defender.getEquipment().setChestplate(armor);
		
		if (info.getMimic() > 0) {
			ItemStack item = player.getInventory().getHelmet();
			if (item == null)
				item = new ItemStack(Material.DIAMOND_HELMET);
			defender.getEquipment().setHelmet(item);
			item = player.getInventory().getLeggings();
			if (item == null)
				item = new ItemStack(Material.DIAMOND_LEGGINGS);
			defender.getEquipment().setLeggings(item);
			item = player.getInventory().getBoots();
			if (item == null)
				item = new ItemStack(Material.DIAMOND_BOOTS);
			defender.getEquipment().setBoots(item);
		}

		Enchantment depth = DeprecationUtils.getEnchantment("DEPTH_STRIDER");
		if (info.getAqua() > 0 && depth != null) {
			ItemStack boots = defender.getEquipment().getBoots();
			if (boots == null || boots.getType() == Material.AIR) {
				defender.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
				boots = defender.getEquipment().getBoots();
			}
			boots.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
			boots.addUnsafeEnchantment(depth, 10);
		}

		int weapon = info.getWeapon();
		switch (weapon) {
			case 0:
				DeprecationUtils.setItemInMainHand(defender, null);
				break;
			case 1:
				Material material = Utils.materialAttempt("WOODEN_SWORD", "WOOD_SWORD");
				DeprecationUtils.setItemInMainHand(defender, new ItemStack(material));
				break;
			case 2:
				DeprecationUtils.setItemInMainHand(defender,  new ItemStack(Material.STONE_SWORD));
				break;
			case 3:
				DeprecationUtils.setItemInMainHand(defender,  new ItemStack(Material.IRON_SWORD));
				break;
			case 4:
				DeprecationUtils.setItemInMainHand(defender,  new ItemStack(Material.DIAMOND_SWORD));
				break;
			default:
				ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
				sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, weapon - 4);
				DeprecationUtils.setItemInMainHand(defender, sword);
				break;
		}

		int speed = info.getSpeed();
		if (speed > 0)
			defender.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, speed - 1));

		int drag = info.getDrag();
		if (drag > 0) {
			int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
				@Override
				public void run() {				
					if (!player.isDead() && !defender.isDead() && defender.isValid() && player.isOnline()) {
						DefenderDragEvent event = new DefenderDragEvent(kingdom, defender, challenger, defenderUpgrades.getDouble("upgrades.drag.range", 7));
						Bukkit.getPluginManager().callEvent(event);
						if (!event.isCancelled() && player.getLocation().distance(location) > event.getRange() + (2 * drag)) {
							player.teleport(location); //TODO remake this to have a pulling animation.
							new MessageBuilder("defenders.drag")
									.setPlaceholderObject(player)
									.setKingdom(kingdom)
									.send(player);
						}
					}
				}
			}, 1L, 40L);
			dragTasks.put(defender.getUniqueId(), task);
		}

		int plow = info.getPlow();
		if (plow > 0) {
			plowTasks.put(defender.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
				@Override
				public void run() {
					if (!player.isDead() && !defender.isDead() && defender.isValid() && player.isOnline()) {
						int radius = 1;
						for (int x = -radius; x <= radius; x++) {
							for (int y = -radius; y <= radius; y++) {
								for (int z = -radius; z <= radius; z++) {
									Block block = location.getBlock().getRelative(x, y, z);
									Material type = block.getType();
									if (type == Utils.materialAttempt("COBWEB", "WEB") || type == Material.LAVA) {
										DefenderPlowEvent plowEvent = new DefenderPlowEvent(kingdom, defender, block);
										if (!plowEvent.isCancelled())
											location.getBlock().setType(Material.AIR);
									}
								}
							}
						}
					}
				}
			}, 1L, 5L));
		}

		int thor = info.getThor();
		if (thor > 0) {
			thorTasks.put(defender.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
				if (!player.isDead() && !defender.isDead() && defender.isValid() && player.isOnline()) {
					sendLightning(player, player.getLocation());
					player.damage(thor, defender);
					new MessageBuilder("defenders.defender-thor")
							.setPlaceholderObject(kingdomPlayer)
							.setKingdom(kingdom)
							.send(player);
				}
				for (Entity entity : defender.getNearbyEntities(7, 7, 7)) {
					if (!(entity instanceof Player))
						continue;
					if (entity.getUniqueId().equals(player.getUniqueId()))
						continue;
					Player p = (Player) entity;
					kingdomPlayer = playerManager.getKingdomPlayer(p);
					Kingdom nearKingdom = kingdomPlayer.getKingdom();
					if (nearKingdom == null || (!nearKingdom.equals(kingdom) && !nearKingdom.isAllianceWith(kingdom))) {
						DefenderThorEvent thorEvent = new DefenderThorEvent(kingdom, defender, kingdomPlayer);
						Bukkit.getPluginManager().callEvent(thorEvent);
						if (thorEvent.isCancelled())
							return;
						sendLightning(p, p.getLocation());
						p.damage(thorEvent.getDamage(), defender);
						new MessageBuilder("defenders.defender-thor")
								.setPlaceholderObject(kingdomPlayer)
								.setKingdom(kingdom)
								.send(p);
					}
				}
			}, 1L, (long) configuration.getDouble("kingdoms.defenders.thor-delay") * 20L));
		}
		return defender;
	}

	/**
	 * Stops the fight between defender on challenger.
	 *
	 * @param challenger KingdomPlayer challenger.
	 */
	public void stopFight(KingdomPlayer challenger) {
		UUID uuid = fighting.get(challenger);
		Entity defender = Bukkit.getEntity(uuid);
		if (defender == null)
			return;
		Integer dragTask = dragTasks.remove(uuid);
		Integer thorTask = thorTasks.remove(uuid);
		Integer plowTask = plowTasks.remove(uuid);
		if (dragTask != null)
			Bukkit.getScheduler().cancelTask(dragTask);
		if (thorTask != null)
			Bukkit.getScheduler().cancelTask(thorTask);
		if (plowTask != null)
			Bukkit.getScheduler().cancelTask(plowTask);
		entities.remove(uuid);
		fighting.remove(challenger);
		invading.remove(challenger.getInvadingLand());
		challenger.setInvadingLand(null);
		defender.remove();
	}

	/**
	 * Spawn custom defender.
	 *
	 * @param location Location of defender to be spawned.
	 * @param challenger KingdomPlayer who challenges the defender.
	 * @param type EntityType of mob the defender is. (MUST EXTEND MONSTER)
	 * @return Monster instance of defender.
	 */
	public Monster spawnDefender(Location location, final KingdomPlayer challenger, EntityType type) {
		Land land = landManager.getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return null;
		Player player = challenger.getPlayer();
		player.setGameMode(GameMode.SURVIVAL);
		Monster defender = (Monster) location.getWorld().spawnEntity(location, type);
		startChampionCountdown(defender);
		challenger.setInvadingLand(land);
		UUID uuid = defender.getUniqueId();
		entities.put(uuid, landKingdom);
		fighting.put(challenger, uuid);
		invading.put(land, uuid);
		defender.setTarget(player);
		return defender;
	}

	private void startChampionCountdown(Monster champion) {
		try {
			Method invulnerable = Monster.class.getMethod("setInvulnerable");
			invulnerable.setAccessible(true);
			Method AI = Monster.class.getMethod("setAI");
			AI.setAccessible(true);
			invulnerable.invoke(champion, true);
			AI.invoke(champion, false);
			Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
				@Override
				public void run() {
					try {
						invulnerable.invoke(champion, false);
						AI.invoke(champion, true);
					} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}, 40L);
		} catch(NoSuchMethodException e) {
			champion.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 255));
			champion.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 255));
		} catch(IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChallengerQuit(PlayerQuitEvent event) {
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(event.getPlayer());
		Optional<Entity> defender = getDefender(kingdomPlayer);
		if (!defender.isPresent())
			return;
		Land land = kingdomPlayer.getInvadingLand();
		if (land == null)
			return;
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null) {
			stopFight(kingdomPlayer);
			return;
		}
		stopFight(kingdomPlayer);
		Bukkit.getPluginManager().callEvent(new InvadingSurrenderEvent(kingdomPlayer, landKingdom, land));
	}

	@EventHandler
	public void onChallengerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
				return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Optional<Entity> defender = getDefender(kingdomPlayer);
		if (!defender.isPresent())
			return;
		Land land = kingdomPlayer.getInvadingLand();
		if (land == null)
			return;
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null) {
			stopFight(kingdomPlayer);
			return;
		}
		stopFight(kingdomPlayer);
		Bukkit.getPluginManager().callEvent(new InvadingSurrenderEvent(kingdomPlayer, landKingdom, land));
	}

	@EventHandler
	public void onChampionEnterVehicle(VehicleEnterEvent event) {
		if (!isDefender(event.getEntered()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderVoidDamage(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.VOID)
			return;
		if (!configuration.getBoolean("invading.defender.void-death-end-invasion"))
			return;
		Entity entity = event.getEntity();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(entity))
				return;
		if (!worldManager.acceptsWorld(entity.getWorld()))
			return;
		Optional<KingdomPlayer> optional = getDefenderChallenger(entity);
		if (!optional.isPresent())
			return;
		KingdomPlayer challenger = optional.get();
		Kingdom kingdom = challenger.getKingdom();
		if (kingdom == null) 
			return;
		Land land = challenger.getInvadingLand();
		OfflineKingdom landKingdom = land.getKingdomOwner();
		Bukkit.getPluginManager().callEvent(new InvadingSurrenderEvent(challenger, landKingdom, land));
		stopFight(challenger);
		new MessageBuilder("kingdoms.defender-void-death")
				.setPlaceholderObject(challenger)
				.setKingdom(landKingdom)
				.send(challenger);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderDamage(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(entity))
				return;
		if (!worldManager.acceptsWorld(entity.getWorld()))
			return;
		Optional<OfflineKingdom> optional = getDefenderOwner(entity);
		if (!optional.isPresent())
			return;
		OfflineKingdom landKingdom = optional.get();
		Entity attacker = event.getDamager();
		if (!(attacker instanceof Player))
			return;
		Player damager = (Player) attacker;
		KingdomPlayer challenger = playerManager.getKingdomPlayer(damager);
		Kingdom kingdom = challenger.getKingdom();
		if (kingdom == null)
			return;
		if (kingdom.equals(landKingdom)) {
			new MessageBuilder("kingdoms.defender-own")
					.setPlaceholderObject(challenger)
					.setKingdom(kingdom)
					.send(challenger);
			event.setDamage(0.0D);
			return;
		}
		DefenderDamageByPlayerEvent damageEvent = new DefenderDamageByPlayerEvent(kingdom, entity, challenger, event.getDamage());
		if (damageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		} else {
			event.setDamage(damageEvent.getDamage());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderTurretDamage(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		World world = event.getEntity().getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		Optional<OfflineKingdom> optional = getDefenderOwner(victim);
		if (!optional.isPresent())
			return;
		OfflineKingdom defenderKingdom = optional.get();
		Entity attacker = event.getDamager();
		optional = turretManager.getProjectileKingdom(attacker);
		if (!optional.isPresent())
			return;
		OfflineKingdom turretKingdom = optional.get();
		if (turretKingdom.equals(defenderKingdom)) {
			event.setCancelled(true);
			event.setDamage(0);
			return;
		}
		DefenderDamageEvent damageEvent = new DefenderDamageEvent(defenderKingdom, victim, event.getDamage(), DefenderDamageCause.TURRET);
		Bukkit.getPluginManager().callEvent(damageEvent);
		if (!damageEvent.isCancelled())
			event.setDamage(damageEvent.getDamage());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderPotionDamage(PotionSplashEvent event) {
		Entity victim = event.getHitEntity();
		World world = victim.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		Iterator<LivingEntity> iterator = event.getAffectedEntities().iterator();
		while (iterator.hasNext()) {
			Entity entity = iterator.next();
			Optional<OfflineKingdom> optional = turretManager.getProjectileKingdom(entity);
			if (!optional.isPresent())
				return;
			OfflineKingdom defenderKingdom = optional.get();
			ProjectileSource thrower = event.getPotion().getShooter();
			if (!(thrower instanceof Player))
				continue;
			Player player = (Player) thrower;
			KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
			if (kingdomPlayer.hasAdminMode())
				continue;
			Kingdom kingdom = kingdomPlayer.getKingdom();
			if (kingdom == null)
				continue;
			if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
				iterator.remove();
			if (defenderKingdom.equals(kingdom) || defenderKingdom.isAllianceWith(kingdom))
				iterator.remove();
		}
	}

	@EventHandler
	public void onDefenderDeath(EntityDeathEvent event) {
		LivingEntity victim = event.getEntity();
		World world = victim.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(victim))
				return;
		Optional<OfflineKingdom> optional = turretManager.getProjectileKingdom(victim);
		if (!optional.isPresent())
			return;
		Player attacker = victim.getKiller();
		if (attacker == null)
			return;
		Optional<KingdomPlayer> challengerOptional = getDefenderChallenger(victim);
		if (!challengerOptional.isPresent())
			return;
		KingdomPlayer challenger = challengerOptional.get();
		Kingdom kingdom = challenger.getKingdom();
		if (kingdom == null)
			return;
		Land land = challenger.getInvadingLand();
		if (land == null)
			return;
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null) {
			stopFight(challenger);
			return;
		}	
		stopFight(challenger);
		event.getDrops().clear();
		// Should not be the end of the invasion, should only be a Defender death event.
		//instance.getServer().getPluginManager().callEvent(new KingdomPlayerWonEvent(challenger, defenderKingdom, land));
	}

	@EventHandler
	public void onDefenderDeathWhileInvade(PlayerDeathEvent event) {
		Player player = event.getEntity();
		World world = player.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(player))
				return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (!getDefender(kingdomPlayer).isPresent())
			return;
		stopFight(kingdomPlayer);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandWhileFight(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Optional<Entity> defender = getDefender(kingdomPlayer);
		if (!defender.isPresent())
			return;
		String command = event.getMessage();
		if (command.equalsIgnoreCase("/k ff") || command.equalsIgnoreCase("/kingdoms ff") || command.equalsIgnoreCase("/kingdom ff")) {
			if (player.hasPermission("kingdoms.surrender") || player.hasPermission("kingdoms.player"))
				return;
		}
		if (command.equalsIgnoreCase("/k forfeit") || command.equalsIgnoreCase("/kingdoms forfeit") || command.equalsIgnoreCase("/kingdom forfeit")) {
			if (player.hasPermission("kingdoms.surrender") || player.hasPermission("kingdoms.player"))
				return;
		}
		if (configuration.getStringList("commands.allowed-during-invasion").contains(command))
			return;
		Optional<OfflineKingdom> landKingdom = getDefenderOwner(defender.get());
		new MessageBuilder("kingdoms.defender-command-blocked")
				.setKingdom(landKingdom.isPresent() ? landKingdom.get() : null)
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		event.setCancelled(true);
	}

	@EventHandler
	public void onTargetChange(EntityTargetLivingEntityEvent event) {
		Entity entity = event.getEntity();
		World world = entity.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(entity))
				return;
		Optional<OfflineKingdom> optional = getDefenderOwner(entity);
	    if (!optional.isPresent())
	        return;
	    OfflineKingdom defenderKingdom = optional.get();
		Optional<KingdomPlayer> challengerOptional = getDefenderChallenger(entity);
		if (!challengerOptional.isPresent())
			return;
		KingdomPlayer challenger = challengerOptional.get();
		LivingEntity target = event.getTarget();
		DefenderTargetEvent targetEvent = new DefenderTargetEvent(defenderKingdom, entity, target);
		Bukkit.getPluginManager().callEvent(targetEvent);
		if (targetEvent.isCancelled())
			return;
		if (target instanceof Player) {
			KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer((Player) target);
			Kingdom kingdom = kingdomPlayer.getKingdom();
			if (kingdom == null)
				return;
			// If the target doesn't belong to the defenders Kingdom nor an alliance with them, continue to target them.
			if (!defenderKingdom.equals(kingdom) && !defenderKingdom.isAllianceWith(kingdom))
				return;
		}
		// If the target is an entity.
		event.setTarget(challenger.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProjectileKnockback(EntityDamageByEntityEvent event) {
		DamageCause cause = event.getCause();
		if (cause != DamageCause.ENTITY_ATTACK || cause != DamageCause.PROJECTILE)
			return;
		Entity entity = event.getEntity();
		Optional<OfflineKingdom> optional = getDefenderOwner(entity);
		if (!optional.isPresent())
			return;
		OfflineKingdom kingdom = optional.get();
		DefenderInfo info = getDefenderInfo(kingdom);
		int resistance = info.getResistance();
		if (resistance <= 0)
			return;
		if (random.nextInt(100) <= resistance) {
			DefenderKnockbackEvent knockbackEvent = new DefenderKnockbackEvent(kingdom, entity, event.getDamager());
			Bukkit.getPluginManager().callEvent(knockbackEvent);
			if (knockbackEvent.isCancelled())
				return;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> entity.setVelocity(new Vector()), 1);
	
		}
	}

	@EventHandler
	public void onMockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		Optional<Entity> optional = getDefender(kingdomPlayer);
		if (!optional.isPresent())
			return;
		Entity defender = optional.get();
		Optional<OfflineKingdom> kingdomOptional = getDefenderOwner(defender);
		if (!kingdomOptional.isPresent())
			return;
		OfflineKingdom defenderKingdom = kingdomOptional.get();
		DefenderInfo info = getDefenderInfo(defenderKingdom);
		int mock = info.getMock();
		if (mock <= 0)
			return;
		DefenderMockEvent mockEvent = new DefenderMockEvent(defenderKingdom, defender, mock);
		Bukkit.getPluginManager().callEvent(mockEvent);
		if (mockEvent.isCancelled())
			return;
		mock = mockEvent.getRange();
		Location location = defender.getLocation();
		Block block = event.getBlock();
		if (block.getLocation().distanceSquared(location) > mock * mock)
			return;
		if (mockEvent.isEventCancelled())
			event.setCancelled(true);
		new MessageBuilder("kingdoms.defender-mock")
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(defenderKingdom)
				.replace("%mock%", mock)
				.send(player);
		return;
	}

	@EventHandler
	public void onDefenderDamageNonInvader(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!(victim instanceof Player)) // Defender can only target players in other parts of the code.
			return;
		Entity attacker = event.getDamager();
		Player player = (Player) victim;
		if (citizensManager.isPresent()) {
			CitizensManager citizens = citizensManager.get();
			if (citizens.isCitizen(player) || citizens.isCitizen(attacker))
				return;
		}
		if (!isDefender(attacker))
			return;
		Optional<OfflineKingdom> defenderOptional = getDefenderOwner(attacker);
		if (!defenderOptional.isPresent())
			return;
		OfflineKingdom defenderKingdom = defenderOptional.get();
		DefenderInfo info = getDefenderInfo(defenderKingdom);
		int duel = info.getDuel();
		if (duel <= 0)
			return;
		event.setDamage(event.getDamage() * 2);//double to non-invader
	}

	@EventHandler
	public void onNonInvaderDamageDefender(EntityDamageByEntityEvent event) {
		Entity attacker = event.getDamager();
		if(!(attacker instanceof Player))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(attacker))
				return;
		Player player = (Player) attacker;
		Entity victim = event.getEntity();
		if (!isDefender(victim))
			return;
		Optional<OfflineKingdom> optional = getDefenderOwner(victim);
		if (!optional.isPresent())
			return;
		OfflineKingdom defenderKingdom = optional.get();
		DefenderInfo info = getDefenderInfo(defenderKingdom);
		int duel = info.getDuel();
		if (duel <= 0)
			return;
		event.setDamage(event.getDamage() / 2);
		new HologramBuilder(victim.getLocation().add(0, 1, 0), "holograms.defender-divided")
				.withDefaultExpiration("2 seconds")
				.setPlaceholderObject(player)
				.setKingdom(defenderKingdom)
				.send(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDefenderDamageMaxed(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		Optional<OfflineKingdom> optional = getDefenderOwner(victim);
		if (!optional.isPresent())
			return;
		OfflineKingdom defenderKingdom = optional.get();
		DefenderInfo info = getDefenderInfo(defenderKingdom);
		int limit = info.getDamageLimit();
		if (limit <= 0)
			return;
		double damage = event.getDamage();
		if (damage > 15) {
			DefenderDamageMaxedEvent damageEvent = new DefenderDamageMaxedEvent(defenderKingdom, victim, event.getDamager(), limit, damage);
			Bukkit.getPluginManager().callEvent(damageEvent);
			if (!damageEvent.isCancelled())
				event.setDamage(damageEvent.getLimit());
		}
	}

	@EventHandler
	public void onFocus(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!(victim instanceof Player))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(victim))
				return;
		Player player = (Player) victim;
		Entity attacker = event.getDamager();
		Optional<OfflineKingdom> optional = getDefenderOwner(attacker);
		if (!optional.isPresent())
			return;
		OfflineKingdom defenderKingdom = optional.get();
		DefenderInfo info = getDefenderInfo(defenderKingdom);
		int focus = info.getFocus();
		if (focus <= 0)
			return;
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		if (effects.size() > 0) {
			DefenderFocusEvent focusEvent = new DefenderFocusEvent(defenderKingdom, attacker, playerManager.getKingdomPlayer(player));
			Bukkit.getPluginManager().callEvent(focusEvent);
			if (focusEvent.isCancelled())
				return;
			for (PotionEffect effect : effects) {
				PotionEffect potion = new PotionEffect(effect.getType(), effect.getDuration() - 1, effect.getAmplifier());
				player.removePotionEffect(effect.getType());
				player.addPotionEffect(potion);
			}
		}
	}

	@EventHandler
	public void onDamageWhileStrength(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (!(victim instanceof Player))
			return;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(victim))
				return;
		Player player = (Player) victim;
		Entity attacker = event.getDamager();
		Optional<OfflineKingdom> optional = getDefenderOwner(attacker);
		if (!optional.isPresent())
			return;
		OfflineKingdom defenderKingdom = optional.get();
		DefenderInfo info = getDefenderInfo(defenderKingdom);
		// TODO this is now called throw.
		int strength = info.getStrength();
		if (strength <= 0)
			return;
		if (new Random().nextInt(100) <= strength) {
			DefenderStrengthEvent strengthEvent = new DefenderStrengthEvent(defenderKingdom, attacker, playerManager.getKingdomPlayer(player), strength);
			Bukkit.getPluginManager().callEvent(strengthEvent);
			if (strengthEvent.isCancelled())
				return;
			victim.setVelocity(new Vector(0, 1.5, 0));
		}
	}
	
	private final Class<?> weatherPacket = getNMSClass("PacketPlayOutSpawnEntityWeather");
	private final Class<?> lightningClass = getNMSClass("EntityLightning");
	private final Class<?> entityClass = getNMSClass("Entity");
	private final Class<?> worldClass = getNMSClass("World");

	public void sendLightning(Player player, Location location) {
		try {
			Constructor<?> constructor = lightningClass.getConstructor(worldClass, double.class, double.class, double.class, boolean.class, boolean.class);
			Object world = player.getWorld().getClass().getMethod("getHandle").invoke(player.getWorld());
			Object lightning = constructor.newInstance(world, location.getX(), location.getY(), location.getZ(), false, false);
			Object object = weatherPacket.getConstructor(entityClass).newInstance(lightning);
			sendPacket(player, object);
			Sound sound = Utils.soundAttempt("ENTITY_LIGHTNING_BOLT_THUNDER", "AMBIENCE_THUNDER");
			if (sound == null) //1.9-1.12 users...
				sound = Utils.soundAttempt("ENTITY_LIGHTNING_THUNDER", "LIGHTNING_THUNDER");
			player.playSound(player.getLocation(), sound, 100, 1);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Class<?> getNMSClass(String name) {
		String version = instance.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void sendPacket(Player player, Object packet) {
		try{
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		infos.clear(); //TODO save these
		fighting.keySet().forEach(kingdomPlayer -> stopFight(kingdomPlayer));
		fighting.clear();
		entities.clear();
		invading.clear();
		defenders.clear();
		dragTasks.values().forEach(task -> instance.getServer().getScheduler().cancelTask(task));
		thorTasks.values().forEach(task -> instance.getServer().getScheduler().cancelTask(task));
		plowTasks.values().forEach(task -> instance.getServer().getScheduler().cancelTask(task));
	}

}
