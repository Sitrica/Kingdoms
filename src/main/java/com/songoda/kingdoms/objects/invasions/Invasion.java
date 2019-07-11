package com.songoda.kingdoms.objects.invasions;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.InvadingManager;
import com.songoda.kingdoms.manager.managers.LandManager.LandInfo;
import com.songoda.kingdoms.objects.invasions.InvasionMechanic.StopReason;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class Invasion {

	// Maps the lands to KingdomPlayers currently invading those lands.
	private final ListMultimap<LandInfo, KingdomPlayer> invading = MultimapBuilder.hashKeys().arrayListValues().build();
	private final long start = System.currentTimeMillis();
	private long deathTime = System.currentTimeMillis();
	private final KingdomPlayer instigator;
	private final OfflineKingdom target;
	private boolean complete, won;
	private int deaths = 0;

	public Invasion(KingdomPlayer instigator, OfflineKingdom target) {
		this.instigator = instigator;
		this.target = target;
	}

	public OfflineKingdom getTarget() {
		return target;
	}

	/**
	 * @return The KingdomPlayer that initiated this Invasion.
	 */
	public KingdomPlayer getInstigator() {
		return instigator;
	}

	/**
	 * @return The time this invasion started.
	 */
	public long getStartingTime() {
		return start;
	}

	public Kingdom getAttacking() {
		return instigator.getKingdom();
	}

	public Multiset<LandInfo> getInvadingLands() {
		return invading.keys();
	}

	/**
	 * Add an invading land, and the KingdomPlayer in-charge of invading that land.
	 * 
	 * @param info The LandInfo to keep invading on.
	 * @param responsible The KingdomPlayer responsible for the invasion of this Land.
	 */
	public void addInvading(LandInfo info, KingdomPlayer responsible) {
		invading.put(info, responsible);
	}

	public void removeInvading(LandInfo info, KingdomPlayer kingdomPlayer) {
		invading.remove(info, kingdomPlayer);
	}

	public void removeInvading(Collection<KingdomPlayer> collection) {
		collection.forEach(kingdomPlayer -> removeInvading(kingdomPlayer));
	}

	public void removeInvading(KingdomPlayer kingdomPlayer) {
		Iterables.removeIf(invading.entries(), invasion -> invasion.getValue().equals(kingdomPlayer));
	}

	public void removeInvading(LandInfo info) {
		invading.removeAll(info);
	}

	/**
	 * @return The last time the defending Kingdom had a death.
	 */
	public long getLastDeathTime() {
		return deathTime;
	}

	/**
	 * Used to be called when a member on the defending Kingdom dies.
	 */
	public void resetDeathTime() {
		deathTime = System.currentTimeMillis();
	}

	/**
	 * Finds the opponent of the other.
	 * 
	 * @param kingdom The OfflineKingdom to search for the opponent on.
	 * @return The opponent if found.
	 */
	public Optional<OfflineKingdom> getOpponentTo(OfflineKingdom kingdom) {
		OfflineKingdom attacking = getAttacking();
		if (!kingdom.equals(attacking) && !kingdom.equals(target))
			return Optional.empty();
		return kingdom.equals(target) ? Optional.of(attacking) : Optional.of(target);
	}

	/**
	 * @return All KingdomPlayers involved in the invasion.
	 */
	public Set<KingdomPlayer> getInvolved() {
		Set<KingdomPlayer> players = Sets.newHashSet(getAttacking().getOnlinePlayers());
		if (target.isOnline())
			players.addAll(target.getKingdom().getOnlinePlayers());
		return players;
	}

	/**
	 * @return true if this mechanic states this invasion is complete.
	 */
	public boolean isCompleted() {
		return complete;
	}

	public int getDeathCount() {
		return deaths;
	}

	public void addDeath() {
		deaths++;
	}

	/**
	 * Set the complete state of this mechanic.
	 * <p>
	 * True states this mechanic is finished.
	 * 
	 * @param defeated
	 */
	public void setCompleted(boolean complete) {
		this.complete = complete;
	}

	/**
	 * @return true if this mechanic states the invading Kingdom won.
	 */
	public boolean hasWon() {
		return won;
	}

	/**
	 * Set the winner state of this mechanic.
	 * <p>
	 * True states this mechanic was won by the invading Kingdom.
	 * 
	 * @param defeated
	 */
	public void setWon(boolean won) {
		this.won = won;
	}

	/**
	 * Uses an OfflineKingdom object to define the winner and complete this invasion.
	 * Used in the surrender command.
	 * 
	 * @param kingdom The OfflineKingdom to award a winner.
	 */
	public void winner(OfflineKingdom kingdom) {
		if (!kingdom.equals(target) && !getAttacking().equals(kingdom))
			return;
		setWon(!kingdom.equals(target));
		StopReason reason = hasWon() ? StopReason.WIN : StopReason.DEFENDED;
		Kingdoms.getInstance().getManager(InvadingManager.class).getInvasionMechanic().onInvasionStop(reason, this);
	}

	/**
	 * Successfully completes this invasion, make sure to set the win and completed state before calling this.
	 */
	public void finish(boolean force) {
		if (force)
			complete = true;
		Validate.isTrue(complete, "An invasion called finish() but was not marked as completed.");
		StopReason reason = force ? StopReason.STOPPED : StopReason.get(won);
		Kingdoms.getInstance().getManager(InvadingManager.class).getInvasionMechanic().onInvasionStop(reason, this);
	}

}
