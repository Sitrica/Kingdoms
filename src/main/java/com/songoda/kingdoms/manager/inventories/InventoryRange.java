package com.songoda.kingdoms.manager.inventories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import com.songoda.kingdoms.Kingdoms;

public class InventoryRange {

	private final List<Integer> slots = new ArrayList<>();
	private final ConfigurationSection section;
	private final int size;

	public InventoryRange(int size, ConfigurationSection section) {
		Set<Integer> filler = new HashSet<>();
		if (section != null) {
			for (String number : section.getStringList("filler-range")) {
				if (number.contains("-")) {
					try {
						String[] split = number.split("-");
						if (split.length < 2)
							continue;
						int start = Integer.parseInt(split[0]);
						if (split[1].equalsIgnoreCase("end"))
							split[1] = size + "";
						int end = Integer.parseInt(split[1]);
						if (end < start)
							start = end;
						if (end <= 0 || start < 0)
							continue;
						for (int i = start; i <= end; i++)
							filler.add(i);
					} catch (NumberFormatException e) {
						Kingdoms.consoleMessage(number + " is an invalid format for filler range.");
					}
					continue;
				}
				try {
					int i = Integer.parseInt(number);
					filler.add(i);
				} catch (NumberFormatException e) {
					Kingdoms.consoleMessage(number + " is an invalid format for filler range.");
				}
			}
			int spacing = section.getInt("spacing", -1);
			for (int i = 0; i < size; i++) {
				if (!filler.contains(i)) {
					if (spacing > 0 && i > 0 && i % spacing > 0)
						continue;
					slots.add(i);
				}
			}
		}
		this.section = section;
		this.size = size;
	}

	public ConfigurationSection getSection() {
		return section;
	}

	public boolean contains(int slot) {
		return slots.contains(slot);
	}

	public List<Integer> getSlots() {
		return Collections.unmodifiableList(slots);
	}

	public InventoryRange clone() {
		return new InventoryRange(size, section);
	}

}
