package com.songoda.kingdoms.objects.kingdom;

import org.bukkit.Material;

public enum ChampionUpgrade {
	
	WEAPON(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Weapon_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Weapon_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Weapon_Title"),
			Material.IRON_SWORD, 1),
	HEALTH(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Health_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Health_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Health_Title"),
			Material.IRON_CHESTPLATE,
            Integer.parseInt(Config.getConfig().getString("magnitude.champion.healthI"))),
	HEALTHII(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Health_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_HealthII_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_HealthII_Title"),
			Material.IRON_CHESTPLATE,
            Integer.parseInt(Config.getConfig().getString("magnitude.champion.healthII"))),
	RESISTANCE(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Resistance_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Resistance_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Resistance_Title"),
			Material.BRICK,20),
	SPEED(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Speed_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Speed_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Speed_Title"),
			Material.QUARTZ,1),
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
	STRENGTH(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Strength_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Strength_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Strength_Title"),
			Materials.GOLDEN_AXE.parseMaterial(),10),
	ARMOR(Kingdoms.getLang().getString("Guis_ChampionUpgrades_Armor_Curr"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Armor_Desc"),
			Kingdoms.getLang().getString("Guis_ChampionUpgrades_Armor_Title"),
			Material.DIAMOND_CHESTPLATE,1),
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
	
	private String curr;
	private String desc;
	private String title;
	private Material display;
	private boolean isToggle = false;
	private int levels = 1;
	ChampionUpgrade(String curr, String desc, String title, Material display, int levels){
		this.curr = curr;
		this.desc = desc;
		this.title = title;
		this.display = display;
		this.levels = levels;
	}
	ChampionUpgrade(String curr, String desc, String title, Material display, int levels, boolean isToggle){
		this.curr = curr;
		this.desc = desc;
		this.title = title;
		this.isToggle = isToggle;
		this.display = display;
		this.levels = levels;
	}
	
	public int getLevels(){
		return levels;
	}
	
	public String getCurr() {
		return curr;
	}
	public String getDesc() {
		return desc;
	}
	public String getTitle() {
		return title;
	}
	public Material getDisplay() {
		return display;
	}
	public boolean isToggle() {
		return isToggle;
	}
	public static boolean getUpgradeEnabled(ChampionUpgrade upgrade){
		if(upgrade == null) return false;
		
		switch(upgrade){
			case WEAPON:
				return Config.getConfig().getBoolean("enable.champion.weapon");
			case AQUA:
				return Config.getConfig().getBoolean("enable.champion.aqua");
			case ARMOR:
				return Config.getConfig().getBoolean("enable.champion.armor");
			case DAMAGE_CAP:
				return Config.getConfig().getBoolean("enable.champion.damagecap");
			case DEATH_DUEL:
				return Config.getConfig().getBoolean("enable.champion.duel");
			case DRAG:
				return Config.getConfig().getBoolean("enable.champion.drag");
			case FOCUS:
				return Config.getConfig().getBoolean("enable.champion.focus");
			case HEALTH:
				return true;
			case HEALTHII:
				return true;
			case MIMIC:
				return Config.getConfig().getBoolean("enable.champion.mimic");
			case MOCK:
				return Config.getConfig().getBoolean("enable.champion.mock");
			case PLOW:
				return Config.getConfig().getBoolean("enable.champion.plow");
			case REINFORCEMENTS:
				return Config.getConfig().getBoolean("enable.champion.reinforcements");
			case RESISTANCE:
				return Config.getConfig().getBoolean("enable.champion.resist");
			case SPEED:
				return Config.getConfig().getBoolean("enable.champion.speed");
			case STRENGTH:
				return Config.getConfig().getBoolean("enable.champion.strength");
			case THOR:
				return Config.getConfig().getBoolean("enable.champion.thor");
			case DETERMINATION:
				return Config.getConfig().getBoolean("enable.champion.determination");
			case DETERMINATIONII:
				return Config.getConfig().getBoolean("enable.champion.determination");
		}
		return false;
	}
	
	
	public static int getUpgradeMax(ChampionUpgrade upgrade){
		if(upgrade == null) return 0;
		
		switch(upgrade){
			case WEAPON:
				return Config.getConfig().getInt("max.champion.weapon");
			case AQUA:
				return 1;
			case ARMOR:
				return Config.getConfig().getInt("max.champion.armor");
			case DAMAGE_CAP:
				return Config.getConfig().getInt("max.champion.damagecap");
			case DEATH_DUEL:
				return Config.getConfig().getInt("max.champion.duel");
			case DRAG:
				return Config.getConfig().getInt("max.champion.drag");
			case FOCUS:
				return Config.getConfig().getInt("max.champion.focus");
			case HEALTH:
				return Config.getConfig().getInt("max.champion.health");
			case HEALTHII:
				return Config.getConfig().getInt("max.champion.health");
			case MIMIC:
				return Config.getConfig().getInt("max.champion.mimic");
			case MOCK:
				return Config.getConfig().getInt("max.champion.mock");
			case PLOW:
				return Config.getConfig().getInt("max.champion.plow");
			case REINFORCEMENTS:
				return Config.getConfig().getInt("max.champion.reinforcements");
			case RESISTANCE:
				return Config.getConfig().getInt("max.champion.resist");
			case SPEED:
				return Config.getConfig().getInt("max.champion.speed");
			case STRENGTH:
				return Config.getConfig().getInt("max.champion.strength");
			case THOR:
				return Config.getConfig().getInt("max.champion.thor");
			case DETERMINATION:
				return Config.getConfig().getInt("max.champion.determination");
			case DETERMINATIONII:
				return Config.getConfig().getInt("max.champion.determination");

		}
		return 0;
	}
	
	public static int getUpgradeCost(ChampionUpgrade upgrade){
		if(upgrade == null) return 0;
		
		switch(upgrade){
			case WEAPON:
				return Config.getConfig().getInt("cost.champion.weapon");
			case AQUA:
				return Config.getConfig().getInt("cost.champion.aqua");
			case ARMOR:
				return Config.getConfig().getInt("cost.champion.armor");
			case DAMAGE_CAP:
				return Config.getConfig().getInt("cost.champion.damagecap");
			case DEATH_DUEL:
				return Config.getConfig().getInt("cost.champion.duel");
			case DRAG:
				return Config.getConfig().getInt("cost.champion.drag");
			case FOCUS:
				return Config.getConfig().getInt("cost.champion.focus");
			case HEALTH:
				return Config.getConfig().getInt("cost.champion.health")*Config.getConfig().getInt("magnitude.champion.healthI");
			case HEALTHII:
				return Config.getConfig().getInt("cost.champion.health")*Config.getConfig().getInt("magnitude.champion.healthII");
			case MIMIC:
				return Config.getConfig().getInt("cost.champion.mimic");
			case MOCK:
				return Config.getConfig().getInt("cost.champion.mock");
			case PLOW:
				return Config.getConfig().getInt("cost.champion.plow");
			case REINFORCEMENTS:
				return Config.getConfig().getInt("cost.champion.reinforcements");
			case RESISTANCE:
				return Config.getConfig().getInt("cost.champion.resist");
			case SPEED:
				return Config.getConfig().getInt("cost.champion.speed");
			case STRENGTH:
				return Config.getConfig().getInt("cost.champion.strength");
			case THOR:
				return Config.getConfig().getInt("cost.champion.thor");
			case DETERMINATION:
				return Config.getConfig().getInt("cost.champion.determination")*Config.getConfig().getInt("magnitude.champion.determinationI");
			case DETERMINATIONII:
				return Config.getConfig().getInt("cost.champion.determination")*Config.getConfig().getInt("magnitude.champion.determinationII");
		}
		return 0;
	}
	

	public static int getUpgradeDefault(ChampionUpgrade upgrade){
		if(upgrade == null) return 0;
		
		switch(upgrade){
			case WEAPON:
				return Config.getConfig().getInt("default.champion.weapon");
			case AQUA:
				return Config.getConfig().getInt("default.champion.aqua");
			case ARMOR:
				return Config.getConfig().getInt("default.champion.armor");
			case DAMAGE_CAP:
				return Config.getConfig().getInt("default.champion.damagecap");
			case DEATH_DUEL:
				return Config.getConfig().getInt("default.champion.duel");
			case DRAG:
				return Config.getConfig().getInt("default.champion.drag");
			case FOCUS:
				return Config.getConfig().getInt("default.champion.focus");
			case HEALTH:
				return Config.getConfig().getInt("default.champion.health");
			case HEALTHII:
				return Config.getConfig().getInt("default.champion.health");
			case MIMIC:
				return Config.getConfig().getInt("default.champion.mimic");
			case MOCK:
				return Config.getConfig().getInt("default.champion.mock");
			case PLOW:
				return Config.getConfig().getInt("default.champion.plow");
			case REINFORCEMENTS:
				return Config.getConfig().getInt("default.champion.reinforcements");
			case RESISTANCE:
				return Config.getConfig().getInt("default.champion.resist");
			case SPEED:
				return Config.getConfig().getInt("default.champion.speed");
			case STRENGTH:
				return Config.getConfig().getInt("default.champion.strength");
			case THOR:
				return Config.getConfig().getInt("default.champion.thor");
			case DETERMINATION:
				return Config.getConfig().getInt("default.champion.determination");
			case DETERMINATIONII:
				return Config.getConfig().getInt("default.champion.determination");
		}
		return 0;
	}

}
