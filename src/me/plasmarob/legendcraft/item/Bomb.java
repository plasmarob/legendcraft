package me.plasmarob.legendcraft.item;

import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.LegendCraft;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Bomb {

	public static ConcurrentHashMap<Integer, Bomb> instances = new ConcurrentHashMap<Integer, Bomb>();

	private static int counter = 0;
	private int number = 0;
	
	ItemStack is;
	Item bombItem;
	int time = 0;
	BombExplosionEffect bombEffect;
	
	@SuppressWarnings("deprecation")
	public Bomb(Player player) {
		is = new ItemStack(Material.INK_SACK,1,(short)0,(byte)5);
		bombItem = player.getWorld().dropItem(player.getEyeLocation(), is);
		bombItem.setPickupDelay(32767);
		bombItem.setVelocity(player.getEyeLocation().getDirection().normalize());
		
		bombEffect = new BombExplosionEffect(LegendCraft.getEffectManager(), bombItem);
		bombEffect.start();	
		number = counter++;
		instances.put(number, this);
	}

	
	
	
	
	private boolean progress() {
		time++;
		if (time > 90) {
			instances.remove(number);
			bombItem.getWorld().createExplosion(bombItem.getLocation().getX(), bombItem.getLocation().getY(), bombItem.getLocation().getZ(), 4f, false, false);
			bombItem.remove();
			bombEffect.cancel();
		}
		return false;
	}
	
	
	public static void progressAll() {
		if (instances.size() > 0)
			for (Integer i : instances.keySet())
				instances.get(i).progress();
	}
	public static boolean progress(Integer i) {
		if (instances.get(i) != null)
			return instances.get(i).progress();
		return false;
	}
}
