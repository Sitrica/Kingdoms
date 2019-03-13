package com.songoda.kingdoms.objects.kingdom;

import org.bukkit.plugin.Plugin;

public class BotKingdom extends Kingdom {

	private final Plugin register;
	
	public BotKingdom(String name, Plugin plugin) {
		super();
		setName(name);
		this.register = plugin;
	}
	
	public Plugin getRegisteringPlugin() {
		return register;
	}
	
	/*public Map<String, String> getInfo(){
		Map<String, String> info = new HashMap<String, String>();
		info.put("kingdom", getName());
		info.put("kingdomLore", getLore());
		info.put("might", getMight() + "");
		info.put("rp", getResourcePoints() + "");
		info.put("land", String.valueOf(getLand()));
		return info;
	}*/
	
	public boolean isShieldUp() {
		return false;
	}
	
	@Override
	public boolean isOnline() {
		return true;
	}

}
