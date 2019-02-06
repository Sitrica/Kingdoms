package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.database.DatabaseTransferTask;
import com.songoda.kingdoms.database.SQLiteDatabase;
import com.songoda.kingdoms.events.KingdomLoadEvent;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.CooldownManager.KingdomCooldown;
import com.songoda.kingdoms.objects.kingdom.BotKingdom;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.IntervalUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class KingdomManager extends Manager {

	static {
		registerManager("kingdom", new KingdomManager());
	}
	
	public static Set<OfflineKingdom> kingdoms = new HashSet<>();
	private final Set<BotKingdom> bots = new HashSet<>();
	private final Database<OfflineKingdom> database;
	private final CooldownManager cooldowns;
	private BukkitTask autoSaveThread;

	protected KingdomManager() {
		super(true);
		this.cooldowns = instance.getManager("cooldown", CooldownManager.class);
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(OfflineKingdom.class);
		else
			database = getSQLiteDatabase(OfflineKingdom.class);
		if (configuration.getBoolean("database.auto-save.enabled")) {
			String interval = configuration.getString("database.auto-save.interval", "5 miniutes");
			autoSaveThread = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
				@Override 
				public void run() {
					for (OfflineKingdom kingdom : kingdoms) {
						UUID uuid = kingdom.getUniqueId();
						String name = kingdom.getName();
						//TODO Add a temp system if it's needed later, this is bad management.
						/*if (name.endsWith("_tmp")) {
							kingdoms.remove(kingdom);
							database.delete(kingdom.getUniqueId() + "");
							continue;
						}*/
						Kingdoms.debugMessage("Saving Kingdom: " + name);
						if (cooldowns.isInCooldown(kingdom, "attackcd"))
							kingdom.setInvasionCooldown(cooldowns.getTimeLeft(kingdom, "attackcd"));
						//null king means ready to remove
						if (kingdom.getKing() == null && !(kingdom instanceof BotKingdom)) {
							database.save(uuid + "", null);
							continue;
						}
						database.save(uuid + "", kingdom);
					}
				}
			}, 0, IntervalUtils.getInterval(interval) * 20);
		}
	}
	
	public Set<OfflineKingdom> getKingdoms() {
		return kingdoms;
	}

	public Set<BotKingdom> getBotKingdoms() {
		return bots;
	}
	
	public Kingdom getKingdom(OfflineKingdom kingdom) {
		return getKingdom(kingdom.getUniqueId());
	}

	public Kingdom getKingdom(UUID uuid) {
		if (uuid == null)
			return null;
		Kingdoms.debugMessage("Fetching info for kingdom: " + uuid);
		return kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getUniqueId().equals(uuid))
				.map(kingdom -> {
					if (kingdom instanceof Kingdom)
						return (Kingdom) kingdom;
					if (kingdom.getKing() == null && !(kingdom instanceof BotKingdom))
						database.delete(uuid + "");
					return null;
				})
				.findAny()
				.orElse(loadKingdom(uuid));
	}
	
	public Optional<Kingdom> getKingdomByName(String name) {
		if (name == null)
			return null;
		Kingdoms.debugMessage("Fetching info for kingdom: " + name);
		return kingdoms.parallelStream()
				.filter(kingdom -> kingdom.getName().equals(name))
				.map(kingdom -> {
					if (kingdom instanceof Kingdom)
						return (Kingdom) kingdom;
					if (kingdom.getKing() == null && !(kingdom instanceof BotKingdom))
						database.delete(kingdom.getUniqueId() + "");
					return null;
				})
				.findAny();
	}
	
	private Kingdom loadKingdom(OfflineKingdom kingdom) {
		return loadKingdom(kingdom.getUniqueId());
	}
	
	private Kingdom loadKingdom(UUID uuid) {
		Kingdoms.consoleMessage("Loading kingdom: " + uuid);
		//kingdoms.parallelStream()
		//		.filter(kingdom -> kingdom.getUniqueId().equals(uuid))
		//		.forEach(kingdom -> database.save(uuid + "", kingdom)); //TODO This used to save the old kingdom from a map? Figure out why?
		// This can cause error can't it? Fix later if it does. - Recode.
		Kingdom kingdom = (Kingdom) database.get(uuid + "");
		if (kingdom != null) {
			kingdom.setNeutral(false);
			long invasionCooldown = kingdom.getInvasionCooldown();
			if (invasionCooldown > 0) {
				KingdomCooldown cooldown = new KingdomCooldown(kingdom, "attackcd", invasionCooldown);
				cooldown.start();
				kingdom.setInvasionCooldown(0);
			}
			checkUpgrades(kingdom);
			checkMight(kingdom);
			kingdoms.add(kingdom);
			Bukkit.getPluginManager().callEvent(new KingdomLoadEvent(kingdom));
		}
		return kingdom;
	}

	public boolean isBotKingdom(Kingdom kingdom) {
		if (kingdom instanceof BotKingdom)
			return true;
		if (bots.contains(kingdom.getName()))
			return true;
		return false;
	}

	public void registerBotKingdom(BotKingdom kingdom) {
		bots.add(kingdom);
		kingdoms.add(kingdom);
		Kingdoms.debugMessage("Registered Bot Kingdom: " + kingdom.getName());
	}

	public boolean renameKingdom(OfflineKingdom kingdom, String name) {
		return kingdoms.parallelStream()
				.filter(k -> k.getName().equals(name))
				.findFirst()
				.isPresent();
	}
	
	public int getRandomColor() {
		Random random = new Random();
		int color = 0;
		int r = random.nextInt(255);
		int g = random.nextInt(255);
		int b = random.nextInt(255);
		color = (r << 16) + (g << 8) + b;
		return color;
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		ArrayList<UUID> toBeRemoved = new ArrayList<>();
		Kingdom kingdom = null;
		for (UUID kingdomUuid : toBeLoaded) {

			kingdom = (Kingdom) databaseLoad(kingdomUuid, null);

			if (kingdom != null) {

				if ((kingdom.getHome_loc() != null && kingdom.getHome_loc().getWorld() != null) ||
						(kingdom.getNexus_loc() != null && kingdom.getNexus_loc().getWorld() != null)) {
					toBeRemoved.add(kingdomUuid);
				}

				kingdomNameList.put(kingdom.getKingdomName(), kingdomUuid);
				kingdomList.put(kingdomUuid, kingdom);
				plugin.getServer().getPluginManager().callEvent(new KingdomLoadEvent(kingdom));

			}
		}
		for (UUID kingdomName : toBeRemoved) {
			toBeLoaded.remove(kingdomName);
		}
	}

	public void checkMight(Kingdom kingdom) {
		if (kingdom == null) return;
		int might = 0;
		if (kingdom.getMisupgradeInfo().isAnticreeper()) might += 53;
		if (kingdom.getMisupgradeInfo().isAntitrample()) might += 53;
		if (kingdom.getMisupgradeInfo().isBombshards()) might += 53;
		if (kingdom.getMisupgradeInfo().isFireproofing()) might += 53;
		if (kingdom.getMisupgradeInfo().isGlory()) might += 53;
		if (kingdom.getMisupgradeInfo().isNexusguard()) might += 53;
		if (kingdom.getMisupgradeInfo().isPsioniccore()) might += 53;
		might += 53 * kingdom.getPowerUp().getArrowboost();
		might += 53 * kingdom.getPowerUp().getDmgboost();
		might += 53 * kingdom.getPowerUp().getDmgreduction();
		might += 53 * kingdom.getPowerUp().getRegenboost();
		might += 53 * kingdom.getMaxMember();
		might += 53 * kingdom.getPowerUp().getRegenboost();
		might += 53 * kingdom.getChampionInfo().getAqua();
		might += 53 * kingdom.getChampionInfo().getArmor();
		might += 53 * kingdom.getChampionInfo().getDamage();
		might += 53 * kingdom.getChampionInfo().getDamagecap();
		might += 53 * kingdom.getChampionInfo().getDetermination();
		might += 53 * kingdom.getChampionInfo().getDrag();
		might += 53 * kingdom.getChampionInfo().getDuel();
		might += 53 * kingdom.getChampionInfo().getFocus();
		might += 53 * kingdom.getChampionInfo().getGrab();
		might += 6 * kingdom.getChampionInfo().getHealth();
		might += 53 * kingdom.getChampionInfo().getMimic();
		might += 53 * kingdom.getChampionInfo().getMock();
		might += 53 * kingdom.getChampionInfo().getPlow();
		might += 53 * kingdom.getChampionInfo().getReinforcements();
		might += 53 * kingdom.getChampionInfo().getResist();
		might += 53 * kingdom.getChampionInfo().getSpeed();
		might += 53 * kingdom.getChampionInfo().getStrength();
		might += 53 * kingdom.getChampionInfo().getSummon();
		might += 53 * kingdom.getChampionInfo().getThor();
		might += 53 * kingdom.getChampionInfo().getWeapon();
		might += 53 * (kingdom.getChestsize() - 9);
		if (kingdom.getTurretUpgrades().isFinalService()) might += 530;
		if (kingdom.getTurretUpgrades().isConcentratedBlast()) might += 530;
		if (kingdom.getTurretUpgrades().isFlurry()) might += 530;
		if (kingdom.getTurretUpgrades().isHellstorm()) might += 530;
		if (kingdom.getTurretUpgrades().isImprovedHeal()) might += 530;
		if (kingdom.getTurretUpgrades().isSimplifiedModel()) might += 530;
		if (kingdom.getTurretUpgrades().isUnrelentingGaze()) might += 530;
		if (kingdom.getTurretUpgrades().isUnrelentingGaze()) might += 530;
		if (kingdom.getTurretUpgrades().isVoodoo()) might += 530;
		kingdom.setMight(might);
	}
	
	public void debugDeleteKingdom(String name) {
		UUID kingdom = kingdomNameList.remove(name);
		db.save(kingdom.toString(), null);
		for (UUID uuid : PlayerManager.userList.keySet()) {
			OfflineKingdomPlayer player = PlayerManager.userList.get(uuid);
			if (player.getKingdomUuid() != null) {
				if (player.getKingdomUuid().equals(kingdom)) {
					player.setKingdomUuid(null);
					PlayerManager.userList.remove(uuid);
					PlayerManager.userList.put(uuid, player);
				}
			}
		}
	}

	public void checkUpgrades(Kingdom kingdom) {

		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {

				if (kingdom == null) return;
				if (Config.getConfig().getBoolean("defaulton.misc-upgrades.anticreeper"))
					kingdom.getMisupgradeInfo().setAnticreeper(true);
				if (Config.getConfig().getBoolean("defaulton.misc-upgrades.bombshards"))
					kingdom.getMisupgradeInfo().setBombshards(true);
				if (Config.getConfig().getBoolean("defaulton.misc-upgrades.antitrample"))
					kingdom.getMisupgradeInfo().setAntitrample(true);
				if (Config.getConfig().getBoolean("defaulton.misc-upgrades.glory"))
					kingdom.getMisupgradeInfo().setGlory(true);
				if (Config.getConfig().getBoolean("defaulton.misc-upgrades.nexusguard"))
					kingdom.getMisupgradeInfo().setNexusguard(true);
				if (Config.getConfig().getBoolean("defaulton.misc-upgrades.psioniccore"))
					kingdom.getMisupgradeInfo().setPsioniccore(true);

				if (kingdom.getTurretUpgrades().isSimplifiedModel()) {
					kingdom.getTurretUpgrades().setSimplifiedModel(Config.getConfig().getBoolean("enable.turretupgrades.simplified-model"));
				}
				if (kingdom.getTurretUpgrades().isFlurry()) {
					kingdom.getTurretUpgrades().setFlurry(Config.getConfig().getBoolean("enable.turretupgrades.flurry"));
				}
				if (kingdom.getTurretUpgrades().isConcentratedBlast()) {
					kingdom.getTurretUpgrades().setConcentratedBlast(Config.getConfig().getBoolean("enable.turretupgrades.concentrated-blast"));
				}
				if (kingdom.getTurretUpgrades().isVirulentPlague()) {
					kingdom.getTurretUpgrades().setVirulentPlague(Config.getConfig().getBoolean("enable.turretupgrades.virulent-plague"));
				}
				if (kingdom.getTurretUpgrades().isImprovedHeal()) {
					kingdom.getTurretUpgrades().setImprovedHeal(Config.getConfig().getBoolean("enable.turretupgrades.improved-healing"));
				}
				if (kingdom.getTurretUpgrades().isVoodoo()) {
					kingdom.getTurretUpgrades().setVoodoo(Config.getConfig().getBoolean("enable.turretupgrades.voodoo"));
				}
				if (kingdom.getTurretUpgrades().isFinalService()) {
					kingdom.getTurretUpgrades().setFinalService(Config.getConfig().getBoolean("enable.turretupgrades.final-service"));
				}
				if (kingdom.getTurretUpgrades().isHellstorm()) {
					kingdom.getTurretUpgrades().setHellstorm(Config.getConfig().getBoolean("enable.turretupgrades.hellstorm"));
				}
				if (kingdom.getTurretUpgrades().isUnrelentingGaze()) {
					kingdom.getTurretUpgrades().setUnrelentingGaze(Config.getConfig().getBoolean("enable.turretupgrades.unrelenting-gaze"));
				}
				if (kingdom.getMisupgradeInfo().isAnticreeper()) {
					kingdom.getMisupgradeInfo().setAnticreeper(Config.getConfig().getBoolean("enable.misc.anticreeper.enabled"));
				}
				if (kingdom.getMisupgradeInfo().isAntitrample()) {
					kingdom.getMisupgradeInfo().setAntitrample(Config.getConfig().getBoolean("enable.misc.antitrample"));
				}
				if (kingdom.getMisupgradeInfo().isBombshards()) {
					kingdom.getMisupgradeInfo().setBombshards(Config.getConfig().getBoolean("enable.misc.bombshards.enabled"));
				}
				if (kingdom.getMisupgradeInfo().isGlory()) {
					kingdom.getMisupgradeInfo().setGlory(Config.getConfig().getBoolean("enable.misc.glory"));
				}
				if (kingdom.getMisupgradeInfo().isNexusguard()) {
					kingdom.getMisupgradeInfo().setNexusguard(Config.getConfig().getBoolean("enable.misc.nexusshards"));
				}
				if (kingdom.getMisupgradeInfo().isPsioniccore()) {
					kingdom.getMisupgradeInfo().setPsioniccore(Config.getConfig().getBoolean("enable.misc.psioniccore"));
				}
				if (kingdom.getPowerUp().getDmgboost() > Config.getConfig().getInt("max.nexusupgrades.dmg-boost")) {
					kingdom.getPowerUp().setDmgboost(Config.getConfig().getInt("max.nexusupgrades.dmg-boost"));
				}
				if (kingdom.getPowerUp().getDmgreduction() > Config.getConfig().getInt("max.nexusupgrades.dmg-reduc")) {
					kingdom.getPowerUp().setDmgreduction(Config.getConfig().getInt("max.nexusupgrades.dmg-reduc"));
				}
				if (kingdom.getPowerUp().getArrowboost() > Config.getConfig().getInt("max.nexusupgrades.arrow-boost")) {
					kingdom.getPowerUp().setArrowboost(Config.getConfig().getInt("max.nexusupgrades.arrow-boost"));
				}
				if (kingdom.getPowerUp().getRegenboost() > Config.getConfig().getInt("max.nexusupgrades.regen-boost")) {
					kingdom.getPowerUp().setRegenboost(Config.getConfig().getInt("max.nexusupgrades.regen-boost"));
				}
				if (kingdom.getMaxMember() > Config.getConfig().getInt("max.nexusupgrades.maxmembers")) {
					kingdom.setMaxMember(Config.getConfig().getInt("max.nexusupgrades.maxmembers"));
				}
				if (kingdom.getChampionInfo().getHealth() > Config.getConfig().getInt("max.champion.health")) {
					kingdom.getChampionInfo().setHealth(Config.getConfig().getInt("max.champion.health"));
				}
				if (kingdom.getChampionInfo().getDetermination() > Config.getConfig().getInt("max.champion.determination")) {
					kingdom.getChampionInfo().setDetermination(Config.getConfig().getInt("max.champion.determination"));
				}
				if (kingdom.getChampionInfo().getSpeed() > Config.getConfig().getInt("max.champion.speed")) {
					kingdom.getChampionInfo().setSpeed(Config.getConfig().getInt("max.champion.speed"));
				}
				if (kingdom.getChampionInfo().getResist() > Config.getConfig().getInt("max.champion.resist")) {
					kingdom.getChampionInfo().setResist(Config.getConfig().getInt("max.champion.resist"));
				}
				if (kingdom.getChampionInfo().getWeapon() > Config.getConfig().getInt("max.champion.weapon")) {
					kingdom.getChampionInfo().setWeapon(Config.getConfig().getInt("max.champion.weapon"));
				}
				if (kingdom.getChampionInfo().getDrag() > Config.getConfig().getInt("max.champion.drag")) {
					kingdom.getChampionInfo().setDrag(Config.getConfig().getInt("max.champion.drag"));
				}
				if (kingdom.getChampionInfo().getMock() > Config.getConfig().getInt("max.champion.mock")) {
					kingdom.getChampionInfo().setMock(Config.getConfig().getInt("max.champion.mock"));
				}
				if (kingdom.getChampionInfo().getAqua() > 0 && !Config.getConfig().getBoolean("enable.champion.aqua") ||
						Enchantment.getByName("DEPTH_STRIDER") == null) {
					kingdom.getChampionInfo().setAqua(0);
				}
				if (kingdom.getChampionInfo().getDuel() > Config.getConfig().getInt("max.champion.duel")) {
					kingdom.getChampionInfo().setDuel(Config.getConfig().getInt("max.champion.duel"));
				}
				if (kingdom.getChampionInfo().getThor() > Config.getConfig().getInt("max.champion.thor")) {
					kingdom.getChampionInfo().setThor(Config.getConfig().getInt("max.champion.thor"));
				}
				if (kingdom.getChampionInfo().getStrength() > Config.getConfig().getInt("max.champion.strength")) {
					kingdom.getChampionInfo().setStrength(Config.getConfig().getInt("max.champion.strength"));
				}

			}
		});

	}

	/**
	 * check if the kingdom exist with specified name;
	 *
	 * @param kingdomName kingdom name to search
	 * @return true if exist; false if not exist
	 */
	public boolean hasKingdom(String kingdomName) {
		return kingdomNameList.containsKey(kingdomName);
	}

	/**
	 * get OfflineKingdom. Reading from database directly
	 *
	 * @param kingdomName kingdomName
	 * @return Kingdom instance; null if not exist
	 */
	public OfflineKingdom getOfflineKingdom(String kingdomName) {
		Kingdoms.logColor("Fetching info for offline kingdom, " + kingdomName);
		OfflineKingdom kingdom = loadKingdom(kingdomNameList.get(kingdomName));

		//was pending for deletion so do it immediately
		if (kingdom != null && kingdom.getKing() == null) {
			db.save(kingdomName, null);
			return null;
		}

		return kingdom;
	}

	public OfflineKingdom getOfflineKingdom(UUID kingdom) {
		Kingdoms.logColor("Fetching info for offline kingdom, " + kingdom);
		OfflineKingdom kingdoms = loadKingdom(kingdom);

		//was pending for deletion so do it immediately
		if (kingdom != null && kingdoms.getKing() == null) {
			db.save(kingdom.toString(), null);
			return null;
		}

		return kingdoms;
	}


	/**
	 * <b>NOT THREAD SAFE</b> (use only on startup)
	 *
	 * @return list of kingdoms by resource points
	 */
	public Map<String, Integer> getAllByResourcePointsFromDB() {
		Map<String, Integer> points = new HashMap<>();

		Set<String> keys = db.getKeys();
		if (keys.isEmpty())
			return points;

		for (String key : keys) {
			UUID uuid = UUID.fromString(key);
			Kingdom kingdom = getOrLoadKingdom(uuid);
			if (kingdom == null) {
				continue;
			}else{
				if (Config.getConfig().getBoolean("hidePacifistsFromTop")&&kingdom.isNeutral())continue;
			points.put(key, kingdom.getResourcepoints());
			}
		}

		return points;
	}

	/**
	 * This method will <b> overwrite existing data! </b> use hasKingdom() to
	 * check if kingdom already exists
	 *
	 * @param kingdomName
	 * @param king		the king of this kingdom
	 * @return false - already in progress, true - create task scheduled
	 */
	public boolean createNewKingdom(String kingdomName, KingdomPlayer king) {
		if (king.isProcessing())
			return false;

		king.sendMessage(Kingdoms.getLang().getString("Misc_Kingdom_Creation_Loading", king.getLang()));
		king.setProcessing(true);
		new Thread(new NewKingdomCreateTask(kingdomList, kingdomName, king)).start();

		return true;
	}

	private class NewKingdomCreateTask implements Runnable {
		Map<UUID, OfflineKingdom> kingdomList;
		String kingdomName;
		KingdomPlayer king;

		public NewKingdomCreateTask(Map<UUID, OfflineKingdom> kingdomList2, String kingdomName, KingdomPlayer king) {
			super();
			this.kingdomList = kingdomList2;
			this.kingdomName = kingdomName;
			this.king = king;
		}

		@Override
		public void run() {
			// create new kingdom instance
			Kingdom kingdom = new Kingdom();
			kingdom.setKingdomName(kingdomName);
			kingdom.setKing(king.getUuid());
			if (Config.getConfig().getBoolean("pacifist-default-on")) kingdom.setNeutral(true);
			// add king to kingdom
			kingdom.addMember(king.getUuid());
			kingdom.getOnlineMembers().add(king);
			kingdom.setMaxMember(Config.getConfig().getInt("base-member-count"));
			// give kingdom shield
			kingdom.giveShield(Config.getConfig().getInt("beginner-shield.duration-in-minutes"));
			// set king data
			king.setRank(Rank.KING);
			king.setKingdom(kingdom);

			// add this kingdom to list
			kingdomList.put(kingdom.getKingdomUuid(), kingdom);
			kingdomNameList.put(kingdom.getKingdomName(), kingdom.getKingdomUuid());
			for (ChampionUpgrade upgrade : ChampionUpgrade.values()) {
				kingdom.getChampionInfo().setUpgradeLevel(upgrade, upgrade.getUpgradeDefault(upgrade));
			}


			for (MiscUpgrade upgrade : MiscUpgrade.values()) {
				kingdom.getMisupgradeInfo().setBought(upgrade, upgrade.isDefaultOn());
			}

			// change player status
			king.sendMessage(Kingdoms.getLang().getString("Misc_Kingdom_Creation_Success", king.getLang())
					.replaceAll("%kingdom%", kingdomName));
			king.setProcessing(false);

			plugin.getServer().getPluginManager().callEvent(new KingdomCreateEvent(kingdom));
		}

	}

	/**
	 * This method will <b> overwrite existing data! </b> Use it carefully
	 *
	 * @param kingdomName
	 * @param king		the king of this kingdom
	 * @return false - already in progress, true - delete task scheduled
	 */
	public boolean deleteKingdom(String kingdomName, KingdomPlayer king) {
		if (king.isProcessing())
			return false;

		Kingdom kingdom = getOrLoadKingdom(kingdomName);
		if (kingdom == null) {
			king.sendMessage("No kingdom found named [" + kingdomName + "].");
			king.setProcessing(false);
			return true;
		}

		king.sendMessage(Kingdoms.getLang().getString("Misc_Kingdom_Deleting", king.getLang()));
		king.setProcessing(true);
		new Thread(new KingdomDeleteTask(kingdomList, kingdom, king)).start();
		return true;
	}

	/**
	 * schedule kingdom delete task
	 *
	 * @param kingdomName name of kingdom to delete
	 * @return true if scheduled; false if kingdom doesn't exist
	 */
	public boolean deleteKingdom(String kingdomName) {
		Kingdom kingdom = getOrLoadKingdom(kingdomName);
		if (kingdom == null) {
			return false;
		}

		new Thread(new KingdomDeleteTask(kingdomList, kingdom)).start();
		return true;
	}

	private class KingdomDeleteTask implements Runnable {
		Map<UUID, OfflineKingdom> kingdomList;
		Kingdom kingdom;
		KingdomPlayer king;

		public KingdomDeleteTask(Map<UUID, OfflineKingdom> kingdomList2, Kingdom kingdom, KingdomPlayer king) {
			super();
			this.kingdomList = kingdomList2;
			this.kingdom = kingdom;
			this.king = king;
		}

		public KingdomDeleteTask(Map<UUID, OfflineKingdom> kingdomList, Kingdom kingdom) {
			super();
			this.kingdomList = kingdomList;
			this.kingdom = kingdom;
			this.king = null;
		}

		@Override
		public void run() {
			try {
				UUID kingdomUUID = kingdom.getKingdomUuid();

				if (kingdom instanceof BotKingdom) {
					plugin.getServer().getPluginManager().callEvent(new KingdomDeleteEvent(kingdom));

					GameManagement.getLandManager().unclaimAllLand(kingdom);
					return;
				}
				if (king != null) {
					king.setKingdom(null);
					king.setRank(Rank.ALL);
				} else {
					OfflineKingdomPlayer kingdomking = GameManagement.getPlayerManager()
							.getOfflineKingdomPlayer(kingdom.getKing());
					if (kingdomking != null) {
						if (kingdomking.isOnline()) {// if online
							kingdomking.getKingdomPlayer().setKingdom(null);
							kingdomking.getKingdomPlayer().setRank(Rank.ALL);

							if (ExternalManager.getScoreboardManager() != null)
								ExternalManager.getScoreboardManager().updateScoreboard((KingdomPlayer) kingdomking);
						} else {// if offline
							kingdomking.setKingdomName(null);
							kingdomking.setRank(Rank.ALL);
						}
					}
				}

				// reset user data
				for (UUID uuid : kingdom.getMembersList()) {
					OfflineKingdomPlayer kp = GameManagement.getPlayerManager().getSession(uuid);
					if (kp == null)
						continue;

					if (kp.isOnline()) {// if online
						kp.getKingdomPlayer().setKingdom(null);
						kp.getKingdomPlayer().setRank(Rank.ALL);

						if (ExternalManager.getScoreboardManager() != null)
							ExternalManager.getScoreboardManager().updateScoreboard(kp.getKingdomPlayer());
					} else {// if offline
						kp.setRank(Rank.ALL);
					}
				}

				// delete all data
				kingdomList.remove(kingdomUUID);
				kingdomNameList.remove(kingdom.getKingdomName());
				db.save(kingdomUUID.toString(), null);
				plugin.getServer().getPluginManager().callEvent(new KingdomDeleteEvent(kingdom));

				// check for lands
				GameManagement.getLandManager().unclaimAllLand(kingdom);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// set king processing state
				if (king != null) {
					king.sendMessage(Kingdoms.getLang().getString("Misc_Kingdom_Deleted", king.getLang()));
					king.setProcessing(false);
				}
			}
		}
	}

	/**
	 * check if the kingdom is loaded
	 *
	 * @param kingdomName name of kingdom
	 * @return true if loaded; false if not loaded
	 */
	public boolean isOnline(String kingdomName) {
		if (!kingdomNameList.containsKey(kingdomName))
			return false;

		return kingdomList.get(kingdomNameList.get(kingdomName)) instanceof Kingdom;
	}


	@EventHandler
	public void onMemberAttacksKingdomMember(EntityDamageByEntityEvent event) {
		if (!Config.getConfig().getStringList("enabled-worlds").contains(event.getEntity().getWorld().getName())) {
			return;
		}
		KingdomPlayer attacked = null;
		if (!(event.getEntity() instanceof Player))
			return;
		GameManagement.getApiManager();
		if (ExternalManager.isCitizen(event.getEntity())) return;
		GameManagement.getApiManager();
		if (ExternalManager.isCitizen(event.getDamager())) return;

		if (event.getDamager().getUniqueId().equals(event.getEntity().getUniqueId()))
			return;
		if (event.getDamager() instanceof Projectile) {
			// skip if ender_pearl
			if (((Projectile) event.getDamager()).getType() == EntityType.ENDER_PEARL)
				return;

			if (((Projectile) event.getDamager()).getShooter() != null) {
				if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
					GameManagement.getApiManager();
					if (ExternalManager.isCitizen((Entity) ((Projectile) event.getDamager()).getShooter())) return;


					attacked = GameManagement.getPlayerManager()
							.getSession((Player) ((Projectile) event.getDamager()).getShooter());
				}
			}

		} else if (event.getDamager() instanceof Player) {
			attacked = GameManagement.getPlayerManager().getSession((Player) event.getDamager());
		}
		if (attacked == null)
			return;

		if (attacked.isAdminMode())
			return;
		if (attacked.getKingdom() == null)
			return;
		KingdomPlayer damaged = GameManagement.getPlayerManager().getSession((Player) event.getEntity());

		if (Config.getConfig().getBoolean("warzone-free-pvp")) {
			Land att = GameManagement.getLandManager().getOrLoadLand(damaged.getLoc());
			if (att.getOwnerUUID() != null) {

			}
		}

		if (damaged.getKingdom() == null)
			return;
		if (attacked.getKingdom().equals(damaged.getKingdom())) {
			event.setDamage(0.0D);
			attacked.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Attack_Own_Member", attacked.getLang()));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onNeutralMemberAttackOrAttacked(EntityDamageByEntityEvent event) {
		if (!Config.getConfig().getStringList("enabled-worlds").contains(event.getEntity().getWorld().getName())) {
			return;
		}
		if (!Config.getConfig().getBoolean("allow-pacifist")) return;
		if (!Config.getConfig().getBoolean("neutralPlayersCannotFightOnOwnLand")) return;
		KingdomPlayer attacked = null;
		if (!(event.getEntity() instanceof Player))
			return;
		if (ExternalManager.isCitizen(event.getEntity())) return;
		if (event.getDamager().getUniqueId().equals(event.getEntity().getUniqueId()))
			return;
		if (event.getDamager() instanceof Projectile) {
			if (((Projectile) event.getDamager()).getShooter() != null) {
				if (((Projectile) event.getDamager()).getShooter() instanceof Player) {

					GameManagement.getApiManager();
					if (ExternalManager.isCitizen((Entity) ((Projectile) event.getDamager()).getShooter())) return;
					attacked = GameManagement.getPlayerManager()
							.getSession((Player) ((Projectile) event.getDamager()).getShooter());
				}
			}

		} else if (event.getDamager() instanceof Player) {
			GameManagement.getApiManager();
			if (ExternalManager.isCitizen(event.getDamager())) return;

			attacked = GameManagement.getPlayerManager().getSession((Player) event.getDamager());
		}
		if (attacked == null)
			return;
		if (attacked.isAdminMode())
			return;
		KingdomPlayer damaged = GameManagement.getPlayerManager().getSession((Player) event.getEntity());

		Land damagedLand = Kingdoms.getManagers().getLandManager().getOrLoadLand(damaged.getLoc());
		Land attackerLand = Kingdoms.getManagers().getLandManager().getOrLoadLand(attacked.getLoc());


		if (damaged.getKingdom() == null && attacked.getKingdom() == null) return;

		if (attacked.getKingdom() != null &&
				attacked.getKingdom().isNeutral()) {
			if (attacked.getKingdomUuid().equals(attackerLand.getOwnerUUID())) {
				attacked.sendMessage(Kingdoms.getLang().getString("Misc_Neutral_Cannot_Pvp_In_Own_Land", attacked.getLang()));
				event.setCancelled(true);
				return;
			}
		}

		if (damaged.getKingdom() != null &&
				damaged.getKingdom().isNeutral()) {
			if (damaged.getKingdomUuid().equals(damagedLand.getOwnerUUID())) {
				attacked.sendMessage(Kingdoms.getLang().getString("Misc_Neutral_Cannot_Pvp_In_Neutral_Land", attacked.getLang()));
				event.setCancelled(true);
				return;
			}
		}


	}

	@EventHandler(priority = EventPriority.LOW)
	public void onMemberAttacksAllyMembers(EntityDamageByEntityEvent event) {
		if (!Config.getConfig().getStringList("enabled-worlds").contains(event.getEntity().getWorld().getName())||Config.getConfig().getBoolean("ally-pvp")) {
			return;
		}
		KingdomPlayer attacked = null;
		if (!(event.getEntity() instanceof Player))
			return;
		GameManagement.getApiManager();
		if (ExternalManager.isCitizen(event.getEntity())) return;
		if (event.getDamager().getUniqueId().equals(event.getEntity().getUniqueId()))
			return;
		if (event.getDamager() instanceof Projectile) {
			if (((Projectile) event.getDamager()).getShooter() != null) {
				if (((Projectile) event.getDamager()).getShooter() instanceof Player) {

					GameManagement.getApiManager();
					if (ExternalManager.isCitizen((Entity) ((Projectile) event.getDamager()).getShooter())) return;
					attacked = GameManagement.getPlayerManager()
							.getSession((Player) ((Projectile) event.getDamager()).getShooter());
				}
			}

		} else if (event.getDamager() instanceof Player) {
			GameManagement.getApiManager();
			if (ExternalManager.isCitizen(event.getDamager())) return;

			attacked = GameManagement.getPlayerManager().getSession((Player) event.getDamager());
		}
		if (attacked == null)
			return;
		if (attacked.isAdminMode())
			return;
		if (attacked.getKingdom() == null)
			return;

		KingdomPlayer damaged = GameManagement.getPlayerManager().getSession((Player) event.getEntity());
		if (Config.getConfig().getBoolean("warzone-free-pvp")) {
			Land att = GameManagement.getLandManager().getOrLoadLand(damaged.getLoc());
			if (att.getOwnerUUID() != null) {

			}
		}

		if (damaged.getKingdom() == null)
			return;

		if (attacked.getKingdom().isAllianceWith(damaged.getKingdom())) {
			event.setDamage(0.0D);
			attacked.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Attack_Own_Ally", attacked.getLang()));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onKingdomPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())) {
			return;
		}
		Kingdoms.logDebug("command: " + e.getMessage());
		if (e.isCancelled())
			return;

		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if (kp == null)
			return;

		Land land = GameManagement.getLandManager().getOrLoadLand(kp.getLoc());
		if (land.getOwnerUUID() == null)
			return;

		Kingdom kingdom = getOrLoadKingdom(land.getOwnerUUID());
		if (kingdom == null)
			return;// Warzone or Safezone

		if (kp.getKingdom() == null) {

			if (isCommandDisabled(e.getMessage(), Config.getConfig().getStringList("denied-commands-neutral"))) {
				kp.sendMessage(Kingdoms.getLang().getString("Kingdom_Command_Denied_Other", kp.getLang()));
				e.setCancelled(true);
			}
		} else if (kingdom.isEnemyMember(kp) || kp.getKingdom().isEnemyWith(kingdom)) {
			if (isCommandDisabled(e.getMessage(), Config.getConfig().getStringList("denied-commands-enemy"))) {
				kp.sendMessage(Kingdoms.getLang().getString("Kingdom_Command_Denied_Enemy", kp.getLang()));
				e.setCancelled(true);
			}
		} else if (kingdom.isMember(kp) || kingdom.isAllyMember(kp)) {
		} else {
			if (isCommandDisabled(e.getMessage(), Config.getConfig().getStringList("denied-commands-neutral"))) {
				kp.sendMessage(Kingdoms.getLang().getString("Kingdom_Command_Denied_Other", kp.getLang()));
				e.setCancelled(true);
			}
		}
	}

	private boolean isCommandDisabled(String message, List<String> list) {
		for (String entry : list) {
			if (entry.equalsIgnoreCase(message)) return true;
		}
		return false;
	}

	///////////////////////////////////////////////////////
	boolean isProcessing = false;

	@EventHandler
	public void onJoin(KingdomPlayerLoginEvent e) {
		if (e.getKp().getKingdom() != null) {
			Kingdoms.logDebug("onJoin? kp received: " + e.getKp());
			e.getKp().getKingdom().onKingdomPlayerLogin(e.getKp());
		}
	}

	@EventHandler
	public void onQuit(KingdomPlayerLogoffEvent e) {
		if (e.getKp().getKingdom() != null) {
			Kingdoms.logDebug("onQuit?");
			e.getKp().getKingdom().onKingdomPlayerLogout(e.getKp());
		}
	}
	//////////////////////////////////////////////////////////////////////////

	@EventHandler
	public void onKingdomDelete(KingdomDeleteEvent e) {
		Kingdoms.logDebug("kdelete event: " + e.getKingdom().getKingdomName());
		for (Entry<UUID, OfflineKingdom> entry : kingdomList.entrySet()) {
			if (!(entry.getValue() instanceof Kingdom))
				continue;
			// String key = entry.getKey();
			Kingdom value = (Kingdom) entry.getValue();

			value.onKingdomDelete(e.getKingdom());
		}
	}

	//////////////////////////////////////////////////////////////////
	@EventHandler
	public void onKingdomMemberJoinEvent(KingdomMemberJoinEvent event) {
		Kingdoms.logDebug("memberjoin");
		for (Entry<UUID, OfflineKingdom> entry : kingdomList.entrySet()) {
			if (!(entry.getValue() instanceof Kingdom))
				continue;
			Kingdom value = (Kingdom) entry.getValue();

			value.onMemberJoinKingdom(event.getKp());
		}
	}

	@EventHandler
	public void onKingdomMemberQuitEvent(KingdomMemberLeaveEvent e) {
		Kingdoms.logDebug("memberquit");
		for (Entry<UUID, OfflineKingdom> entry : kingdomList.entrySet()) {
			if (!(entry.getValue() instanceof Kingdom))
				continue;
			Kingdom value = (Kingdom) entry.getValue();

			value.onMemberQuitKingdom(e.getKp());
		}
	}

	public void stopAutoSave() {
		autoSaveThread.interrupt();
		// 2016-08-22
		try {
			autoSaveThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public synchronized void onDisable() {
		// 2016-08-11
		stopAutoSave();
		Kingdoms.logInfo("Saving [" + kingdomNameList.size() + "] loaded kingdoms...");
		try {
			saveAll();
			Kingdoms.logInfo("Done!");
		} catch (Exception e) {
			Kingdoms.logInfo("SQL connection failed! Saving to file DB");
			db = createFileDB();
			saveAll();
			Config.getConfig().set("DO-NOT-TOUCH.grabKingdomsFromFileDB", true);
		}
		kingdomNameList.clear();
	}

}
