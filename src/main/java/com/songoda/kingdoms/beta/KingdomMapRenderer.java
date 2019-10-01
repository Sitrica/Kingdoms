package com.songoda.kingdoms.beta;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class KingdomMapRenderer extends MapRenderer {

	@Override
	public void render(MapView arg0, MapCanvas arg1, Player arg2) {
		// TODO Auto-generated method stub
		
	}

//	private final Queue<ChunkDaddy> queue = new LinkedList<>();
//	private final int scale;
//
//	public KingdomMapRenderer(int scale) {
//		this.scale = scale;
//	}
//
//	@Override
//	public void render(MapView map, MapCanvas canvas, Player player) {
//		if (!Kingdoms.getInstance().getManager(WorldManager.class).acceptsWorld(player.getWorld()))
//			return;
//		World world = player.getWorld();
//		int locX = player.getLocation().getBlockX() / scale - 64;
//		int locZ = player.getLocation().getBlockZ() / scale - 64;
//		for (int i = 0; i < 128; ++i) {
//			for (int j = 0; j < 128; ++j) {
//				int x = (locX + i) / 16;
//				if (locX + i < 0 && (locX + i) % 16 != 0)
//					--x;
//				int z = (locZ + j) / 16;
//				if (locZ + j < 0 && (locZ + j) % 16 != 0)
//					--z;
//				if (cacheMap.containsKey(x) && cacheMap.get(x).containsKey(z)) {
//	                    MapMaterial color = cacheMap.get(x).get(z).get(Math.abs(locX + i + 16 * Math.abs(x)) % 16, Math.abs(locZ + j + 16 * Math.abs(z)) % 16);
//	                    short avgY = cacheMap.get(x).get(z).getY(Math.abs(locX + i + 16 * Math.abs(x)) % 16, Math.abs(locZ + j + 16 * Math.abs(z)) % 16);
//						short prevY = this.getPrevY(x, z, Math.abs(locX + i + 16 * Math.abs(x)) % 16, Math.abs(locZ + j + 16 * Math.abs(z)) % 16, player.getWorld().getName());
//	                    double d2 = (avgY - prevY) * 4.0 / (scale + 4) + ((i + j & 0x1) - 0.5) * 0.4;
//	                    byte b0 = 1;
//	                    if (d2 > 0.6)
//	                        b0 = 2;
//	                    if (d2 < -0.6)
//	                        b0 = 0;
//	                    canvas.setPixel(i, j, (byte)(color.getM() * 4 + b0));
//	                } else {
//	                    canvas.setPixel(i, j, (byte)0);
//	                    if (this.queue.size() >= 200)
//	                        break;
//	                    addToQueue(x, z, true, world.getName());
//	                }
//			}
//		}
//	}

}
