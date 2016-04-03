package me.plasmarob.legendcraft.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import me.plasmarob.legendcraft.LegendCraft;
import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.NBTTagCompound;


public class FireRodBlast {
	
	public static ConcurrentHashMap<Player, FireRodBlast> instances = new ConcurrentHashMap<Player, FireRodBlast>();
	
	int time;
	BlockIterator bit;
	static final int damage = 4;
	Player p;
	FireRodEffect fireEffect;
	Block block;
	int wait = 0;
	boolean hasExploded = false;
	
	public FireRodBlast(Player player) {
		
		if (instances.containsKey(player))
		{
			instances.get(player).explode();
		}
		else {
			fireEffect = new FireRodEffect(LegendCraft.getEffectManager(), player);
			fireEffect.start();	
			instances.put(player, this);
			p = player;
			bit = new BlockIterator(player, 12);
			block = bit.next();
		}
	}
	
	//@SuppressWarnings("unused")
	private boolean progress() {
			
		if (bit.hasNext()) {
			block = bit.next();
		} else {
			explode();
			return true;
		}
		
		
		
		
		
		
		Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getLocation(), 3, 3, 3);
		
		for (Entity e : entities) {
			if (!(e instanceof LivingEntity) || e instanceof Player)
				continue;
			
			if(e instanceof Blaze || e instanceof MagmaCube || e instanceof Ghast ) {
				if (e instanceof Damageable) {
					((Damageable)e).damage(damage/2);
				}
				explode();
			}
			else
			{
				if (e instanceof Damageable) {
					((Damageable)e).damage(damage);
					e.setFireTicks(100);
				}
				explode();
			}
		}
		
		if (block.getType() != Material.AIR) {
			explode();
		}

		return false;
	}
	
	public void explode()
	{
		if (hasExploded)
			return;
		hasExploded = true;
		if (block != null)
			block.getWorld().createExplosion(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ(), 2f, false, false);
		instances.remove(p);
		fireEffect.cancel();
	}
	
	public static void progressAll() {
		if (instances.size() > 0)
			for (Player p : instances.keySet())
				instances.get(p).progress();
	}
	public static boolean progress(Player p) {
		if (instances.get(p) != null)
			return instances.get(p).progress();
		return false;
	}
}


