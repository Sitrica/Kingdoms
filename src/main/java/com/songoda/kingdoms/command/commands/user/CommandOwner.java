package com.songoda.kingdoms.command.commands.user;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandOwner extends AbstractCommand {

	public CommandOwner() {
		super(false, "owner", "king", "o");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {//		//		
		Player player = (Player) sender;
		if (arguments.length > 1)
			return ReturnType.SYNTAX_ERROR;
		PlayerManager playerManager = instance.getManager(PlayerManager.class);
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.owner.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (arguments.length == 0) {
			new MessageBuilder("commands.owner.owner")
					.replace("%player%", kingdomPlayer.getPlayer().getName())
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.SUCCESS;
		}
		Optional<OfflineKingdomPlayer> owner = kingdom.getOwner();
		if (!owner.isPresent() || !owner.get().equals(kingdomPlayer)) {
			new MessageBuilder("commands.owner.not-owner")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer newOwner = Bukkit.getOfflinePlayer(arguments[0]);
		if (newOwner == null) {
			new MessageBuilder("commands.owner.not-a-valid-player")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		Optional<OfflineKingdomPlayer> member = playerManager.getOfflineKingdomPlayer(newOwner);
		if (!member.isPresent()) {
			new MessageBuilder("commands.owner.never-played")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		OfflineKingdom memberKingdom = member.get().getKingdom();
		if (memberKingdom == null) {
			new MessageBuilder("commands.owner.not-in-a-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!memberKingdom.equals(kingdom)) {
			new MessageBuilder("commands.owner.not-in-your-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		kingdom.setOwner(member.get());
		List<Rank> ranks = kingdom.getSortedRanks();
		kingdomPlayer.setRank(ranks.get(ranks.size() - 1));
		member.get().setRank(ranks.get(0));
		new MessageBuilder("commands.owner.new-owner")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%owner%", arguments[0])
				.send(kingdom.getOnlinePlayers());
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "owner";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.owner", "kingdoms.player"};
	}

}
