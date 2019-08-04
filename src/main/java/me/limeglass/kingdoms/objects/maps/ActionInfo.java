package me.limeglass.kingdoms.objects.maps;

import java.util.Optional;
import java.util.UUID;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.manager.managers.LandManager.LandInfo;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.maps.RelationOptions.RelationAction;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

/**
 * Used to execute BiConsumers(ActionConsumers) in ClickEvents for the '/k map' command.
 */
public class ActionInfo {

	private final RelationAction action;
	private final LandInfo land;
	private final UUID uuid;

	public ActionInfo(LandInfo land, UUID uuid, RelationAction action) {
		this.action = action;
		this.land = land;
		this.uuid = uuid;
	}

	public Optional<KingdomPlayer> getKingdomPlayer() {
		return Kingdoms.getInstance().getManager(PlayerManager.class).getKingdomPlayer(uuid);
	}

	public RelationAction getAction() {
		return action;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Land getLand() {
		return land.get();
	}

	public void execute() {
		Optional<KingdomPlayer> player = getKingdomPlayer();
		if (player.isPresent())
			action.execute(getLand(), player.get());
	}

}
