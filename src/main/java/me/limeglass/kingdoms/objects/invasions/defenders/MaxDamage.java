package me.limeglass.kingdoms.objects.invasions.defenders;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.DefenderDamageByPlayerEvent;
import me.limeglass.kingdoms.events.DefenderDamageMaxedEvent;
import me.limeglass.kingdoms.events.DefenderDamagePlayerEvent;
import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.DefenderAbility;
import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;

public class MaxDamage extends DefenderAbility {

	public MaxDamage() {
		super(true, "damage-cap");
	}

	@Override
	public void tick(LivingEntity defender, DefenderInfo infom, Invasion invasion) {}

	@Override
	public void onDamaging(DefenderDamagePlayerEvent event) {}

	@Override
	public void onDamage(DefenderDamageByPlayerEvent event) {
		Defender defender = event.getDefender();
		DefenderInfo info = defender.getDefenderInfo();
		int limit = info.getDamageLimit();
		if (limit <= 0)
			return;
		double damage = event.getDamage();
		if (damage > 15) {
			DefenderDamageMaxedEvent damageEvent = new DefenderDamageMaxedEvent(defender, event.getPlayer(), limit, damage);
			Bukkit.getPluginManager().callEvent(damageEvent);
			if (!damageEvent.isCancelled())
				event.setDamage(damageEvent.getLimit());
		}
	}

	@Override
	public boolean initialize(Kingdoms instance) {
		return true;
	}

	@Override
	public void onDeath(LivingEntity defender) {}

	@Override
	public void onAdd(LivingEntity defender) {}

}
