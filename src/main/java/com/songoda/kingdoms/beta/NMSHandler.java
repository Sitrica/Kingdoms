package com.songoda.kingdoms.beta;

import org.bukkit.block.Block;

public interface NMSHandler {

	MapMaterial getBlockColor(Block block);

	MapMaterial getColorNeutral();

	boolean isOffhand();

}
