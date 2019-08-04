package me.limeglass.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import me.limeglass.kingdoms.manager.Manager;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.IntervalUtils;

public class InviteManager extends Manager {

	private final static Set<PlayerInvite> invites = new HashSet<>();
	private final long expiration;

	public InviteManager() {
		super(false);
		expiration = IntervalUtils.getInterval(configuration.getString("plugin.invite-expiration", "%10 minutes%"));
	}

	private interface Invite {}

	public class PlayerInvite implements Invite {

		private final KingdomPlayer player;
		private final Kingdom kingdom;
		private final BukkitTask task;

		public PlayerInvite(KingdomPlayer player, Kingdom kingdom) {
			this.kingdom = kingdom;
			this.player = player;
			this.task = Bukkit.getScheduler().runTaskLater(instance, () -> invites.remove(this), expiration);
		}

		public void accepted() {
			invites.remove(this);
			cancel();
		}

		public KingdomPlayer getWho() {
			return player;
		}

		public Kingdom getKingdom() {
			return kingdom;
		}

		public void cancel() {
			task.cancel();
		}

	}

	public Optional<PlayerInvite> getInvite(Kingdom kingdom) {
		return invites.parallelStream()
				.filter(invite -> invite.getKingdom().equals(kingdom))
				.findFirst();
	}

	public Optional<PlayerInvite> getInvite(KingdomPlayer player) {
		return invites.parallelStream()
				.filter(invite -> invite.getWho().equals(player))
				.findFirst();
	}

	public boolean addInvite(KingdomPlayer player, Kingdom kingdom) {
		if (getInvite(player).isPresent())
			return true;
		PlayerInvite invite = new PlayerInvite(player, kingdom);
		invites.add(invite);
		return false;
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {
		invites.clear();
	}

}
