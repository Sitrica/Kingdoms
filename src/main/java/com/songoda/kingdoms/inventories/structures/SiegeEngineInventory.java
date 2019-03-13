package com.songoda.kingdoms.inventories.structures;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.worldguard.bukkit.util.Materials;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.inventories.StructureInventory;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.SiegeEngine;
import com.songoda.kingdoms.objects.structures.Structure;

public class SiegeEngineInventory extends StructureInventory {

	protected SiegeEngineInventory() {
		super(InventoryType.CHEST, "siege-engine", 27);
	}
	
	@Override
	public void openInventory(KingdomPlayer kingdomPlayer) {
		throw new UnsupportedOperationException("This method should not be called, use openSiegeMenu(Land, KingdomPlayer)");
	}
	
	public void openSiegeMenu(Land land, KingdomPlayer kingdomPlayer) {
		Structure structure = land.getStructure();
		if (structure == null)
			return;
		SiegeEngine engine = (SiegeEngine) structure;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Player player = kingdomPlayer.getPlayer();
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				//No diagonal Firing.
				if (x != 0 && z != 0)
					continue;
				ItemStack item;
				if (x == 0 && z == 0) {
					item = new ItemStack(Materials.GLASS_PANE.parseMaterial());
					ItemMeta meta = item.getItemMeta();
					ArrayList<String> lore = new ArrayList<>();
					lore.add(Kingdoms.getLang().getString("Structures_SiegeEngine", kp.getLang()));
					meta.setDisplayName(Kingdoms.getLang().getString("Guis_SiegeEngine_Land_Title", kp.getLang())
							.replaceAll("%x%",""+sc.getX())
							.replaceAll("%z%",""+sc.getZ())
							.replaceAll("%tag%",""+ChatColor.GREEN + kingdom.getName()));
					lore.add(ChatColor.AQUA + "\\N/");
					lore.add(ChatColor.AQUA + "W+E");
					lore.add(ChatColor.AQUA + "/S\\");
					meta.setLore(LoreOrganizer.organize(lore));
					item.setItemMeta(meta);
				} else {
					SimpleChunkLocation chunk = new SimpleChunkLocation(sc.getWorld(),sc.getX()+x,sc.getZ()+z);
					Land land = Kingdoms.getManagers().getLandManager().getLand(chunk);
					OfflineKingdom landKingdom = land.getKingdomOwner();
					String kname = land.getOwner();
					if (kname == null) {
						kname = Kingdoms.getLang().getString("Map_Unoccupied", kp.getLang());
					}
					item = new ItemStack(Materials.WHITE_STAINED_GLASS_PANE.parseMaterial());
					ItemMeta meta = item.getItemMeta();
					ArrayList<String> lore = new ArrayList<>();
					meta.setDisplayName(Kingdoms.getLang().getString("Guis_SiegeEngine_Land_Title", kp.getLang())
							.replaceAll("%x%",""+(sc.getX()+x))
							.replaceAll("%z%",""+(sc.getZ()+z))
							.replaceAll("%tag%",""+ChatColor.RED + kname));
					if (!engine.isReadyToFire()) {
						lore.add(Kingdoms.getLang().getString("Guis_SiegeEngine_Land_Reloading", kp.getLang()));
						lore.add(ChatColor.RED + TimeUtils.parseTimeMinutes(engine.getConcBlastCD()));
					} else if (landKingdom == null) {
						lore.add(Kingdoms.getLang().getString("Guis_SiegeEngine_Land_Invalid_Target", kp.getLang()));
					} else if (landKingdom.equals(kingdom) || kingdom.getAllies().contains(landKingdom)) {
						lore.add(Kingdoms.getLang().getString("Guis_SiegeEngine_Land_Your_Land", kp.getLang()));
					}else if (landKingdom.isShieldUp()) {
						lore.add(Kingdoms.getLang().getString("Guis_SiegeEngine_Land_Invade_Shield", kp.getLang()));
					} else {
						int cost = Config.getConfig().getInt("siege.fire.cost");
						if (landKingdom.isWithinNexusShieldRange(chunk))
							lore.add(Kingdoms.getLang().getString("Guis_SiegeEngine_Shield", kp.getLang())
								.replaceAll("%value%", ""+landKingdom.getShieldValue())
								.replaceAll("%max%", ""+landKingdom.getShieldMax()));
						
						//Allowed to Fire.
						lore.add(Kingdoms.getLang().getString("Guis_SiegeEngine_Land_Click_To_Fire", kp.getLang()));
						lore.add(Kingdoms.getLang().getString("Guis_Cost_Text", kp.getLang())
								.replaceAll("%cost%",""+cost));
						setAction((1 + x) + (9 * (z + 1)), event -> {
							player.closeInventory();
							getSiegeEngineManager().fireSiegeEngine(engine, land, kingdom, landKingdom);
						});
					}
					meta.setLore(LoreOrganizer.organize(lore));
					item.setItemMeta(meta);
				}
				inventory.setItem((1+x)+(9*(z+1)), item);
			}
			openInventory(player);
		}
		ItemStack r = new ItemStack(Material.HAY_BLOCK);
		ItemMeta rm = r.getItemMeta();
		rm.setDisplayName(Kingdoms.getLang().getString("Guis_ResourcePoints_Title", kp.getLang()));
		ArrayList rl = new ArrayList();
		rl.add(Kingdoms.getLang().getString("Guis_ResourcePoints_Desc", kp.getLang()));
		rl.add(Kingdoms.getLang().getString("Guis_ResourcePoints_Count", kp.getLang()).replaceAll("%amount%", ""+kingdom.getResourcepoints()));
		rm.setLore(LoreOrganizer.organize(rl));
		r.setItemMeta(rm);

		inventory.setItem(8, r);

	}

}
