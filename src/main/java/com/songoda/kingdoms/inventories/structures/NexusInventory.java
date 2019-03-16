package com.songoda.kingdoms.inventories.structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.manager.managers.ChestManager;
import com.songoda.kingdoms.manager.managers.MasswarManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.kingdom.PowerupType;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class NexusInventory extends StructureInventory {

	private final Map<Player, OfflineKingdom> donations = new HashMap<>();
	private final MasswarManager masswarManager;
	private final PlayerManager playerManager;
	private final ChestManager chestManager;
	
	public NexusInventory() {
		super(InventoryType.CHEST, "nexus", 27);
		this.chestManager = instance.getManager("chest", ChestManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.masswarManager = instance.getManager("masswar", MasswarManager.class);
	}
	
	public void openDonateInventory(OfflineKingdom kingdom, KingdomPlayer kingdomPlayer) {
		String title = new MessageBuilder(false, "inventories.nexus.title")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.get();
		Player player = kingdomPlayer.getPlayer();
		Inventory inventory = instance.getServer().createInventory(null, 54, title);
		player.openInventory(inventory);
		donations.put(player, kingdom);
	}
	
	@Override
	public void openInventory(KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		Kingdom kingdom = kingdomPlayer.getKingdom(); // Can't be null.
		ConfigurationSection section = inventories.getConfigurationSection("inventories.nexus");
		ItemStack filler = new ItemStackBuilder(section.getConfigurationSection("filler"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		if (section.getBoolean("use-filler", true)) {
			for (int i = 0; i < inventory.getType().getDefaultSize(); i++)
				inventory.setItem(i, filler);
		}
		ItemStack converter = new ItemStackBuilder(section.getConfigurationSection("converter"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(0, converter);
		setAction(0, event -> {
			openDonateInventory(kingdom, kingdomPlayer);
		});
		int memberCost = configuration.getInt("kingdoms.cost-per-max-member-upgrade", 10);
		int max = configuration.getInt("kingdoms.max-members-via-upgrade", 30);
		ItemStack maxMembers = new ItemStackBuilder(section.getConfigurationSection("max-members"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.replace("%cost%", memberCost)
				.replace("%max%", max)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(1, maxMembers);
		setAction(1, event -> {
			long points = kingdom.getResourcePoints();
			if (memberCost > points) {
				new MessageBuilder("structures.nexus-max-member-cant-afford")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", memberCost)
						.replace("%max%", max)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (kingdom.getMaxMembers() + 1 > max) {
				new MessageBuilder("structures.max-members-reached")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", memberCost)
						.replace("%max%", max)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			kingdom.subtractResourcePoints(memberCost);
			kingdom.setMaxMembers(kingdom.getMaxMembers() + 1);
			openInventory(kingdomPlayer);
		});
		ItemStack battle = new ItemStackBuilder(section.getConfigurationSection("battle-logs"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(7, battle);
//TODO		setAction(7, event -> GUIManagement.getLogManager().openMenu(kingdomPlayer));
		ItemStack permissions = new ItemStackBuilder(section.getConfigurationSection("permissions"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(8, permissions);
//TODO		setAction(8, event -> GUIManagement.getPermissionsGUIManager().openMenu(kingdomPlayer));
		ItemStack defender = new ItemStackBuilder(section.getConfigurationSection("defender-upgrades"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(9, defender);
//TODO		setAction(9, event ->  inventoryManager.getInventory(DefenderInventory.class).openMenu(kingdomPlayer));
		ItemStack misc = new ItemStackBuilder(section.getConfigurationSection("misc-upgrades"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(10, misc);
//TODO		setAction(10, event ->  GUIManagement.getMisGUIManager().openMenu(kingdomPlayer));
		ItemStack structure = new ItemStackBuilder(section.getConfigurationSection("structures"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(11, structure);
//TODO		setAction(11, event -> GUIManagement.getStructureGUIManager().openMenu(kingdomPlayer));
		ItemStack turret = new ItemStackBuilder(section.getConfigurationSection("turrets"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(12, turret);
//TODO		setAction(12, event -> GUIManagement.getTurretGUIManager().openMenu(kingdomPlayer));
		ItemStack members = new ItemStackBuilder(section.getConfigurationSection("members"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(13, members);
//TODO		setAction(13, event -> GUIManagement.getMemberManager().openMenu(kingdomPlayer));
		ItemStackBuilder masswar = new ItemStackBuilder(section.getConfigurationSection("masswar-on"))
				.replace("%time%", masswarManager.getTimeLeftInString())
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom);
		if (masswarManager.isWarOn())
			masswar.setConfigurationSection(section.getConfigurationSection("masswar-off"));
		inventory.setItem(14, masswar.build());
//TODO		setAction(14, event -> masswarMenu.openMenu(kingdomPlayer));
		ItemStack points = new ItemStackBuilder(section.getConfigurationSection("resource-points"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(15, points);
		ItemStack chest = new ItemStackBuilder(section.getConfigurationSection("chest"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(16, chest);
		setAction(16, event -> openKingdomChest(kingdomPlayer));
		KingdomChest kingdomChest = kingdom.getKingdomChest();
		int size = kingdomChest.getSize();
		int cost = configuration.getInt("kingdoms.chest-size-upgrade-cost", 30);
		cost += configuration.getInt("kingdoms.chest-size-upgrade-multiplier", 10) * ((size / 9) - 3);
		int chestCost = cost;
		ItemStack chestSize = new ItemStackBuilder(section.getConfigurationSection("chest-size"))
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.replace("%size%", size)
				.replace("%cost%", chestCost)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(17, chestSize);
		setAction(17, event -> {
			if (chestCost > kingdom.getResourcePoints()) {
				new MessageBuilder("kingdoms.not-enough-resourcepoints-chest-upgrade")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", chestCost)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (size + 9 > max) {
				new MessageBuilder("kingdoms.nexus-chest-maxed")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			kingdom.subtractResourcePoints(chestCost);
			kingdomChest.setSize(size + 9);
			new MessageBuilder("kingdoms.chest-size-upgraded")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%size%", (size + 9) / 9)
					.setKingdom(kingdom)
					.send(player);
			openInventory(kingdomPlayer);
		});
		if (configuration.getBoolean("kingdoms.allow-pacifist")) {
			ItemStackBuilder builder = new ItemStackBuilder(section.getConfigurationSection("neutral-off"))
					.replace("%status%", kingdom.isNeutral())
					.setPlaceholderObject(kingdomPlayer)
					.fromConfiguration(inventories)
					.setKingdom(kingdom);
			if (kingdom.isNeutral()) {
				builder = new ItemStackBuilder(section.getConfigurationSection("neutral-on"))
						.replace("%status%", kingdom.isNeutral())
						.setPlaceholderObject(kingdomPlayer)
						.fromConfiguration(inventories)
						.setKingdom(kingdom);
			}
			inventory.setItem(22, builder.build());
			setAction(22, event -> {
				if (kingdom.hasInvaded()) {
					new MessageBuilder("kingdoms.cannot-be-neutral")
							.replace("%status%", kingdom.isNeutral())
							.setPlaceholderObject(kingdomPlayer)
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				kingdom.setNeutral(!kingdom.isNeutral());
				new MessageBuilder("kingdoms.neutral-toggled")
						.replace("%status%", kingdom.isNeutral())
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(player);
				openInventory(kingdomPlayer);
				return;
			});
		}
		Powerup powerup = kingdom.getPowerup();
		List<Integer> slots = Lists.newArrayList(18, 19, 25, 26);
		int i = 0;
		for (PowerupType type: PowerupType.values()) {
			if (!type.isEnabled())
				continue;
			int slot = Optional.ofNullable(slots.get(i)).orElse(18 + i);
			inventory.setItem(slot, type.getItemStackBuilder()
					.withPlaceholder(powerup, new Placeholder<Powerup>("%amount%", "%level%") {
						@Override
						public Integer replace(Powerup powerup) {
							return powerup.getLevel(type);
						}
					}).build());
			setAction(slot, event -> {
				if (type.getCost() > kingdom.getResourcePoints()) {
					new MessageBuilder("kingdoms.not-enough-resourcepoints-powerup")
							.replace("%powerup%", type.name().toLowerCase().replaceAll("_", "-"))
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", type.getCost())
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				int level = powerup.getLevel(type);
				if (level + 1 > type.getMax()) {
					new MessageBuilder("kingdoms.powerup-maxed")
							.replace("%powerup%", type.name().toLowerCase().replaceAll("_", "-"))
							.setPlaceholderObject(kingdomPlayer)
							.replace("%cost%", type.getCost())
							.setKingdom(kingdom)
							.send(player);
					return;
				}
				kingdom.subtractResourcePoints(type.getCost());
				powerup.setLevel(level + 1, type);
				openInventory(kingdomPlayer);
			});
		}
		//getShieldDisplayItem(kingdom); This was planned but never made it to the nexus menu I guess - Lime
		openInventory(player);
	}
	
	public void openKingdomChest(KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).hasChestAccess()) {
			new MessageBuilder("kingdoms.rank-too-low-chest-access")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.hasChestAccess()), new Placeholder<Optional<Rank>>("%rank%") {
						@Override
						public String replace(Optional<Rank> rank) {
							if (rank.isPresent())
								return rank.get().getName();
							return "(Not attainable)";
						}
					})
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send(kingdomPlayer);
			return;
		}
		chestManager.openChest(kingdomPlayer, kingdom);
	}
	
	private int consumeDonationItems(Inventory inventory, KingdomPlayer kingdomPlayer) {
		Set<ItemStack> returning = new HashSet<>();
		Player player = kingdomPlayer.getPlayer();
		ConfigurationSection section = configuration.getConfigurationSection("kingdoms.resource-donation");
		int worth = 0;
		ItemStack[] items = inventory.getContents();
		if (section.getBoolean("use-list", false)) {
			ConfigurationSection list = section.getConfigurationSection("list");
			Set<String> nodes = list.getKeys(false);
			for (ItemStack item : items) {
				if (item == null)
					continue;
				Material type = item.getType();
				Optional<Double> points = nodes.parallelStream()
						.filter(node -> node.equals(type.name()))
						.map(node -> list.getDouble(node, 3))
						.findFirst();
				if (!points.isPresent()) {
					returning.add(item);
					continue;
				}
				worth += item.getAmount() * points.get();
			}
		} else {
			double points = section.getDouble("points-per-item", 3);
			for (ItemStack item : items) {
				if (item == null)
					continue;
				String name = item.getType().name();
				if (section.getStringList("blacklist").contains(name)) {
					returning.add(item);
					continue;
				}
				worth += item.getAmount() * points;
			}
		}
		Map<Material, Integer> map = new HashMap<>();
		for (ItemStack item : returning) {
			Material material = item.getType();
			if (!map.containsKey(material))
				map.put(item.getType(), inventory.all(material).size());
		}
		Set<Material> displayed = new HashSet<>();
		for (ItemStack item : returning) {
			Material material = item.getType();
			String name = map.get(material) + " " + material.name().toLowerCase();
			ItemMeta meta = item.getItemMeta();
			boolean modified = false;
			if (meta != null && meta.getDisplayName() != null) {
				name = meta.getDisplayName();
				modified = true;
			} else {
				short durability = DeprecationUtils.getDurability(item);
				if (durability > 0) {
					name += ":" + durability;
					modified = true;
				}
			}
			if (!displayed.contains(material) || modified)
				new MessageBuilder("kingdoms.cannot-be-donated")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%item%", name)
						.send(player);
			displayed.add(material);
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}
		return worth;
	}
	
	@EventHandler
	public void onDonateInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (donations.containsKey(player)) {
			int donated = consumeDonationItems(event.getInventory(), kingdomPlayer);
			Kingdom kingdom = kingdomPlayer.getKingdom();
			if (kingdom == null)
				return;
			OfflineKingdom donatingTo = donations.get(player);
			if (kingdom.equals(donatingTo)) {
				kingdom.addResourcePoints(donated);
				new MessageBuilder("kingdoms.donated-kingdom")
						.toKingdomPlayers(donatingTo.getKingdom().getOnlinePlayers())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", donated)
						.setKingdom(kingdom)
						.send();
			} else { // It's an ally donating.
				donatingTo.addResourcePoints(donated);
				new MessageBuilder("kingdoms.donated-alliance")
						.toKingdomPlayers(donatingTo.getKingdom().getOnlinePlayers())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", donated)
						.setKingdom(kingdom)
						.send();
			}
			new MessageBuilder("kingdoms.donated-self")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%amount%", donated)
					.setKingdom(kingdom)
					.send(player);
			//TODO make a donation tracker and add the donation time and amount from here.
			return;
		}
		donations.remove(player);
	}

	/*public ItemStack getShieldDisplayItem(Kingdom kingdom){
		ItemStack shield = new ItemStack(Material.OBSIDIAN);
		ItemMeta shieldm = shield.getItemMeta();
		shieldm.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_Shield_Title"));
		ArrayList shieldl = new ArrayList();
		int value = kingdom.getShieldValue();
		int max = kingdom.getShieldMax();
		
		shieldl.add(Kingdoms.getLang().getString("Guis_Nexus_Shield_Value")
				.replaceAll("%value%",""+value)
				.replaceAll("%max%",""+max));
		shieldl.add(Kingdoms.getLang().getString("Guis_Nexus_Shield_Recharge_Amount")
				.replaceAll("%amt%",""+kingdom.getShieldRecharge()));
		if(value < max){
			shieldl.add(Kingdoms.getLang().getString("Guis_Nexus_Shield_Recharge_Cost")
					.replaceAll("%cost%",""+kingdom.getShieldRechargeCost()));
		}
		if(kingdom.isRechargingShield()){
			shieldl.add(ChatColor.RED + TimeUtils.parseTimeMillis(kingdom.getTimeLeft(Kingdom.RECHARGE_COOLDOWN)));
		}
		shieldl.add(Kingdoms.getLang().getString("Guis_Nexus_Shield_Info"));
		shieldm.setLore(LoreOrganizer.organize(shieldl));
		shield.setItemMeta(shieldm);
		return shield;
	}
	*/

}
