package com.songoda.kingdoms.objects.kingdom;

public class Powerup {

	private int reduction, regeneration, damageBoost, arrowBoost;

	public int getLevel(PowerupType type) {
		switch (type) {
			case DAMAGE_REDUCTION:
				return reduction;
			case REGENERATION_BOOST:
				return regeneration;
			case DAMAGE_BOOST:
				return damageBoost;
			case ARROW_BOOST:
				return arrowBoost;
		}
		return 0;
	}

	public void setLevel(int level, PowerupType type) {
		switch (type) {
			case DAMAGE_REDUCTION:
				reduction = level;
				break;
			case REGENERATION_BOOST:
				regeneration = level;
				break;
			case DAMAGE_BOOST:
				damageBoost = level;
				break;
			case ARROW_BOOST:
				arrowBoost = level;
				break;
		}
	}

	public int getDamageReduction() {
		return reduction;
	}

	public void setDamageReduction(int reduction) {
		this.reduction = reduction;
	}

	public int getRegeneration() {
		return regeneration;
	}

	public void setRegeneration(int regeneration) {
		this.regeneration = regeneration;
	}

	public int getDamageBoost() {
		return damageBoost;
	}

	public void setDamageBoost(int damageBoost) {
		this.damageBoost = damageBoost;
	}

	public int getArrowBoost() {
		return arrowBoost;
	}

	public void setArrowBoost(int arrowBoost) {
		this.arrowBoost = arrowBoost;
	}

}
