package com.songoda.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

import net.md_5.bungee.api.ChatColor;

public class RankManager extends Manager {

	static {
		registerManager("rank", new RankManager());
	}
	
	private final List<Rank> ranks = new ArrayList<>();
	private final ConfigurationSection section;
	
	protected RankManager() {
		super(false);
		this.section = configuration.getConfigurationSection("ranks");
		for (String rank : section.getKeys(false)) {
			ChatColor chat = ChatColor.valueOf(section.getString(rank + ".chat-color", "WHITE"));
			ChatColor color = ChatColor.valueOf(section.getString(rank + ".color", "WHITE"));
			String unicode = section.getString(rank + ".unicode-icon", "");
			int priority = section.getInt(rank + ".priority", 99);
			String name = section.getString(rank + ".name", rank);
			String node = "ranks." + rank;
			ranks.add(new Rank(rank, node, name, unicode, chat, color, priority));
		}
	}
	
	public class Rank {
		
		private final String name, unicode, node, configurationName;
		private final ChatColor color, chat;
		private final int priority;
		
		private Rank(String configurationName, String node, String name, String unicode, ChatColor chat, ChatColor color, int priority) {
			this.configurationName = configurationName;
			this.priority = priority;
			this.unicode = unicode;
			this.color = color;
			this.chat = chat;
			this.node = node;
			this.name = name;
		}
		
		public String getPrefix(KingdomPlayer player) {
			return new MessageBuilder(node + ".prefix").fromConfiguration(configuration)
					.replace("%player%", player.getPlayer().getName())
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
		
		public ChatColor getColor() {
			return color;
		}
		
		public String getUnicode() {
			return unicode;
		}
		
		public ChatColor getChat() {
			return chat;
		}
		
		public int getPriority() {
			return priority;
		}
		
		public String getName() {
			return name;
		}
		
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
	
	public Rank getDefaultRank() {
		return getSortedOrder().get(ranks.size());
	}
	
	public Rank getOwnerRank() {
		return getSortedOrder().get(0);
	}

	@Override
	public void onDisable() {
		ranks.clear();
	}

}
