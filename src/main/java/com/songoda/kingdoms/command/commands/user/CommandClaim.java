package com.songoda.kingdoms.command.commands.user;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandClaim extends AbstractCommand {

	private final PlayerManager playerManager;

	public CommandClaim() {
		super(false, "claim", "c");
		playerManager = instance.getManager(PlayerManager.class);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		LandManager landManager = instance.getManager(LandManager.class);
		if (arguments.length <= 1) {
			landManager.playerClaimLand(kingdomPlayer, landManager.getLand(kingdomPlayer.getChunkAt()));
			if (arguments.length == 0)
				return ReturnType.SUCCESS;
			if (arguments[0].equalsIgnoreCase("auto") || arguments[0].equalsIgnoreCase("automatic")) {
				kingdomPlayer.setAutoClaiming(!kingdomPlayer.isAutoClaiming());
				if (kingdomPlayer.isAutoClaiming()) {
					new MessageBuilder("commands.claim.auto-claim-on")
							.setPlaceholderObject(kingdomPlayer)
							.send();
				} else {
					new MessageBuilder("commands.claim.auto-claim-off")
							.setPlaceholderObject(kingdomPlayer)
							.send();
				}
			}
		} else {
			int x = Integer.parseInt(arguments[0]);
			int z = Integer.parseInt(arguments[1]);
			Chunk chunk = player.getWorld().getChunkAt(x, z);
			Land land = landManager.getLand(chunk);
			landManager.playerClaimLand(kingdomPlayer, land);
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "claim";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.claim";
	}

}
