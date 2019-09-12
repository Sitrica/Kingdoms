package com.songoda.kingdoms.command.commands.admin;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.events.MemberJoinEvent;
import com.songoda.kingdoms.events.MemberLeaveEvent;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandJoin extends AdminCommand {

	public CommandJoin() {
		super(true, "join", "force", "j");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		if (arguments.length < 2)
			return ReturnType.SYNTAX_ERROR;
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(arguments[0]);
		if (player == null) {
			new MessageBuilder("commands.join.no-player-found")
					.replace("%input%", arguments[0])
					.send(sender);
			return ReturnType.FAILURE;
		}
		Optional<OfflineKingdomPlayer> optional = instance.getManager(PlayerManager.class).getOfflineKingdomPlayer(player);
		if (!optional.isPresent()) {
			new MessageBuilder("commands.join.never-played")
					.replace("%input%", arguments[0])
					.send(sender);
			return ReturnType.FAILURE;
		}
		OfflineKingdomPlayer offlineKingdomPlayer = optional.get();
		String name = String.join(" ", Arrays.copyOfRange(arguments, 1, arguments.length));
		KingdomManager kingdomManager = instance.getManager(KingdomManager.class);
		Optional<OfflineKingdom> kingdom = kingdomManager.getOfflineKingdom(name);
		if (!kingdom.isPresent()) {
			new MessageBuilder("commands.join.no-kingdom-found")
					.setPlaceholderObject(offlineKingdomPlayer)
					.replace("%kingdom%", name)
					.send(sender);
			return ReturnType.FAILURE;
		}
		MemberLeaveEvent leaveEvent = new MemberLeaveEvent(offlineKingdomPlayer, offlineKingdomPlayer.getKingdom());
		Bukkit.getPluginManager().callEvent(leaveEvent);
		kingdom.get().addMember(offlineKingdomPlayer);
		offlineKingdomPlayer.setKingdom(kingdom.get().getName());
		MemberJoinEvent joinEvent = new MemberJoinEvent(offlineKingdomPlayer, offlineKingdomPlayer.getKingdom());
		Bukkit.getPluginManager().callEvent(joinEvent);
		Optional<KingdomPlayer> kingdomPlayer = offlineKingdomPlayer.getKingdomPlayer();
		if (kingdomPlayer.isPresent() && instance.getConfig().getBoolean("kingdoms.admin-forcejoin-message", true))
			new MessageBuilder("commands.join.joined")
					.setPlaceholderObject(kingdomPlayer.get())
					.replace("%kingdom%", name)
					.send(kingdomPlayer.get());
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "join";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.join", "kingdoms.admin"};
	}

}
