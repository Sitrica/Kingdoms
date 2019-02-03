package com.songoda.kingdoms.objects.kingdom;

import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.objects.KingdomPlayer;

public class KingdomChest{
	private List<ItemStack> inv;
	
	public List<ItemStack> getInv() {
		return inv;
	}

	public void setInv(List<ItemStack> inv) {
		this.inv = inv;
	}
	
	private KingdomPlayer using;
	public KingdomPlayer getUsing() {
		return using;
	}

	public void setUsing(KingdomPlayer using) {
		this.using = using;
	}
	
	public static String serializeChest(KingdomChest chest){
		YamlConfiguration conf = new YamlConfiguration();
		conf.set("inv", chest.inv);
		return conf.saveToString();
	}
	
	public static KingdomChest deserializeChest(String ser){
		YamlConfiguration conf = new YamlConfiguration();
		KingdomChest chest = new KingdomChest();
		try {
			conf.loadFromString(ser);
			chest.inv = (List<ItemStack>) conf.get("inv");
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return chest;
		} catch (Exception e){
			e.printStackTrace();
			return chest;
		}
		return chest;
	}
	
}
