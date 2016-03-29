package me.plasmarob.legendcraft.blocks;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * manager, which manages moves each tick
 * 
 * @author      Robert Thorne <plasmarob@gmail.com>
 * @version     0.3                
 * @since       2014-10-05
 */
@SuppressWarnings("unused")
public class SenderDelayThread implements Runnable {
	
	Sender sender;
	SenderDelayThread(Sender detector)
	{
		this.sender = detector;
	}
	
	public void update()
	{
		sender.run();
	}
	
	public void run()
	{
	    try {
	        update();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
