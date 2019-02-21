package com.songoda.kingdoms.objects.kingdom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.manager.managers.WorldManager;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Kingdom extends OfflineKingdom implements KingdomEventHandler {
	
	private final PlayerManager playerManager;
	private final WorldManager worldManager;
	private int max;
	
	int chestsize = 9;
	long timestamp = 0;
	int maxMember = 10;
	ArmyInfo armyInfo = new ArmyInfo();
	AggressorInfo aggressorInfo = new AggressorInfo();
	MisupgradeInfo misupgradeInfo = new MisupgradeInfo();
	KingdomChest kingdomChest = new KingdomChest();
	PowerUp powerUp = new PowerUp();
	TurretUpgradeInfo turretUpgrades = new TurretUpgradeInfo();
	
	// Only used for BotKingdoms.
	protected Kingdom() {
		this(UUID.randomUUID(), null);
	}
	
	public Kingdom(OfflineKingdom kingdom) {
		super(kingdom.getUniqueId(), kingdom.getKing(), true);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.max = instance.getConfig().getInt("base-max-members", 10);
	}
	
	public Kingdom(KingdomPlayer king) {
		this(UUID.randomUUID(), king);
	}
	
	public Kingdom(UUID uuid, KingdomPlayer king) {
		super(uuid, king);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.max = instance.getConfig().getInt("base-max-members", 10);
	}
	
	public Set<KingdomPlayer> getOnlinePlayers() {
		return members.parallelStream()
				.filter(player -> player.isOnline())
				.map(player -> player.getKingdomPlayer())
				.collect(Collectors.toSet());
	}
	
	public Set<KingdomPlayer> getOnlineAllies() {
		Set<KingdomPlayer> allies = new HashSet<>();
		Bukkit.getWorlds().parallelStream()
				.filter(world -> worldManager.acceptsWorld(world))
				.forEach(world -> {
					for (Player player : world.getPlayers()) {
						KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
						Kingdom playerKingdom = kingdomPlayer.getKingdom();
						if (playerKingdom == null)
							continue;
						if (this.isAllianceWith(playerKingdom) && playerKingdom.isAllianceWith(this))
							allies.add(kingdomPlayer);
					}
				});
		return allies;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	public int getChestSize() {
		return chestsize;
	}

	public void setChestSize(int chestsize) {
		this.chestsize = chestsize;
		
	}

	public ChampionInfo getChampionInfo() {
		if(championInfo == null)
			championInfo = new ChampionInfo();
		return championInfo;
	}

	public ArmyInfo getArmyInfo() {
		if(armyInfo == null)
			armyInfo = new ArmyInfo();
		return armyInfo;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
		
	}

	public AggressorInfo getAggressorInfo() {
		if(aggressorInfo == null)
			aggressorInfo = new AggressorInfo();
		return aggressorInfo;
	}

	public TurretUpgradeInfo getTurretUpgrades() {
		return turretUpgrades;
	}

	public int getMaxMember() {
		return maxMember;
	}

	public void setMaxMember(int maxMember) {
		this.maxMember = maxMember;
		
	}

	public MisupgradeInfo getMisupgradeInfo() {
		if(misupgradeInfo == null)
			misupgradeInfo = new MisupgradeInfo();
		return misupgradeInfo;
	}

	public KingdomChest getKingdomChest() {
		if(kingdomChest == null)
			kingdomChest = new KingdomChest();
		return kingdomChest;
	}

	public void setKingdomChest(KingdomChest kingdomChest) {
		this.kingdomChest = kingdomChest;
		
	}

	public PowerUp getPowerUp() {
		if(powerUp == null)
			powerUp = new PowerUp();
		return powerUp;
	}

	public void setPowerUp(PowerUp powerUp) {
		this.powerUp = powerUp;
		
	}

	public void setResourcepoints(int resourcepoints) {
		Kingdom k = this;
		if(resourcepoints != this.resourcepoints){
			Bukkit.getScheduler().runTask(Kingdoms.getInstance(), new Runnable(){
				public void run(){
					Bukkit.getPluginManager().callEvent(new KingdomResourcePointChangeEvent(k));
					
				}
			});
		}
		this.resourcepoints = resourcepoints;
		if(this.resourcepoints < 0){
			this.resourcepoints = 0;
		}
		
	}
	public void setMight(int might) {
		this.might = might;
		
	}
	
	public int getShieldMax() {
		int max = 1000;
		int radius = this.getShieldRadius();
		ChunkLocation sc = new ChunkLocation(nexus_loc.getChunk());
		if(radius> 0){
			for(int x = -radius;x<= radius; x++){
				for(int z = -radius;z<=radius;z++){
					ChunkLocation chunk = new ChunkLocation(sc.getWorld(),sc.getX()+x,sc.getZ()+z);
					Land land = Kingdoms.getManagers().getLandManager().getOrLoadLand(chunk);
					if(!land.getOwnerUUID().equals(kingdomUuid)) continue;
					if(land.getStructure() == null) continue;
					if(land.getStructure().getType() != StructureType.SHIELDBATTERY) continue;
					max+=Config.getConfig().getInt("siege.shield.batteryshield");
				}
			}
		}
		
		return max;
	}
	
	public int getShieldRecharge(){
		int recharge = Config.getConfig().getInt("siege.shield.base-recharge");
		int radius = this.getShieldRadius();
		ChunkLocation sc = new ChunkLocation(nexus_loc.getChunk());
		if(radius> 0){
			for(int x = -radius;x<= radius; x++){
				for(int z = -radius;z<=radius;z++){
					ChunkLocation chunk = new ChunkLocation(sc.getWorld(),sc.getX()+x,sc.getZ()+z);
					Land land = Kingdoms.getManagers().getLandManager().getOrLoadLand(chunk);
					if(!land.getOwnerUUID().equals(getKingdomUuid())) continue;
					if(land.getStructure() == null) continue;
					if(land.getStructure().getType() != StructureType.SHIELDBATTERY) continue;
					recharge+=Config.getConfig().getInt("siege.shield.batteryrecharge");
				}
			}
		}
		
		return recharge;
	}
	
	public static String RECHARGE_COOLDOWN = "RECHARGE_COOLDOWN";
	public int getShieldRechargeCost(){
		double cost = Config.getConfig().getInt("siege.shield.recharge-cost-base");
		if(getTimeLeft(RECHARGE_COOLDOWN) > 0){
			double timeLeft = TimeUnit.MILLISECONDS.toMinutes(getTimeLeft(RECHARGE_COOLDOWN));
			cost += Math.pow(cost, 2)*(timeLeft/Config.getConfig().getInt("siege.shield.recharge-cost-reset-time-in-min"));
		}
		return (int) cost;
	}
	
	public void beginShieldRechargeTimer(){
		beginCooldown(RECHARGE_COOLDOWN,Config.getConfig().getInt("siege.shield.recharge-cost-reset-time-in-min"));
	}
	
	public boolean isRechargingShield(){
		return getTimeLeft(RECHARGE_COOLDOWN) > 0;
	}
	
//	public static final String CAMO = "CAMO";
//
//    public void giveCamo(int camoTimeInMin){
//    	beginCooldown(CAMO, camoTimeInMin);
//    }
//
//	public boolean isCamoUp(){
//		return getTimeLeft(CAMO) > 0;
//	}
//	
//	public void removeCamo(){
//		cancelCooldown(CAMO);
//	}
//	

	public int getLand() {
		int land = 0;
		LandManager landMan = Kingdoms.getManagers().getLandManager();
		if(landMan == null) return 0;
		for (ChunkLocation loc : landMan.getAllLandLoc()) {
			if(Config.getConfig().getStringList("infinite-claim-worlds").contains(loc.getWorld())) continue;
			Land l = landMan.getOrLoadLand(loc);
			if(l.getOwnerUUID() == null) continue;
			if(l.getOwnerUUID().equals(this.getKingdomUuid())) land++;
		}
		return land;
	}

	public void setDynmapColor(int dynmapColor) {
		this.dynmapColor = dynmapColor;
		
	}
	public void setKingdomName(String kingdomName) {
		this.kingdomName = kingdomName;
		
	}
	public void setKingdomLore(String kingdomLore) {
		this.kingdomLore = kingdomLore;
		
	}
	
	public void addMember(UUID uuid){
		if(members.contains(uuid)) return;
		members.add(uuid);
	}
	
	public void removeMember(UUID uuid){
		members.remove(uuid);
	}
	
	//public void setmembers(List<UUID> members) {
	//	this.members = members;
	//	
	//}
	
	public void enemyKingdom(Kingdom target){
		if(this.getAlliesList().contains(target.getKingdomUuid())){
			this.getAlliesList().remove(target.getKingdomUuid());
			if(GameManagement.getKingdomManager().isOnline(target.getKingdomName())){
				this.getOnlineAllies().remove(target);
			}
		}
		
		this.getEnemiesList().add(target.getKingdomUuid());
		this.sendAnnouncement(null, Kingdoms.getLang().getString("Command_Enemy_Success").replaceAll("%kingdom%", target.getKingdomName()), false);
		if(GameManagement.getKingdomManager().isOnline(target.getKingdomName())){
			this.getOnlineEnemies().add(target);
		}
	}
	
//	public void addEnemy(String kingdomName){
//		if(enemiesList.contains(kingdomName)) return;
//		enemiesList.add(kingdomName);
//		
//	}
//	public void removeEnemy(String kingdomName){
//		enemiesList.remove(kingdomName);
//		
//	}
//	public void setEnemiesList(List<String> enemiesList) {
//		this.enemiesList = enemiesList;
//		
//	}
	
	public boolean isWithinNexusShieldRange(ChunkLocation sc){
		if(nexus_loc == null) return false;
		ChunkLocation nexusChunk = new ChunkLocation(nexus_loc.getChunk());
		if(Math.abs(nexusChunk.getX() - sc.getX()) <= getShieldRadius()&&
				Math.abs(nexusChunk.getZ() - sc.getZ()) <= getShieldRadius()){
			return true;
		}
		
		return false;
	}
	//public void setAlliesList(List<String> alliesList) {
	//	this.alliesList = alliesList;
	//	
	//}
	
	public void sendAnnouncement(KingdomPlayer sender, String announce, boolean nameless) {
		for (KingdomPlayer kp : getOnlineMembers()) {
			if(sender == null){
				kp.getPlayer()
				.sendMessage(Kingdoms.getLang().getString("Misc_Announcer_Nameless_Format").replaceAll("%kingdom%", getKingdomName()).replaceAll("%message%", announce));
				continue;
			}
			
			if(sender != null && !nameless){
//				kp.getPlayer()
//				.sendMessage(ChatColor.AQUA + "[" + this.kingdomName + ChatColor.DARK_GRAY + " ("
//						+ "[" + Rank.getFancyMarkByRank(sender.getRank()) + "]" 
//						+ sender.getName()
//						+ ChatColor.DARK_GRAY + ")" + ChatColor.AQUA + "] " 
//						+ ChatColor.GRAY + announce);
				
				kp.getPlayer()
				.sendMessage(Kingdoms.getLang().getString("Misc_Announcer_Named_Format", kp.getLang()).replaceAll("%kingdom%", getKingdomName()).replaceAll("%rank%", Rank.getFancyMarkByRank(sender.getRank())).replaceAll("%player%", sender.getName()).replaceAll("%message%", announce));
			}
			if(nameless){
				kp.getPlayer()
				.sendMessage(Kingdoms.getLang().getString("Misc_Announcer_Nameless_Format", kp.getLang()).replaceAll("%kingdom%", getKingdomName()).replaceAll("%message%", announce));
			}
		}
	}
	
	public void sendWarAnnouncement(String announce){
		for(KingdomPlayer kp:onlineMembers){
			kp.getPlayer().sendMessage(ChatColor.DARK_GRAY + "========================================");
			kp.getPlayer().sendMessage("");
			kp.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + announce);
		}
	}
	
	public boolean isMember(KingdomPlayer kp){
		if(kp == null) return false;
		return this.equals(kp.getKingdom());
	}
	
	public boolean isAllyMember(KingdomPlayer kp){
		if(kp == null) return false;
		return isAllianceWith(kp.getKingdom()) ;
	}
	
	public boolean isEnemyMember(KingdomPlayer kp){
		if(kp == null) return false;
		return isEnemyWith(kp.getKingdom());
	}
	
	public Map<String, String> getInfo(){
		Map<String, String> info = new HashMap<String, String>();
		
		info.put("kingdom", String.valueOf(this.kingdomName));
		info.put("kingdomLore", String.valueOf(this.kingdomLore));
		info.put("king", Bukkit.getOfflinePlayer(king) != null ? Bukkit.getOfflinePlayer(king).getName() : null);
		info.put("might", String.valueOf(this.might));
		info.put("nexusAt", this.nexus_loc != null ? this.nexus_loc.toString() : null);
		info.put("homeAt", this.home_loc != null ? this.home_loc.toString() : null);
		info.put("rp", String.valueOf(this.resourcepoints));
		String members = "";
		for(KingdomPlayer member : onlineMembers) members +=" "+member.getPlayer().getName();
		info.put("online", members);
		info.put("land", String.valueOf(getLand()));
		
		return info;
	}

	@Override
	public void onOtherKingdomLoad(Kingdom k) {
		if(alliesList.contains(k.getKingdomUuid())){
			onlineAllies.add(k);
		}
		
		if(enemiesList.contains(k.getKingdomUuid())){
			onlineEnemies.add(k);
		}
	}

	@Override
	public void onOtherKingdomUnLoad(Kingdom k) {
		if(onlineAllies.contains(k)){
			onlineAllies.remove(k);
		}
		
		if(onlineEnemies.contains(k)){
			onlineEnemies.remove(k);
		}
	}

	@Override
	public void onKingdomPlayerLogin(KingdomPlayer kp) {
		if(kp == null) return;
		Kingdoms.logDebug("event login? "+onlineMembers.contains(kp));
		if(Kingdoms.getManagers().getKingdomManager().isBotKingdom(this)) return;
		if(king == null) return;
		if(!onlineMembers.contains(kp) && king.equals(kp.getUuid())){
			onlineMembers.add(kp);
			
			kp.setRank(Rank.KING);
			if(!kp.isVanishMode()&&
					!Config.getConfig().getBoolean("disable-join-messages")) {
				sendAnnouncement(null, Kingdoms.getLang().getString("Misc_Announcer_King_Online").replaceAll("%player%", kp.getPlayer().getName()), true);
			}
		}else if(!onlineMembers.contains(kp) && members.contains(kp.getUuid())){
			if(kp.getRank() == Rank.KING) kp.setRank(Rank.ALL);
			onlineMembers.add(kp);
			
			if(!kp.isVanishMode()&&
					!Config.getConfig().getBoolean("disable-join-messages"))sendAnnouncement(null, Kingdoms.getLang().getString("Misc_Announcer_Online").replaceAll("%player%",kp.getPlayer().getName()), true);
		}
	}

	@Override
	public void onKingdomPlayerLogout(KingdomPlayer kp) {
		Kingdoms.logDebug("event logout? "+onlineMembers.contains(kp));
		if(onlineMembers.contains(kp) && members.contains(kp.getUuid())){
			onlineMembers.remove(kp);
			
			if(!kp.isVanishMode()&&
					!Config.getConfig().getBoolean("disable-join-messages"))sendAnnouncement(null, Kingdoms.getLang().getString("Misc_Announcer_Offline").replaceAll("%player%",kp.getPlayer().getName()), true);
		}
	}
	
	@Override
	public void onMemberJoinKingdom(OfflineKingdomPlayer kp) {
		if(kp.getKingdomName() != null){
			if(!kp.getKingdomName().equalsIgnoreCase(kingdomName)) return;
		}
		kp.setRank(Rank.ALL);
		this.members.add(kp.getUuid());
		if(kp.isOnline()) this.onlineMembers.add((KingdomPlayer) kp);
		
		sendAnnouncement(null, Kingdoms.getLang().getString("Command_Accept_Announcement").replaceAll("%player%", kp.getName()), true);
	}

	@Override
	public void onMemberQuitKingdom(OfflineKingdomPlayer kp) {
		if(!this.members.contains(kp.getUuid())) return;
		
		if(kp instanceof KingdomPlayer){
			if(((KingdomPlayer) kp).getPlayer() != null){
				if(((KingdomPlayer) kp).getPlayer().isOnline()){
					((KingdomPlayer) kp).getPlayer().closeInventory();
				}
			}
			((KingdomPlayer)kp).setRank(Rank.ALL);
			((KingdomPlayer)kp).setKingdom(null);
			kp.setDonatedAmt(0);
			kp.setLastDonatedAmt(0);
			kp.setLastTimeDonated(null);
			
			this.members.remove(kp.getUuid());
			if(this.onlineMembers.contains((KingdomPlayer) kp))this.onlineMembers.remove((KingdomPlayer) kp);
		}else{
			kp.setRank(Rank.ALL);
			kp.setKingdomName(null);
			kp.setDonatedAmt(0);
			kp.setLastDonatedAmt(0);
			kp.setLastTimeDonated(null);
			this.members.remove(kp.getUuid());
		}

		sendAnnouncement(null, "["+kp.getName()+"] has left your kingdom!", true);
	}
	
	@Override
	public void onKingdomDelete(Kingdom k) {
		String kname = k.getKingdomName();
		
		if(alliesList.contains(k.getKingdomUuid())) alliesList.remove(k.getKingdomUuid());
		if(enemiesList.contains(k.getKingdomUuid())) enemiesList.remove(k.getKingdomUuid());
		
		if(onlineAllies.contains(k)) onlineAllies.remove(k);
		if(onlineEnemies.contains(k)) onlineEnemies.remove(k);
	}
	*/

}
