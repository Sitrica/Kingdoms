package com.songoda.kingdoms.objects.turrets;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.SoundPlayer;
import com.songoda.kingdoms.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TurretType {

	/*
	HEATBEAM(Kingdoms.getLang().getString("Guis_Turret_Heatbeam"),
		Kingdoms.getLang().getString("Guis_Turret_Heatbeam_Desc"),
		ChatColor.GREEN + "=========",
		"MHF_Guardian",
		4,
		TurretTargetType.MONSTERS, TurretTargetType.ENEMY_PLAYERS),
	HELLFIRE(Kingdoms.getLang().getString("Guis_Turret_Hellfire"),
		Kingdoms.getLang().getString("Guis_Turret_Hellfire_Desc"),
		ChatColor.RED + "---------",
		"BadLuck",
		2,
		TurretTargetType.MONSTERS, TurretTargetType.ENEMY_PLAYERS),
	MINE_CHEMICAL(Kingdoms.getLang().getString("Guis_Turret_ChemicalMine"),
		Kingdoms.getLang().getString("Guis_Turret_ChemicalMine_Desc"),
		ChatColor.DARK_GREEN + "______",
		"lol",
		0,
		TurretTargetType.ENEMY_PLAYERS),
	MINE_PRESSURE(Kingdoms.getLang().getString("Guis_Turret_PressureMine"),
		Kingdoms.getLang().getString("Guis_Turret_PressureMine_Desc"),
		ChatColor.DARK_AQUA + "______",
		"lol",
		0,
		TurretTargetType.ENEMY_PLAYERS),
	PSIONIC(Kingdoms.getLang().getString("Guis_Turret_Psionic"),
		Kingdoms.getLang().getString("Guis_Turret_Psionic_Desc"),
		ChatColor.GREEN + "O-O-O-O-O",
		"MHF_Creeper",
		1,
		TurretTargetType.ENEMY_PLAYERS),
	SOLDIER(Kingdoms.getLang().getString("Guis_Turret_Soldier"),
		Kingdoms.getLang().getString("Guis_Turret_Soldier_Desc"),
		ChatColor.GOLD + "=|=======>",
		"CybermanAC",
		8,
		TurretTargetType.ENEMY_PLAYERS);
	*/

	private final boolean enabled, natural, critical, flame, particle, heal, usePotions;
	private final String node, title, decal, skin, meta, reload;
	private final List<String> description = new ArrayList<>();
	private final int cost, damage, range, max, ammo, spread;
	private final Set<TargetType> targets = new HashSet<>();
	private SoundPlayer placing, reloading, shooting;
	private final FileConfiguration configuration;
	private ParticleProjectile particleProjectile;
	private final long cooldown, firerate;
	private final EntityType projectile;
	private final Material material;
	private HealthInfo health;
	private Potions potions;

	public TurretType(String node) {
		configuration = Kingdoms.getInstance().getConfiguration("turrets").get();
		ConfigurationSection section = configuration.getConfigurationSection("turrets.turrets." + node);
		ConfigurationSection item = section.getConfigurationSection("item");
		if (section.getBoolean("use-place-sound", false))
			if (section.isConfigurationSection("place-sounds"))
				this.placing = new SoundPlayer(section.getConfigurationSection("place-sounds"));
		if (section.getBoolean("use-shoot-sound", false))
			if (section.isConfigurationSection("shoot-sounds"))
				this.shooting = new SoundPlayer(section.getConfigurationSection("shoot-sounds"));
		if (section.getBoolean("use-reload-sound", false))
			if (section.isConfigurationSection("reload-sounds"))
				this.reloading = new SoundPlayer(section.getConfigurationSection("reload-sounds"));
		this.particle = section.getBoolean("particle-projectile.enabled", false);
		if (particle && section.isConfigurationSection("particle-projectile"))
			particleProjectile = new ParticleProjectile(section.getConfigurationSection("particle-projectile"));
		this.heal = section.getBoolean("health-gain.enabled", false);
		if (heal)
			health = new HealthInfo(section.getConfigurationSection("health-gain"));
		this.usePotions = section.getBoolean("potions.enabled", false);
		if (usePotions && section.isConfigurationSection("potions.list"))
			potions = new Potions(section.getConfigurationSection("potions.list"));
		this.material = Utils.materialAttempt(item.getString("material", "MUSIC_DISC_STAL"), "GOLD_RECORD");
		this.projectile = Utils.entityAttempt(section.getString("projectile", "ARROW"), "ARROW");
		this.firerate = IntervalUtils.getInterval(section.getString("fire-rate", "1 second"));
		this.cooldown = IntervalUtils.getInterval(section.getString("cooldown", "5 seconds"));
		this.reload = section.getString("reloading-skull-skin", "Redstone");
		this.skin = section.getString("skull-skin", "MHF_Skeleton");
		this.description.addAll(item.getStringList("description"));
		this.natural = section.getBoolean("natural-damage", false);
		this.critical = section.getBoolean("critical", false);
		this.decal = section.getString("decal", "---------");
		this.enabled = section.getBoolean("enabled", true);
		this.flame = section.getBoolean("flame", false);
		this.max = section.getInt("max-per-land", 50);
		this.spread = section.getInt("spread", 12);
		this.damage = section.getInt("damage", 4);
		this.range = section.getInt("range", 8);
		this.cost = section.getInt("cost", 300);
		this.ammo = section.getInt("ammo", 1);
		this.title = item.getString("title");
		this.meta = item.getString("meta");
		this.node = node;
	}

	public ParticleProjectile getParticleProjectile() {
		return particleProjectile;
	}

	public SoundPlayer getReloadingSounds() {
		return reloading;
	}

	public SoundPlayer getShootingSounds() {
		return shooting;
	}

	public SoundPlayer getPlacingSounds() {
		return placing;
	}

	public boolean isParticleProjectile() {
		return particle;
	}

	public List<String> getDescription() {
		return description;
	}

	public Set<TargetType> getTargets() {
		return targets;
	}

	public HealthInfo getHealthInfo() {
		return health;
	}

	public EntityType getProjectile() {
		return projectile;
	}

	/**
	 * Cooldown is in milliseconds.
	 */
	public long getReloadCooldown() {
		return cooldown * 1000;
	}

	public Material getMaterial() {
		return material;
	}

	public boolean isCritical() {
		return critical;
	}

	public int getArrowSpread() {
		return spread;
	}

	public Potions getPotions() {
		return potions;
	}

	public boolean hasPotions() {
		return usePotions;
	}

	public boolean isNatural() {
		return natural;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isHealer() {
		return heal;
	}

	/*
	 * Firerate is in milliseconds.
	 */
	public long getFirerate() {
		return firerate * 1000;
	}

	public String getTitle() {
		return Formatting.color(title);
	}

	public boolean isFlame() {
		return flame;
	}

	public String getDecal() {
		return decal;
	}

	public String getName() {
		return node;
	}

	public int getMaximum() {
		return max;
	}

	public int getDamage() {
		return damage;
	}

	public int getRange() {
		return range;
	}

	public int getCost() {
		return cost;
	}

	public int getAmmo() {
		return ammo;
	}

	public OfflinePlayer getSkullOwner() {
		return DeprecationUtils.getSkullOwner(skin);
	}

	public OfflinePlayer getReloadingSkullOwner() {
		return DeprecationUtils.getSkullOwner(reload);
	}

	public enum TargetType {
		ALLIANCE,
		MONSTERS,
		KINGDOM,
		ENEMIES;
	}

	public ItemStack build(OfflineKingdom kingdom, boolean shop) {
		boolean useDecals = configuration.getBoolean("turrets.decals", false);
		boolean spacing = configuration.getBoolean("turrets.spacing", false);
		ItemStack itemstack = new ItemStack(material);
		ItemMeta itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(Formatting.color(title));
		List<String> lores = new ArrayList<>();
		description.forEach(message -> lores.add(Formatting.color(message)));
		if (spacing)
			lores.add(" ");
		if (useDecals)
			lores.add(Formatting.color(decal));
		lores.add(new MessageBuilder(false, "turrets.formats.range")
				.fromConfiguration(configuration)
				.replace("%range%", range)
				.get());
		lores.add(new MessageBuilder(false, "turrets.formats.damage")
				.fromConfiguration(configuration)
				.replace("%range%", damage)
				.get());
		lores.add(new MessageBuilder(false, "turrets.formats.rate")
				.fromConfiguration(configuration)
				.replace("%rate%", cooldown)
				.get());
		if (shop) {
			if (configuration.getBoolean("turrets.spacing-costs", false))
				lores.add(" ");
			lores.add(new MessageBuilder(false, "turrets.formats.cost")
					.fromConfiguration(configuration)
					.replace("%cost%", cost)
					.get());
			lores.add(new MessageBuilder(false, "turrets.formats.kingdom-rp")
					.fromConfiguration(configuration)
					.replace("%amount%", kingdom.getResourcePoints())
					.get());
		}
		if (spacing)
			lores.add(" ");
		if (useDecals)
			lores.add(Formatting.color(decal));
		itemmeta.setLore(lores);
		itemstack.setItemMeta(DeprecationUtils.setupItemMeta(itemmeta, meta));
		return itemstack;
	}

}
