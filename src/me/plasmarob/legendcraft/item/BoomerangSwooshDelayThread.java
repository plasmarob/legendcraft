package me.plasmarob.legendcraft.item;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * manager, which manages moves each tick
 * 
 * @author      Robert Thorne <plasmarob@gmail.com>
 * @version     0.3                
 * @since       2014-08-01
 */
public class BoomerangSwooshDelayThread implements Runnable {
	
	Player player;
	Entity e;
	BoomerangSwooshDelayThread(Player p, Entity e) {
		this.e = e;
		player = p;
	}
	
	public void update() {
		if (!player.isDead() && !e.isDead()) {
			player.playSound(e.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.2f, 0.5f);
			Boomerang.repeatSound(player);
		}
	}
	
	public void run() {
	    try {
	        update();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
