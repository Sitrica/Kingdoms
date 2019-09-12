package com.songoda.kingdoms.beta.nms.v1_14_R1;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;

import com.songoda.kingdoms.beta.MapMaterial;
import com.songoda.kingdoms.beta.NMSHandler;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.IBlockAccess;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.MaterialMapColor;

public class v1_14_R1Handler implements NMSHandler {

	private final Map<MaterialMapColor, MapMaterial> colors = new HashMap<>();

	public v1_14_R1Handler() {
		for (MaterialMapColor color : MaterialMapColor.a) {
			if (color == null)
				continue;
			colors.put(color, new v1_14_R1Wrapper(color));
		}
	}

	@Override
	public MapMaterial getBlockColor(Block block) {
		net.minecraft.server.v1_14_R1.Block nmsblock = CraftMagicNumbers.getBlock(block.getType());
		CraftChunk chunk = (CraftChunk)block.getChunk();
		BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		IBlockData data = chunk.getHandle().getType(pos);
		IBlockAccess access = (IBlockAccess)chunk.getHandle().getWorld();
		@SuppressWarnings("deprecation")
		MaterialMapColor nms = nmsblock.e(data, access, pos);
		if (!colors.containsKey(nms))
			Bukkit.getLogger().severe("[ServerMinimap] unknown color, error in NMSHandler - please report to author!");
		return colors.get(nms);
	}

	@Override
	public MapMaterial getColorNeutral() {
		return colors.get(MaterialMapColor.b);
	}

	@Override
	public boolean isOffhand() {
		return false;
	}

}
