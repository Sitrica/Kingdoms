package me.limeglass.kingdoms.objects.invasions;

import me.limeglass.kingdoms.manager.managers.LandManager.LandInfo;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

public class CommandTrigger extends InvasionTrigger {

	public CommandTrigger(Invasion invasion, LandInfo info, KingdomPlayer player) {
		super(invasion, info, player.getUniqueId());
	}

}
