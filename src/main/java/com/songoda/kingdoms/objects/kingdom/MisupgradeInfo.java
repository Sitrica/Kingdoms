package com.songoda.kingdoms.objects.kingdom;

public class MisupgradeInfo{
	boolean antitrample = false;
	boolean anticreeper = false;
	boolean nexusguard = false;
	boolean glory = false;
	boolean bombshards = false;
	boolean psioniccore = false;
	boolean fireproofing = false;
	boolean enabledantitrample = true;
	boolean enabledanticreeper = true;
	boolean enablednexusguard = true;
	boolean enabledglory = true;
	boolean enabledbombshards = true;
	boolean enabledpsioniccore = true;
	boolean enabledfireproofing = true;
	
	public boolean isBought(MiscUpgrade upgrade){
		if(upgrade == null) return false;
		
		switch(upgrade){
			case ANTICREEPER:
				return isAnticreeper();
			case ANTITRAMPLE:
				return isAntitrample();
			case BOMBSHARDS:
				return isBombshards();
			case GLORY:
				return isGlory();
			case NEXUSGUARD:
				return isNexusguard();
			case PSIONICCORE:
				return isPsioniccore();
		}
		return false;
	}
	
	public boolean isEnabled(MiscUpgrade upgrade){
		if(upgrade == null) return false;
		
		switch(upgrade){
			case ANTICREEPER:
				return isEnabledanticreeper();
			case ANTITRAMPLE:
				return isEnabledantitrample();
			case BOMBSHARDS:
				return isEnabledbombshards();
			case GLORY:
				return isEnabledglory();
			case NEXUSGUARD:
				return isEnablednexusguard();
			case PSIONICCORE:
				return isEnabledpsioniccore();
		}
		return false;
	}
	

	public void setEnabled(MiscUpgrade upgrade, boolean enabled){
		
		switch(upgrade){
			case ANTICREEPER:
				setEnabledanticreeper(enabled);
				break;
			case ANTITRAMPLE:
				setEnabledantitrample(enabled);
				break;
			case BOMBSHARDS:
				setEnabledbombshards(enabled);
				break;
			case GLORY:
				setEnabledglory(enabled);
				break;
			case NEXUSGUARD:
				setEnablednexusguard(enabled);
				break;
			case PSIONICCORE:
				setEnabledpsioniccore(enabled);
				break;
		}
	}

	public void setBought(MiscUpgrade upgrade, boolean enabled){
		
		switch(upgrade){
			case ANTICREEPER:
				setAnticreeper(enabled);
				break;
			case ANTITRAMPLE:
				setAntitrample(enabled);
				break;
			case BOMBSHARDS:
				setBombshards(enabled);
				break;
			case GLORY:
				setGlory(enabled);
				break;
			case NEXUSGUARD:
				setNexusguard(enabled);
				break;
			case PSIONICCORE:
				setPsioniccore(enabled);
				break;
		}
	}

	/**
	 * soil cannot be trampled
	 * @return
	 */
	public boolean isAntitrample() {
		return antitrample;
	}

	public void setAntitrample(boolean antitrample) {
		this.antitrample = antitrample;
	}

	/**
	 * no land damage + no player damage
	 */
	public boolean isAnticreeper() {
		return anticreeper;
	}

	public void setAnticreeper(boolean anticreeper) {
		this.anticreeper = anticreeper;
	}

	/**
	 * light armor with champ weap spawn 2 of them
	 * @return
	 */
	public boolean isNexusguard() {
		return nexusguard;
	}

	public void setNexusguard(boolean nexusguard) {
		this.nexusguard = nexusguard;
	}

	/**
	 * x3 exp
	 * @return
	 */
	public boolean isGlory() {
		return glory;
	}

	public void setGlory(boolean glory) {
		this.glory = glory;
	}

	/**
	 * no land damage, 5 damage to players around
	 * @return
	 */
	public boolean isBombshards() {
		return bombshards;
	}

	public void setBombshards(boolean bombshards) {
		this.bombshards = bombshards;
	}

	/**
	 * apply strength 1 for 10 sec to champ and guard
	 * @return
	 */
	public boolean isPsioniccore() {
		return psioniccore;
	}

	public void setPsioniccore(boolean psioniccore) {
		this.psioniccore = psioniccore;
	}
/*
	public MisupgradeInfo(Map<String, Object> ser){
		antitrample = (boolean) (ser.get("antitrample") != null ? ser.get("antitrample") : false);
		anticreeper = (boolean) (ser.get("anticreeper") != null ? ser.get("anticreeper") : false);
		nexusguard = (boolean) (ser.get("nexusguard") != null ? ser.get("nexusguard") : false);
		glory = (boolean) (ser.get("glory") != null ? ser.get("glory") : false);
		bombshards = (boolean) (ser.get("bombshards") != null ? ser.get("bombshards") : false);
		psioniccore = (boolean) (ser.get("psioniccore") != null ? ser.get("psioniccore") : false);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> ser = new HashMap<String, Object>();
		
		ser.put("antitrample", antitrample);
		ser.put("anticreeper", anticreeper);
		ser.put("nexusguard", nexusguard);
		ser.put("glory", glory);
		ser.put("bombshards", bombshards);
		ser.put("psioniccore", psioniccore);
		
		return ser;
	}*/

	public boolean isFireproofing() {
		return fireproofing;
	}

	public void setFireproofing(boolean fireproofing) {
		this.fireproofing = fireproofing;
	}

	public boolean isEnabledantitrample() {
		return enabledantitrample;
	}

	public boolean isEnabledanticreeper() {
		return enabledanticreeper;
	}

	public boolean isEnablednexusguard() {
		return enablednexusguard;
	}

	public boolean isEnabledglory() {
		return enabledglory;
	}

	public boolean isEnabledbombshards() {
		return enabledbombshards;
	}

	public boolean isEnabledpsioniccore() {
		return enabledpsioniccore;
	}

	public boolean isEnabledfireproofing() {
		return enabledfireproofing;
	}

	public void setEnabledantitrample(boolean enabledantitrample) {
		this.enabledantitrample = enabledantitrample;
	}

	public void setEnabledanticreeper(boolean enabledanticreeper) {
		this.enabledanticreeper = enabledanticreeper;
	}

	public void setEnablednexusguard(boolean enablednexusguard) {
		this.enablednexusguard = enablednexusguard;
	}

	public void setEnabledglory(boolean enabledglory) {
		this.enabledglory = enabledglory;
	}

	public void setEnabledbombshards(boolean enabledbombshards) {
		this.enabledbombshards = enabledbombshards;
	}

	public void setEnabledpsioniccore(boolean enabledpsioniccore) {
		this.enabledpsioniccore = enabledpsioniccore;
	}

	public void setEnabledfireproofing(boolean enabledfireproofing) {
		this.enabledfireproofing = enabledfireproofing;
	}

}
