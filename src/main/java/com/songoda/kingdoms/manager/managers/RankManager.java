package com.songoda.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.RankPermissions;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class RankManager extends Manager {

	private final List<Rank> ranks = new ArrayList<>();
	private final FileConfiguration rankConfiguration;
	private final ConfigurationSection section;

	public RankManager() {
		super("rank", false);
		rankConfiguration = instance.getConfiguration("ranks").get();
		this.section = rankConfiguration.getConfigurationSection("ranks");
		for (String rank : section.getKeys(false)) {
			Material material = Utils.materialAttempt(section.getString(rank + ".permission-editor-item", "STONE"), "WHITE_WOOL");
			ChatColor chat = ChatColor.valueOf(section.getString(rank + ".chat-color", "WHITE"));
			ChatColor color = ChatColor.valueOf(section.getString(rank + ".color", "WHITE"));
			String unicode = section.getString(rank + ".unicode-icon", "");
			int priority = section.getInt(rank + ".priority", 99);
			String name = section.getString(rank + ".name", rank);
			String node = "ranks." + rank;
			ranks.add(new Rank(rank, node, name, unicode, chat, color, priority, material));
		}
	}

	public class Rank {

		private final String name, unicode, node, configurationName;
		private final ChatColor color, chat;
		private final Material material;
		private final int priority;

		private Rank(String configurationName, String node, String name, String unicode, ChatColor chat, ChatColor color, int priority, Material material) {
			this.configurationName = configurationName;
			this.material = material;
			this.priority = priority;
			this.unicode = unicode;
			this.color = color;
			this.chat = chat;
			this.node = node;
			this.name = name;
		}

		public String getPrefix(KingdomPlayer player) {
			return new MessageBuilder(false, node + ".prefix")
					.replace("%player%", player.getPlayer().getName())
					.fromConfiguration(rankConfiguration)
					.setKingdom(player.getKingdom())
					.replace("%priority%", priority)
					.replace("%unicode%", unicode)
					.replace("%icon%", unicode)
					.replace("%name%", name)
					.get();
		}

		public boolean isHigherOrEqual(Rank target) {
			if (this.priority <= target.priority)
				return true;
			return false;
		}

		public boolean isHigher(Rank target) {
			if (this.priority < target.priority)
				return true;
			return false;
		}

		public String getConfigurationName() {
			return configurationName;
		}

		public String getConfigurationNode() {
			return node;
		}

		public Material getEditMaterial() {
			return material;
		}

		public ChatColor getChatColor() {
			return chat;
		}

		public ChatColor getColor() {
			return color;
		}

		public String getUnicode() {
			return unicode;
		}

		public int getPriority() {
			return priority;
		}

		public String getName() {
			return name;
		}

	}

	public Rank getLowestAndDefault(OfflineKingdom kingdom, Predicate<RankPermissions> predicate) {
		return getLowestFor(kingdom, predicate).orElse(getDefaultRank());
	}

	public Optional<Rank> getLowestFor(OfflineKingdom kingdom, Predicate<RankPermissions> predicate) {
		List<Rank> sorted = getSortedOrder();
		Collections.reverse(sorted);
		Iterator<Rank> iterator = sorted.iterator();
		while (iterator.hasNext()) {
			Rank rank = iterator.next();
			if (predicate.test(kingdom.getPermissions(rank)))
				return Optional.of(rank);
		}
		return Optional.empty();
	}

	public Optional<Rank> getRankByColor(ChatColor color) {
		return ranks.parallelStream()
				.filter(rank -> rank.getColor() == color)
				.findFirst();
	}

	public Optional<Rank> getRankByPriority(int priority) {
		return ranks.parallelStream()
				.filter(rank -> rank.getPriority() == priority)
				.findFirst();
	}

	public Optional<Rank> getRank(String name) {
		return ranks.parallelStream()
				.filter(rank -> rank.getConfigurationName().equals(name))
				.findFirst();
	}

	public List<Rank> getSortedOrder() {
		return ranks.parallelStream()
				.sorted(Comparator.comparing(Rank::getPriority))
				.collect(Collectors.toList());
	}

	public List<Rank> getRanksAbove(Rank rank) {
		return ranks.parallelStream()
				.filter(spot -> ranks.indexOf(spot) < ranks.indexOf(rank))
				.collect(Collectors.toList());
	}

	public List<Rank> getRanksBelow(Rank rank) {
		return ranks.parallelStream()
				.filter(spot -> ranks.indexOf(spot) > ranks.indexOf(rank))
				.collect(Collectors.toList());
	}

	/**
	 * Only used for chat formatting, displaying in the /k info
	 * 
	 * @param players The Collection to format.
	 * @return The formatted collection.
	 */
	public String list(Collection<OfflineKingdomPlayer> players) {
		StringBuilder builder = new StringBuilder();
		players.parallelStream()
				.map(player -> {
					//INFO Reminder: Unicodes reset the format.
					Rank rank = player.getRank();
					String format = rank.getChatColor() + rank.getUnicode() + player.getName();
					if (rankConfiguration.isConfigurationSection("list-offline-online-colors")) {
						ConfigurationSection section = rankConfiguration.getConfigurationSection("list-offline-online-colors");
						if (section.getBoolean("enabled", false)) {
							if (player.isOnline()) {
								ChatColor color = ChatColor.valueOf(section.getString("online", "GREEN"));
								format = color + rank.getUnicode() + color + player.getName();
							} else {
								ChatColor color = ChatColor.valueOf(section.getString("offline", "GRAY"));
								format = color + rank.getUnicode() + color + player.getName();
							}
						} else if (player.isOnline())
							format = rank.getChatColor() + rank.getUnicode() + rank.getChatColor() + ChatColor.BOLD + player.getName();
					} else if (player.isOnline())
						format = rank.getChatColor() + rank.getUnicode() + rank.getChatColor() + ChatColor.BOLD + player.getName();
					return Formatting.color(format);
				})
				.forEach(line -> {
					builder.append(line);
					builder.append(", ");
				});
		String string = builder.toString();
		return string.substring(0, string.lastIndexOf(", "));
	}

	public Rank getDefaultRank() {
		return getSortedOrder().get(ranks.size() - 1);
	}

	public List<Rank> getRanks() {
		return getSortedOrder();
	}

	public Rank getOwnerRank() {
		return getSortedOrder().get(0);
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
