package me.limeglass.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.manager.managers.InviteManager;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.manager.managers.RankManager;
import me.limeglass.kingdoms.manager.managers.InviteManager.PlayerInvite;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class CommandAccept extends AbstractCommand {

	public CommandAccept() {
		super(false, "accept");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Optional<PlayerInvite> optional = instance.getManager(InviteManager.class).getInvite(kingdomPlayer);
		if (!optional.isPresent()) {
			new MessageBuilder("commands.accept.no-invite")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		PlayerInvite invite = optional.get();
		invite.accepted();
		Kingdom kingdom = invite.getKingdom();
		RankManager rankManager = instance.getManager(RankManager.class);
		kingdomPlayer.setRank(rankManager.getDefaultRank());
		kingdomPlayer.setKingdom(kingdom.getName());
		kingdom.addMember(kingdomPlayer);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return null;
	}

	@Override
	public String[] getPermissionNodes() {
		return null;
	}

}
