package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Set;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager extends Manager {

	private final FileConfiguration ranks;
	private PlayerManager playerManager;
	private WorldManager worldManager;

	public ChatManager() {
		super("chat", true);
		this.ranks = instance.getConfiguration("ranks").get();
	}

	@Override
	public void initalize() {
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
	}

	public enum ChatChannel {
		KINGDOM,
		PUBLIC,
		ALLY;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (!kingdomPlayer.hasKingdom())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		Set<KingdomPlayer> senders = new HashSet<>();
		String message = event.getMessage();
		switch (kingdomPlayer.getChatChannel()) {
			case ALLY:
				senders.addAll(kingdom.getOnlineAllies());
			case KINGDOM:
				senders.addAll(kingdom.getOnlinePlayers());
				senders.forEach(k -> k.getPlayer().sendMessage(format(k, message)));
				break;
			case PUBLIC:
				if (ranks.getBoolean("format-public-chat", false)) {
					event.setFormat(format(false, kingdomPlayer, message));
					event.setMessage(format(true, kingdomPlayer, message));
				}
				break;
		}
	}

	private String format(KingdomPlayer kingdomPlayer, String message) {
		return Formatting.color(format(false, kingdomPlayer, message) + format(true, kingdomPlayer, message));
	}

	private String format(boolean chat, KingdomPlayer kingdomPlayer, String message) {
		Rank rank = kingdomPlayer.getRank();
		MessageBuilder builder = new MessageBuilder(false, "prefix-format")
				.replace("%prefix%", rank.getPrefix(kingdomPlayer))
				.replace("%chatcolor%", rank.getChatColor() + "")
				.replace("%color%", rank.getColor() + "")
				.replace("%message%", message)
				.withPlaceholder(kingdomPlayer, new SimplePlaceholder("%player%") {
					@Override
					public String get() {
						if (ranks.getBoolean("use-display-name", false))
							return kingdomPlayer.getPlayer().getDisplayName();
						else
							return kingdomPlayer.getName();
					}
				}).fromConfiguration(ranks);
		if (chat)
			builder.setNodes("message-format");
		return builder.get();
	}

	@Override
	public void onDisable() {}

}
