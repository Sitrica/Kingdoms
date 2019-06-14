package com.songoda.kingdoms.manager.inventories;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public abstract class KingdomInventory {

	protected final FileConfiguration configuration, inventories;
	protected final Set<SlotAction> actions = new HashSet<>();
	protected final ConfigurationSection section;
	protected final Kingdoms instance;
	private final InventoryType type;
	protected InventoryRange range;
	protected final int size;

	public KingdomInventory(InventoryType type, String path, int size) {
		this(type, path, null, size);
	}

	public KingdomInventory(InventoryType type, String path, ConfigurationSection section, int size) {
		this.size = size;
		this.type = type;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.inventories = instance.getConfiguration("inventories").get();
		this.section = section == null ? inventories.getConfigurationSection("inventories." + path) : section;
		this.range = new InventoryRange(size, this.section);
	}

	public void open(KingdomPlayer kingdomPlayer) {
		close(kingdomPlayer.getUniqueId());
		Inventory inventory = createInventory(kingdomPlayer);
		Inventory built = build(inventory, kingdomPlayer);
		openInventory(built, kingdomPlayer.getPlayer());
	}

	protected void openInventory(Inventory inventory, Player player) {
		player.openInventory(inventory);
		instance.getManager(InventoryManager.class).opening(player.getUniqueId(), this);
	}

	protected Inventory createInventory(KingdomPlayer kingdomPlayer) {
		String title = new MessageBuilder(false, "title")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(section)
				.get();
		Inventory inventory;
		if (type == InventoryType.CHEST)
			inventory = instance.getServer().createInventory(null, size, title);
		else
			inventory = instance.getServer().createInventory(null, type, title);
		if (section.getBoolean("use-filler", false)) {
			ItemStack filler = new ItemStackBuilder(section.getConfigurationSection("filler"))
					.setPlaceholderObject(kingdomPlayer)
					.build();
			for (int i = 0; i < inventory.getSize(); i++) {
				if (range.contains(i))
					continue;
				inventory.setItem(i, filler);
			}
		}
		return inventory;
	}

	protected abstract Inventory build(Inventory inventory, KingdomPlayer kingdomPlayer);

	protected void setAction(UUID uuid, int slot, Consumer<InventoryClickEvent> consummer) {
		actions.add(new SlotAction(uuid, slot, consummer));
	}

	protected void setAction(Inventory inventory, UUID uuid, int slot, Consumer<InventoryClickEvent> consummer) {
		actions.add(new SlotAction(inventory, uuid, slot, consummer));
	}

	public Optional<SlotAction> getAction(Inventory inventory, UUID uuid, int slot) {
		return actions.parallelStream()
				.filter(action -> {
					Optional<Inventory> optional = action.getInventory();
					if (optional.isPresent())
						return optional.get().equals(inventory);
					return true;
				})
				.filter(action -> action.getSlot() == slot)
				.filter(action -> action.isFor(uuid))
				.findFirst();
	}

	protected void reopen(KingdomPlayer kingdomPlayer) {
		open(kingdomPlayer);
	}

	public void close(UUID uuid) {
		actions.removeIf(action -> action.getUniqueId().equals(uuid));
	}

	public class SlotAction {

		private final Consumer<InventoryClickEvent> consumer;
		private Inventory inventory;
		private final UUID uuid;
		private final int slot;

		public SlotAction(UUID uuid, int slot, Consumer<InventoryClickEvent> consumer) {
			this.consumer = consumer;
			this.uuid = uuid;
			this.slot = slot;
		}

		public SlotAction(Inventory inventory, UUID uuid, int slot, Consumer<InventoryClickEvent> consumer) {
			this.inventory = inventory;
			this.consumer = consumer;
			this.uuid = uuid;
			this.slot = slot;
		}

		public void accept(InventoryClickEvent event) {
			consumer.accept(event);
		}

		public boolean isFor(KingdomPlayer player) {
			return uuid.equals(player.getUniqueId());
		}

		public Optional<Inventory> getInventory() {
			return Optional.ofNullable(inventory);
		}

		public boolean isFor(UUID uuid) {
			return this.uuid.equals(uuid);
		}

		public UUID getUniqueId() {
			return uuid;
		}

		public int getSlot() {
			return slot;
		}

	}

}
