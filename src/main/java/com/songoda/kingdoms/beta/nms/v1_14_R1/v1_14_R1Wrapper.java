package com.songoda.kingdoms.beta.nms.v1_14_R1;

import com.songoda.kingdoms.beta.MapMaterial;

import net.minecraft.server.v1_14_R1.MaterialMapColor;

public class v1_14_R1Wrapper implements MapMaterial {

	private final MaterialMapColor color;

	public v1_14_R1Wrapper(MaterialMapColor color) {
		this.color = color;
	}

	@Override
	public int getMaterialID() {
		return color.ac;
	}

}
