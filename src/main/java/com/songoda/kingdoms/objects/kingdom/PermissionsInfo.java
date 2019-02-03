package com.songoda.kingdoms.objects.kingdom;

import com.songoda.kingdoms.constants.Rank;

public class PermissionsInfo {
	Rank nexus = Rank.KING;
	Rank claim = Rank.MODS;
	Rank unclaim = Rank.MODS;
	Rank invade = Rank.ALL;
	Rank ally = Rank.MODS;
	Rank turret = Rank.MODS;
	Rank sethome = Rank.KING;
	Rank chest = Rank.MODS;
	Rank openallchest = Rank.KING;
	Rank invite = Rank.MODS;
	Rank broad = Rank.MODS;
	Rank RPConvert = Rank.ALL;
	Rank structures = Rank.MODS;
	Rank buildInNexus = Rank.ALL;
	Rank useKHome = Rank.ALL;
	Rank overrideRegulator = Rank.KING;
	Rank buyXpBottles = Rank.MODS;
	public PermissionsInfo(){
		
	}
	public Rank getNexus() {
		return nexus;
	}
	public void setNexus(Rank nexus) {
		this.nexus = nexus;
	}
	public Rank getClaim() {
		return claim;
	}
	public void setClaim(Rank claim) {
		this.claim = claim;
	}
	public Rank getUnclaim() {
		return unclaim;
	}
	public void setUnclaim(Rank unclaim) {
		this.unclaim = unclaim;
	}
	public Rank getInvade() {
		return invade;
	}
	public void setInvade(Rank invade) {
		this.invade = invade;
	}
	public Rank getAlly() {
		return ally;
	}
	public void setAlly(Rank ally) {
		this.ally = ally;
	}
	public Rank getTurret() {
		return turret;
	}
	public void setTurret(Rank turret) {
		this.turret = turret;
	}
	public Rank getSethome() {
		return sethome;
	}
	public void setSethome(Rank sethome) {
		this.sethome = sethome;
	}
	public Rank getChest() {
		return chest;
	}
	public void setChest(Rank chest) {
		this.chest = chest;
	}
	public Rank getOpenallchest() {
		return openallchest;
	}
	public void setOpenallchest(Rank openallchest) {
		this.openallchest = openallchest;
	}
	public Rank getInvite() {
		return invite;
	}
	public void setInvite(Rank invite) {
		this.invite = invite;
	}
	
	public Rank getBroad() {
		return broad;
	}
	public void setBroad(Rank broad) {
		this.broad = broad;
	}
	
	public Rank getBuildInNexus() {
		return buildInNexus;
	}
	public void setBuildInNexus(Rank buildInNexus) {
		this.buildInNexus = buildInNexus;
	}
	public Rank getRPConvert() {
		return RPConvert;
	}
	public void setRPConvert(Rank rPConvert) {
		RPConvert = rPConvert;
	}
	public Rank getStructures() {
		return structures;
	}
	public void setStructures(Rank structures) {
		this.structures = structures;
	}
	public Rank getBuyXpBottles() {
		return buyXpBottles;
	}
	public void setBuyXpBottles(Rank buyXpBottles) {
		this.buyXpBottles = buyXpBottles;
	}
	public Rank getOverrideRegulator() {
		return overrideRegulator;
	}
	public void setOverrideRegulator(Rank overrideRegulator) {
		this.overrideRegulator = overrideRegulator;
	}
	public Rank getUseKHome() {
		return useKHome;
	}
	public void setUseKHome(Rank useKHome) {
		this.useKHome = useKHome;
	}
	
/*	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> ser = new HashMap<String, Object>();
		
		ser.put("nexus",nexus.toString());
		ser.put("claim",claim.toString());
		ser.put("unclaim",unclaim.toString());
		ser.put("invade",invade.toString());
		ser.put("ally",ally.toString());
		ser.put("turret",turret.toString());
		ser.put("sethome",sethome.toString());
		ser.put("chest",chest.toString());
		ser.put("openallchest",openallchest.toString());
		ser.put("invite", invite.toString());
		ser.put("broad", broad.toString());
		
		return ser;
	}
	
	public PermissionsInfo(Map<String, Object> ser){
		nexus = Rank.valueOf((String)ser.getOrDefault("nexus","KING"));
		claim = Rank.valueOf((String)ser.getOrDefault("claim","MODS"));
		unclaim = Rank.valueOf((String)ser.getOrDefault("unclaim","MODS"));
		invade = Rank.valueOf((String)ser.getOrDefault("invade","ALL"));
		ally = Rank.valueOf((String)ser.getOrDefault("ally","MODS"));
		turret = Rank.valueOf((String)ser.getOrDefault("turret","MODS"));
		sethome = Rank.valueOf((String)ser.getOrDefault("sethome","KING"));
		chest = Rank.valueOf((String)ser.getOrDefault("chest","MODS"));
		openallchest = Rank.valueOf((String)ser.getOrDefault("openallchest","KING"));
		invite = Rank.valueOf((String) ser.getOrDefault("invite", "MODS"));
		broad = Rank.valueOf((String) ser.getOrDefault("broad", "MODS"));
	}*/
}
