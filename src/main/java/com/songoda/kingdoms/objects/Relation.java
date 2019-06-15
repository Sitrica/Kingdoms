package com.songoda.kingdoms.objects;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.maps.MapElement;

public enum Relation {

	ALLIANCE {
		public ChatColor getColor() {
			return fromName("alliance", ChatColor.GREEN);
		}
	},
	NEUTRAL {
		@Override
		public ChatColor getColor() {
			return fromName("neutral", ChatColor.WHITE);
		}
	},
	ENEMY {
		@Override
		public ChatColor getColor() {
			return fromName("enemy", ChatColor.RED);
		}
	},
	OWN {
		@Override
		public ChatColor getColor() {
			return fromName("own", ChatColor.YELLOW);
		}
	};

	private static ChatColor fromName(String node, ChatColor def) {
		ChatColor color = def;
		try {
			color = ChatColor.valueOf(configuration.getString("kingdoms.relations.colors." + node, "make this null"));
			return color;
		} catch (Exception e) {
			return color;
		}
	}

	public final static FileConfiguration configuration = Kingdoms.getInstance().getConfig();

	public abstract ChatColor getColor();

	public static Relation getRelation(OfflineKingdom kingdom, OfflineKingdom target) {
		if (kingdom == null || target == null)
			return NEUTRAL;
		if (kingdom.equals(target))
			return OWN;
		else if (kingdom.isEnemyWith(target))
			return ENEMY;
		else if (kingdom.isAllianceWith(target))
			return ALLIANCE;
		return NEUTRAL;
	}

	public static Relation getRelation(Land land, OfflineKingdom kingdom) {
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (optional.isPresent() && kingdom != null) {
			OfflineKingdom landKingdom = optional.get();
			if (kingdom.equals(landKingdom))
				return OWN;
			else if (kingdom.isEnemyWith(landKingdom))
				return ENEMY;
			else if (kingdom.isAllianceWith(landKingdom))
				return ALLIANCE;
		}
		return NEUTRAL;
	}

	public String getColorFor(MapElement element) {
		return element.getRelationColor(this);
	}

}
