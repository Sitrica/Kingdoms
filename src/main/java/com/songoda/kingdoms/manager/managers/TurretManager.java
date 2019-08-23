package com.songoda.kingdoms.manager.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.LandLoadEvent;
import com.songoda.kingdoms.events.TurretBreakEvent;
import com.songoda.kingdoms.events.TurretFireEvent;
import com.songoda.kingdoms.events.TurretPlaceEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.manager.managers.external.EffectLibManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.objects.turrets.HealthInfo;
import com.songoda.kingdoms.objects.turrets.Potions;
import com.songoda.kingdoms.objects.turrets.Turret;
import com.songoda.kingdoms.objects.turrets.TurretTask;
import com.songoda.kingdoms.objects.turrets.TurretType;
import com.songoda.kingdoms.objects.turrets.TurretType.TargetType;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TurretManager extends Manager {

	private final Set<TurretType> types = new HashSet<>();
	public final String METADATA_CONQUEST = "conquest-arrow";
	public final String METADATA_KINGDOM = "turret-kingdom";
	public final String METADATA_POTIONS = "turret-potions";
	public final String METADATA_CHANCE = "turret-chance";
	public final String METADATA_HEALTH = "turret-health";
	public final String METADATA_VALUE = "turret-value";
	private Optional<EffectLibManager> effectLibManager;
	private Optional<CitizensManager> citizensManager;
	private InvadingManager invadingManager;
	private KingdomManager kingdomManager;
	private PlayerManager playerManager;
	private LandManager landManager;

	public TurretManager() {
		super(true);
		Bukkit.getScheduler().runTaskTimer(instance, new TurretTask(instance), 0, 1);
		for (String turret : instance.getConfiguration("turrets").get().getConfigurationSection("turrets.turrets").getKeys(false)) {
			types.add(new TurretType(turret));
		}
	}

	@Override
	public void initalize() {
		this.effectLibManager = instance.getExternalManager("effectlib", EffectLibManager.class);
		this.citizensManager = instance.getExternalManager("citizens", CitizensManager.class);
		this.invadingManager = instance.getManager("invading", InvadingManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}
 
	public Set<TurretType> getTypes() {
		return types;
	}

	public Optional<TurretType> getTurretTypeByName(String name) {
		return types.stream()
				.filter(type -> type.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	public boolean isHealthProjectile(Metadatable metadatable) {
		if (!metadatable.hasMetadata(METADATA_HEALTH))
			return false;
		return metadatable.getMetadata(METADATA_HEALTH).parallelStream()
				.filter(metadata -> metadata.getOwningPlugin().equals(instance))
				.findFirst().isPresent();
	}

	public Optional<Boolean> getChance(Metadatable metadatable) {
		if (!metadatable.hasMetadata(METADATA_CHANCE))
			return Optional.empty();
		return metadatable.getMetadata(METADATA_CHANCE).parallelStream()
				.filter(metadata -> metadata.getOwningPlugin().equals(instance))
				.map(metadata -> metadata.asBoolean())
				.findFirst();
	}

	public Optional<Potions> getPotions(Metadatable metadatable) {
		if (!metadatable.hasMetadata(METADATA_POTIONS))
			return Optional.empty();
		return metadatable.getMetadata(METADATA_POTIONS).parallelStream()
				.filter(metadata -> metadata.getOwningPlugin().equals(instance))
				.map(metadata -> new Potions(metadata.asString()))
				.findFirst();
	}

	public Optional<Double> getProjectileDamage(Metadatable metadatable) {
		if (!metadatable.hasMetadata(METADATA_VALUE))
			return Optional.empty();
		return metadatable.getMetadata(METADATA_VALUE).parallelStream()
				.filter(metadata -> metadata.getOwningPlugin().equals(instance))
				.map(metadata -> metadata.asDouble())
				.findFirst();
	}

	public Optional<OfflineKingdom> getProjectileKingdom(Metadatable metadatable) {
		if (!metadatable.hasMetadata(METADATA_KINGDOM))
			return Optional.empty();
		return metadatable.getMetadata(METADATA_KINGDOM).parallelStream()
				.filter(metadata -> metadata.getOwningPlugin().equals(instance))
				.map(metadata -> metadata.asString())
				.map(name -> kingdomManager.getOfflineKingdom(name))
				.filter(kingdom -> kingdom.isPresent())
				.map(optional -> optional.get())
				.findFirst();
	}

	public long getTurretCount(Land land, TurretType type) {
		return land.getTurrets().parallelStream()
				.filter(turret -> turret.getType() != null)
				.filter(turret -> turret.getType().equals(type))
				.count();
	}

	public enum TurretBlock {
		TURRET_BASE,
		NOT_TURRET,
		TURRET;
	}

	public Optional<Turret> getTurret(Block block) {
		TurretBlock turretBlock = getTurretBlock(block);
		Location location = block.getLocation();
		Land land = landManager.getLand(location.getChunk());
		if (turretBlock == TurretBlock.TURRET) {
			return Optional.ofNullable(land.getTurret(location));
		} else if (turretBlock == TurretBlock.TURRET_BASE) {
			return Optional.ofNullable(land.getTurret(location.add(0, 1, 0)));
		}
		return Optional.empty();
	}

	public TurretBlock getTurretBlock(Block block) {
		Location location = block.getLocation();
		Land land = landManager.getLand(location.getChunk());
		if (land.getTurret(location) == null) {
			if (land.getTurret(location.add(0, 1, 0)) != null)
				return TurretBlock.TURRET_BASE;
			return TurretBlock.NOT_TURRET;
		}
		return TurretBlock.TURRET;
	}

	public void breakTurret(Turret turret) {
		Bukkit.getScheduler().runTask(instance, () -> {
			Location location = turret.getHeadLocation();
			Land land = landManager.getLand(location.getChunk());
			World world = location.getWorld();
			TurretType type = turret.getType();
			Optional<OfflineKingdom> optional = land.getKingdomOwner();
			if (optional.isPresent())
				world.dropItem(location, type.build(optional.get(), false));
			Block block = location.getBlock();
			block.setType(Material.AIR);
			block.getRelative(BlockFace.DOWN).setType(Material.AIR);
			land.removeTurret(turret);
		});
	}

	public TurretType getTurretTypeFrom(ItemStack item) {
		if (item == null)
			return null;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return null;
		List<String> lores = meta.getLore();
		if (lores == null || lores.isEmpty())
			return null;
		for (TurretType type : types) {
			if (type.getMaterial() == item.getType()) {
				if (Formatting.stripColor(meta.getDisplayName()).equalsIgnoreCase(Formatting.colorAndStrip(type.getTitle()))) {
					return type;
				}
			}
		}
		return null;
	}

	public boolean canBeTargeted(OfflineKingdom kingdom, Entity target) {
		if (!(target instanceof LivingEntity))
			return false;
		if (target.isDead() || !target.isValid())
			return false;
		if (citizensManager.isPresent())
			if (citizensManager.get().isCitizen(target))
				return false;
		if (invadingManager.isDefender(target))
			return false;
		OfflineKingdom playerKingdom = null;
		if (target instanceof Player) {
			Player player = (Player) target;
			KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
			playerKingdom = kingdomPlayer.getKingdom();
			if (kingdomPlayer.hasAdminMode() || kingdomPlayer.isVanished())
				return false;
			GameMode gamemode = player.getGameMode();
			if (gamemode != GameMode.SURVIVAL && gamemode != GameMode.ADVENTURE)
				return false;
		} else if (target instanceof Wolf) {
			Wolf wolf = (Wolf) target;
			AnimalTamer owner = wolf.getOwner();
			if (owner != null) {
				Optional<OfflineKingdomPlayer> kingdomPlayer = playerManager.getOfflineKingdomPlayer(owner.getUniqueId());
				if (kingdomPlayer.isPresent())
					playerKingdom = kingdomPlayer.get().getKingdom();
			}
		}
		if (playerKingdom == null)
			return true;
		if (kingdom.equals(playerKingdom))
			return false;
		return !kingdom.isAllianceWith(playerKingdom);
	}


	public boolean canBeTargeted(Turret turret, LivingEntity target) {
		// If this entity is alive
		if (target.isDead() || !target.isValid())
			return false;
		if (invadingManager.isDefender(target))
			return false;
		Location location = turret.getHeadLocation();
		if (!location.getWorld().equals(target.getWorld()))
			return false;
		// Turret shot is in range.
		if (target.getLocation().distanceSquared(location) > Math.pow(turret.getType().getRange(), 2))
			return false;
		TurretType type = turret.getType();
		if (target instanceof Player) {
			Player player = (Player) target;
			KingdomPlayer kingdomPLayer = playerManager.getKingdomPlayer(player);
			if (kingdomPLayer.hasAdminMode() || kingdomPLayer.isVanished())
				return false;
			Kingdom kingdom = kingdomPLayer.getKingdom();
			GameMode gamemode = player.getGameMode();
			if (gamemode != GameMode.SURVIVAL && gamemode != GameMode.ADVENTURE)
				return false;
			Land land = landManager.getLand(location.getChunk());
			Optional<OfflineKingdom> optional = land.getKingdomOwner();
			// This turret has no Kingdom owner??
			if (!optional.isPresent())
				return false;
			OfflineKingdom landKingdom = optional.get();
			if (type.getTargets().contains(TargetType.KINGDOM)) {
				if (landKingdom.equals(kingdom))
					return true;
			} else if (type.getTargets().contains(TargetType.ALLIANCE)) {
				if (kingdom == null)
					return false;
				if (landKingdom.equals(kingdom))
					return true;
				return landKingdom.isAllianceWith(kingdom);
			} else if (type.getTargets().contains(TargetType.ENEMIES)) {
				if (kingdom == null)
					return true;
				if (landKingdom.equals(kingdom))
					return false;
				return !landKingdom.isAllianceWith(kingdom);
			}
		} else if (target instanceof Wolf) {
			Wolf wolf = (Wolf) target;
			AnimalTamer owner = wolf.getOwner();
			if (owner != null) {
				Optional<OfflineKingdomPlayer> kingdomPlayer = playerManager.getOfflineKingdomPlayer(owner.getUniqueId());
				if (kingdomPlayer.isPresent()) {
					Optional<KingdomPlayer> player = kingdomPlayer.get().getKingdomPlayer();
					if (player.isPresent())
						return canBeTargeted(turret, player.get().getPlayer());
				}
			}
		}
		if (type.getTargets().contains(TargetType.MONSTERS))
			return target instanceof Monster;
		// If this turret targets entities, fire at them, else don't.
		return type.getTargets().contains(TargetType.ENTITIES);
	}

	public void fire(Turret turret, LivingEntity target) {
		if (target == null)
			return;
		if (!canBeTargeted(turret, target))
			return;
		Location location = turret.getHeadLocation();
		TurretType type = turret.getType();
		Land land = landManager.getLand(location.getChunk());
		Optional<OfflineKingdom> landOptional = land.getKingdomOwner();
		TurretFireEvent event = new TurretFireEvent(turret, target, landOptional.orElse(null));
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		// Handle the fire rate
		if (System.currentTimeMillis() - turret.getFireCooldown() < type.getFirerate())
			return;
		turret.setFireCooldown();

		// Handle ammo reloading. There is another code block below that handles reloading.
		if (System.currentTimeMillis() - turret.getReloadCooldown() < type.getReloadCooldown())
			return;

		// Setup vectors
		Location fromLocation = location.clone().add(0.5, 1.0, 0.5);
		Vector to = target.getLocation().clone().add(0.0, 0.75, 0.0).toVector();
		Vector from = fromLocation.toVector();
		Vector direction = to.subtract(from);
		direction.normalize();

		// Execute
		if (turret.getAmmo() > 0) {
			turret.useAmmo();
			if (type.isParticleProjectile() && effectLibManager.isPresent()) {
				effectLibManager.get().shootParticle(turret, fromLocation, target, new Runnable() {
					@Override
					public void run() {
						if (type.isHealer()) {
							double health = target.getHealth();
							HealthInfo info = type.getHealthInfo();
							health += info.getHealth();
							if (health > DeprecationUtils.getMaxHealth(target))
								return;
							EntityRegainHealthEvent event = new EntityRegainHealthEvent(target, info.getHealth(), RegainReason.CUSTOM);
							Bukkit.getPluginManager().callEvent(event);
							if (event.isCancelled())
								return;
							if (info.chance())
								target.setHealth(health);
							if (type.hasPotions()) {
								for (PotionEffect effect : type.getPotions().getPotionEffects()) {
									target.addPotionEffect(effect, true);
								}
							}
							return;
						}
						target.damage(type.getDamage());
					}
				});
			} else if (type.getProjectile() == EntityType.ARROW) {
				Arrow arrow = (Arrow) location.getWorld().spawnArrow(fromLocation, direction, 1.5F, type.getArrowSpread());
				arrow.setCritical(type.isCritical());
				if (landOptional.isPresent())
					arrow.setMetadata(METADATA_KINGDOM, new FixedMetadataValue(instance, landOptional.get().getName()));
				if (type.isFlame())
					arrow.setFireTicks(Integer.MAX_VALUE);
				arrow.setMetadata(METADATA_VALUE, new FixedMetadataValue(instance, "" + type.getDamage()));
				if (type.hasPotions())
					arrow.setMetadata(METADATA_POTIONS, new FixedMetadataValue(instance, "" + type.getDamage()));
			} else {
				Entity projectile = location.getWorld().spawnEntity(fromLocation, type.getProjectile());
				if (type.hasPotions()) {
					if (projectile instanceof Arrow) {
						Arrow tipped = (Arrow) projectile;
						for (PotionEffect effect : type.getPotions().getPotionEffects()) {
							tipped.addCustomEffect(effect, true);
						}
					}
					projectile.setMetadata(METADATA_POTIONS, new FixedMetadataValue(instance, "" + type.getDamage()));
				}
				projectile.setVelocity(direction);
				if (landOptional.isPresent())
					projectile.setMetadata(METADATA_KINGDOM, new FixedMetadataValue(instance, landOptional.get().getName()));
				if (type.isHealer()) {
					HealthInfo health = type.getHealthInfo();
					projectile.setMetadata(METADATA_HEALTH, new FixedMetadataValue(instance, true));
					projectile.setMetadata(METADATA_CHANCE, new FixedMetadataValue(instance, health.chance()));
					projectile.setMetadata(METADATA_VALUE, new FixedMetadataValue(instance, "" + health.getHealth()));
				} else {
					projectile.setMetadata(METADATA_VALUE, new FixedMetadataValue(instance, "" + type.getDamage()));
				}
			}
			if (turret.getAmmo() <= 0) {
				turret.setReloadCooldown();
				Block block = location.getBlock();
				Material head = Utils.materialAttempt("PLAYER_HEAD", "SKULL");
				block.setType(head);
				BlockState state = block.getState();
				// 1.8 users...
				if (head.name().equalsIgnoreCase("SKULL"))
					DeprecationUtils.setupOldSkull(state);
				if (state instanceof Skull) {
					Skull skull = (Skull) state;
					skull.setOwningPlayer(type.getReloadingSkullOwner());
					state.update(true);
				}
				type.getReloadingSounds().playAt(location);
				instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
					@Override
					public void run() {
						// Check that the block wasn't removed.
						if (!block.getType().name().contains("SKULL") && !block.getType().name().contains("HEAD"))
							return;
						BlockState state = block.getState();
						if (state instanceof Skull) {
							Skull skull = (Skull) state;
							skull.setOwningPlayer(type.getSkullOwner());
							state.update(true);
						}
						turret.resetAmmo();
					}
				}, (type.getReloadCooldown() / 1000) * 20);
			}
		}
		/*switch (type) {
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
		*/
	}

	@EventHandler
	public void onTurretBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (getTurretBlock(block) != TurretBlock.NOT_TURRET) {
			Player player = event.getPlayer();
			Location location = block.getLocation();
			Land land = landManager.getLand(location.getChunk());
			Turret turret = land.getTurret(location);
			if (turret == null)
				turret = land.getTurret(location.add(0, 1, 0));
				if (turret == null)
					return;
			KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
			Optional<OfflineKingdom> optional = land.getKingdomOwner();
			if (!optional.isPresent()) {
				TurretBreakEvent breakEvent = new TurretBreakEvent(land, turret, kingdomPlayer);
				Bukkit.getPluginManager().callEvent(breakEvent);
				if (!breakEvent.isCancelled())
					breakTurret(turret);
			}
			OfflineKingdom landKingdom = optional.get();
			Kingdom kingdom = kingdomPlayer.getKingdom();
			if (kingdomPlayer.hasAdminMode())
				return;
			event.setCancelled(true);
			if (kingdom == null) {
				new MessageBuilder("kingdoms.no-kingdom").send(player);
				return;
			}
			if (!kingdom.equals(landKingdom)) {
				new MessageBuilder("kingdoms.not-in-land")
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (!kingdom.getPermissions(kingdomPlayer.getRank()).canBuildStructures()) {
				new MessageBuilder("kingdoms.rank-too-low-structure-build")
						.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canBuildStructures()), new Placeholder<Optional<Rank>>("%rank%") {
							@Override
							public String replace(Optional<Rank> rank) {
								if (rank.isPresent())
									return rank.get().getName();
								return "(Not attainable)";
							}
						})
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			TurretBreakEvent breakEvent = new TurretBreakEvent(land, turret, kingdomPlayer, kingdom);
			Bukkit.getPluginManager().callEvent(breakEvent);
			if (!breakEvent.isCancelled())
				breakTurret(turret);
		}
	}

	@EventHandler
	public void onPlaceTurret(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (event.getBlockFace() != BlockFace.UP)
			return;
		Player player = event.getPlayer();
		TurretType type = getTurretTypeFrom(DeprecationUtils.getItemInMainHand(player));
		if (type == null)
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (!kingdomPlayer.hasAdminMode() && kingdom == null) {
			new MessageBuilder("kingdoms.no-kingdom").send(player);
			return;
		}
		Block block = event.getClickedBlock();
		Material material = block.getType();
		Block post = block.getRelative(0, 1, 0);
		Land land = landManager.getLand(post.getChunk());
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (!kingdomPlayer.hasAdminMode() && !optional.isPresent()) {
			new MessageBuilder("kingdoms.not-in-land")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return;
		}
		OfflineKingdom landKingdom = optional.get();
		if (!kingdom.equals(landKingdom)) {
			new MessageBuilder("kingdoms.not-in-land")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		if (getTurretCount(land, type) >= type.getMaximum()) {
			new MessageBuilder("turrets.turret-limit")
					.replace("%type%", Formatting.color(type.getTitle()))
					.replace("%amount%", type.getMaximum())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
			return;
		}
		FileConfiguration turrets = instance.getConfiguration("turrets").get();
		if (!material.isOccluding() || !material.isSolid() || turrets.getStringList("turrets.illegal-placements").contains(material.name())) {
			new MessageBuilder("turrets.illegal-placement")
					.replace("%material%", material.name().toLowerCase())
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			event.setCancelled(true);
			return;
		}
		Block head = post.getRelative(0, 1, 0);
		if (post.getType() != Material.AIR || head.getType() != Material.AIR) {
			new MessageBuilder("turrets.already-occupied")
					.setKingdom(kingdom)
					.send(player);
			return;
		}
		Turret turret = new Turret(head.getLocation(), type);
		TurretPlaceEvent placeEvent = new TurretPlaceEvent(land, turret, kingdomPlayer, kingdom);
		Bukkit.getPluginManager().callEvent(placeEvent);
		if (placeEvent.isCancelled())
			return;
		Material headMaterial = Utils.materialAttempt(turrets.getString("turrets.default-post", "FENCE"), "FENCE");
		post.setType(headMaterial);
		land.addTurret(turret);
		ItemStack item = DeprecationUtils.getItemInMainHand(player);
		int amount = item.getAmount();
		if (amount > 1)
			item.setAmount(amount - 1);
		else
			DeprecationUtils.setItemInMainHand(player, null);
		head.setType(Utils.materialAttempt("PLAYER_HEAD", "SKULL"));
		BlockState state = head.getState();
		// 1.8 users...
		if (headMaterial.name().equalsIgnoreCase("SKULL"))
			DeprecationUtils.setupOldSkull(state);
		if (state instanceof Skull) {
			Skull skull = (Skull) state;
			skull.setOwningPlayer(type.getSkullOwner());
			state.update(true);
		}
		type.getPlacingSounds().playAt(block.getLocation());
	}

	@EventHandler
	public void onDispense(BlockDispenseEvent event) {
		Block block = event.getBlock();
		BlockState state = block.getState();
		if (!(state instanceof Dispenser))
			return;
		Dispenser dispenser = (Dispenser) state;
		BlockFace face = ((org.bukkit.material.Dispenser) dispenser.getData()).getFacing();
		if (getTurretBlock(block.getRelative(face)) != TurretBlock.NOT_TURRET)
			event.setCancelled(true);
	}

	// Fixes heads not being removed from water
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBucketPlace(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			ItemStack item;
			try {
				item = player.getItemInHand();
			} catch (Exception e) {
				item = player.getInventory().getItemInMainHand();
			}
			if (item != null) {
				Material type = item.getType();
				if (type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET) {
					if (getTurretBlock(event.getClickedBlock().getRelative(event.getBlockFace())) != TurretBlock.NOT_TURRET) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onWaterPassThrough(BlockFromToEvent event) {
		if (getTurretBlock(event.getToBlock()) != TurretBlock.NOT_TURRET)
			event.setCancelled(true);
	}

	@EventHandler
	public void onProjectileLand(ProjectileHitEvent event) {
		Entity projectile = event.getEntity();
		if (projectile.hasMetadata(METADATA_KINGDOM) || projectile.hasMetadata(METADATA_CONQUEST)) {
			Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			@Override
				public void run() {
					projectile.remove();
				}
			}, 1);
		}
	}

	@EventHandler
	public void onTurretHit(EntityDamageByEntityEvent event) {
		Entity attacker = event.getDamager();
		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity))
			return;
		LivingEntity victim = (LivingEntity) entity;
		Optional<OfflineKingdom> kingdom = getProjectileKingdom(attacker);
		Optional<Double> value = getProjectileDamage(attacker);
		if (value.isPresent() && kingdom.isPresent()) {
			if (canBeTargeted(kingdom.get(), entity)) {
				if (isHealthProjectile(attacker)) {
					double health = victim.getHealth();
					health += value.get();
					EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(victim, value.get(), RegainReason.CUSTOM);
					Bukkit.getPluginManager().callEvent(healthEvent);
					if (healthEvent.isCancelled())
						return;
					if (health > DeprecationUtils.getMaxHealth(victim))
						return;
					Optional<Boolean> chance = getChance(attacker);
					if (chance.isPresent() && chance.get())
						victim.setHealth(health);
					return;
				}
				Optional<Potions> potions = getPotions(attacker);
				if (potions.isPresent()) {
					if (attacker instanceof Arrow) // Minecraft will already do the setting of the effect for us.
						return;
					for (PotionEffect effect : potions.get().getPotionEffects()) {
						victim.addPotionEffect(effect, true);
					}
				}
				event.setDamage(value.get());
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onTurretProjectileBurn(EntityCombustByEntityEvent event) {
		Entity combuster = event.getCombuster();
		Optional<OfflineKingdom> kingdom = getProjectileKingdom(combuster);
		if (kingdom.isPresent()) {
			if (!canBeTargeted(kingdom.get(), event.getEntity())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onLandLoad(LandLoadEvent event) {
		for (Turret turret : event.getLand().getTurrets()) {
			Block block = turret.getHeadLocation().getBlock();
			if (block.getType() != Utils.materialAttempt("PLAYER_HEAD", "SKULL")) {
				breakTurret(turret);
			}
		}
	}

	@Override
	public void onDisable() {
		types.clear();
	}

	/*
	private ArrayList<Turret> initTurret(Land land, Turret turret) {
		Set<Turret> turrets = land.getTurrets();
		if (turrets.isEmpty())
			return null;
		Set<Turret> remove = new HashSet<>();
		for(Turret t : turrets) {
			TurretType type = t.getType();
			if (type == null) {
				remove.add(t);
				continue;
			}
			if (Config.getConfig().getBoolean("destroy-extra-turrets-to-enforce-max")) {
				if (isOverHitInLand(land, type)) {
					toBeRemoved.add(t);
					continue;
				}
			}
			Block turretBlock = t.getLoc().toLocation().getBlock();
			if(turretBlock.getType().isSolid() &&
				turretBlock.getType() != Materials.SKELETON_SKULL.parseMaterial() &&
				turretBlock.getType() != Materials.OAK_PRESSURE_PLATE.parseMaterial() &&
				turretBlock.getType() != Materials.STONE_PRESSURE_PLATE.parseMaterial()){
			toBeRemoved.add(t);
			Kingdoms.logInfo("A turret at " + t.getLoc().toString() + " is not a skull or a pressure plate! Removing.");
			continue;
			}
			if(type == TurretType.MINE_CHEMICAL){
			turretBlock.setType(Materials.OAK_PRESSURE_PLATE.parseMaterial());
			}
			else if(type == TurretType.MINE_PRESSURE){
			turretBlock.setType(Materials.STONE_PRESSURE_PLATE.parseMaterial());
			}
			else{
			if(turretBlock.getType() != Materials.SKELETON_SKULL.parseMaterial()){
				turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
				//turretBlock.setData((byte) 1);
			}
			}
	
		}
		return toBeRemoved;
	}

	public void onChunkLoad(ChunkLoadEvent event) {
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			@Override
			public void run() {
				Land land = GameManagement.getLandManager().getLand(new SimpleChunkLocation(event.getChunk()));
				if (land.getTurrets().isEmpty())
					return;
				for (Turret t : land.getTurrets()) {
					TurretType type = t.getType();
					Block turretBlock = t.getLoc().toLocation().getBlock();
					if (type != TurretType.MINE_CHEMICAL && type != TurretType.MINE_PRESSURE) {
						if (!(turretBlock.getState() instanceof Skull)) {
							turretBlock.setType(Materials.SKELETON_SKULL.parseMaterial());
							//turretBlock.setData((byte) 1);
						}
						Skull s = (Skull) turretBlock.getState();
						if (s == null)
							continue;
						if (type.getSkin() == null)
							continue;
						//s.setOwner(type.getSkin());
						//s.update();
					}
				}
			}
		}, 1L);
	}
	
	plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
		@Override
		public void run(){
		for(SimpleChunkLocation loc : GameManagement.getLandManager().getAllLandLoc()){
			Land land = GameManagement.getLandManager().getOrLoadLand(loc);
			World world = Bukkit.getWorld(loc.getWorld());
			if(world == null) continue;
			if(!world.isChunkLoaded(loc.getX(), loc.getZ())) continue;
			Chunk c = loc.toChunk();

			if(canChunkBeUnloaded(c)){
			continue;
			}

			if(land.getOwnerUUID() == null)
			continue;
			if(!land.getLoc().toChunk().isLoaded()) continue;
			Iterator<Turret> iter = land.getTurrets().iterator();


			while(iter.hasNext()){
			Turret turret = iter.next();
			if(turret == null){
				iter.remove();
				continue;
			}
			if(turret.getType() == null){
				iter.remove();
				continue;
			}
			if(!turret.getType().isEnabled()) continue;
			if(turret.getType().toString().startsWith("MINE")) continue;
			if(!turret.isValid()) continue;
			Collection<Entity> nearby = getNearbyChunkEntities(c, turret.getType().getRange());
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable() {
				@Override
				public void run(){

				if(turret.aim(nearby)){
					Bukkit.getScheduler().runTask(plugin, new BukkitRunnable() {
					@Override
					public void run(){
						turret.fire();
					}
					});

				}
				}
			});
			}
		}
		}
	}, 0, 5L);
	*/
	
	/*
	@EventHandler
	public void onBlockUnderPressurePlateBreak(BlockBreakEvent event) {
		Block block = event.getBlock().getRelative(0, 1, 0);
		if (block.getType().toString().contains("PLATE") && isTurret(block)) {
			Location location = block.getLocation();
			Land land = landManager.getLand(location.getChunk());
			Turret turret = land.getTurret(location);
			turret.breakTurret();
		}
	}
	
	@EventHandler
	public void onMineTrigger(PlayerInteractEvent event){
		if (event.getAction() != Action.PHYSICAL)
			return;
		Block mine = event.getClickedBlock();
		Location location = mine.getLocation();
		Land land = landManager.getLand(location.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null)
			return;
		Turret turret = land.getTurret(location);
		if (turret == null)
			return;
		if (!canBeTargeted(landKingdom, event.getPlayer()))
			return;
		event.setCancelled(true);
		TurretType type = turret.getType();
		if (!type.isEnabled())
			return;
		Player player = event.getPlayer();
		if (type is a chemical mine) {
			int dur = 100;
			if (landKingdom.getTurretUpgrades().isVirulentPlague())
				dur = 200;
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dur, Config.getConfig().getInt("turret-specs.chemicalmine.poison-potency")));
	
		} else if (type is a regular mine) {
			World world = mine.getWorld();
			world.createExplosion(location, type.getDamage(), false);
			if (landKingdom.getTurretUpgrades().isConcentratedBlast())
				world.createExplosion(location, type.getDamage(), false);
		}
		breakTurret(turret);
	}
	*/

}
