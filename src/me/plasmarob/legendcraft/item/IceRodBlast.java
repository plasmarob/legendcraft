package me.plasmarob.legendcraft.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import me.plasmarob.legendcraft.LegendCraft;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.NBTTagCompound;


public class IceRodBlast {
	
	public static ConcurrentHashMap<Integer, IceRodBlast> instances = new ConcurrentHashMap<Integer, IceRodBlast>();
	public static ConcurrentHashMap<LivingEntity, FreezeBlock> victims = new ConcurrentHashMap<LivingEntity, FreezeBlock>();
	
	
	private static int counter = 0;
	private int id = 0;
	int time;
	BlockIterator bit;
	static final int damage = 4;
	
	IceRodEffect iceEffect;
	
	public IceRodBlast(Player player) {
		iceEffect = new IceRodEffect(LegendCraft.getEffectManager(), player);
		iceEffect.start();	
		id = counter++;
		instances.put(id, this);
		
		bit = new BlockIterator(player, 12);
		
	}
	
	@SuppressWarnings("deprecation")
	private boolean progress() {
		Block block;
		if (bit.hasNext()) {
			block = bit.next();
		} else {
			instances.remove(id);
			iceEffect.cancel();
			return true;
		}
		
		
		
		
		
		
		Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getLocation(), 2, 2, 2);
		
		for (Entity e : entities) {
			if (!(e instanceof LivingEntity) || e instanceof Player)
				continue;
			if(e instanceof Blaze || e instanceof MagmaCube || e instanceof Ghast ) {
				if (e instanceof Damageable) {
					((Damageable)e).damage(damage*2);
				}
				instances.remove(id);
				iceEffect.cancel();
				return true;
			}
			
			if (!victims.containsKey(e)) {
				victims.put((LivingEntity)e, new FreezeBlock((LivingEntity)e));
				//stun
				net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) e).getHandle();
		        NBTTagCompound tag = new NBTTagCompound();
		        nmsEntity.c(tag);
		        tag.setBoolean("NoAI", true);
		        tag.setBoolean("Silent", true);
		        EntityLiving el = (EntityLiving) nmsEntity;
		        el.a(tag);
			}
		}
		
		if (block.getType() == Material.STATIONARY_WATER && block.getData() == (byte)0) {
			//block.setType(Material.FROSTED_ICE);
			Block t;
			ArrayList<Block> ices = new ArrayList<Block>();
			for (int i = -1; i <=1; i++)
				for (int j = -1; j <=1; j++) {
					t = block.getRelative(i,0,j);
					if (t.getType() == Material.STATIONARY_WATER && t.getData() == (byte)0) {
						ices.add(t);
					}
				}
			for (Block b : ices) {
				b.setType(Material.FROSTED_ICE);
			}		
		}
		
		if (block.getType() != Material.AIR) {
			instances.remove(id);
			iceEffect.cancel();
		}

		return false;
	}
	
	public static void progressAll() {
		if (instances.size() > 0)
			for (Integer i : instances.keySet())
				instances.get(i).progress();
		
		for (Entity e : victims.keySet()) {
			int num = victims.get(e).update();
			if (num > 120) {
				victims.get(e).melt();
				victims.remove(e);
				
				//stun
				net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) e).getHandle();
		        NBTTagCompound tag = new NBTTagCompound();
		        nmsEntity.c(tag);
		        tag.setBoolean("NoAI", false);
		        tag.setBoolean("Silent", false);
		        EntityLiving el = (EntityLiving) nmsEntity;
		        el.a(tag);
		        
				if (e instanceof Damageable) {
					((Damageable)e).damage(damage);
				}
			}
		}
	}
	public static boolean progress(Integer i) {
		if (instances.get(i) != null)
			return instances.get(i).progress();
		return false;
	}
}

class FreezeBlock {
	
	private Location loc;
	private int timeLeft = 0;
	private Block b;
	FreezeBlock(LivingEntity e) {
		timeLeft = 0;
		loc = e.getEyeLocation();
		b = loc.getBlock();
		
		for (int i = -1; i <= 1; i++)
		for (int j = -1; j <= 1; j++)
		for (int k = -1; k <= 1; k++) {
			if (b.getRelative(i,j,k).getType() == Material.AIR)
				b.getRelative(i,j,k).setType(Material.ICE);
		}
	}
	
	int update() {
		timeLeft++;
		return timeLeft;
	}
	
	public void melt() {
		for (int i = -1; i <= 1; i++)
		for (int j = -1; j <= 1; j++)
		for (int k = -1; k <= 1; k++) {
			if (b.getRelative(i,j,k).getType() == Material.ICE || 
					b.getRelative(i,j,k).getType() == Material.WATER || 
					b.getRelative(i,j,k).getType() == Material.STATIONARY_WATER )
				b.getRelative(i,j,k).setType(Material.AIR);
		}
	}
	
}








