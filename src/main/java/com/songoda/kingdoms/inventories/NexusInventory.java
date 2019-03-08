package com.songoda.kingdoms.inventories;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.StructureInventory;
import com.songoda.kingdoms.manager.managers.ChestManager;
import com.songoda.kingdoms.manager.managers.MasswarManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.PowerUp;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.ItemStackBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class NexusInventory extends StructureInventory {
	
	private final MasswarManager masswarManager;
	private final ChestManager chestManager;
	
	public NexusInventory() {
		super(InventoryType.CHEST, "nexus", 27);
		this.chestManager = instance.getManager("chest", ChestManager.class);
		this.masswarManager = instance.getManager("masswar", MasswarManager.class);
	}
	
	@Override
	public void openInventory(KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		Kingdom kingdom = kingdomPlayer.getKingdom(); // Can't be null.
		ConfigurationSection section = inventories.getConfigurationSection("inventories.nexus");
		ItemStack filler = new ItemStackBuilder(section.getConfigurationSection("filler"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		if (section.getBoolean("use-filler", true)) {
			for (int i = 0; i < inventory.getType().getDefaultSize(); i++)
				inventory.setItem(i, filler);
		}
		ItemStack converter = new ItemStackBuilder(section.getConfigurationSection("converter"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(0, converter);
		String title = new MessageBuilder(false, "inventories.nexus.title")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.get();
		setAction(0, event -> {
			Inventory inventory = instance.getServer().createInventory(null, 54, title);
			player.openInventory(inventory);
		});
		int cost = configuration.getInt("kingdoms.cost-per-max-member-upgrade");
		int max = configuration.getInt("kingdoms.max-members-via-upgrade");
		ItemStack maxMembers = new ItemStackBuilder(section.getConfigurationSection("max-members"))
				.setPlaceholderObject(kingdomPlayer)
				.replace("%cost%", cost)
				.replace("%max%", max)
				.setKingdom(kingdom)
				.build();
		setAction(1, event -> {
			long points = kingdom.getResourcePoints();
			if (cost > points) {
				new MessageBuilder("structures.nexus-max-member-cant-afford")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost)
						.replace("%max%", max)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			if (kingdom.getMaxMembers() + 1 > max) {
				new MessageBuilder("structures.max-members-reached")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%cost%", cost)
						.replace("%max%", max)
						.setKingdom(kingdom)
						.send(player);
				return;
			}
			kingdom.subtractResourcePoints(cost);
			kingdom.setMaxMembers(kingdom.getMaxMembers() + 1);
			openNexusGui(kingdomPlayer);
		});
		ItemStack battle = new ItemStackBuilder(section.getConfigurationSection("battle-logs"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(7, battle);
		setAction(7, event -> GUIManagement.getLogManager().openMenu(kingdomPlayer));
		ItemStack permissions = new ItemStackBuilder(section.getConfigurationSection("permissions"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(8, permissions);
		setAction(8, event -> GUIManagement.getPermissionsGUIManager().openMenu(kingdomPlayer));
		ItemStack defender = new ItemStackBuilder(section.getConfigurationSection("defender-upgrades"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(9, defender);
		setAction(9, event ->  inventoryManager.getInventory(DefenderInventory.class).openMenu(kingdomPlayer));
		ItemStack misc = new ItemStackBuilder(section.getConfigurationSection("misc-upgrades"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(10, defender);
		setAction(10, event ->  GUIManagement.getMisGUIManager().openMenu(kingdomPlayer));
		ItemStack structure = new ItemStackBuilder(section.getConfigurationSection("structures"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(11, structure);
		setAction(11, event -> GUIManagement.getStructureGUIManager().openMenu(kingdomPlayer));
		ItemStack turret = new ItemStackBuilder(section.getConfigurationSection("turrets"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(12, turret);
		setAction(12, event -> GUIManagement.getTurretGUIManager().openMenu(kingdomPlayer));
		ItemStack members = new ItemStackBuilder(section.getConfigurationSection("members"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(13, members);
		setAction(13, event -> GUIManagement.getMemberManager().openMenu(kingdomPlayer));
		ItemStackBuilder masswar = new ItemStackBuilder(section.getConfigurationSection("masswar-on"))
				.replace("%time%", masswarManager.getTimeLeftInString())
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom);
		if (masswarManager.isWarOn())
			masswar.setConfigurationSection(section.getConfigurationSection("masswar-off"));
		inventory.setItem(14, masswar.build());
		setAction(14, event -> GUIManagement.getMemberManager().openMenu(kingdomPlayer));
		ItemStack points = new ItemStackBuilder(section.getConfigurationSection("resource-points"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(15, points);
		ItemStack chest = new ItemStackBuilder(section.getConfigurationSection("chest"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(16, chest);
		setAction(16, event -> openKingdomChest(kingdomPlayer));
	}
	
	
	
	
	
	
	
	
	

	//white listed items -- Material and the item worth corresponding to the Material
	public static Map<ItemStack, Integer> whiteListed = new HashMap<ItemStack, Integer>();
		
	//black listed items -- this can be null if Kingdoms.config.useWhiteList is true
	public static ArrayList<ItemStack> blackListed = new ArrayList<ItemStack>(){{
		add(new ItemStack(Material.AIR));
	}};
	
	//special items -- Material and the item worth corresponding to the Material
	public static Map<ItemStack, Integer> specials = new HashMap<ItemStack, Integer>();

	public void openNexusGui(KingdomPlayer kp){
		Kingdom kingdom = kp.getKingdom();
		PowerUp powerup = kp.getKingdom().getPowerUp();
		int slot = 9;
		for (PowerUpType type: PowerUpType.values()) {
			ItemStack i2 = new ItemStack(type.getMat());
			ItemMeta i2m = i2.getItemMeta();
			i2m.setDisplayName(type.getTitle());
			ArrayList<String> i2l = new ArrayList<String>();
			i2l.add(ChatColor.GREEN + type.getDesc());
			i2l.add(ChatColor.RED + type.getCurrlevel()
					+ powerup.getLevel(type) + "%");
			String cost = ""+type.getCost();
			i2l.add(Kingdoms.getLang().getString("Guis_Cost_Text", kp.getLang()).replaceAll("%cost%", cost));
			i2l.add(ChatColor.RED + Kingdoms.getLang().getString("Guis_Max", kp.getLang())
					+ type.getMax());
			i2l.add(ChatColor.LIGHT_PURPLE + "Nexus Upgrade");
			i2m.setLore(LoreOrganizer.organize(i2l));
			i2.setItemMeta(i2m);
			if(type.isEnabled()){
				gui.getInventory().setItem(slot, i2);
				final int meow = slot;
				gui.setAction(slot, new Runnable(){
					public void run(){
						Kingdoms.logDebug("Called update for " + type + " at " + meow);
						upgradePowerUp(kp, type);
						openNexusGui(kp);
					}
				});
				slot++;
			}
		}
		ItemStack i7 = new ItemStack(Materials.END_PORTAL_FRAME.parseMaterial());
		ItemMeta i7m = i7.getItemMeta();
		i7m.setDisplayName(Kingdoms.getLang().getString("Guis_KingdomChestSize_Title", kp.getLang()));
		ArrayList<String> i7l = new ArrayList<String>();
		i7l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_KingdomChestSize_Description", kp.getLang()));
		i7l.add(ChatColor.RED + Kingdoms.getLang().getString("Guis_KingdomChestSize_CurrLevel", kp.getLang())
				+ kingdom.getChestsize() + " slots");
		String cost = "30";
		i7l.add(Kingdoms.getLang().getString("Guis_Cost_Text", kp.getLang()).replaceAll("%cost%", cost));
		i7l.add(ChatColor.LIGHT_PURPLE + "Nexus Upgrade");
		i7m.setLore(LoreOrganizer.organize(i7l));
		i7.setItemMeta(i7m);
		
		gui.getInventory().setItem(13, i7);
		gui.setAction(13, new Runnable(){
			public void run(){
				int cost = 30;
				int max = 27;
				if(kingdom.getResourcepoints() - cost < 0){
					kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_Enough_Points", kp.getLang()).replaceAll("%cost%", "" + cost));
					return;
				}
				if(kingdom.getChestsize() + 9 > max){
					kp.sendMessage(Kingdoms.getLang().getString("Misc_Max_Level_Reached", kp.getLang()));
					return;
				}
				kingdom.setResourcepoints(kingdom.getResourcepoints() - cost);
				kingdom.setChestsize(kingdom.getChestsize() + 9);
				kingdom.sendAnnouncement(kp, ChatColor.GOLD+"ChestSize: "+ChatColor.DARK_GREEN+kingdom.getChestsize(), false);
				openNexusGui(kp);
			}
		});		
		
		ItemStack neutral = new ItemStack(Materials.WHITE_WOOL.parseMaterial(), 1, DyeColor.WHITE.getWoolData());
		ItemMeta neutralm = neutral.getItemMeta();
		neutralm.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_Neutral_Title", kp.getLang()));
		ArrayList<String> neutrall = new ArrayList<String>();
		neutrall.add(ChatColor.YELLOW + Kingdoms.getLang().getString("Guis_Nexus_Neutral_Desc", kp.getLang()));
		neutrall.add(Kingdoms.getLang().getString("Guis_Nexus_Neutral_Status", kp.getLang()).replaceAll("%status%", "" + kingdom.isNeutral()));
		neutralm.setLore(LoreOrganizer.organize(neutrall));
		neutral.setItemMeta(neutralm);

		if(Config.getConfig().getBoolean("allow-pacifist")){
			gui.getInventory().setItem(2, neutral);
			gui.setAction(2, new Runnable(){
				public void run(){
					if(kingdom.hasInvaded()){
						kp.sendMessage(Kingdoms.getLang().getString("Misc_Cannot_Neutral", kp.getLang()));
						return;
					}
					kingdom.setNeutral(!kingdom.isNeutral());
					kp.sendMessage(Kingdoms.getLang().getString("Misc_Neutral_Toggled", kp.getLang()));
					openNexusGui(kp);
					return;
				}
			});
		}
		
		
		getShieldDisplayItem(kingdom);
		KingdomNexusGUIOpenEvent event = new KingdomNexusGUIOpenEvent(kp, gui);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
			gui.openInventory(kp.getPlayer());
		
	}
	
	public ItemStack getShieldDisplayItem(Kingdom kingdom){
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
	
	private void upgradePowerUp(KingdomPlayer kp, PowerUp.PowerUpType type){
		Kingdom kingdom = kp.getKingdom();
		PowerUp powerup = kingdom.getPowerUp();
		Kingdoms.logDebug("DMG" + powerup.getDmgboost());
		Kingdoms.logDebug("DEF" + powerup.getDmgreduction());
		Kingdoms.logDebug("REGEN" + powerup.getRegenboost());
		Kingdoms.logDebug("ARROW" + powerup.getArrowboost());
		int resourcePoint = kingdom.getResourcepoints();
		int cost = type.getCost();
		int max = type.getMax();
		if(resourcePoint - cost < 0){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_Enough_Points", kp.getLang()).replaceAll("%cost%", "" + cost));
			return;
		}
		
		if(powerup.getLevel(type) + 1 > max){
			kp.sendMessage(Kingdoms.getLang().getString("Misc_Max_Level_Reached", kp.getLang()));
			return;
		}
		Kingdoms.logDebug("Ran upgrade method with type " + type);
		kingdom.setResourcepoints(resourcePoint - cost);
		powerup.setLevel(type, powerup.getLevel(type) + 1);
	}
	
	@EventHandler
	public void onDonateInventoryClose(InventoryCloseEvent event) {
		if(!(event.getPlayer() instanceof Player)) return;
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession((Player) event.getPlayer());

		String invName = event.getInventory().getName();
		if(invName.equals(Kingdoms.getLang().getString("Guis_Nexus_RP_Trade", kp.getLang()))){
			int donatedamt = consumeDonationItems(event.getInventory().getContents(), kp);
			kp.getKingdom().setResourcepoints(kp.getKingdom().getResourcepoints() + donatedamt);
			kp.sendMessage(Kingdoms.getLang().getString("Guis_Nexus_RP_Trade_Success", kp.getLang()).replaceAll("%amount%", ""+donatedamt));
			kp.setLastTimeDonated(new Date());
			kp.setDonatedAmt(kp.getDonatedAmt() + donatedamt);
			kp.setLastDonatedAmt(donatedamt);
			return;
		}
		
		if(invName.startsWith(ChatColor.DARK_BLUE + "Donate to " + ChatColor.DARK_GREEN)){
			String[] nsplit = event.getInventory().getName().split(" ");
			String sentKingdomName = ChatColor.stripColor(nsplit[(nsplit.length - 1)]);
			
			Kingdom sentTo = GameManagement.getKingdomManager().getOrLoadKingdom(sentKingdomName);
			int donatedamt = consumeDonationItems(event.getInventory().getContents(), kp);
			sentTo.setResourcepoints(sentTo.getResourcepoints() + donatedamt);
			kp.sendMessage(Kingdoms.getLang().getString("Guis_Nexus_RP_Trade_Success", kp.getLang()).replaceAll("%amount%", ""+donatedamt).replaceAll("%kingdom%", ""+sentTo.getKingdomName()));
			sentTo.sendAnnouncement(null,Kingdoms.getLang().getString("Guis_Nexus_RP_Trade_Success", kp.getLang()).replaceAll("%amount%", ""+donatedamt).replaceAll("%player%", ""+kp.getName()),true);
			
			return;
		}
		
		//?
		if(invName.equals(Kingdoms.getLang().getString("Guis_Nexus_KingdomChest_Title", kp.getLang()))){
			
			
			return;
		}
	}
	
	//2016-10-11
		/**
		 * DOES NOT award the resource points. It does, however, consume the used items or return the items depending on the situation
		 * @param items items to donated
		 * @param p donator
		 * @return total points the items are worth.
		 */
		public int consumeDonationItems(ItemStack[] items, KingdomPlayer p) {
			//items to return
			ArrayList<ItemStack> returningIS = new ArrayList<ItemStack>();
			//items to consume
			//ArrayList<ItemStack> consumingIS = new ArrayList<ItemStack>();
			//special items that doesn't affected by items_needed_for_one_resource_point
			//ArrayList<ItemStack> extraIS = new ArrayList<ItemStack>();
			int itemworth = 0;
			int itemsperrp = Config.getConfig().getInt("items-needed-for-one-resource-point");
			
			if(Config.getConfig().getBoolean("use-whitelist")){
				for(ItemStack item:items){
					if(item == null)continue;
					ItemStack type = new ItemStack(item.clone().getType(),1,item.getDurability());
					if(whiteListed.containsKey(type)){
						itemworth += item.getAmount() * whiteListed.get(type);
					}else{
						returningIS.add(item);
					}
				}
			}else{
				for(ItemStack item:items){
					if(item == null)continue;
					Kingdoms.logDebug(item.getType().toString() + "," + item.getAmount());
					
					
					if (isBlackListed(item)) {
						String name = item.getType().toString();
						if(item.getItemMeta() != null &&
								item.getItemMeta().getDisplayName() != null){
							name = item.getItemMeta().getDisplayName();
						}else if(item.getDurability() != 0){
							name += ":" + item.getDurability();
						}
						String message = Kingdoms.getLang().getString("Misc_Cannot_Be_Traded_For_Points").replaceAll("%item%", name);
						
						
						p.sendMessage(message);
						returningIS.add(item);
						Kingdoms.logDebug("BLACKLISTED: " + item.toString());
						continue;
					}else if(getSpecialWorth(item) != -1){
						itemworth += item.getAmount() * getSpecialWorth(item);
					}else{
						itemworth += item.getAmount();
					}
				}
			}
			
			if(itemworth % Config.getConfig().getInt("items-needed-for-one-resource-point") != 0){
				int itemoverflow = itemworth % Config.getConfig().getInt("items-needed-for-one-resource-point");
				for(ItemStack item:items){
					if(item == null)continue;
					ItemStack type = new ItemStack(item.clone().getType(),1,item.getDurability());
					if (isBlackListed(item)) continue;
					int worth = 1;
					if(getSpecialWorth(item) != -1){
						worth = getSpecialWorth(item);
					}
					if(Config.getConfig().getBoolean("use-whitelist") && whiteListed.containsKey(type)){
						worth = whiteListed.get(type);
					}
					int originalAmount = item.getAmount();
					int removedAmount = 0;
					while((itemworth) % Config.getConfig().getInt("items-needed-for-one-resource-point") != 0 &&
							(itemworth - worth) >= ((itemworth/Config.getConfig().getInt("items-needed-for-one-resource-point"))*Config.getConfig().getInt("items-needed-for-one-resource-point")) &&
							originalAmount - removedAmount > 0){

						//Kingdoms.logDebug("===Type: " + type.getType().toString());
						//Kingdoms.logDebug("===Condition 1: " + (itemworth-worth) + " / " + Kingdoms.config.items_needed_for_one_resource_point + " Remainder not 0");
						//Kingdoms.logDebug("===Condition 2: " + (itemworth - worth) + " > " + ((itemworth/Kingdoms.config.items_needed_for_one_resource_point)*Kingdoms.config.items_needed_for_one_resource_point));
						//Kingdoms.logDebug("old: " + itemworth);
						itemworth -= worth;
						//Kingdoms.logDebug("new: " + itemworth);
						returningIS.add(type);
						Kingdoms.logDebug("EXTRA: " + type.toString());
						removedAmount++;
					}
					
					if((itemworth) % Config.getConfig().getInt("items-needed-for-one-resource-point") == 0){
						break;
					}
				}
			}
			//Kingdoms.logDebug("================Final");
			//Kingdoms.logDebug("===Condition 1: " + (itemworth) + " / " + Kingdoms.config.items_needed_for_one_resource_point + " Remainder not 0");
			//Kingdoms.logDebug("===Condition 2: " + (itemworth) + " > " + ((itemworth/Kingdoms.config.items_needed_for_one_resource_point)*Kingdoms.config.items_needed_for_one_resource_point));
			
			
			if((itemworth) %Config.getConfig().getInt("items-needed-for-one-resource-point") != 0 ){
				returningIS.clear();
				for(ItemStack item:items){
					if(item== null)continue;
				
					p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), item);
				}
				p.sendMessage(Kingdoms.getLang().getString("Guis_Nexus_RP_Trade_Insufficient_Itemcount", p.getLang()).replaceAll("%amount%", "" + (Config.getConfig().getInt("items-needed-for-one-resource-point") - (itemworth%Config.getConfig().getInt("items-needed-for-one-resource-point")))));
				return 0;
			}
			
			int returned = returningIS.size();
			if(returned != 0) p.sendMessage(Kingdoms.getLang().getString("Guis_Nexus_RP_Trade_Overflowing_Itemcount", p.getLang()).replaceAll("%amount%", "" + returned));
			
			for(ItemStack item:returningIS){
				Kingdoms.logDebug(item.getType().toString() + "," + item.getAmount());
				p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), item);
			}
			
			return itemworth/Config.getConfig().getInt("items-needed-for-one-resource-point");
		}
		
		private int getSpecialWorth(ItemStack item){
			ItemStack type = new ItemStack(item.clone().getType(),1,item.getDurability());
			if(!specials.containsKey(type)){
				return -1;
			}
			
			return specials.get(type);
			
		}
		
		private boolean isBlackListed(ItemStack item){
			ItemStack clone = item.clone();
			clone.setAmount(1);
			if(blackListed.contains(clone)) return true;
			if(item.getItemMeta() == null) return false;
			if(item.getItemMeta().getLore() != null &&
					!item.getItemMeta().getLore().isEmpty()){
				if(Config.getConfig().getBoolean("disallow-named-or-lored-items-from-rp-trade")) return true;
			}
			if(item.getItemMeta().getDisplayName() == null) return false;
			if(Config.getConfig().getBoolean("disallow-named-or-lored-items-from-rp-trade")) return true;
			
			for(ItemStack bl:blackListed){
				if(bl == null) continue;
				if(bl.getItemMeta() == null) continue;
				if(item.getItemMeta().getDisplayName().equals(bl.getItemMeta().getDisplayName())){
					return true;
				}
			}
			
			return false;
		}

	
	public static ItemStack getBackButton(){
	    ItemStack backbtn = new ItemStack(Material.REDSTONE_BLOCK);
	    ItemMeta backbtnmeta = backbtn.getItemMeta();
	    backbtnmeta.setDisplayName(ChatColor.RED + Kingdoms.getLang().getString("Guis_Back_Btn"));
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.RED + "" + ChatColor.YELLOW + "" + ChatColor.GREEN);
		backbtnmeta.setLore(lore);
	    backbtn.setItemMeta(backbtnmeta);
	    return backbtn;
	}
	
	@EventHandler
	public void onBackBtnMove(InventoryClickEvent event){
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession((Player) event.getWhoClicked());
		Kingdom kingdom = kp.getKingdom();
		if(kingdom == null) return;
		if(event.getCurrentItem() == null) return;
		if(event.getCurrentItem().getItemMeta() == null) return;
		if(event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
		if (event.getCurrentItem().getItemMeta().getDisplayName()
				.equals(ChatColor.RED + Kingdoms.getLang().getString("Guis_Back_Btn"))){
			if(event.getCurrentItem().getItemMeta().getLore() == null) return;
			if(!event.getCurrentItem().getItemMeta().getLore().contains(ChatColor.RED + "" + ChatColor.YELLOW + "" + ChatColor.GREEN)) return;
				event.setCancelled(true);
			
			kp.getPlayer().closeInventory();
			
			openNexusGui(kp);
			return;
		}
		
	}
	
	
	//Initiate item trade lists.
	public void init(){
		if(Config.getConfig().getBoolean("use-whitelist")){
			for(int i = 0; i < Config.getConfig().getStringList("whitelist-items").size(); i++){
				String str = Config.getConfig().getStringList("whitelist-items").get(i);
				if(str == null)
					continue;
					
				String[] split = Config.getConfig().getStringList("whitelist-items").get(i).split(",");
				if(split.length != 2){
					Kingdoms.logInfo("[ERROR]: Your whitelist item, " + Config.getConfig().getStringList("whitelist-items").get(i) + " needs to be in the format of [MATERIAL],[ITEM VALUE]");
					continue;
				}
				try{
					ItemStack item = null;
					int point = Integer.parseInt(split[1]);
					String[] matSplit = split[0].split(":");
					if(matSplit.length == 2){
						item = new ItemStack(Material.valueOf(matSplit[0]), 1, (byte) Integer.parseInt(matSplit[1]));
					}else{
						item = new ItemStack(Material.valueOf(split[0]), 1);
					}
					whiteListed.put(item, point);
				}catch(NumberFormatException e){
					Kingdoms.logInfo("[ERROR]: Your whitelist item, " + Config.getConfig().getStringList("whitelist-items").get(i) + " has invalid point value ["+split[1]+"]");
				}catch(IllegalArgumentException e){
					Kingdoms.logInfo("[ERROR]: Your whitelist item, " + Config.getConfig().getStringList("whitelist-items").get(i) + " has unknown material name ["+split[0]+"]");
				}
			}
		}else{
			ArrayList<ItemStack> blacklists = new ArrayList<ItemStack>();
			for(String matName : Config.getConfig().getStringList("resource-point-trade-blacklist")){
				try{
					ItemStack item = null;
					String[] matNameSplit = matName.split(":");
					if(matNameSplit.length == 2){
						item = new ItemStack(Material.valueOf(matNameSplit[0]),1,(byte) Integer.parseInt(matNameSplit[1]));
					}else{
						item = new ItemStack(Material.valueOf(matName), 1);
					}
					blacklists.add(item);
					
				}catch(NumberFormatException e){
					Kingdoms.logInfo("[ERROR]: Your blacklisted item " + matName + " has an invalid item value");
				}catch(IllegalArgumentException e){
					Kingdoms.logInfo("[ERROR]: Your blacklisted item " + matName + " is an unknown material name.");
				}
			}
			for(String name:Config.getConfig().getStringList("resource-point-trade-blacklist")){
				ItemStack item = new ItemStack(Material.BEDROCK);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
				item.setItemMeta(meta);
				blacklists.add(item);
			}
			if(!blacklists.isEmpty()){
				blackListed = blacklists;
			}else{
				blacklists.add(new ItemStack(Material.AIR));
				blackListed = blacklists;
			}
			
			for(int i = 0; i < Config.getConfig().getStringList("special-item-cases").size(); i++){
				String str = Config.getConfig().getStringList("special-item-cases").get(i);
				if(str == null)
					continue;
				
				String[] split = Config.getConfig().getStringList("special-item-cases").get(i).split(",");
				if(split.length != 2){
					Kingdoms.logInfo("[ERROR]: Your special item, " + Config.getConfig().getStringList("special-item-cases").get(i) + " needs to be in the format of [MATERIAL],[ITEM VALUE]");
					continue;
				}
				
				try{
					ItemStack item = null;
					int point = Integer.parseInt(split[1]);
					String[] matSplit = split[0].split(":");
					if(matSplit.length == 2){
						item = new ItemStack(Material.valueOf(matSplit[0]), 1, (byte) Integer.parseInt(matSplit[1]));
					}else{
						item = new ItemStack(Material.valueOf(split[0]), 1);
					}
					specials.put(item, point);
				}catch(NumberFormatException e){
					Kingdoms.logInfo("[ERROR]: Your special item, " + Config.getConfig().getStringList("special-item-cases").get(i) + " has invalid point value ["+split[1]+"]");
				}catch(IllegalArgumentException e){
					Kingdoms.logInfo("[ERROR]: Your special item, " + Config.getConfig().getStringList("special-item-cases").get(i) + " has unknown material name ["+split[0]+"]");
				}
			}
		}
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

}
