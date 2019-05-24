package com.songoda.kingdoms.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.inventories.PagesInventory;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.RankPermissions;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class PermissionsMenu extends PagesInventory {

	private final RankManager rankManager;

	public PermissionsMenu() {
		super("permissions", 45);
		this.rankManager = instance.getManager(RankManager.class);
	}

	public List<PageItem> getItems(KingdomPlayer kingdomPlayer) {
		List<PageItem> items = new ArrayList<>();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null)
			return items;
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canEditPermissions()) {
			new MessageBuilder("kingdoms.rank-too-low-edit-permissions")
					.withPlaceholder(kingdom.getLowestRankFor(low -> low.canEditPermissions()), new Placeholder<Optional<Rank>>("%rank%") {
						@Override
						public String replace(Optional<Rank> rank) {
							if (rank.isPresent())
								return rank.get().getName();
							return "(Not attainable)";
						}
					})
					.setKingdom(kingdom)
					.send(kingdomPlayer);
			return items;
		}

		// Access Protected
		Rank accessProtected = kingdom.getLowestRankOrDefault(permission -> permission.canAccessProtected());
		ItemStack item = getPermissionItem(kingdomPlayer, "inventories.permissions.access-protected", accessProtected);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, accessProtected, (permissions, value) -> permissions.setProtectedAccess(value))));

		// Alliance
		Rank alliance = kingdom.getLowestRankOrDefault(permission -> permission.canAlliance());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.alliance", alliance);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, alliance, (permissions, value) -> permissions.setAlliance(value))));

		// Broadcast
		Rank broadcast = kingdom.getLowestRankOrDefault(permission -> permission.canBroadcast());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.broadcast", broadcast);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, broadcast, (permissions, value) -> permissions.setBroadcast(value))));

		// Build
		Rank build = kingdom.getLowestRankOrDefault(permission -> permission.canBuild());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.build", build);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, build, (permissions, value) -> permissions.setBuild(value))));

		// Build in Nexus
		Rank buildNexus = kingdom.getLowestRankOrDefault(permission -> permission.canBuildInNexus());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.build-in-nexus", buildNexus);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, buildNexus, (permissions, value) -> permissions.setNexusBuild(value))));

		// Build Structures
		Rank buildStructures = kingdom.getLowestRankOrDefault(permission -> permission.canBuildStructures());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.build-structures", buildStructures);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, buildStructures, (permissions, value) -> permissions.setBuildStructures(value))));

		// Claiming
		Rank claim = kingdom.getLowestRankOrDefault(permission -> permission.canClaim());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.claiming", claim);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, claim, (permissions, value) -> permissions.setClaiming(value))));

		// Edit Permissions
		Rank editPermissions = kingdom.getLowestRankOrDefault(permission -> permission.canEditPermissions());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.edit-permissions", editPermissions);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, editPermissions, (permissions, value) -> permissions.setEditPermissions(value))));

		// Grab Experience
		Rank experience = kingdom.getLowestRankOrDefault(permission -> permission.canGrabExperience());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.grab-experience", experience);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, experience, (permissions, value) -> permissions.setGrabExperience(value))));

		// Invade
		Rank invade = kingdom.getLowestRankOrDefault(permission -> permission.canInvade());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.invade", invade);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, invade, (permissions, value) -> permissions.setInvade(value))));

		// Invite
		Rank invite = kingdom.getLowestRankOrDefault(permission -> permission.canInvite());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.invite", invite);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, invite, (permissions, value) -> permissions.setInvite(value))));

		// Override Regulator
		Rank regulator = kingdom.getLowestRankOrDefault(permission -> permission.canOverrideRegulator());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.regulator", regulator);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, regulator, (permissions, value) -> permissions.setOverrideRegulator(value))));

		// Set Spawn
		Rank setSpawn = kingdom.getLowestRankOrDefault(permission -> permission.canSetSpawn());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.set-spawn", setSpawn);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, setSpawn, (permissions, value) -> permissions.setSpawn(value))));

		// Unclaim
		Rank unclaim = kingdom.getLowestRankOrDefault(permission -> permission.canUnclaim());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.unclaim", unclaim);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, unclaim, (permissions, value) -> permissions.setUnclaiming(value))));

		// Use Spawn
		Rank spawn = kingdom.getLowestRankOrDefault(permission -> permission.canUseSpawn());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.spawn", spawn);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, spawn, (permissions, value) -> permissions.setUseSpawn(value))));

		// Use Turrets
		Rank turrets = kingdom.getLowestRankOrDefault(permission -> permission.canUseTurrets());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.turrets", turrets);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, turrets, (permissions, value) -> permissions.setTurrets(value))));

		// Chest Access
		Rank chest = kingdom.getLowestRankOrDefault(permission -> permission.hasChestAccess());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.chest-access", chest);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, chest, (permissions, value) -> permissions.setChestAccess(value))));

		// Nexus Access
		Rank nexus = kingdom.getLowestRankOrDefault(permission -> permission.hasNexusAccess());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.nexus-access", nexus);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, nexus, (permissions, value) -> permissions.setNexusAccess(value))));

		// Kick
		Rank kick = kingdom.getLowestRankOrDefault(permission -> permission.canKick());
		item = getPermissionItem(kingdomPlayer, "inventories.permissions.kick", kick);
		items.add(new PageItem(item, event -> cycle(kingdomPlayer, kick, (permissions, value) -> permissions.setKick(value))));

		// Maximum Claims
		for (Rank rank : rankManager.getRanks()) {
			if (rank.equals(rankManager.getOwnerRank()))
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
			int allowed = configuration.getInt("claiming.maximum-claims", -1);
			items.add(new PageItem(maxClaims, event -> {
				RankPermissions permissions = kingdom.getPermissions(rank);
				int max = permissions.getMaximumClaims();
				if (event.isRightClick()) {
					if (max <= 0)
						return;
					permissions.setMaximumClaims(max - 1);
					kingdom.setRankPermissions(permissions);
					reopen(kingdomPlayer);
				} else if (event.isLeftClick()) {
					if (allowed <= 0 || max < allowed) {
						permissions.setMaximumClaims(max + 1);
						kingdom.setRankPermissions(permissions);
						reopen(kingdomPlayer);
					}
				}
			}));
		}
		return items;
	}

	public Rank cycle(KingdomPlayer kingdomPlayer, Rank rank, BiConsumer<RankPermissions, Boolean> consumer) {
		List<Rank> list = rankManager.getRanks();
		int index = list.indexOf(rank);
		if (index < 0)
			return rank;
		Rank next = null;
		if (index == list.size() - 1)
			next = list.get(0);
		else if (index + 1 < list.size())
			next = list.get(index + 1);
		if (next == null)
			return rank;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		for (Rank below : rankManager.getRanksBelow(next)) {
			RankPermissions permissions = kingdom.getPermissions(below);
			consumer.accept(permissions, false);
			kingdom.setRankPermissions(permissions);
		}
		for (Rank above : rankManager.getRanksAbove(next)) {
			RankPermissions permissions = kingdom.getPermissions(above);
			consumer.accept(permissions, true);
			kingdom.setRankPermissions(permissions);
		}
		RankPermissions permissions = kingdom.getPermissions(next);
		consumer.accept(permissions, true);
		kingdom.setRankPermissions(permissions);
		reopen(kingdomPlayer);
		return next;
	}

	public ItemStack getPermissionItem(KingdomPlayer kingdomPlayer, String node, Rank rank) {
		ItemStack item = new ItemStack(rank.getEditMaterial());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(new MessageBuilder(false, node + ".title")
				.setKingdom(kingdomPlayer.getKingdom())
				.setPlaceholderObject(kingdomPlayer)
				.replace("%color%", rank.getColor())
				.replace("%rank%", rank.getName())
				.fromConfiguration(inventories)
				.get());
		meta.setLore(new ListMessageBuilder(false, node + ".lore")
				.setKingdom(kingdomPlayer.getKingdom())
				.setPlaceholderObject(kingdomPlayer)
				.replace("%color%", rank.getColor())
				.replace("%rank%", rank.getName())
				.fromConfiguration(inventories)
				.get());
		item.setItemMeta(meta);
		return item;
	}

	@Override
	protected Consumer<InventoryClickEvent> getBackAction(KingdomPlayer kingdomPlayer) {
		return event -> instance.getManager(InventoryManager.class).getInventory(NexusInventory.class).open(kingdomPlayer);
	}

}
