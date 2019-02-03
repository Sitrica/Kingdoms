package com.songoda.kingdoms.objects.kingdom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.songoda.kingdoms.objects.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public abstract class BotKingdom extends Kingdom {

	private Plugin register;
	public BotKingdom(String kingdomName, Plugin plugin) {
		super();
		setKingdomName(kingdomName);
		register = plugin;
	}
	
	public Plugin getRegisteringPlugin(){
		return register;
	}
	
	@Override
	public void addMember(UUID uuid){
	}
	
	@Override
	public void removeMember(UUID uuid){
	}
	
	@Override
	public void sendAnnouncement(KingdomPlayer sender, String announce, boolean nameless) {
		
	}

	@Override
	public void sendWarAnnouncement(String announce){
	
	}

	@Override
	public boolean isMember(KingdomPlayer kp){
		return false;
	}
	
	@Override
	public Map<String, String> getInfo(){
		Map<String, String> info = new HashMap<String, String>();
		
		info.put("kingdom", String.valueOf(this.kingdomName));
		info.put("kingdomLore", String.valueOf(this.kingdomLore));
		//info.put("king", Bukkit.getOfflinePlayer(king) != null ? Bukkit.getOfflinePlayer(king).getName() : null);
		info.put("might", String.valueOf(this.might));
		//info.put("nexusAt", this.nexus_loc != null ? this.nexus_loc.toString() : null);
		//info.put("homeAt", this.home_loc != null ? this.home_loc.toString() : null);
		info.put("rp", String.valueOf(this.resourcepoints));
		String members = "";
		//for(KingdomPlayer member : onlineMembers) members +=" "+member.getPlayer().getName();
		//info.put("online", members);
		info.put("land", String.valueOf(getLand()));
		
		return info;
	}
	
	public abstract void displayInfo(CommandSender sender);
	
	@Override
	public boolean isShieldUp(){
		return false;
	}
	
	@Override
	public List<UUID> getMembersList() {
		ArrayList<UUID> contained = new ArrayList<UUID>();
		return membersList;
	}
	
	@Override
	public boolean isOnline(){
		return true;
	}
	
	@Override
	public String getKingName(){
		return null;
	}
	
	@Override
	public UUID getKing() {
		return null;
	}
	
	@Override
	public void onKingdomPlayerLogin(KingdomPlayer kp) {
		
	}

	@Override
	public void onKingdomPlayerLogout(KingdomPlayer kp) {
		
	}
	
	@Override
	public void onMemberJoinKingdom(OfflineKingdomPlayer kp) {
		
	}

	@Override
	public void onMemberQuitKingdom(OfflineKingdomPlayer kp) {
		
	}
	
	@Override
	public void onKingdomDelete(Kingdom k) {
		String kname = k.getKingdomName();
		
		if(alliesList.contains(kname)) alliesList.remove(kname);
		if(enemiesList.contains(kname)) enemiesList.remove(kname);
		
		if(onlineAllies.contains(k)) onlineAllies.remove(k);
		if(onlineEnemies.contains(k)) onlineEnemies.remove(k);
	}

}
