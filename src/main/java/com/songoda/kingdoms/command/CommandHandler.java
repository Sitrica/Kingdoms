package com.songoda.kingdoms.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand.ReturnType;
import com.songoda.kingdoms.command.commands.KingdomsCommand;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class CommandHandler implements CommandExecutor {

	private final List<AbstractCommand> commands = new ArrayList<>();
	private final transient Kingdoms instance;

	public CommandHandler(Kingdoms instance) {
		this.instance = instance;
		instance.getCommand("kingdoms").setExecutor(this);
		Utils.getClassesOf(instance, instance.getPackageName() + ".command.commands", AbstractCommand.class).forEach(clazz -> {
			try {
				AbstractCommand command = clazz.newInstance();
				commands.add(command);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		for (AbstractCommand abstractCommand : commands) {
			// It's the main command
			if (arguments.length <= 0 && abstractCommand instanceof KingdomsCommand) {
				processRequirements(abstractCommand, sender, arguments);
				return true;
			} else if (arguments.length > 0 && abstractCommand.containsCommand(arguments[0])) {
				processRequirements(abstractCommand, sender, arguments);
				return true;
			}
		}
		new MessageBuilder("messages.command-doesnt-exist").send(sender);
		return true;
	}

	private void processRequirements(AbstractCommand command, CommandSender sender, String[] arguments) {
		if (!(sender instanceof Player) && !command.isConsoleAllowed()) {
			 new MessageBuilder("messages.must-be-player")
			 		.replace("%command%", command.getSyntax(sender))
			 		.setPlaceholderObject(sender)
			 		.send(sender);
			return;
		}
		if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
			String[] array = arguments;
			if (arguments.length > 0)
				array = Arrays.copyOfRange(arguments, 1, arguments.length);
			ReturnType returnType = command.runCommand(instance, sender, array);
			if (returnType == ReturnType.SYNTAX_ERROR) {
				 new MessageBuilder("messages.invalid-command", "messages.invalid-command-correction")
				 		.replace("%command%", command.getSyntax(sender))
				 		.setPlaceholderObject(sender)
				 		.send(sender);
			}
			return;
		}
		new MessageBuilder("messages.no-permission").send(sender);
	}

	public List<AbstractCommand> getCommands() {
		return Collections.unmodifiableList(commands);
	}

}
