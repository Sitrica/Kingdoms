package com.songoda.kingdoms.command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class CommandHandler implements CommandExecutor {

	private Set<AbstractCommand> commands = new HashSet<>();
	private final transient Kingdoms instance;

	public CommandHandler(Kingdoms instance) {
		this.instance = instance;
		instance.getCommand("Kingdoms").setExecutor(this);
		Utils.loadClasses(instance, instance.getPackageName() + ".commands", "commands");
	}

	protected void registerCommand(AbstractCommand abstractCommand) {
		commands.add(abstractCommand);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
		for (AbstractCommand abstractCommand : commands) {
			if (abstractCommand.getCommand().equalsIgnoreCase(command.getName())) {
				if (strings.length == 0) {
					processRequirements(abstractCommand, sender, strings);
					return true;
				}
			} else if (strings.length != 0 && abstractCommand.getParent() != null && abstractCommand.getParent().getCommand().equalsIgnoreCase(command.getName())) {
				String cmd = strings[0];
				if (cmd.equalsIgnoreCase(abstractCommand.getCommand())) {
					processRequirements(abstractCommand, sender, strings);
					return true;
				}
			}
		}
		new MessageBuilder("messages.command-doesnt-exist").send(sender);
		return true;
	}

	private void processRequirements(AbstractCommand command, CommandSender sender, String[] strings) {
		if (!(sender instanceof Player) && !command.allowConsole()) {
			sender.sendMessage("You must be a player to use this command.");
			return;
		}
		if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
			AbstractCommand.ReturnType returnType = command.runCommand(instance, sender, strings);
			if (returnType == AbstractCommand.ReturnType.SYNTAX_ERROR) {
				 new MessageBuilder("messages.invalid-command", "messages.invalid-command-correction")
				 		.replace("%command%", command.getSyntax())
				 		.setPlaceholderObject(sender)
				 		.send(sender);
			}
			return;
		}
		new MessageBuilder("messages.no-permission").send(sender);
	}

	public Set<AbstractCommand> getCommands() {
		return Collections.unmodifiableSet(commands);
	}

}
