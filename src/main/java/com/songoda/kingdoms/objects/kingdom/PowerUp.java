package com.songoda.kingdoms.objects.kingdom;

public class Powerup {
	
	private int reduction, regeneration, damageBoost, arrowBoost;
	private final OfflineKingdom kingdom;

	public Powerup(OfflineKingdom kingdom) {
		this.kingdom = kingdom;
	}
	
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
	
	public void setLevel(int level, PowerupType... types) {
		for (PowerupType type : types) {
			switch (type) {
				case DAMAGE_REDUCTION:
					this.reduction = level;
				case REGENERATION_BOOST:
					this.regeneration = level;
				case DAMAGE_BOOST:
					this.damageBoost = level;
				case ARROW_BOOST:
					this.arrowBoost = level;
			}
		}
	}
	
	public OfflineKingdom getKingdom() {
		return kingdom;
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
