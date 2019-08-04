package me.limeglass.kingdoms.objects.invasions.defenders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.DefenderDamageByPlayerEvent;
import me.limeglass.kingdoms.events.DefenderDamagePlayerEvent;
import me.limeglass.kingdoms.events.DefenderDragEvent;
import me.limeglass.kingdoms.objects.DefenderAbility;
import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class Drag extends DefenderAbility {

	public Drag() {
		super(false, "drag");
	}

	@Override
	public boolean initialize(Kingdoms instance) {
		return true;
	}

	@Override
	public void tick(LivingEntity defender, DefenderInfo info, Invasion invasion) {
		KingdomPlayer instigator = invasion.getInstigator();
		Player player = instigator.getPlayer();
		if (player.isDead() || !player.isOnline())
			return;
		double range = section.getDouble("range", 7);
		DefenderDragEvent event = new DefenderDragEvent(defender, invasion, range);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		Location location = defender.getLocation();
		int drag = info.getDrag();
		if (player.getLocation().distance(location) < event.getRange() + (2 * drag))
			return;
		player.teleport(location); //TODO remake this to have a pulling animation. youtu.be/QCRreb2659A
		new MessageBuilder("defenders.drag")
				.setKingdom(invasion.getTarget())
				.setPlaceholderObject(player)
				.send(player);
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
