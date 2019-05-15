package com.songoda.kingdoms.inventories;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.inventories.KingdomInventory;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.RankPermissions;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class PermissionsMenu extends KingdomInventory {

	private final RankManager rankManager;

	public PermissionsMenu() {
		super(InventoryType.CHEST, "permissions", 45);
		this.rankManager = instance.getManager("rank", RankManager.class);
	}

	@Override
	public void build(KingdomPlayer kingdomPlayer) {
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return;
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canEditPermissions()) {
			new MessageBuilder("kingdoms.rank-too-low-edit-permissions")
					.withPlaceholder(kingdom.getLowestRankFor(low -> low.canBuildStructures()), new Placeholder<Optional<Rank>>("%rank%") {
						@Override
						public String replace(Optional<Rank> rank) {
							if (rank.isPresent())
								return rank.get().getName();
							return "(Not attainable)";
						}
					})
					.setKingdom(kingdom)
					.send(kingdomPlayer);
			return;
		}
		// Access Protected
		Rank accessProtected = kingdom.getLowestRankOrDefault(permission -> permission.canAccessProtected());
		inventory.setItem(0, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.access-protected", accessProtected));
		setAction(0, event -> shuffle(kingdom, kingdomPlayer, accessProtected, permissions -> permissions.setProtectedAccess(true)));

		// Alliance
		Rank alliance = kingdom.getLowestRankOrDefault(permission -> permission.canAlliance());
		inventory.setItem(1, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.alliance", alliance));
		setAction(1, event -> shuffle(kingdom, kingdomPlayer, alliance, permissions -> permissions.setAlliance(true)));

		// Broadcast
		Rank broadcast = kingdom.getLowestRankOrDefault(permission -> permission.canBroadcast());
		inventory.setItem(2, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.broadcast", broadcast));
		setAction(2, event -> shuffle(kingdom, kingdomPlayer, broadcast, permissions -> permissions.setBroadcast(true)));

		// Build
		Rank build = kingdom.getLowestRankOrDefault(permission -> permission.canBuild());
		inventory.setItem(3, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.build", build));
		setAction(3, event -> shuffle(kingdom, kingdomPlayer, build, permissions -> permissions.setBuild(true)));

		// Build in Nexus
		Rank buildNexus = kingdom.getLowestRankOrDefault(permission -> permission.canBuildInNexus());
		inventory.setItem(4, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.build-in-nexus", buildNexus));
		setAction(4, event -> shuffle(kingdom, kingdomPlayer, buildNexus, permissions -> permissions.setNexusBuild(true)));

		// Build Structures
		Rank buildStructures = kingdom.getLowestRankOrDefault(permission -> permission.canBuildStructures());
		inventory.setItem(5, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.build-structures", buildStructures));
		setAction(5, event -> shuffle(kingdom, kingdomPlayer, buildStructures, permissions -> permissions.setBuildStructures(true)));

		// Claiming
		Rank claim = kingdom.getLowestRankOrDefault(permission -> permission.canClaim());
		inventory.setItem(6, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.claiming", claim));
		setAction(6, event -> shuffle(kingdom, kingdomPlayer, claim, permissions -> permissions.setClaiming(true)));

		// Edit Permissions
		Rank editPermissions = kingdom.getLowestRankOrDefault(permission -> permission.canEditPermissions());
		inventory.setItem(7, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.edit-permissions", editPermissions));
		setAction(7, event -> shuffle(kingdom, kingdomPlayer, editPermissions, permissions -> permissions.setEditPermissions(true)));

		// Grab Experience
		Rank experience = kingdom.getLowestRankOrDefault(permission -> permission.canGrabExperience());
		inventory.setItem(8, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.grab-experience", experience));
		setAction(8, event -> shuffle(kingdom, kingdomPlayer, experience, permissions -> permissions.setGrabExperience(true)));

		// Invade
		Rank invade = kingdom.getLowestRankOrDefault(permission -> permission.canInvade());
		inventory.setItem(9, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.invade", invade));
		setAction(9, event -> shuffle(kingdom, kingdomPlayer, invade, permissions -> permissions.setInvade(true)));

		// Invite
		Rank invite = kingdom.getLowestRankOrDefault(permission -> permission.canInvite());
		inventory.setItem(10, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.invite", invite));
		setAction(10, event -> shuffle(kingdom, kingdomPlayer, invite, permissions -> permissions.setInvite(true)));

		// Override Regulator
		Rank regulator = kingdom.getLowestRankOrDefault(permission -> permission.canOverrideRegulator());
		inventory.setItem(11, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.regulator", regulator));
		setAction(11, event -> shuffle(kingdom, kingdomPlayer, regulator, permissions -> permissions.setOverrideRegulator(true)));

		// Set Spawn
		Rank setSpawn = kingdom.getLowestRankOrDefault(permission -> permission.canSetSpawn());
		inventory.setItem(12, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.set-spawn", setSpawn));
		setAction(12, event -> shuffle(kingdom, kingdomPlayer, setSpawn, permissions -> permissions.setSpawn(true)));

		// Unclaim
		Rank unclaim = kingdom.getLowestRankOrDefault(permission -> permission.canUnclaim());
		inventory.setItem(13, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.unclaim", unclaim));
		setAction(13, event -> shuffle(kingdom, kingdomPlayer, unclaim, permissions -> permissions.setUnclaiming(true)));

		// Use Spawn
		Rank spawn = kingdom.getLowestRankOrDefault(permission -> permission.canUseSpawn());
		inventory.setItem(14, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.spawn", spawn));
		setAction(14, event -> shuffle(kingdom, kingdomPlayer, spawn, permissions -> permissions.setUseSpawn(true)));

		// Use Turrets
		Rank turrets = kingdom.getLowestRankOrDefault(permission -> permission.canUseTurrets());
		inventory.setItem(15, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.turrets", turrets));
		setAction(15, event -> shuffle(kingdom, kingdomPlayer, turrets, permissions -> permissions.setTurrets(true)));

		// Chest Access
		Rank chest = kingdom.getLowestRankOrDefault(permission -> permission.hasChestAccess());
		inventory.setItem(16, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.chest-access", chest));
		setAction(16, event -> shuffle(kingdom, kingdomPlayer, chest, permissions -> permissions.setChestAccess(true)));

		// Nexus Access
		Rank nexus = kingdom.getLowestRankOrDefault(permission -> permission.hasNexusAccess());
		inventory.setItem(17, getPermissionItem(kingdom, kingdomPlayer, "inventories.permissions.nexus-access", nexus));
		setAction(17, event -> shuffle(kingdom, kingdomPlayer, nexus, permissions -> permissions.setNexusAccess(true)));

		// Maximum Claims
		int i = 18;
		for (Rank rank : rankManager.getRanks()) {
			if (i >= inventory.getSize() - 1) // Holy cow that's a lot of ranks! (That many ranks is not going to be supported)
				continue;
			ItemStack maxClaims = new ItemStack(rank.getEditMaterial());
		    ItemMeta meta = maxClaims.getItemMeta();
			meta.setDisplayName(new MessageBuilder(false, "inventories.permissions.max-claims.title")
					.replace("%amount%", kingdom.getPermissions(rank).getMaximumClaims())
					.setPlaceholderObject(kingdomPlayer)
					.replace("%rank%", rank.getName())
					.fromConfiguration(inventories)
					.setKingdom(kingdom)
					.get());
			meta.setLore(new ListMessageBuilder(false, "inventories.permissions.max-claims.lore")
					.replace("%amount%", kingdom.getPermissions(rank).getMaximumClaims())
					.setPlaceholderObject(kingdomPlayer)
					.replace("%rank%", rank.getName())
					.fromConfiguration(inventories)
					.setKingdom(kingdom)
					.get());
			maxClaims.setItemMeta(meta);
			inventory.setItem(i, maxClaims);
			int allowed = configuration.getInt("claiming.maximum-claims", -1);
			setAction(i, event -> {
				RankPermissions permissions = kingdom.getPermissions(rank);
				int max = permissions.getMaximumClaims();
				if (event.isRightClick() && max > 0) {
					permissions.setMaximumClaims(max--);
					reopen(kingdomPlayer);
				} else if (event.isLeftClick() && allowed <= 0 || max < allowed) {
					permissions.setMaximumClaims(max++);
					reopen(kingdomPlayer);
				}
			});
			i++;
		}
	    ItemStack back = new ItemStack(Material.REDSTONE_BLOCK);
	    ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(new MessageBuilder(false, "inventories.permissions.back-button.title")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.get());
		meta.setLore(new ListMessageBuilder(false, "inventories.permissions.back-button.lore")
				.setPlaceholderObject(kingdomPlayer)
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.get());
		back.setItemMeta(meta);
		inventory.setItem(inventory.getSize() - 1, back);
		setAction(inventory.getSize() - 1, event -> instance.getManager("inventory", InventoryManager.class).getInventory(NexusInventory.class).build(kingdomPlayer));
	    openInventory(kingdomPlayer.getPlayer());
	}

	public Rank shuffle(Kingdom kingdom, KingdomPlayer kingdomPlayer, Rank rank, Consumer<RankPermissions> predicate) {
		List<RankPermissions> list = kingdom.getPermissions();
		RankPermissions permissions = kingdom.getPermissions(rank);
		int index = list.indexOf(permissions);
		if (index <= 0)
			return rank;
		RankPermissions next = list.get(index + 1);
		if (next == null)
			return rank;
		predicate.accept(next);
		kingdom.setRankPermissions(next);
		reopen(kingdomPlayer);
		return next.getRank();
	}

	public ItemStack getPermissionItem(Kingdom kingdom, KingdomPlayer kingdomPlayer, String node, Rank rank) {
		ItemStack item = new ItemStack(rank.getEditMaterial());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(new MessageBuilder(false, node + ".title")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%color%", rank.getColor())
				.replace("%rank%", rank.getName())
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.get());
		meta.setLore(new ListMessageBuilder(false, node + ".lore")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%color%", rank.getColor())
				.replace("%rank%", rank.getName())
				.fromConfiguration(inventories)
				.setKingdom(kingdom)
				.get());
		item.setItemMeta(meta);
		return item;
	}

}
