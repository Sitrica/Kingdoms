package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.events.InvadingSurrenderEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.kingdom.ChampionUpgrade;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.land.Structure;
import com.songoda.kingdoms.objects.land.StructureType;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.TurretUtil;
import com.songoda.kingdoms.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InvadingManager extends Manager {

	static {
		registerManager("invading", new InvadingManager());
	}

	//private final Map<Integer, KingdomPlayer> targets = new ConcurrentHashMap<>();
	//private final Map<Integer, Integer> determination = new HashMap<>();

	private final Map<OfflineKingdom, DefenderInfo> infos = new HashMap<>();
	private final Map<KingdomPlayer, Location> defenders = new HashMap<>();
	private final Map<UUID, OfflineKingdom> entities = new HashMap<>();
	private final Map<KingdomPlayer, UUID> fighting = new HashMap<>();
	private final Map<UUID, Integer> dragTasks = new HashMap<>();
	private final Map<UUID, Integer> thorTasks = new HashMap<>();
	private final Map<UUID, Integer> plowTasks = new HashMap<>();
	private final Map<Land, UUID> invading = new HashMap<>();
	private final CitizensManager citizensManager;
	private final PlayerManager playerManager;
	private final WorldManager worldManager;
	private final LandManager landManager;

	protected InvadingManager() {
		super(true);
		this.citizensManager = instance.getManager("citizens", CitizensManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
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
	public Entity getDefender(KingdomPlayer kingdomPlayer) {
		Optional<UUID> uuid = Optional.ofNullable(fighting.get(kingdomPlayer));
		if (!uuid.isPresent())
			return null;
		return Bukkit.getEntity(uuid.get());
	}

	/**
	 * Get the Kingdom that owns the defender's UUID
	 *
	 * @param uuid Entity's UUID of defender.
	 * @return owner Optional<OfflineKingdom>
	 */
	public Optional<OfflineKingdom> getDefenderOwner(UUID uuid) {
		return Optional.ofNullable(entities.get(uuid));
	}
	
	/**
	 * Get the KingdomPlayer challenger of the Entity UUID.
	 *
	 * @param uuid Entity's UUID of defender.
	 * @return challenger Optional<KingdomPlayer>
	 */
	public Optional<KingdomPlayer> getDefenderChallenger(UUID uuid) {
		return fighting.entrySet().parallelStream()
				.filter(entry -> entry.getValue() == uuid)
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
			DefenderInfo defenderInfo = new DefenderInfo();
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
		DefenderInfo info = getDefenderInfo(kingdom);
		Player player = challenger.getPlayer();
		player.setGameMode(GameMode.SURVIVAL);
		
		// Spawn defender zombie.
		Zombie defender = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		startChampionCountdown(defender);
		int value = configuration.getInt("invading.defender.health", 2048);
		int health = info.getHealth() > value ? value : info.getHealth();
		DeprecationUtils.setMaxHealth(defender, health);
		double amount = info.getResistance() / 100f;
		DeprecationUtils.setKnockbackResistance(defender, amount);
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

		// 200 bonus health for Nexus defense
		Structure structure = land.getStructure();
		if (structure != null) {
			if (structure.getType() == StructureType.NEXUS) {
				Land nexusLand = landManager.getLand(structure.getLocation().getChunk());
				if (land.equals(nexusLand)) {
					DeprecationUtils.setMaxHealth(defender, defender.getHealth() + 200);
					if (kingdom.getMisupgradeInfo().hasNexusguard()) {
						GameManagement.getGuardsManager().spawnNexusGuard(location, kingdom, challenger);
						//callReinforcement(location, challenger, 50);
					}
				}
			} else if (structure.getType() == StructureType.POWERCELL) {
				if (kingdom.getMisupgradeInfo().isPsioniccore() && Config.getConfig().getBoolean("enable.psioniccore"))
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
						ChampionDragEvent event = new ChampionDragEvent(defender, challenger);
						Bukkit.getPluginManager().callEvent(event);
						if (!event.isCancelled() && player.getLocation().distance(location) > event.getDragRange() + (2 * drag)) {
							player.teleport(location); //TODO remake this to have a pulling animation.
							player.sendMessage(Kingdoms.getLang().getString("Champion_Drag", kp.getLang()));
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
									if (type == Materials.COBWEB.parseMaterial() || type == Material.LAVA) {
										ChampionPlowEvent plowEvent = new ChampionPlowEvent(champion, block);
										if (!plowEvent.isCancelled()) {
											location.getBlock().setType(Material.AIR);
										}
									}
								}
							}
						}
					}
				}
			}, 1L, 5L));
		}

		//champ.aqua champ teleport to challenger in water
		//int aqua = info.get (?)

		//champ.thor struct lightning and damage manually, 6 dmg, 8dmg all around 7,7,7 area if not ally
		int thor = info.getThor();
		if (thor > 0) {
			thorTasks.put(defender.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
				@Override
				public void run() {
					if (!player.isDead() && !defender.isDead() && defender.isValid() && player.isOnline()) {
						sendLightning(player);
						p.damage(kingdom.getChampionInfo().getThor(), champion);
	
						p.sendMessage(Kingdoms.getLang().getString("Champion_Thor", GameManagement.getPlayerManager().getSession(p).getLang()));
						}
	
						for(Entity e : champion.getNearbyEntities(7, 7, 7)){
						if(e instanceof Player && !e.getUniqueId().equals(p.getUniqueId())){
							KingdomPlayer kpNear = GameManagement.getPlayerManager().getSession(e.getUniqueId());
							if(kpNear.getKingdom() == null || (!kpNear.getKingdom().equals(kingdom)
								&& !kpNear.getKingdom().isAllianceWith(kingdom))){
							ChampionThorEvent thorEvent = new ChampionThorEvent(champion, GameManagement.getPlayerManager().getSession(p));
							Bukkit.getPluginManager().callEvent(thorEvent);
							if(thorEvent.isCancelled()){
								return;
							}
							//p.getWorld().strikeLightningEffect(p.getLocation());
							sendLightning(p, p.getLocation());
							p.damage(thorEvent.getDmg(), champion);
	
							p.sendMessage(Kingdoms.getLang().getString("Champion_Thor", GameManagement.getPlayerManager().getSession(p).getLang()));
							}
						}
					}
				}
			}, 1L, (long) (Config.getConfig().getDouble("champion-specs.thor-delay") * 20L)));
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
		fighting.remove(uuid);
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
		Entity defender = getDefender(kingdomPlayer);
		if (defender == null)
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
		if (citizensManager.isCitizen(player))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Entity defender = getDefender(kingdomPlayer);
		if (defender == null)
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDefenderDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (citizensManager.isCitizen(entity))
			return;
		World world = entity.getWorld();
		if (!worldManager.acceptsWorld(world))
			return;
		if (!entities.containsKey(entity.getUniqueId()))
			return;
		if(!(entity instanceof Damageable))
			return;
		Damageable defender = (Damageable) entity;
		OfflineKingdom kingdom = entities.get(defender.getUniqueId());
		//String name = new MessageBuilder("invading.defenders-name")
		//		.setPlaceholderObject(defender)
		//		.setKingdom(kingdom)
		//		.get();
		String name = ChatColor.RED + kingdom.getKingdomName() + "'s Champion "
			+ ChatColor.GRAY + "[" +
			ChatColor.GREEN + ((int) (champion.getHealth() - e.getFinalDamage())) +
			ChatColor.AQUA + "/" +
			ChatColor.GREEN + champion.getMaxHealth() +
			ChatColor.GRAY + "]";
		if (kingdom.getChampionInfo().getDetermination() > 0) {
			name += ChatColor.GRAY + "[" + ChatColor.RED
				+ determination.get(e.getEntity().getEntityId())
				+ ChatColor.AQUA + "/"
				+ ChatColor.RED
				+ kingdom.getChampionInfo().getDetermination() + ChatColor.GRAY + "]";
		}
		defender.setCustomName(name);
		defender.setCustomNameVisible(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderVoidDamage(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.VOID)
			return;
		if (!Config.getConfig().getBoolean("champion-specs.invader-lose-on-champion-void-damage"))
			return;
		Entity entity = event.getEntity();
		if (citizensManager.isCitizen(entity))
			return;
		if (!worldManager.acceptsWorld(entity.getWorld()))
			return;
		Optional<KingdomPlayer> optional = getDefenderChallenger(entity.getUniqueId());
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
		challenger.sendMessage(Kingdoms.getLang().getString("Champion_Void_Death", challenger.getLang()));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDefenderDamage(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (citizensManager.isCitizen(entity))
			return;
		if (!worldManager.acceptsWorld(entity.getWorld()))
			return;
		Optional<OfflineKingdom> optional = getDefenderOwner(entity.getUniqueId());
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
			challenger.sendMessage(Kingdoms.getLang().getString("Champion_Own_Kingdom", challenger.getLang()));
			event.setDamage(0.0D);
			return;
		}
		ChampionByPlayerDamageEvent damageEvent = new ChampionByPlayerDamageEvent(e.getEntity(), challenger, e.getDamage());
		if (damageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		} else {
			event.setDamage(damageEvent.getDamage());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChampionDetermination(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (citizensManager.isCitizen(entity))
			return;
		if (!worldManager.acceptsWorld(entity.getWorld()))
			return;
		Optional<OfflineKingdom> optional = getDefenderOwner(entity.getUniqueId());
		if (!optional.isPresent())
			return;
		OfflineKingdom landKingdom = optional.get().getKingdom();
		Entity attacker = event.getDamager();
		if (!(attacker instanceof Player))
			return;
		Player damager = (Player) attacker;
		KingdomPlayer challenger = playerManager.getKingdomPlayer(damager);
		DefenderInfo info = this.getDefenderInfo(landKingdom);
		if (info.getDetermination() > 0) {
			//Also this appears to ignore damage to an extent? I do 14 dmg and boss has 1 determination it does nothing???
			//Also why only rip determination when hit by another entity?
			if (!determination.containsKey(e.getEntity().getEntityId())) {
				determination.put(e.getEntity().getEntityId(), info.getDetermination());
			}
			if(determination.get(e.getEntity().getEntityId()) > 0) {
				ChampionDeterminationDamageEvent determinationDamageEvent =
					new ChampionDeterminationDamageEvent(e.getEntity(), e.getDamage(), determination.get(e.getEntity().getEntityId()), challenger);
				Bukkit.getPluginManager().callEvent(determinationDamageEvent);
				if(determinationDamageEvent.isCancelled()){
					return;
			}
			e.setDamage(determinationDamageEvent.getDamage());
			int newd = (int) (determination.get(e.getEntity().getEntityId()) - e.getDamage());
			e.setDamage(0.0);
			//TODO possible fix
			/*
			if(newd < 0){
				e.setDamage(-1 * newD);
			}
			else{
				e.setDamage(0)
			}
			 */
			if (newd < 0)
				newd = 0;
			determination.put(e.getEntity().getEntityId(), newd);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChampionDamageByTurretArrow(EntityDamageByEntityEvent e){
	World bukkitWorld = e.getEntity().getWorld();
	if(!Config.getConfig().getStringList("enabled-worlds").contains(bukkitWorld.getName())) return;

	if(!entityOwners.containsKey(e.getEntity().getEntityId())) return;
	Kingdom champKingdom = entityOwners.get(e.getEntity().getEntityId());

	if(!(e.getDamager() instanceof Arrow)) return;

	Arrow a = (Arrow) e.getDamager();

	if(a.getMetadata(TurretUtil.META_SHOOTER) == null) return;
	if(a.getMetadata(TurretUtil.META_SHOOTER).size() < 1) return;

	String shooterKingdom = a.getMetadata(TurretUtil.META_SHOOTER).get(0).asString();
	if(shooterKingdom == null) return;

	Kingdom shootKingdom = GameManagement.getKingdomManager().getOrLoadKingdom(shooterKingdom);
	if(shootKingdom == null) return;

	if(shootKingdom.equals(champKingdom)){
		e.setDamage(0.0D);
		e.setCancelled(true);
		return;
	}

	ChampionDamageEvent damageEvent = new ChampionDamageEvent(e.getEntity(), e.getDamage(), ChampionDamageEvent.ChampionDamageCause.TURRET);
	Bukkit.getPluginManager().callEvent(damageEvent);
	if(!damageEvent.isCancelled()){
		e.setDamage(damageEvent.getDamage());
	}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChampionDamageByPotion(PotionSplashEvent e){

	World bukkitWorld = e.getEntity().getWorld();
	if(!Config.getConfig().getStringList("enabled-worlds").contains(bukkitWorld.getName())) return;

	for(Iterator<LivingEntity> iter = e.getAffectedEntities().iterator(); iter.hasNext(); ){
		Entity entity = iter.next();
		if(!entityOwners.containsKey(entity.getEntityId())) continue;
		Kingdom champKingdom = entityOwners.get(entity.getEntityId());

		if(!(e.getPotion().getShooter() instanceof Player)) continue;
		KingdomPlayer shooter = GameManagement.getPlayerManager().getSession((Player) e.getPotion().getShooter());

		if(champKingdom.equals(shooter.getKingdom())
			|| champKingdom.isAllianceWith(shooter.getKingdom())
			|| shooter.getPlayer().getGameMode() != GameMode.SURVIVAL)
		iter.remove();
	}
	}

	/**
	 * Listener (do not touch)
	 *
	 * @param event
	 */
	@EventHandler
	public void onChampionDeath(EntityDeathEvent event){
	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(event.getEntity())) return;
	World bukkitWorld = event.getEntity().getWorld();
	if(!Config.getConfig().getStringList("enabled-worlds").contains(bukkitWorld.getName())) return;

	if(!entityOwners.containsKey(event.getEntity().getEntityId())) return;

	Player killer = event.getEntity().getKiller();
	if(killer == null) return;

	KingdomPlayer challenger = targets.get(event.getEntity().getEntityId());
	if(challenger == null){
		Kingdoms.logInfo("Fatal error! challenger was null!");
		return;
	}

	if(challenger.getKingdom() == null) return;

	if(event.getEntity() != challenger.getChampionPlayerFightingWith()) return;

	SimpleChunkLocation chunk = challenger.getFightZone().clone();
	Land land = GameManagement.getLandManager().getOrLoadLand(chunk);
	if(land.getOwnerUUID() == null){
		Kingdoms.logInfo("Error! champion of [" + chunk.toString() + "] is dead.");
		Kingdoms.logInfo("But no kingdom owns this land.");
		stopFight(challenger);
		return;
	}

	Kingdom defending = entityOwners.get(event.getEntity().getEntityId());

	stopFight(challenger);
	event.getDrops().clear();
	plugin.getServer().getPluginManager().callEvent(new KingdomPlayerWonEvent(challenger, defending, chunk));
	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler
	public void onChallengerDeathWhileInvade(PlayerDeathEvent e){
	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(e.getEntity())) return;
	World bukkitWorld = e.getEntity().getWorld();

	if(!Config.getConfig().getStringList("enabled-worlds").contains(bukkitWorld.getName())) return;

	KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getEntity());
	if(kp == null) return;
	if(kp.getChampionPlayerFightingWith() == null) return;


	stopFight(kp);
	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandWhileFight(PlayerCommandPreprocessEvent e){
	KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
	if(kp == null) return;//GameManagement.getPlayerManager().preloadKingdomPlayer(e.getPlayer());
	if(kp.getChampionPlayerFightingWith() == null) return;
	if(e.getMessage().equalsIgnoreCase("/k surrender") ||
		e.getMessage().equalsIgnoreCase("/kingdoms surrender") ||
		e.getMessage().equalsIgnoreCase("/kingdom surrender") ||
		e.getMessage().equalsIgnoreCase("/k ff") ||
		e.getMessage().equalsIgnoreCase("/kingdoms ff") ||
		e.getMessage().equalsIgnoreCase("/kingdom ff")){
		if(kp.getPlayer().hasPermission("kingdoms.surrender") ||
			kp.getPlayer().hasPermission("kingdoms.player"))
		return;
	}
	kp.sendMessage(Kingdoms.getLang().getString("Champion_Command_Block", kp.getLang()));
	e.setCancelled(true);
	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler
	public void onTargetChange(EntityTargetLivingEntityEvent e){
	World bukkitWorld = e.getEntity().getWorld();
	if(!Config.getConfig().getStringList("enabled-worlds").contains(bukkitWorld.getName())) return;
	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(e.getTarget())) return;
	if(!entityOwners.containsKey(e.getEntity().getEntityId())) return;
	Kingdom kingdom = entityOwners.get(e.getEntity().getEntityId());
	KingdomPlayer challenger = targets.get(e.getEntity().getEntityId());
	ChampionTargetChangeEvent championTargetChangeEvent = new ChampionTargetChangeEvent(e.getEntity(), e.getTarget());
	if(e.getTarget() instanceof Player){
		Player targetP = (Player) e.getTarget();
		KingdomPlayer target = GameManagement.getPlayerManager().getSession(targetP);
		if(target.getKingdom() == null) return; // don't change if has no kingdom

		if(kingdom.equals(target.getKingdom())){//Why this???????? -> || kingdom.equals(target.getKingdom())){
		e.setTarget(challenger.getPlayer());// change target if ally or own kingdom member
		}
		//TODO check that this is right lmao
		else{
			Bukkit.getPluginManager().callEvent(championTargetChangeEvent);
			if(championTargetChangeEvent.isCancelled()){
				e.setTarget(challenger.getPlayer());
		}
		}
	}
	}


	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKnockBack(EntityDamageByEntityEvent e){

	if(e.getCause() != DamageCause.ENTITY_ATTACK //only arrow and projectile
		|| e.getCause() != DamageCause.PROJECTILE) return;

	if(e.getEntity().getType() != EntityType.ZOMBIE) return; //check if zombie

	if(!entityOwners.containsKey(e.getEntity().getEntityId())) return; //check if champion

	Kingdom kingdom = entityOwners.get(e.getEntity().getEntityId());
	ChampionInfo info = kingdom.getChampionInfo();

	int resist = info.getResist();
	if(!(resist > 0)) return;

	if(ProbabilityTool.testProbability100(resist)){
		ChampionIgnoreKnockbackEvent ignoreKnockbackEvent = new ChampionIgnoreKnockbackEvent(e.getEntity());
		Bukkit.getPluginManager().callEvent(ignoreKnockbackEvent);
		if(ignoreKnockbackEvent.isCancelled()){
			return;
		}
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> e.getEntity().setVelocity(new Vector())
			, 1L);

	}

	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler
	public void onPlaceInMockRange(BlockPlaceEvent e){
	KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
	if(kp.getKingdom() == null) return; //check if has kingdom

	Entity entity = kp.getChampionPlayerFightingWith();
	if(entity == null) return;//check if fighting

	Kingdom defender = entityOwners.get(entity.getEntityId());
	ChampionInfo info = defender.getChampionInfo();
	int mock = info.getMock();

	if(!(mock > 0)) return;
	ChampionPreMockEvent preMockEvent = new ChampionPreMockEvent(entity, mock);
	Bukkit.getPluginManager().callEvent(preMockEvent);
	if(preMockEvent.isCancelled()){
		return;
	}
	mock = preMockEvent.getMockRange();
	Location champLoc = entity.getLocation();
	int champX = champLoc.getBlockX();
	int champZ = champLoc.getBlockZ();

	int placingX = e.getBlock().getX();
	int placingZ = e.getBlock().getZ();

	if(e.getBlock().getLocation().distanceSquared(champLoc) > mock * mock) return;
	ChampionMockEvent mockEvent = new ChampionMockEvent(entity);
	Bukkit.getPluginManager().callEvent(mockEvent);
	e.setCancelled(!mockEvent.isCancelled());
	kp.sendMessage(Kingdoms.getLang().getString("Champion_Mock", kp.getLang()).replaceAll("%mock%", mock + ""));
	return;

	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler
	public void onDeathDuelChampDamageToNonInvader(EntityDamageByEntityEvent e){
	if(e.getDamager().getType() != EntityType.ZOMBIE) return; //damager is not zombie
	if(!(e.getEntity() instanceof Player)) return; //damaged is not player

	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(e.getEntity())) return;
	if(!entityOwners.containsKey(e.getDamager().getEntityId())) return; //damager not champion
	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(e.getDamager())) return;
	KingdomPlayer damaged = GameManagement.getPlayerManager().getSession((Player) e.getEntity());
	//if(damaged.getKingdom() == null)//not in kingdom

	if(damaged.getChampionPlayerFightingWith() != null) return; //it's invader

	Kingdom kingdom = entityOwners.get(e.getDamager().getEntityId());
	ChampionInfo info = kingdom.getChampionInfo();
	int duel = info.getDuel();
	if(!(duel > 0)) return;// duel is not on

	e.setDamage(e.getDamage() * 2);//double to non-invader
	//damaged.sendMessage(ChatColor.RED+"Death duel rage!!!");
	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler
	public void onDeathDuelNonInvaderDamageToChamp(EntityDamageByEntityEvent e){
	if(!(e.getDamager() instanceof Player)) return; //damager is not player
	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(e.getDamager())) return;
	if(e.getEntity().getType() != EntityType.ZOMBIE) return; //damaged is not zombie

	if(!entityOwners.containsKey(e.getEntity().getEntityId())) return;//damaged is not champion

	KingdomPlayer damager = GameManagement.getPlayerManager().getSession((Player) e.getDamager());
	//if(damager.getKingdom() == null);//not in kingdom

	if(damager.getChampionPlayerFightingWith() != null) return; //it's invader

	Kingdom kingdom = entityOwners.get(e.getEntity().getEntityId());
	ChampionInfo info = kingdom.getChampionInfo();
	int duel = info.getDuel();
	if(!(duel > 0)) return;// duel is not on

	e.setDamage(e.getDamage() / 2);//double to non-invader
	damager.sendMessage(Kingdoms.getLang().getString("Champion_DeathDuel", damager.getLang()));
	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChampDamageWhileDamageCapOn(EntityDamageByEntityEvent e){
	if(e.getEntity().getType() != EntityType.ZOMBIE) return; //damaged is not zombie
	if(!entityOwners.containsKey(e.getEntity().getEntityId())) return;//damaged is not champion

	Kingdom kingdom = entityOwners.get(e.getEntity().getEntityId());
	ChampionInfo info = kingdom.getChampionInfo();
	int damageCap = info.getDamagecap();
	if(!(damageCap > 0)) return;// damageCap is not on

	if(e.getDamage() > 15.0D){
		ChampionDamageCapEvent damageCapEvent = new ChampionDamageCapEvent(e.getEntity(), e.getDamager(), damageCap, e.getDamage());
		Bukkit.getPluginManager().callEvent(damageCapEvent);
		if(!damageCapEvent.isCancelled()){
		e.setDamage(damageCapEvent.getDamageCap());
		}
	}

	}

	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler
	public void onFocus(EntityDamageByEntityEvent e){
	if(e.getDamager().getType() != EntityType.ZOMBIE) return; //damager is not zombie
	if(!(e.getEntity() instanceof Player)) return; //damaged is not player
	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(e.getEntity())) return;

	if(!entityOwners.containsKey(e.getDamager().getEntityId())) return; //damager not champion
	Player p = (Player) e.getEntity();
	Kingdom kingdom = entityOwners.get(e.getDamager().getEntityId());
	ChampionInfo info = kingdom.getChampionInfo();
	int focus = info.getFocus();
	if(focus <= 0) return;// focus is not on
	Collection<PotionEffect> effects = p.getActivePotionEffects();
	if(effects.size() > 0){
		ChampionFocusEvent focusEvent = new ChampionFocusEvent(e.getDamager(), GameManagement.getPlayerManager().getSession(p));
		Bukkit.getPluginManager().callEvent(focusEvent);
		if(focusEvent.isCancelled()){
			return;
		}
		for(PotionEffect effect : effects){
		PotionEffect pe = new PotionEffect(effect.getType(), effect.getDuration() - 1, effect.getAmplifier());
		p.removePotionEffect(effect.getType());
		p.addPotionEffect(pe);
		}
	}
	}


	/**
	 * Listener (do not touch)
	 *
	 * @param e
	 */
	@EventHandler
	public void onDamageWhileStrengthUp(EntityDamageByEntityEvent e){
	if(e.getDamager().getType() != EntityType.ZOMBIE) return; //damager is not zombie
	if(!(e.getEntity() instanceof Player)) return; //damaged is not player
	GameManagement.getApiManager();
	if(ExternalManager.isCitizen(e.getEntity())) return;

	if(!entityOwners.containsKey(e.getDamager().getEntityId())) return; //damager not champion

	Kingdom kingdom = entityOwners.get(e.getDamager().getEntityId());
	ChampionInfo info = kingdom.getChampionInfo();
	int strength = info.getStrength();
	if(!(strength > 0)) return;// strength is not on

	if(ProbabilityTool.testProbability100(strength)){
		ChampionStrengthEvent strengthEvent = new ChampionStrengthEvent(e.getDamager(), GameManagement.getPlayerManager().getSession(e.getEntity().getUniqueId()));
		Bukkit.getPluginManager().callEvent(strengthEvent);
		if(strengthEvent.isCancelled()){
			return;
		}
		e.getEntity().setVelocity(new Vector(0, 1.5, 0));
	}
	}

	/**
	 * Listener (do not touch)
	 * @param e
	 */

	/**
	 * get Entity instance from its id
	 *
	 * @param world world
	 * @param id		entityID
	 * @return Entity if found; null if not found
	 */
	public static Entity getEntityByEntityID(World world, int id){
	Iterator<Entity> iter = world.getEntities().iterator();
	for(; iter.hasNext(); ){
		Entity e = iter.next();
		if(e.getEntityId() == id) return e;
	}

	return null;
	}

	@Override
	public void onDisable(){
	for(Map.Entry<Integer, KingdomPlayer> entry : targets.entrySet()){
		stopFight(entry.getValue());
	}

	targets.clear();
	}

	public static void sendLightning(Player p, Location l){
	Class<?> light = getNMSClass("EntityLightning");
	try{
		Constructor<?> constu =
			light
				.getConstructor(getNMSClass("World"),
					double.class, double.class,
					double.class, boolean.class, boolean.class);
		Object wh = p.getWorld().getClass().getMethod("getHandle").invoke(p.getWorld());
		Object lighobj = constu.newInstance(wh, l.getX(), l.getY(), l.getZ(), false, false);

		Object obj =
			getNMSClass("PacketPlayOutSpawnEntityWeather")
				.getConstructor(getNMSClass("Entity")).newInstance(lighobj);


		try{
		sendPacket(p, obj);
		p.playSound(p.getLocation(), Sound.valueOf("AMBIENCE_THUNDER"), 100, 1);
		}catch(IllegalArgumentException e){
		try{
			sendPacket(p, obj);
			p.playSound(p.getLocation(), Sound.valueOf("ENTITY_LIGHTNING_THUNDER"), 100, 1);
		}catch(IllegalArgumentException ex){
			p.getWorld().strikeLightningEffect(p.getLocation());
		}
		}
//				} catch (NoSuchMethodException | SecurityException |
//								IllegalAccessException | IllegalArgumentException |
//								InvocationTargetException | InstantiationException e) {
	}catch(Exception e){
		e.printStackTrace();
	}
	}

	public static Class<?> getNMSClass(String name){
	String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	try{
		return Class.forName("net.minecraft.server." + version + "." + name);
	}catch(ClassNotFoundException e){
		e.printStackTrace();
		return null;
	}
	}

	public static void sendPacket(Player player, Object packet){
	try{
		Object handle = player.getClass().getMethod("getHandle").invoke(player);
		Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
		playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"))
			.invoke(playerConnection, packet);
	}catch(Exception e){
		e.printStackTrace();
	}
	}

}
