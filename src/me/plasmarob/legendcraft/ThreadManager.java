package me.plasmarob.legendcraft;

import me.plasmarob.legendcraft.blocks.Tune;
import me.plasmarob.legendcraft.item.Bomb;
import me.plasmarob.legendcraft.item.Boomerang;
import me.plasmarob.legendcraft.item.FireRodBlast;
import me.plasmarob.legendcraft.item.GustJar;
import me.plasmarob.legendcraft.item.Hookshot;
import me.plasmarob.legendcraft.item.IceRodBlast;

/**
 * manager, which manages moves each tick
 * 
 * @author      Robert Thorne <plasmarob@gmail.com>
 * @version     0.3                
 * @since       2014-08-01
 */
public class ThreadManager implements Runnable {
	ThreadManager()
	{
	}
	
	public void update()
	{
		for (String s : Dungeon.getDungeons().keySet())
			Dungeon.getDungeons().get(s).updateIfEnabled();
		
		Tune.progressAll();
		
		GustJar.progressAll();
		Boomerang.progressAll();
		Hookshot.progressAll();
		Bomb.progressAll();
		IceRodBlast.progressAll();
		FireRodBlast.progressAll();
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