package me.limeglass.kingdoms.objects.invasions.defenders;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.DefenderDamageByPlayerEvent;
import me.limeglass.kingdoms.events.DefenderDamagePlayerEvent;
import me.limeglass.kingdoms.objects.DefenderAbility;
import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;

public class Speed extends DefenderAbility {

	public Speed() {
		super(false, "speed");
	}

	@Override
	public boolean initialize(Kingdoms instance) {
		return true;
	}

	@Override
	public void tick(LivingEntity defender, DefenderInfo info, Invasion invasion) {
		int speed = info.getSpeed();
		if (speed > 0)
			defender.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, tick + 1, speed - 1));
	}

	@Override
	public void onDamaging(DefenderDamagePlayerEvent event) {}

	@Override
	public void onDamage(DefenderDamageByPlayerEvent event) {}

	@Override
	public void onDeath(LivingEntity defender) {}

	@Override
	public void onAdd(LivingEntity defender) {}

}
