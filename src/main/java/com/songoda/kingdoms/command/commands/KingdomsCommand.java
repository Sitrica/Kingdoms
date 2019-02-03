package com.songoda.kingdoms.command.commands;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.command.CommandSender;

public class KingdomsCommand extends AbstractCommand {
	
	static {
		registerCommand(new KingdomsCommand());
	}
	
	public KingdomsCommand() {
		super("kingdoms", null, false);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... args) {
		sender.sendMessage("");
		new MessageBuilder("messages.version")
				.replace("%version%", instance.getDescription().getVersion())
				.send(sender);
		for (AbstractCommand command : instance.getCommandHandler().getCommands()) {
			if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
				sender.sendMessage(Formatting.color("&8 - &6" + command.getSyntax() + "&7 - " + command.getDescription()));
			}
		}
		sender.sendMessage("");
		return ReturnType.SUCCESS;
	}

	@Override
	public String getPermissionNode() {
		return null;
	}

	@Override
	public String getDescription() {
		return "display the main page.";
	}

	@Override
	public String getSyntax() {
		return "/k";
	}

}
