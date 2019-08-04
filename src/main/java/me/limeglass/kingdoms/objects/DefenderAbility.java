package me.limeglass.kingdoms.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.events.DefenderDamageByPlayerEvent;
import me.limeglass.kingdoms.events.DefenderDamagePlayerEvent;
import me.limeglass.kingdoms.objects.invasions.Invasion;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;

public abstract class DefenderAbility {

	protected final ConfigurationSection section;
	private final boolean asynchronous;
	private final String[] names;
	protected final int tick;

	/**
	 * The constructor for creating abilities on Defenders.
	 * 
	 * @param asynchronous if this ability can run it's runnable ticker on an asynchronous thread.
	 * @param node The node within the upgrades.* in the defender-upgrades.yml
	 */
	public DefenderAbility(boolean asynchronous, String node) {
		this.asynchronous = asynchronous;
		names = new String[] {node};
		this.section = Kingdoms.getInstance().getConfiguration("defender-upgrades").get().getConfigurationSection("upgrades." + node);
		this.tick = section.getInt("tick", 5);
	}

	/**
	 * The constructor for creating abilities on Defenders.
	 * 
	 * @param asynchronous if this ability can run it's runnable ticker on an asynchronous thread.
	 * @param node The node within the upgrades.* in the defender-upgrades.yml
	 * @param names The names used to match this ability to the configuration.
	 */
	public DefenderAbility(boolean asynchronous, String node, String... names) {
		this.asynchronous = asynchronous;
		this.names = names;
		if (names == null)
			names = new String[] {node};
		this.section = Kingdoms.getInstance().getConfiguration("defender-upgrades").get().getConfigurationSection("upgrades." + node);
		this.tick = section.getInt("tick", 5);
	}

	/**
	 * The calling tick on the entity to do magical abilities on.
	 * 
	 * @param defender The Entity to execute things on.
	 * @param info The DefenderInfo relating to the defender.
	 * @param invasion The Invasion relating to the defender.
	 */
	public abstract void tick(LivingEntity defender, DefenderInfo infom, Invasion invasion);

	/**
	 * Called when a player is being attacked by the Defender.
	 * 
	 * @param event The DefenderDamagePlayerEvent involved.
	 */
	public abstract void onDamaging(DefenderDamagePlayerEvent event);

	/**
	 * Called when a player has attacked the Defender.
	 * 
	 * @param event The DefenderDamageByPlayerEvent involved.
	 */
	public abstract void onDamage(DefenderDamageByPlayerEvent event);

	/**
	 * Called when the extended Ability gets initialized.
	 * 
	 * @param instance The Kingdoms instance for easy manipulating.
	 * @return boolean if it should not be initalized.
	 */
	public abstract boolean initialize(Kingdoms instance);

	/**
	 * Called when the defender dies.
	 * 
	 * @param defender The Entity having this ability.
	 */
	public abstract void onDeath(LivingEntity defender);

	/**
	 * Called when an ability is added onto a defender.
	 * 
	 * @param defender The Entity having this ability applied to.
	 */
	public abstract void onAdd(LivingEntity defender);

	/**
	 * @return boolean if this ability can run it's ticks asynchronously.
	 */
	public boolean isAsynchronous() {
		return asynchronous;
	}

	public String[] getNames() {
		return names;
	}

	/**
	 * @return The asking amount of times this ability should tick a second.
	 */
	public int getTick() {
		return tick;
	}

}
