package com.songoda.kingdoms.objects.kingdom;

public class DefenderInfo {

	private final String kingdom;
	private int reinforcements = 0;
	private int megaHealth = 0;
	private int absorption = 0;
	private int resistance = 0;
	private int specials = 0;
	private int strength = 0;
	private int health = 100;
	private int damage = 1;
	private int weapon = 0;
	private int summon = 0;
	private int thrown = 0;
	private int speed = 0;
	private int armor = 0;
	private int focus = 0;
	private int limit = 0;
	private int tier = 1;
	private int thor = 0;
	private int grab = 0;
	private int plow = 0;
	private int drag = 0;
	private int duel = 0;
	private int aqua = 0;

	public DefenderInfo(OfflineKingdom kingdom) {
		this.kingdom = kingdom.getName();
	}

	public String getKingdomName() {
		return kingdom;
	}

	public int getDrag() {
		return drag;
	}

	public void setDrag(int drag) {
		this.drag = drag;
	}

	public int getDuel() {
		return duel;
	}

	public void setDuel(int duel) {
		this.duel = duel;
	}

	public int getPlow() {
		return plow;
	}

	public void setPlow(int plow) {
		this.plow = plow;
	}

	public int getAqua() {
		return aqua;
	}

	public void setAqua(int aqua) {
		this.aqua = aqua;
	}

	public int getThor() {
		return thor;
	}

	public void setThor(int thor) {
		this.thor = thor;
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

	public int getFocus() {
		return focus;
	}

	public void setFocus(int focus) {
		this.focus = focus;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getArmor() {
		return armor;
	}

	public void setArmor(int armor) {
		this.armor = armor;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getWeapon() {
		return weapon;
	}

	public void setWeapon(int weapon) {
		this.weapon = weapon;
	}

	public int getSummon() {
		return summon;
	}

	public void setSummon(int summon) {
		this.summon = summon;
	}

	public int getThrow() {
		return thrown;
	}

	public void setThrow(int thrown) {
		this.thrown = thrown;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getMegaHealth() {
		return megaHealth;
	}

	public void setMegaHealth(int megaHealth) {
		this.megaHealth = megaHealth;
	}

	public int getSpecials() {
		return specials;
	}

	public void setSpecials(int specials) {
		this.specials = specials;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getDamageLimit() {
		return limit;
	}

	public void setDamageLimit(int limit) {
		this.limit = limit;
	}

	public int getResistance() {
		return resistance;
	}

	public void setResistance(int resistance) {
		this.resistance = resistance;
	}

	public int getAbsorption() {
		return absorption;
	}

	public void setAbsorption(int absorption) {
		this.absorption = absorption;
	}

	public int getReinforcements() {
		return reinforcements;
	}

	public void setReinforcements(int reinforcements) {
		this.reinforcements = reinforcements;
	}

	public int getUpgradeLevel(DefenderUpgrade upgrade) {
		if (upgrade == null)
			return 0;
		switch (upgrade) {
			case AQUA:
				return aqua;
			case ARMOR:
				return armor;
			case DAMAGE_CAP:
				return limit;
			case DEATH_DUEL:
				return duel;
			case DRAG:
				return drag;
			case FOCUS:
				return focus;
			case HEALTH:
				return health;
			case PLOW:
				return plow;
			case REINFORCEMENTS:
				return reinforcements;
			case RESISTANCE:
				return resistance;
			case SPEED:
				return speed;
			case STRENGTH:
				return strength;
			case THOR:
				return thor;
			case WEAPON:
				return weapon;
			case MEGA_HEALTH:
				return megaHealth;
			case THROW:
				return thrown;
		}
		return 0;
	}

	public void setUpgradeLevel(DefenderUpgrade upgrade, int level) {
		if (upgrade == null)
			return;
		switch (upgrade) {
			case AQUA:
				this.aqua = level; 
				break;
			case ARMOR:
				this.armor = level; 
				break;
			case DAMAGE_CAP:
				this.limit = level; 
				break;
			case DEATH_DUEL:
				this.duel = level;  
				break;
			case DRAG:
				this.drag = level; 
				break;
			case FOCUS:
				this.focus = level; 
				break;
			case HEALTH:
				this.health = level; 
			case PLOW:
				this.plow = level; 
				break;
			case REINFORCEMENTS:
				this.reinforcements = level; 
				break;
			case RESISTANCE:
				this.resistance = level; 
				break;
			case SPEED:
				this.speed = level; 
				break;
			case STRENGTH:
				this.strength = level; 
				break;
			case THOR:
				this.thor = level; 
				break;
			case WEAPON:
				this.weapon = level; 
				break;
			case MEGA_HEALTH:
				this.megaHealth = level;
				break;
			case THROW:
				this.thrown = level;
				break;
		}
	}

}
