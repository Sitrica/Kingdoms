package com.songoda.kingdoms.utils;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AsciiCompass {

	public enum Point {

		N('N'),
		NE('/'),
		E('E'),
		SE('\\'),
		S('S'),
		SW('/'),
		W('W'),
		NW('\\');

		public final char character;

		private Point(final char character) {
			this.character = character;
		}

		public Point reverseDirection() {
			switch(this) {
				case E:
					return W;
				case N:
					return S;
				case NE:
					return SW;
				case NW:
					return SE;
				case S:
					return N;
				case SE:
					return NW;
				case SW:
					return NE;
				case W:
					return E;
				}
			return null;
		}

		@Override
		public String toString() {
			return String.valueOf(character);
		}

		public String toString(boolean active, ChatColor color, String colorDefault) {
			return (active ? color : colorDefault)+ toString();
		}

	}

	public static Point getCompassDirection(double degrees) {
		degrees = (degrees - 180) % 360;
		if (degrees < 0)
			degrees += 360;
		if (0 <= degrees && degrees < 22.5)
			return Point.S;
		else if (22.5 <= degrees && degrees < 67.5)
			return Point.SE;
		else if (67.5 <= degrees && degrees < 112.5)
			return Point.E;
		else if (112.5 <= degrees && degrees < 157.5)
			return Point.NE;
		else if (157.5 <= degrees && degrees < 202.5)
			return Point.N;
		else if (202.5 <= degrees && degrees < 247.5)
			return Point.SW;
		else if (247.5 <= degrees && degrees < 292.5)
			return Point.W;
		else if (292.5 <= degrees && degrees < 337.5)
			return Point.NW;
		else if (337.5 <= degrees && degrees < 360.0)
			return Point.S;
		return null;
	}

	public static Point getCardinalDirection(Player player) {
		double rotation = (player.getLocation().getYaw() - 90) % 360;
		if (rotation < 0)
			rotation += 360.0;
		if (0 <= rotation && rotation < 22.5)
			return Point.W;
		else if (22.5 <= rotation && rotation < 67.5)
			return Point.NW;
		else if (67.5 <= rotation && rotation < 112.5)
			return Point.N;
		else if (112.5 <= rotation && rotation < 157.5)
			return Point.NE;
		else if (157.5 <= rotation && rotation < 202.5)
			return Point.E;
		else if (202.5 <= rotation && rotation < 247.5)
			return Point.SE;
		else if (247.5 <= rotation && rotation < 292.5)
			return Point.S;
		else if (292.5 <= rotation && rotation < 337.5)
			return Point.SW;
		else if (337.5 <= rotation && rotation < 360.0)
			return Point.W;
		return null;
	}

	public static List<String> getAsciiCompass(Point point, ChatColor color, String colorDefault) {
		List<String> list = new ArrayList<>();
		String row = "";
		row += Point.NW.toString(Point.NW == point, color, colorDefault);
		row += Point.W.toString(Point.W == point, color, colorDefault);
		row += Point.SW.toString(Point.NE == point, color, colorDefault);
		list.add(row);

		row = "";
		row += Point.N.toString(Point.N == point, color, colorDefault);
		row += colorDefault+"+";
		row += Point.S.toString(Point.S == point, color, colorDefault);
		list.add(row);

		row = "";
		row += Point.NE.toString(Point.SW == point, color, colorDefault);
		row += Point.E.toString(Point.E == point, color, colorDefault);
		row += Point.SE.toString(Point.SE == point, color, colorDefault);
		list.add(row);
		return list;
	}

	public static List<String> getAsciiCompass(double degrees, ChatColor color, String colorDefault) {   
		return getAsciiCompass(getCompassDirection(degrees), color, colorDefault);
	}

}
