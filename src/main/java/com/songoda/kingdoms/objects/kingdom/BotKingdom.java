package com.songoda.kingdoms.objects.kingdom;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;

public class BotKingdom extends Kingdom {

	private final Plugin register;
	
	public BotKingdom(String name, Plugin plugin) {
		super();
		setKingdomName(name);
		this.register = plugin;
	}
	
	public Plugin getRegisteringPlugin() {
		return register;
	}
	
	@Override
	public Map<String, String> getInfo(){
		Map<String, String> info = new HashMap<String, String>();
		info.put("kingdom", getName());
		info.put("kingdomLore", getLore());
		info.put("might", getMight() + "");
		info.put("rp", getResourcePoints() + "");
		info.put("land", String.valueOf(getLand()));
		
		return info;
	}
	
	@Override
	public boolean isShieldUp() {
		return false;
	}
	
	@Override
	public boolean isOnline() {
		return true;
	}

}
