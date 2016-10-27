package me.plasmarob.legendcraft.item;

import org.bukkit.block.Block;

import me.plasmarob.legendcraft.util.Tools;

/**
 * button delay, which simulating redstone interaction for boomerang
 * 
 * @author      Robert Thorne <plasmarob@gmail.com>
 * @version     0.5                
 * @since       2015-08-01
 */
public class ButtonDelayThread implements Runnable {
	
	Block block;
	public ButtonDelayThread(Block block) {
		this.block = block;
	}
	
	public void update() {
		Tools.toggleButton(block, false);
	}
	
	public void run() {
	    try {
	        update();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
