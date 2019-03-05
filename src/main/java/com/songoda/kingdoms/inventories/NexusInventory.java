package com.songoda.kingdoms.inventories;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.StructureInventory;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.PowerUp;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ItemStackBuilder;

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
	
	public NexusInventory() {
		super(InventoryType.CHEST, "nexus", 27);
	}
	
	@Override
	public void openInventory(KingdomPlayer kingdomPlayer) {
		Player player = kingdomPlayer.getPlayer();
		Kingdom kingdom = kingdomPlayer.getKingdom(); // Can't be null.
		ConfigurationSection section = inventories.getConfigurationSection("inventories.nexus");
		ItemStack converter = new ItemStackBuilder(section.getConfigurationSection("converter"))
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.build();
		inventory.setItem(0, converter);
		setAction(0, event -> {
			Inventory inventory = instance.getServer().createInventory(null, 54, Kingdoms.getLang().getString("Guis_Nexus_RP_Trade", kp.getLang()));
			player.openInventory(inventory);
		});
		/*
		InteractiveGUI gui = new InteractiveGUI(ChatColor.AQUA + kp.getKingdom().getKingdomName(), 27);
		ItemStack i1 = new ItemStack(Material.WHEAT);
		ItemMeta i1m = i1.getItemMeta();
		i1m.setDisplayName(
				ChatColor.AQUA + Kingdoms.getLang().getString("Guis_ResourcePointsConverter"));
		ArrayList<String> i1l = new ArrayList<String>();
		i1l.add(ChatColor.GREEN+ Kingdoms.getLang().getString("Guis_ResourcePointsConverter_Lore1").replace("%itemrp%",String.valueOf(Config.getConfig().getInt("items-needed-for-one-resource-point"))));
		i1l.add(ChatColor.LIGHT_PURPLE + "Nexus Option");
		i1m.setLore(LoreOrganizer.organize(i1l));
		i1.setItemMeta(i1m);
		gui.getInventory().setItem(0, getConvertor());
		gui.setAction(0, new Runnable(){
			public void run(){
				Inventory inv = Bukkit.createInventory(null, 54, Kingdoms.getLang().getString("Guis_Nexus_RP_Trade", kp.getLang()));
				kp.getPlayer().openInventory(inv);
			}
		});
		*/
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
		for (PowerUpType type: PowerUpType.values()){
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
		
		gui.getInventory().setItem(1, getMemberUpgradeItem(kingdom));
		gui.setAction(1, new Runnable(){
			public void run(){
				int cost = Config.getConfig().getInt("cost.nexusupgrades.maxmembers");
				int max = Config.getConfig().getInt("max.nexusupgrades.maxmembers");
				if(kingdom.getResourcepoints() - cost < 0){
					kp.sendMessage(Kingdoms.getLang().getString("Misc_Not_Enough_Points", kp.getLang()).replaceAll("%cost%", "" + cost));
					return;
				}
				
				if(kingdom.getMaxMember() + 1 > max){
					kp.sendMessage(Kingdoms.getLang().getString("Misc_Max_Level_Reached", kp.getLang()));
					return;
				}
				
				kingdom.setResourcepoints(kingdom.getResourcepoints() - cost);
				kingdom.setMaxMember(kingdom.getMaxMember() + 1);
				openNexusGui(kp);
			}
		});
		
		ItemStack i5 = new ItemStack(Material.BLAZE_ROD);
		ItemMeta i5m = i5.getItemMeta();
		i5m.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_ChampionUpgrades_Title", kp.getLang()));
		ArrayList<String> i5l = new ArrayList<String>();
		i5l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_ChampionUpgrades_Desc", kp.getLang()));
		i5m.setLore(LoreOrganizer.organize(i5l));
		i5.setItemMeta(i5m);
		
		gui.getInventory().setItem(18, i5);
		gui.setAction(18, new Runnable(){
			public void run(){ GUIManagement.getChampGUIManager().openMenu(kp); }
		});

		ItemStack i6 = new ItemStack(Materials.GHAST_SPAWN_EGG.parseMaterial());
		ItemMeta i6m = i6.getItemMeta();
		i6m.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_MiscUpgrades_Title", kp.getLang()));
		ArrayList<String> i6l = new ArrayList<String>();
		i6l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_MiscUpgrades_Desc", kp.getLang()));
		i6m.setLore(LoreOrganizer.organize(i6l));
		i6.setItemMeta(i6m);

		gui.getInventory().setItem(19, i6);
		gui.setAction(19, new Runnable(){
			public void run(){ GUIManagement.getMisGUIManager().openMenu(kp); }
		});

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

		ItemStack i8 = new ItemStack(Material.DISPENSER);
		ItemMeta i8m = i8.getItemMeta();
		i8m.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_Turrets_Title", kp.getLang()));
		ArrayList<String> i8l = new ArrayList<String>();
		i8l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_Turrets_Desc", kp.getLang()));
		i8m.setLore(LoreOrganizer.organize(i8l));
		i8.setItemMeta(i8m);
		

		gui.getInventory().setItem(20, i8);
		gui.setAction(20, new Runnable(){
			public void run(){ GUIManagement.getTurretGUIManager().openMenu(kp); }
		});

		ItemStack i9 = new ItemStack(Materials.MAP.parseMaterial());
		ItemMeta i9m = i9.getItemMeta();
		i9m.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_Conquests_Title", kp.getLang()));
		ArrayList<String> i9l = new ArrayList<String>();
		i9l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_Conquests_Desc", kp.getLang()));
		i9m.setLore(LoreOrganizer.organize(i9l));
		i9.setItemMeta(i9m);
		
		if(Kingdoms.getManagers().getConquestManager() != null){
			gui.getInventory().setItem(7, i9);
			gui.setAction(7, new Runnable(){
				public void run(){ GUIManagement.getConquestGUIManager().openMenu(kp); }
			});
		}
		
		ItemStack i10 = new ItemStack(Material.BEACON);
		ItemMeta i10m = i10.getItemMeta();
		i10m.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_Structures_Title", kp.getLang()));
		ArrayList<String> i10l = new ArrayList<String>();
		i10l.add(ChatColor.GREEN+ Kingdoms.getLang().getString("Guis_Nexus_Structures_Desc", kp.getLang()));
		i10m.setLore(LoreOrganizer.organize(i10l));
		i10.setItemMeta(i10m);
		
		gui.getInventory().setItem(14, i10);
		gui.setAction(14, new Runnable(){
			public void run(){ GUIManagement.getStructureGUIManager().openMenu(kp); }
		});

		ItemStack i11 = new ItemStack(Materials.BLUE_WOOL.parseMaterial(), 1, DyeColor.BLUE.getWoolData());
		ItemMeta i11m = i11.getItemMeta();
		i11m.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_Permissions_Title", kp.getLang()));
		ArrayList<String> i11l = new ArrayList<String>();
		i11l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_Permissions_Desc", kp.getLang()));
		i11m.setLore(LoreOrganizer.organize(i11l));
		i11.setItemMeta(i11m);
		
		gui.getInventory().setItem(15, i11);
		gui.setAction(15, new Runnable(){
			public void run(){ GUIManagement.getPermissionsGUIManager().openMenu(kp); }
		});
		
		ItemStack i12 = new ItemStack(Materials.GREEN_WOOL.parseMaterial(), 1, DyeColor.GREEN.getWoolData());
		ItemMeta i12m = i12.getItemMeta();
		i12m.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_Members_Title", kp.getLang()));
		ArrayList<String> i12l = new ArrayList<String>();
		i12l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_Members_Desc", kp.getLang()));
		i12m.setLore(LoreOrganizer.organize(i12l));
		i12.setItemMeta(i12m);
		
		gui.getInventory().setItem(16, i12);
		gui.setAction(16, new Runnable(){
			public void run(){ GUIManagement.getMemberManager().openMenu(kp); }
		});


		ItemStack i13 = new ItemStack(Material.BOOK);
		ItemMeta i13m = i13.getItemMeta();
		i13m.setDisplayName(Kingdoms.getLang().getString("Guis_Logs_Title", kp.getLang()));
		ArrayList<String> i13l = new ArrayList<String>();
		i13l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Logs_Description", kp.getLang()));
		i13l.add(ChatColor.LIGHT_PURPLE + "Nexus Function");
		i13m.setLore(LoreOrganizer.organize(i13l));
		i13.setItemMeta(i13m);
		
		gui.getInventory().setItem(6, i13);
		gui.setAction(6, new Runnable(){
			public void run(){ GUIManagement.getLogManager().openMenu(kp); }
		});
		
		ItemStack chest = new ItemStack(Material.CHEST);
		ItemMeta chestm = chest.getItemMeta();
		chestm.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_KingdomChest_Title", kp.getLang()));
		ArrayList<String> chestl = new ArrayList<String>();
		chestl.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_KingdomChest_Desc", kp.getLang()));
		chestm.setLore(LoreOrganizer.organize(chestl));
		chest.setItemMeta(chestm);
		
		gui.getInventory().setItem(26, chest);
		gui.setAction(26, new Runnable(){
			public void run(){ openKingdomChest(kp); }
		});
		
		gui.getInventory().setItem(8, getMasswarStatusItem());

		gui.getInventory().setItem(17, getRPDisplayItem(kingdom));
		
		
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
	
	public ItemStack getRPDisplayItem(Kingdom kingdom){
		ItemStack r = new ItemStack(Material.HAY_BLOCK);
		ItemMeta rm = r.getItemMeta();
		rm.setDisplayName(Kingdoms.getLang().getString("Guis_ResourcePoints_Title"));
		ArrayList rl = new ArrayList();
		rl.add(Kingdoms.getLang().getString("Guis_ResourcePoints_Desc"));
		rl.add(Kingdoms.getLang().getString("Guis_ResourcePoints_Count").replaceAll("%amount%", ""+kingdom.getResourcepoints()));
		rm.setLore(LoreOrganizer.organize(rl));
		r.setItemMeta(rm);
		return r;
	}
	
	private ItemStack getMasswarStatusItem(){
		ItemStack masswarstatus  = new ItemStack(Material.AIR);
		
		if(GameManagement.getMasswarManager().isMassWarOn()){
			masswarstatus = new ItemStack(Materials.LIME_WOOL.parseMaterial(), 1, (byte) 5);
		}else{
			masswarstatus = new ItemStack(Materials.RED_WOOL.parseMaterial(), 1, (byte) 14);
			
		}
		ItemMeta masswarstatusm = masswarstatus.getItemMeta();
		masswarstatusm.setDisplayName(Kingdoms.getLang().getString("Guis_Nexus_MassWarStatus_Title"));
		ArrayList<String> masswarstatusl = new ArrayList<String>();
		
		if(GameManagement.getMasswarManager().isMassWarOn()){
			masswarstatusl.add(Kingdoms.getLang().getString("Guis_Nexus_MassWarStatus_On"));
		}else{
			masswarstatusl.add(Kingdoms.getLang().getString("Guis_Nexus_MassWarStatus_Off"));
			
		}
		masswarstatusl.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_MassWarStatus_Desc1"));
		masswarstatusl.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_MassWarStatus_Desc2"));
		masswarstatusl.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_Nexus_MassWarStatus_Desc3")
				.replaceAll("%time%", "" + GameManagement.getMasswarManager().getTimeLeftInString()));
		masswarstatusm.setLore(masswarstatusl);
		masswarstatus.setItemMeta(masswarstatusm);
		return masswarstatus;
	}
	
	private ItemStack getMemberUpgradeItem(Kingdom kingdom){
		ItemStack i4p2 = new ItemStack(Materials.OAK_BOAT.parseMaterial());
		ItemMeta i4p2m = i4p2.getItemMeta();
		i4p2m.setDisplayName(Kingdoms.getLang().getString("Guis_MaxMembers_Title"));
		ArrayList<String> i4p2l = new ArrayList<String>();
		i4p2l.add(ChatColor.GREEN + Kingdoms.getLang().getString("Guis_MaxMembers_Description"));
		i4p2l.add(ChatColor.RED + Kingdoms.getLang().getString("Guis_MaxMembers_CurrMaxMem")
				+ (kingdom.getMaxMember()));
		String cost = ""+Config.getConfig().getInt("cost.nexusupgrades.maxmembers");
		i4p2l.add(Kingdoms.getLang().getString("Guis_Cost_Text").replaceAll("%cost%", cost));
		i4p2l.add(ChatColor.RED + Kingdoms.getLang().getString("Guis_Max")
				+ Config.getConfig().getInt("max.nexusupgrades.maxmembers"));
		i4p2l.add(ChatColor.LIGHT_PURPLE + "Nexus Upgrade");
		i4p2m.setLore(LoreOrganizer.organize(i4p2l));
		i4p2.setItemMeta(i4p2m);
		return i4p2;
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
	
	public void openKingdomChest(KingdomPlayer kp, Kingdom kingdom) {
		if(kingdom == null) return;
		
//		if(!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getChest())){
//			kp.sendMessage(ChatColor.RED + "You are not high-ranking enough to use this function!");
//			return;
//		}
		
		if(!GameManagement.getChestManager().useKingdomChest(kp, kingdom)){
			kp.sendMessage(ChatColor.RED + "Someone else is using the kingdom chest!");
			return;
		}
	}

	public void openKingdomChest(KingdomPlayer kp) {
		Kingdom kingdom = kp.getKingdom();
		if(kingdom == null) return;
		
		if(!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getChest())){
			kp.sendMessage(ChatColor.RED + "You are not high-ranking enough to use this function!");
			return;
		}
		
		if(!GameManagement.getChestManager().useKingdomChest(kp, kingdom)){
			kp.sendMessage(ChatColor.RED + "Someone else is using the kingdom chest!");
			return;
		}
	}
	
	
	@Override
	public void onDisable() {
		//2016-08-29
		whiteListed.clear();
		specials.clear();

		if (blackListed != null)
			blackListed.clear();
	}

}
