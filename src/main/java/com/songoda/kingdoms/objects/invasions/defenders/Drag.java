package com.songoda.kingdoms.objects.invasions.defenders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.DefenderDamageByPlayerEvent;
import com.songoda.kingdoms.events.DefenderDamagePlayerEvent;
import com.songoda.kingdoms.events.DefenderDragEvent;
import com.songoda.kingdoms.objects.DefenderAbility;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

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
