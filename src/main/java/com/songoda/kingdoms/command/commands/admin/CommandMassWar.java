package com.songoda.kingdoms.command.commands.admin;

import org.bukkit.command.CommandSender;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.manager.managers.MasswarManager;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandMassWar extends AdminCommand {

	public CommandMassWar() {
		super(true, "masswar", "war");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		if (arguments.length <= 0)
			return ReturnType.SYNTAX_ERROR;
		String input = String.join(" ", arguments);
		MasswarManager masswarManager = instance.getManager(MasswarManager.class);
		if (input.equalsIgnoreCase("stop")) {
			masswarManager.stopMassWar();
			new MessageBuilder("commands.masswar.stopped")
					.replace("%input%", input)
					.send(sender);
			return ReturnType.SUCCESS;
		}
		long seconds = IntervalUtils.getSeconds(input);
		if (!masswarManager.isWarOn()) {
			masswarManager.startWar(seconds);
			new MessageBuilder("commands.masswar.enabled")
					.replace("%input%", input)
					.send(sender);
			return ReturnType.SUCCESS;
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "masswar";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.masswar", "kingdoms.admin"};
	}

}
