package com.songoda.kingdoms.objects.kingdom;

import com.songoda.kingdoms.main.Config;
import com.songoda.kingdoms.main.Kingdoms;
import com.songoda.kingdoms.utils.Materials;
import org.bukkit.Material;

public class PowerUp{
	int dmgreduction = 0;
	int regenboost = 0;
	int dmgboost = 0;
	int doublelootchance = 0;
	int arrowboost = 0;
	
	public int getLevel(PowerUpType type){
		switch(type){
		case ARROW_BOOST:
			return getArrowboost();
		case DAMAGE_BOOST:
			return getDmgboost();
		case DAMAGE_REDUCTION:
			return getDmgreduction();
		case REGENERATION_BOOST:
			return getRegenboost();
		
		}
		return 0;
	}
	
	public void setLevel(PowerUpType type, int level){
		Kingdoms.logDebug("setLevel: " + type);
		switch(type){
		case ARROW_BOOST:
			setArrowboost(level);
			return;
		case DAMAGE_BOOST:
			setDmgboost(level);
			return;
		case DAMAGE_REDUCTION:
			setDmgreduction(level);
			return;
		case REGENERATION_BOOST:
			setRegenboost(level);
			return;
		
		}
	}

	public int getDmgreduction() {
		return dmgreduction;
	}

	public void setDmgreduction(int dmgreduction) {
		Kingdoms.logDebug("setDmgreduct");
		this.dmgreduction = dmgreduction;
	}

	public int getRegenboost() {
		return regenboost;
	}

	public void setRegenboost(int regenboost) {
		Kingdoms.logDebug("setRegen");
		this.regenboost = regenboost;
	}

	public int getDmgboost() {
		return dmgboost;
	}

	public void setDmgboost(int dmgboost) {
		Kingdoms.logDebug("setDmgBoost");
		this.dmgboost = dmgboost;
	}

	public int getDoublelootchance() {
		return doublelootchance;
	}

	public void setDoublelootchance(int doublelootchance) {
		this.doublelootchance = doublelootchance;
	}

	public int getArrowboost() {
		return arrowboost;
	}

	public void setArrowboost(int arrowboost) {
		Kingdoms.logDebug("setArrow");
		this.arrowboost = arrowboost;
	}
	
	public void resetAll(){
		this.arrowboost = 0;
		this.dmgboost = 0;
		this.dmgreduction = 0;
		this.doublelootchance = 0;
		this.regenboost = 0;
	}
	
	public enum PowerUpType{
		DAMAGE_REDUCTION(Kingdoms.getLang().getString("Guis_DamageReduction_Title"),
				Kingdoms.getLang().getString("Guis_DamageReduction_Description"),
				Kingdoms.getLang().getString("Guis_DamageReduction_CurrLevel")),
		REGENERATION_BOOST(Kingdoms.getLang().getString("Guis_Regeneration_Title"),
				Kingdoms.getLang().getString("Guis_Regeneration_Description"),
				Kingdoms.getLang().getString("Guis_Regeneration_CurrLevel")),
		ARROW_BOOST(Kingdoms.getLang().getString("Guis_ArrowDamage_Title"),
				Kingdoms.getLang().getString("Guis_ArrowDamage_Description"),
				Kingdoms.getLang().getString("Guis_ArrowDamage_CurrLevel")),
		DAMAGE_BOOST(Kingdoms.getLang().getString("Guis_DamageBoost_Title"),
				Kingdoms.getLang().getString("Guis_DamageBoost_Description"),
				Kingdoms.getLang().getString("Guis_DamageBoost_CurrLevel"));
		
		String title;
		String desc;
		String currlevel;
		
		PowerUpType(String title, String desc, String currlevel){
			this.title = title;
			this.desc = desc;
			this.currlevel = currlevel;
		}
		
		public int getCost(){
			switch(this){
			case ARROW_BOOST:
				return Config.getConfig().getInt("cost.nexusupgrades.arrow-boost");
			case DAMAGE_BOOST:
				return Config.getConfig().getInt("cost.nexusupgrades.dmg-boost");
			case DAMAGE_REDUCTION:
				return Config.getConfig().getInt("cost.nexusupgrades.dmg-reduc");
			case REGENERATION_BOOST:
				return Config.getConfig().getInt("cost.nexusupgrades.regen-boost");
			}
			return 0;
		}

		public int getMax(){
			switch(this){
			case ARROW_BOOST:
				return Config.getConfig().getInt("max.nexusupgrades.arrow-boost");
			case DAMAGE_BOOST:
				return Config.getConfig().getInt("max.nexusupgrades.dmg-boost");
			case DAMAGE_REDUCTION:
				return Config.getConfig().getInt("max.nexusupgrades.dmg-reduc");
			case REGENERATION_BOOST:
				return Config.getConfig().getInt("max.nexusupgrades.regen-boost");
			}
			return 0;
		}


		public boolean isEnabled(){
			switch(this){
			case ARROW_BOOST:
				return Config.getConfig().getBoolean("enable.nexus.arrowboost");
			case DAMAGE_BOOST:
				return Config.getConfig().getBoolean("enable.nexus.dmgboost");
			case DAMAGE_REDUCTION:
				return Config.getConfig().getBoolean("enable.nexus.dmgreduc");
			case REGENERATION_BOOST:
				return Config.getConfig().getBoolean("enable.nexus.regenboost");
			}
			return false;
		}

		public Material getMat(){
			switch(this){
			case ARROW_BOOST:
				return Material.ARROW;
			case DAMAGE_BOOST:
				return Material.IRON_SWORD;
			case DAMAGE_REDUCTION:
				return Material.IRON_CHESTPLATE;
			case REGENERATION_BOOST:
				return Materials.POPPY.parseMaterial();
			}
			return Material.STONE;
		}

		public String getTitle() {
			return title;
		}

		public String getDesc() {
			return desc;
		}

		public String getCurrlevel() {
			return currlevel;
		}
		
	}
	
}
