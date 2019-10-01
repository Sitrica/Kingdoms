package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.SimplePlaceholder;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;

public class ChatManager extends Manager {

	private final FileConfiguration ranks;

	public ChatManager() {
		super(true);
		this.ranks = instance.getConfiguration("ranks").get();
	}

	public enum ChatChannel {

		KINGDOM,
		PUBLIC,
		ALLY;

		public String getName() {
			return new MessageBuilder(false, "commands.chat." + name().toLowerCase()).get();
		}

		public static ChatChannel get(String string) {
			if (string.equalsIgnoreCase("kingdom") || string.equalsIgnoreCase("k"))
				return KINGDOM;
			if (string.equalsIgnoreCase("public") || string.equalsIgnoreCase("p"))
				return PUBLIC;
			if (string.equalsIgnoreCase("ally") || string.equalsIgnoreCase("a"))
				return ALLY;
			return PUBLIC;
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (!instance.getManager(WorldManager.class).acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		Set<KingdomPlayer> senders = new HashSet<>();
		String message = event.getMessage();
		switch (kingdomPlayer.getChatChannel()) {
			case ALLY:
				senders.addAll(kingdom.getOnlineAllies());
			case KINGDOM:
				event.setCancelled(true);
				senders.addAll(kingdom.getOnlinePlayers());
				senders.forEach(k -> k.getPlayer().sendMessage(format(k, message)));
				break;
			case PUBLIC:
				if (ranks.getBoolean("format-public-chat", false))
					event.setFormat(format(kingdomPlayer, message));
				break;
		}
	}

	private String format(KingdomPlayer kingdomPlayer, String message) {
		return format(false, kingdomPlayer, message) + format(true, kingdomPlayer, message);
	}

	private String format(boolean chat, KingdomPlayer kingdomPlayer, String message) {
		color : if (ranks.getBoolean("color-symbols.enabled", true)) {
			if (ranks.getBoolean("color-symbols.kingdom-only", false) && kingdomPlayer.getChatChannel() != ChatChannel.KINGDOM)
				break color;
			if (!ranks.getBoolean("color-symbols.permission-required", true))
				message = Formatting.color(message);
			else if (kingdomPlayer.getPlayer().hasPermission(ranks.getString("color-symbols.permission", "kingdoms.chatcolor")))
				message = Formatting.color(message);
		}
		Rank rank = kingdomPlayer.getRank();
		MessageBuilder builder = new MessageBuilder(false, "prefix-format")
				.replace("%chatcolor%", rank == null ? "&7" : rank.getChatColor().toString())
				.replace("%prefix%", rank == null ? "" : rank.getPrefix(kingdomPlayer))
				.replace("%color%", rank == null ? "" : rank.getColor().toString())
				.replace("%message%", message)
				.withPlaceholder(kingdomPlayer, new SimplePlaceholder("%player%") {
					@Override
					public String get() {
						if (ranks.getBoolean("use-display-name", false))
							return kingdomPlayer.getPlayer().getDisplayName();
						else
							return kingdomPlayer.getName();
					}
				})
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(ranks);
		if (chat)
			builder.setNodes("message-format");
		return builder.get();
	}

	@Override
	public void onDisable() {}

	@Override
	public void initalize() {}

}
