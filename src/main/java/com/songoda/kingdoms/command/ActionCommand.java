package com.songoda.kingdoms.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.exceptions.KingdomsAPIException;
import com.songoda.kingdoms.objects.maps.ActionInfo;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class ActionCommand implements CommandExecutor {

	private final Map<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();
	private final Map<UUID, ActionInfo> waiting = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;
		UUID uuid = player.getUniqueId();
		Optional<ActionInfo> action = Optional.ofNullable(waiting.get(uuid));
		if (!action.isPresent())
			return false;
		ActionInfo info = action.get();
		if (!uuid.equals(info.getUniqueId()))
			throw new KingdomsAPIException("The UUID of the executor did not match that of the ActionInfo, wrong match?");
		info.execute();
		return true;
	}

	public void add(KingdomPlayer kingdomPlayer, ActionInfo action) {
		UUID uuid = kingdomPlayer.getUniqueId();
		waiting.put(uuid, action);
		tasks.compute(uuid, (unique, oldTask) -> {
			BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(Kingdoms.getInstance(), () -> {
				waiting.remove(uuid);
				tasks.remove(uuid);
			}, 120 * 20); // 2 minutes.
			if (oldTask != null) {
				oldTask.cancel();
				tasks.remove(unique);
			}
			return task;
		});
	}

}
