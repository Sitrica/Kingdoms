package com.songoda.kingdoms.command;

import org.bukkit.command.CommandSender;

import com.songoda.kingdoms.Kingdoms;

public abstract class AbstractCommand {

	private final AbstractCommand parent;
	private final boolean noConsole;
	private final String command;

	protected AbstractCommand(String command, AbstractCommand parent, boolean noConsole) {
		this.noConsole = noConsole;
		this.command = command;
		this.parent = parent;
	}
	
	protected static void registerCommand(AbstractCommand command) {
		Kingdoms.getInstance().getCommandHandler().registerCommand(command);
	}
	
	public enum ReturnType {
		SUCCESS,
		FAILURE,
		SYNTAX_ERROR
	}

	public AbstractCommand getParent() {
		return parent;
	}
	
	public boolean allowConsole() {
		return noConsole;
	}

	public String getCommand() {
		return command;
	}

	protected abstract ReturnType runCommand(Kingdoms instance, CommandSender sender, String... args);

	public abstract String getPermissionNode();

	public abstract String getDescription();
	
	public abstract String getSyntax();

}
