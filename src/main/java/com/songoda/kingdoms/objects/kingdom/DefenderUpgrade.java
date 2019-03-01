package com.songoda.kingdoms.objects.kingdom;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.Utils;

public enum DefenderUpgrade {
	
	MEGA_HEALTH("mega-health"),
	RESISTANCE("resistance"),
	STRENGTH("strength"),
	WEAPON("weapon"),
	HEALTH("health"),
	THROW("throw"),
	SPEED("speed"),
	ARMOR("armor");
	
	/*
	DRAG(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Drag_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Drag_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Drag_Title"),
			Material.FISHING_ROD,1,true),
	MOCK(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Mock_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Mock_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Mock_Title"),
			Material.STONE,1),
	DEATH_DUEL(Kingdoms.getLang().getString("Guis_ChampionUpgrades_DeathDuel_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_DeathDuel_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_DeathDuel_Title"),
			Materials.GOLDEN_SWORD.parseMaterial(),
			1,
			true),
	THOR(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Thor_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Thor_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Thor_Title"),
			Material.DIAMOND_AXE,1),
	DAMAGE_CAP(Kingdoms.getLang().getString("Guis_ChampionUpgrades_DamageCap_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_DamageCap_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_DamageCap_Title"),
			Material.DIAMOND_CHESTPLATE,1,
			true),
	PLOW(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Plow_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Plow_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Plow_Title"),
			Material.BUCKET,1,
			true),
	REINFORCEMENTS(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Reinforcements_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Reinforcements_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Reinforcements_Title"),
			Materials.ZOMBIE_HEAD.parseMaterial(),1),
	MIMIC(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Mimic_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Mimic_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Mimic_Title"),
			Materials.GLASS_PANE.parseMaterial(),1,
			true),
	FOCUS(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Focus_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Focus_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Focus_Title"),
			Materials.ENDER_EYE.parseMaterial(),1,true),
	AQUA(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Aqua_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Aqua_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Aqua_Title"),
			Material.IRON_BOOTS,1,
			true), 
	DETERMINATION(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Determination_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Determination_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Determination_Title"),
			Materials.GOLDEN_CHESTPLATE.parseMaterial(),
            Config.getConfig().getInt("magnitude.champion.determinationI")),
	DETERMINATIONII(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Determination_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_DeterminationII_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_DeterminationII_Title"),
			Materials.GOLDEN_CHESTPLATE.parseMaterial(),
            Config.getConfig().getInt("magnitude.champion.determinationII"));
    */

	private final List<String> description = new ArrayList<>();
	private final int max, value, cost, multiplier;
	private final boolean glowing, enabled;
	private final String node, title, meta;
	private final Material material;
	
	private DefenderUpgrade(String node) {
		FileConfiguration configuration = Kingdoms.getInstance().getConfiguration("defender-upgrades").get();
		ConfigurationSection section = configuration.getConfigurationSection("upgrades." + node);
		this.material = Utils.materialAttempt(section.getString("material", "WOODEN_SWORD"), "WOOD_SWORD");
		this.description.addAll(section.getStringList("description"));
		this.multiplier = section.getInt("cost-multiplier", 10);
		this.glowing = section.getBoolean("glowing", false);
		this.enabled = section.getBoolean("enabled", false);
		this.title = section.getString("title", "Not set");
		this.meta = section.getString("material-meta", "");
		this.max = section.getInt("max-level", 0);
		this.value = section.getInt("value", 1);
		this.cost = section.getInt("cost", 10);
		this.node = node;
	}
	
	public List<String> getDescription() {
		return description;
	}
	
	public int getCostAt(int level) {
		return cost + (level * multiplier);
	}
	
	public int getCostMultiplier() {
		return multiplier;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public boolean isGlowing() {
		return glowing;
	}
	
	public int getMaxLevel() {
		return max;
	}

	public String getTitle() {
		return title;
	}

	public String getNode() {
		return node;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getCost() {
		return cost;
	}
	
	public void execute() {
		
	}
	
	public ItemStack build(OfflineKingdom kingdom, boolean shop) {
		ItemStack itemstack = new ItemStack(material);
		ItemMeta itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(Formatting.color(title));
		List<String> lores = new ArrayList<>();
		description.forEach(message -> lores.add(Formatting.color(message)));
		itemmeta.setLore(lores);
		itemstack.setItemMeta(itemmeta);
		return DeprecationUtils.setupItemMeta(itemstack, meta);
	}

}
