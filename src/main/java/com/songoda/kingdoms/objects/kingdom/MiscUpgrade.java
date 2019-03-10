package com.songoda.kingdoms.objects.kingdom;

import org.bukkit.Material;

public enum MiscUpgrade {

	//TODO finish recoding this, last left off here.
	
	ANTICREEPER(Kingdoms.getLang().getString("Guis_Misc_AntiCreeper_Desc"),
			Kingdoms.getLang().getString("Guis_Misc_AntiCreeper_Title"),
			Materials.GUNPOWDER.parseMaterial()),
	ANTITRAMPLE(Kingdoms.getLang().getString("Guis_Misc_AntiTrample_Desc"),
			Kingdoms.getLang().getString("Guis_Misc_AntiTrample_Title"),
			Materials.WHEAT_SEEDS.parseMaterial()),
	NEXUSGUARD(Kingdoms.getLang().getString("Guis_Misc_NexusGuard_Desc"),
			Kingdoms.getLang().getString("Guis_Misc_NexusGuard_Title"),
			Material.IRON_AXE),
	GLORY(Kingdoms.getLang().getString("Guis_Misc_Glory_Desc"),
			Kingdoms.getLang().getString("Guis_Misc_Glory_Title"),
			Material.NETHER_STAR),
	BOMBSHARDS(Kingdoms.getLang().getString("Guis_Misc_BombShards_Desc"),
			Kingdoms.getLang().getString("Guis_Misc_BombShards_Title"),
			Material.TNT),
	PSIONICCORE(Kingdoms.getLang().getString("Guis_Misc_PsionicCore_Desc"),
			Kingdoms.getLang().getString("Guis_Misc_PsionicCore_Title"),
			Materials.ENDER_EYE.parseMaterial());

	private final String desc, title;
	private final Material display;

	MiscUpgrade(String desc, String title, Material display){
		this.desc = desc;
		this.title = title;
		this.display = display;
	}
	
	public int getCost(){
		switch(this){
			case ANTICREEPER:
				return Config.getConfig().getInt("cost.misc-upgrades.anticreeper");
			case ANTITRAMPLE:
				return Config.getConfig().getInt("cost.misc-upgrades.antitrample");
			case BOMBSHARDS:
				return Config.getConfig().getInt("cost.misc-upgrades.bombshards");
			case GLORY:
				return Config.getConfig().getInt("cost.misc-upgrades.glory");
			case NEXUSGUARD:
				return Config.getConfig().getInt("cost.misc-upgrades.nexusguard");
			case PSIONICCORE:
				return Config.getConfig().getInt("cost.misc-upgrades.psioniccore");
		 }
		return 0;

	}

	public boolean isConfigEnabled(){
		switch(this){
			case ANTICREEPER:
				return Config.getConfig().getBoolean("enable.misc.anticreeper.enabled");
			case ANTITRAMPLE:
				return Config.getConfig().getBoolean("enable.misc.antitrample");
			case BOMBSHARDS:
				return Config.getConfig().getBoolean("enable.misc.bombshards.enabled");
			case GLORY:
				return Config.getConfig().getBoolean("enable.misc.glory");
			case NEXUSGUARD:
				return Config.getConfig().getBoolean("enable.misc.nexusguard");
			case PSIONICCORE:
				return Config.getConfig().getBoolean("enable.misc.psioniccore");
		 }
		return false;

	}


	public boolean isDefaultOn(){
		switch(this){
			case ANTICREEPER:
				return Config.getConfig().getBoolean("defaulton.misc-upgrades.anticreeper");
			case ANTITRAMPLE:
				return Config.getConfig().getBoolean("defaulton.misc-upgrades.antitrample");
			case BOMBSHARDS:
				return Config.getConfig().getBoolean("defaulton.misc-upgrades.bombshards");
			case GLORY:
				return Config.getConfig().getBoolean("defaulton.misc-upgrades.glory");
			case NEXUSGUARD:
				return Config.getConfig().getBoolean("defaulton.misc-upgrades.nexusguard");
			case PSIONICCORE:
				return Config.getConfig().getBoolean("defaulton.misc-upgrades.psioniccore");
		 }
		return false;
		
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
}
