package com.songoda.kingdoms.objects.kingdom;

import com.songoda.kingdoms.constants.kingdom.champion.ChampionUpgrade;

public class ChampionInfo {
	int health = 100;
	int damage = 1;
	int specials = 0;
	int speed = 0;
	int thor = 0;
	int resist = 0;
	int tier = 1;
	int grab = 0;
	int summon = 0;
	int damagecap = 0;
	int plow = 0;
	int strength = 0;
	int armor = 0;
	int reinforcements = 0;
	int mimic = 0;
	int weapon = 0;
	int drag = 0;
	int mock = 0;
	int duel = 0;
	int focus = 0;
	int aqua = 0;
	int determination = 0;
	
	public ChampionInfo(){
		
	}
	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}
	
	
	
	
	public int getUpgradeLevel(ChampionUpgrade upgrade){
		if(upgrade == null) return 0;
		
		switch(upgrade){
			case WEAPON:
				return getWeapon();
			case AQUA:
				return getAqua();
			case ARMOR:
				return getArmor();
			case DAMAGE_CAP:
				return getDamagecap();
			case DEATH_DUEL:
				return getDuel();
			case DRAG:
				return getDrag();
			case FOCUS:
				return getFocus();
			case HEALTH:
				return getHealth();
			case HEALTHII:
				return getHealth();
			case MIMIC:
				return getMimic();
			case MOCK:
				return getMock();
			case PLOW:
				return getPlow();
			case REINFORCEMENTS:
				return getReinforcements();
			case RESISTANCE:
				return getResist();
			case SPEED:
				return getSpeed();
			case STRENGTH:
				return getStrength();
			case THOR:
				return getThor();
			case DETERMINATION:
				return getDetermination();
			case DETERMINATIONII:
				return getDetermination();
		}
		return 0;
	}
	
	public void setUpgradeLevel(ChampionUpgrade upgrade, int lvl){
		if(upgrade == null) return;
		
		switch(upgrade){
			case WEAPON:
				setWeapon(lvl); 
				break;
			case AQUA:
				setAqua(lvl); 
				break;
			case ARMOR:
				setArmor(lvl); 
				break;
			case DAMAGE_CAP:
				setDamagecap(lvl); 
				break;
			case DEATH_DUEL:
				setDuel(lvl); 
				break;
			case DRAG:
				setDrag(lvl); 
				break;
			case FOCUS:
				setFocus(lvl); 
				break;
			case HEALTH:
				setHealth(lvl);
			case HEALTHII:
				setHealth(lvl);  
				break;
			case MIMIC:
				setMimic(lvl); 
				break;
			case MOCK:
				setMock(lvl); 
				break;
			case PLOW:
				setPlow(lvl); 
				break;
			case REINFORCEMENTS:
				setReinforcements(lvl); 
				break;
			case RESISTANCE:
				setResist(lvl); 
				break;
			case SPEED:
				setSpeed(lvl); 
				break;
			case STRENGTH:
				setStrength(lvl); 
				break;
			case THOR:
				setThor(lvl); 
				break;
			case DETERMINATION:
				setDetermination(lvl); 
			case DETERMINATIONII:
				setDetermination(lvl); 
				break;
		}
		return;
	}
	
	public int getDetermination() {
		return determination;
	}
	public void setDetermination(int determination) {
		this.determination = determination;
	}
	public int getDamage() {
		return damage;
	}
	public void setDamage(int damage) {
		this.damage = damage;
	}
	public int getSpecials() {
		return specials;
	}
	public void setSpecials(int specials) {
		this.specials = specials;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public int getThor() {
		return thor;
	}
	public void setThor(int thor) {
		this.thor = thor;
	}
	public int getResist() {
		return resist;
	}
	public void setResist(int resist) {
		this.resist = resist;
	}
	public int getTier() {
		return tier;
	}
	public void setTier(int tier) {
		this.tier = tier;
	}
	public int getGrab() {
		return grab;
	}
	public void setGrab(int grab) {
		this.grab = grab;
	}
	public int getSummon() {
		return summon;
	}
	public void setSummon(int summon) {
		this.summon = summon;
	}
	public int getDamagecap() {
		return damagecap;
	}
	public void setDamagecap(int damagecap) {
		this.damagecap = damagecap;
	}
	public int getPlow() {
		return plow;
	}
	public void setPlow(int plow) {
		this.plow = plow;
	}
	public int getStrength() {
		return strength;
	}
	public void setStrength(int strength) {
		this.strength = strength;
	}
	public int getArmor() {
		return armor;
	}
	public void setArmor(int armor) {
		this.armor = armor;
	}
	public int getReinforcements() {
		return reinforcements;
	}
	public void setReinforcements(int reinforcements) {
		this.reinforcements = reinforcements;
	}
	public int getMimic() {
		return mimic;
	}
	public void setMimic(int mimic) {
		this.mimic = mimic;
	}
	public int getWeapon() {
		return weapon;
	}
	public void setWeapon(int weapon) {
		this.weapon = weapon;
	}
	public int getDrag() {
		return drag;
	}
	public void setDrag(int drag) {
		this.drag = drag;
	}
	public int getMock() {
		return mock;
	}
	public void setMock(int mock) {
		this.mock = mock;
	}
	public int getDuel() {
		return duel;
	}
	public void setDuel(int duel) {
		this.duel = duel;
	}
	public int getFocus() {
		return focus;
	}
	public void setFocus(int focus) {
		this.focus = focus;
	}
	
	public int getAqua() {
		return aqua;
	}
	public void setAqua(int aqua) {
		this.aqua = aqua;
	}
/*	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> ser = new HashMap<String, Object>();
		
		ser.put("health", health);
		ser.put("damage",damage);
		ser.put("specials",specials);
		ser.put("speed",speed);
		ser.put("thor",thor);
		ser.put("resist",resist);
		ser.put("tier",tier);
		ser.put("grab",grab);
		ser.put("summon",summon);
		ser.put("damagecap",damagecap);
		ser.put("plow",plow);
		ser.put("strength",strength);
		ser.put("armor",armor);
		ser.put("reinforcements",reinforcements);
		ser.put("mimic",mimic);
		ser.put("weapon",weapon);
		ser.put("drag",drag);
		ser.put("mock",mock);
		ser.put("duel",duel);
		ser.put("focus",focus);
		ser.put("aqua", aqua);
		
		return ser;
	}
	
	public ChampionInfo(Map<String, Object> ser){
		health = (int) ser.getOrDefault("health", 100);
		damage = (int) ser.getOrDefault("damage", 1);
		specials = (int) ser.getOrDefault("specials", 0);
		speed = (int) ser.getOrDefault("speed", 0);
		thor = (int) ser.getOrDefault("thor", 0);
		resist = (int) ser.getOrDefault("resist", 0);
		tier = (int) ser.getOrDefault("tier", 1);
		grab = (int) ser.getOrDefault("grab", ser);
		summon = (int) ser.getOrDefault("summon", 0);
		damagecap = (int) ser.getOrDefault("damagecap", 0);
		plow = (int) ser.getOrDefault("plow", 0);
		strength = (int) ser.getOrDefault("strength", 0);
		armor = (int) ser.getOrDefault("armor", 0);
		reinforcements = (int) ser.getOrDefault("reinforcements", 0);
		mimic = (int) ser.getOrDefault("mimic", 0);
		weapon = (int) ser.getOrDefault("weapon", 0);
		drag = (int) ser.getOrDefault("drag", 0);
		mock = (int) ser.getOrDefault("mock", 0);
		duel = (int) ser.getOrDefault("duel", 0);
		focus = (int) ser.getOrDefault("focus", 0);
		aqua = (int) ser.getOrDefault("aqua", 0);
	}*/
}
