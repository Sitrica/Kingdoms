package com.songoda.kingdoms.api;

import java.util.Set;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.InvadingManager;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic;
import com.songoda.kingdoms.objects.invasions.InvasionTrigger;

public class Invasions {

	private final InvadingManager invadingManager;

	Invasions() {
		invadingManager = Kingdoms.getInstance().getManager(InvadingManager.class);
	}

	/**
	 * Set the current invasion mechanic. Use this method to set the mechanic to your custom one.
	 * 
	 * @param <T> InvasionMechanic<T extends InvasionTrigger>
	 * @param mechanic InvasionMechanic<T extends InvasionTrigger>
	 */
	public <T extends InvasionTrigger> void setMechanic(InvasionMechanic<T> mechanic) {
		invadingManager.setInvasionMechanic(mechanic);
	}

	/**
	 * @return A set of all current invasions going on.
	 */
	public Set<Invasion> getInvasions() {
		return invadingManager.getInvasions();
	}

}
