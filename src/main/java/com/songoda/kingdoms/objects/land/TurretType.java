package com.songoda.kingdoms.objects.land;

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
	
	/*FLAME(Kingdoms.getLang().getString("Guis_Turret_Flame"),
		Kingdoms.getLang().getString("Guis_Turret_Flame_Desc"),
		ChatColor.RED + "=-=-=-=-=",
		"MHF_WSkeleton",
		2,
		TurretTargetType.MONSTERS, TurretTargetType.ENEMY_PLAYERS),
	HEALING(Kingdoms.getLang().getString("Guis_Turret_Healing"),
		Kingdoms.getLang().getString("Guis_Turret_Healing_Desc"),
		ChatColor.GREEN + "=-=-=-=-=",
		"MHF_Zombie",
		1,
		TurretTargetType.ALLY_PLAYERS),
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

	private final String node, title, decal, skin, meta, reload;
	private final List<String> description = new ArrayList<>();
	private final Set<TargetType> targets = new HashSet<>();
	private SoundPlayer placing, reloading, shooting;
	private final int cost, damage, range, max, ammo;
	private final FileConfiguration configuration;
	private final boolean enabled, natural;
	private final long cooldown, firerate;
	private final EntityType projectile;
	private final Material material;
	
	public TurretType(String node) {
		configuration = Kingdoms.getInstance().getConfiguration("turrets").get();
		ConfigurationSection section = configuration.getConfigurationSection("turrets.turrets" + node);
		ConfigurationSection item = section.getConfigurationSection("item");
		if (section.getBoolean("use-place-sound", false))
			this.placing = new SoundPlayer(section.getConfigurationSection("place-sounds"));
		if (section.getBoolean("use-shoot-sound", false))
			this.shooting = new SoundPlayer(section.getConfigurationSection("shoot-sound"));
		if (section.getBoolean("use-reload-sound", false))
			this.reloading = new SoundPlayer(section.getConfigurationSection("reload-sounds"));
		this.material = Utils.materialAttempt(item.getString("material", "MUSIC_DISC_STAL"), "GOLD_RECORD");
		this.projectile = Utils.entityAttempt(section.getString("projectile", "ARROW"), "ARROW");
		this.firerate = IntervalUtils.getInterval(section.getString("fire-rate", "1 second"));
		this.cooldown = IntervalUtils.getInterval(section.getString("cooldown", "5 seconds"));
		this.reload = section.getString("reloading-skull-skin", "Redstone");
		this.description.addAll(item.getStringList("description"));
		this.natural = section.getBoolean("natural-damage", false);
		this.skin = section.getString("skull-skin", "MHF_Zombie");
		this.decal = section.getString("decal", "---------");
		this.enabled = section.getBoolean("enabled", true);
		this.max = section.getInt("max-per-land", 50);
		this.damage = section.getInt("damage", 4);
		this.range = section.getInt("range", 8);
		this.cost = section.getInt("cost", 300);
		this.ammo = section.getInt("ammo", 1);
		this.title = item.getString("title");
		this.meta = item.getString("meta");
		this.node = node;
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
	
	public List<String> getDescription() {
		return description;
	}
	
	public Set<TargetType> getTargets() {
		return targets;
	}
	
	public EntityType getProjectile() {
		return projectile;
	}
	
	public long getReloadCooldown() {
		return cooldown;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public boolean isNatural() {
		return natural;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public long getFirerate() {
		return firerate;
	}

	public String getTitle() {
		return title;
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
		ENEMIES;
	}
	
	public ItemStack build(OfflineKingdom kingdom, boolean shop) {
		boolean useDecals = configuration.getBoolean("turrets.decals", false);
		boolean spacing = configuration.getBoolean("turrets.spacing", false);
		ItemStack itemstack = new ItemStack(material);
		ItemMeta itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(Formatting.color(title));
		List<String> lores = new ArrayList<>();
		lores.addAll(description);
		if (spacing)
			lores.add(" ");
		if (useDecals)
			lores.add(Formatting.colorAndStrip(decal));
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
			lores.add(Formatting.colorAndStrip(decal));
		itemmeta.setLore(lores);
		itemstack.setItemMeta(itemmeta);
		return DeprecationUtils.setupItemMeta(itemstack, meta);
	}

}