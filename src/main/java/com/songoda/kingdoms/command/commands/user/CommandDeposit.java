package com.songoda.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.external.VaultManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandDeposit extends AbstractCommand {

	public CommandDeposit() {
		super(false, "deposit", "d");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		if (arguments.length != 1)
			return ReturnType.SYNTAX_ERROR;
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Optional<VaultManager> optional = instance.getExternalManager("vault", VaultManager.class);
		if (!optional.isPresent() || !optional.get().isEnabled()) {
			new MessageBuilder("commands.deposit.no-economy")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		VaultManager vaultManager = optional.get();
		double amount = 0;
		try {
			amount = Double.parseDouble(arguments[0]);
		} catch (NumberFormatException e) {
			return ReturnType.SYNTAX_ERROR;
		}
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.deposit.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (vaultManager.getBalance(player) < amount) {
			new MessageBuilder("commands.deposit.insufficient-balance")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%amount%", amount)
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.FAILURE;
		}
		int cost = instance.getConfig().getInt("economy.resource-point-cost", 35);
    	if (amount < cost) {
    		new MessageBuilder("commands.deposit.insufficient-amount-trade")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%cost%", cost)
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.FAILURE;
    	}
		long points = Math.round(amount / cost);
		double leftover = amount - (points * cost);
		new MessageBuilder("commands.deposit.trade-successful")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%amount%", amount)
				.replace("%points%", points)
				.setKingdom(kingdom)
				.send(player);
		vaultManager.withdraw(player, amount);
		if (leftover > 0)
			vaultManager.deposit(player, leftover);
		kingdom.addResourcePoints(points);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "deposit";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.deposit", "kingdoms.player"};
	}

}
