package me.plasmarob.legendcraft.item;

import org.bukkit.entity.Player;

/**
 * gust jar 2-second delay
 * 
 * @author      Robert Thorne <plasmarob@gmail.com>
 * @version     0.8               
 * @since       2015-08-01
 */
public class GustJarPhaseThread implements Runnable {
	
	Player p;
	GustJarPhaseThread(Player p) {
		this.p = p;
	}
	
	public void update() {
		GustJar.nextPhase(p);
	}
	
	public void run() {
	    try {
	        update();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
