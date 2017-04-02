package me.plasmarob.legendcraft.blocks;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class KeyholeRenderer extends MapRenderer {

	@SuppressWarnings("deprecation")
	@Override
	public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
		mapCanvas.setPixel(5, 5, (byte)5);
		mapCanvas.setPixel(15, 15, (byte)6);
		mapCanvas.setPixel(15, 16, (byte)7);
		mapCanvas.setPixel(15, 18, MapPalette.DARK_GRAY);
	}

}
