package me.limeglass.kingdoms.objects.invasions.defenders;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.DefenderDamageByPlayerEvent;
import me.limeglass.kingdoms.events.DefenderDamagePlayerEvent;
import me.limeglass.kingdoms.events.DefenderFocusEvent;
import me.limeglass.kingdoms.objects.Defender;
import me.limeglass.kingdoms.objects.DefenderAbility;
import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class Focus extends DefenderAbility {

	public Focus() {
		super(true, "focus");
	}

	@Override
	public void tick(LivingEntity defender, DefenderInfo infom, Invasion invasion) {}

	@Override
	public void onDamaging(DefenderDamagePlayerEvent event) {}

	@Override
	public void onDamage(DefenderDamageByPlayerEvent event) {
		Defender defender = event.getDefender();
		DefenderInfo info = defender.getDefenderInfo();
		int focus = info.getFocus();
		if (focus <= 0)
			return;
		KingdomPlayer kingdomPlayer = event.getPlayer();
		Player player = kingdomPlayer.getPlayer();
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		if (effects.isEmpty())
			return;
		DefenderFocusEvent focusEvent = new DefenderFocusEvent(defender, kingdomPlayer);
		Bukkit.getPluginManager().callEvent(focusEvent);
		if (focusEvent.isCancelled())
			return;
		for (PotionEffect effect : effects) {
			PotionEffect potion = new PotionEffect(effect.getType(), effect.getDuration() - section.getInt("remove", 2), effect.getAmplifier());
			player.removePotionEffect(effect.getType());
			player.addPotionEffect(potion);
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
