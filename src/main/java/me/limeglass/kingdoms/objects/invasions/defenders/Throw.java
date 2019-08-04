package me.limeglass.kingdoms.objects.invasions.defenders;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.DefenderDamageByPlayerEvent;
import me.limeglass.kingdoms.events.DefenderDamagePlayerEvent;
import me.limeglass.kingdoms.events.DefenderThrowEvent;
import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.DefenderAbility;
import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class Throw extends DefenderAbility {

	public Throw() {
		super(true, "throw");
	}

	@Override
	public void tick(LivingEntity defender, DefenderInfo infom, Invasion invasion) {}

	@Override
	public void onDamaging(DefenderDamagePlayerEvent event) {}

	@Override
	public void onDamage(DefenderDamageByPlayerEvent event) {
		Defender defender = event.getDefender();
		DefenderInfo info = defender.getDefenderInfo();
		KingdomPlayer victim = event.getPlayer();
		int throwing = info.getThrow();
		if (throwing <= 0)
			return;
		if (new Random().nextInt(100) <= throwing) {
			DefenderThrowEvent throwEvent = new DefenderThrowEvent(defender, victim, throwing);
			Bukkit.getPluginManager().callEvent(throwEvent);
			if (throwEvent.isCancelled())
				return;
			victim.getPlayer().setVelocity(new Vector(0, 1.5, 0));
		}
	}

	@Override
	public boolean initialize(Kingdoms instance) {
		return false;
	}

	@Override
	public void onDeath(LivingEntity defender) {}

	@Override
	public void onAdd(LivingEntity defender) {}

}
