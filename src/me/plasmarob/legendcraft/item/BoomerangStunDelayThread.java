package me.plasmarob.legendcraft.item;

import org.bukkit.entity.LivingEntity;

/**
 * manager, which manages moves each tick
 * 
 * @author      Robert Thorne <plasmarob@gmail.com>
 * @version     0.3                
 * @since       2014-08-01
 */
public class BoomerangStunDelayThread implements Runnable {
	
	LivingEntity le;
	int i;
	BoomerangStunDelayThread(LivingEntity le, int i) {
		this.le = le;
		this.i = i;
	}
	
	public void update() {
		Boomerang.unStun(le,i);
	}
	
	public void run() {
	    try {
	        update();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
