package me.limeglass.kingdoms.objects.invasions.defenders;

import java.util.Optional;

import org.bukkit.entity.LivingEntity;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.DefenderDamageByPlayerEvent;
import me.limeglass.kingdoms.events.DefenderDamagePlayerEvent;
import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.DefenderAbility;
import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.utils.HologramBuilder;

public class Duel extends DefenderAbility {

	public Duel() {
		super(true, "death-duel");
	}

	@Override
	public void tick(LivingEntity defender, DefenderInfo infom, Invasion invasion) {}

	@Override
	public void onDamage(DefenderDamageByPlayerEvent event) {
		Defender defender = event.getDefender();
		if (event.getPlayer().equals(defender.getInvasion().getInstigator()))
			return;
		Optional<LivingEntity> entity = defender.getDefender();
		event.setDamage(event.getDamage() / 2);
		if (entity.isPresent())
			new HologramBuilder(entity.get().getLocation().add(0, 1, 0), "holograms.defender-divided")
					.withDefaultExpiration("2 seconds")
					.setPlaceholderObject(event.getPlayer())
					.setKingdom(defender.getOwner())
					.send(event.getPlayer());
	}

	@Override
	public void onDamaging(DefenderDamagePlayerEvent event) {
		Invasion invasion = event.getDefender().getInvasion();
		if (invasion.getInvolved().stream().anyMatch(involved -> involved.equals(event.getPlayer())))
			return;
		event.setDamage(event.getDamage() * 2);
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
