package com.songoda.kingdoms.objects.invasions;

import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class CommandTrigger extends InvasionTrigger {

	public CommandTrigger(Invasion invasion, LandInfo info, KingdomPlayer player) {
		super(invasion, info, player.getUniqueId());
	}

}
