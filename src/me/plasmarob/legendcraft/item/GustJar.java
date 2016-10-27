package me.plasmarob.legendcraft.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.util.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class GustJar {
	
	public static HashMap<Player, GustJar> instances = new HashMap<Player, GustJar>();
	public static ConcurrentHashMap<LivingEntity,Integer> stunned = new ConcurrentHashMap<LivingEntity,Integer>();
	private List<FallingBlock> gravels = new ArrayList<FallingBlock>();
	public static List<FallingBlock> fallers = new ArrayList<FallingBlock>();
	
	protected static ConcurrentHashMap<Vector,BlockIterator> puffPhys = 
			new ConcurrentHashMap<Vector,BlockIterator>();
	
	private int phase = 0;
	Player p;
	protected GustJarEffect gustEffect;
	int gravelCount = 0;
	
	int sound = 0;
	
	public GustJar(Player p) {
		if (instances.containsKey(p))
			return;
		
		this.p = p;
		instances.put(p, this);
		
		gustEffect = new GustJarEffect(LegendCraft.getEffectManager(), p);
		gustEffect.start();	
		increment();
	}

	@SuppressWarnings("deprecation")
	private void increment() {
		gustEffect.cancel();
		gustEffect = new GustJarEffect(LegendCraft.getEffectManager(), p);
		gustEffect.start();
		
		if (phase < 3)
		{
			phase++;
			p.setItemInHand(new ItemStack(Material.INK_SACK, 1, (short)0, (byte)phase));
			LegendCraft.plugin.getServer().getScheduler().scheduleSyncDelayedTask(LegendCraft.plugin, 
				new GustJarPhaseThread(p), Math.round(2.0*20));
		}
	}
	
	public static void nextPhase(Player p) {
		if (instances.containsKey(p))
			instances.get(p).increment();
	}
	
	@SuppressWarnings("deprecation")
	public static void remove(Player p) {
		if (instances.containsKey(p))
		{
			Location loc = p.getEyeLocation().clone();
			loc.setPitch(0);
			double vel = -1.5;
			if (!p.isOnGround())
				vel = -0.5;
			p.setVelocity(loc.getDirection().normalize().multiply(vel));
			
			GustPuffEffect puffEffect = new GustPuffEffect(LegendCraft.getEffectManager(), p);
			puffEffect.start();	
			
			instances.get(p).tryLaunch();
			
			instances.get(p).gustEffect.cancel();
			instances.remove(p);
		}	
	}

	@SuppressWarnings("deprecation")
	public void tryLaunch() {
		
		puffPhys.put(p.getEyeLocation().getDirection().normalize(), 
				new BlockIterator(p,12));
		
		
		BlockIterator bit = new BlockIterator(p,2);
		Block next = bit.next();
		while (bit.hasNext())
			next = bit.next();
		
		Vector v = p.getEyeLocation().getDirection();
		
		if (gravelCount >= 12)
		{
			FallingBlock fb = p.getWorld().spawnFallingBlock(
					next.getLocation(), Material.STONE, (byte)0 );
			fb.setDropItem(false);
			fb.setVelocity(v.normalize().multiply(1.2));
			fallers.add(fb);
		} else if (gravelCount >= 4)
		{
			FallingBlock fb = p.getWorld().spawnFallingBlock(
					next.getLocation(), Material.COBBLESTONE, (byte)0 );
			fb.setDropItem(false);
			fb.setVelocity(v.normalize().multiply(1.2));
			fallers.add(fb);
		} else if (gravelCount > 0)
		{
			FallingBlock fb = p.getWorld().spawnFallingBlock(
					next.getLocation(), Material.GRAVEL, (byte)0 );
			fb.setDropItem(false);
			fb.setVelocity(v.normalize().multiply(1.2));
			fallers.add(fb);
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean progress()
	{
		List<Entity> entities = p.getNearbyEntities(10, 5, 10);
		List<Entity> list = new ArrayList<Entity>();
		list.addAll(entities);
		for (Entity entity : entities) {
			if ( !(entity instanceof Item) )
				list.remove(entity);
		}
		List<Entity> items = new ArrayList<Entity>();
		
		for (int g = 0; g < gravels.size(); g++)
		{
			if (gravels.get(g) == null || gravels.get(g).isDead()) {
				gravels.remove(g);
				g--;
				continue;
			}
			if (gravels.get(g).getLocation().distance(p.getEyeLocation()) < 4) {
				gravels.get(g).remove();
				gravels.remove(g);
				g--;
				gravelCount++;
				continue;
			}
			Vector v = Tools.getDirection(gravels.get(g).getLocation().clone(), 
					p.getLocation().clone().add(0,1,0));
			gravels.get(g).setVelocity(v.normalize().multiply(1.6));
		}
		
		BlockIterator bit = new BlockIterator (p,6+(2*phase)); 
		Block next = bit.next();
		while (bit.hasNext())
		{
			for (Entity entity : list) {
				if (entity.getLocation().distance(next.getLocation()) < 1) 
					items.add(entity);
			}
				
			if (next.getType() == Material.CARPET) {
				next.setType(Material.AIR);
				break;
			} else if (next.getType() == Material.GRAVEL) {
				next.setType(Material.AIR);
				FallingBlock fb = p.getWorld().spawnFallingBlock(
						next.getLocation(), Material.GRAVEL, (byte)0 );
				fb.setDropItem(false);
				Vector v = Tools.getDirection(next.getLocation().clone(), 
						p.getLocation().clone().add(0,1,0));
				fb.setVelocity(v.normalize().multiply(1.6));
				
				gravels.add(fb);
				
				break;	
			}
			
			if (!Tools.canSeeThrough(next.getType()))
				break;
			next = bit.next();
		}
		
		
		
		if (sound == 0){
			p.playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.7f, 0.1f);
			// sound = (int)Math.pow(2, 4-phase); // 1:8, 2:4, 3:2
			switch (phase) {
			case 1: sound = 7;
				break;
			case 2: sound = 4;
				break;
			case 3: sound = 2;
			}
		}
		sound--;
		
		
		for (Entity it : items) {
			Vector v = Tools.getDirection(it.getLocation().clone(), 
					p.getLocation().clone().add(0,1,0));
			it.setVelocity(v.normalize().multiply(1.6));
		}
		
		return false;
	}
	
	// List<Entity> entities = fallers.get(i).getNearbyEntities(1, 1, 1);
	
	public static void progressAll() {
		if (instances.size() > 0)
			for (Player p : instances.keySet())
				instances.get(p).progress();
		
		List<Vector> removers = new ArrayList<Vector>();
		for (Vector v : puffPhys.keySet()) {
			BlockIterator bit = puffPhys.get(v);
			if (bit != null && bit.hasNext()) {
				Block next = bit.next();
				if (next.getType() == Material.TORCH ||
						next.getType() == Material.REDSTONE_TORCH_OFF ||
						next.getType() == Material.REDSTONE_TORCH_ON)
				{
					next.setType(Material.AIR);
				}
				
				List<Entity> entities = Tools.getEntitiesAroundPoint(next.getLocation(), 2.0); 
				for (Entity e: entities)
				{
					if (!(e instanceof Player))
						e.setVelocity(v.multiply(5));
				}
			}
			else
				removers.add(v);
		}
		for (Vector v : removers)
			puffPhys.remove(v);
	
		for (int i = 0; i < fallers.size() && i > -1; i++)
		{
			Block current = fallers.get(i).getLocation().getBlock();
			
			if (fallers.get(i).isDead())
			{
				fallers.remove(i);
				i--;
				continue;
			}
			
			if (current.getType() == Material.WOOD_BUTTON ||
					current.getType() == Material.STONE_BUTTON)
			{
				Tools.toggleButton(current, true);
				fallers.get(i).remove();
				fallers.remove(i);
				i--;
				continue;
			}
			if (current.getType() == Material.LEVER)
			{
				Tools.toggleLever(current);
				fallers.get(i).remove();
				fallers.remove(i);
				i--;
				continue;
			}
			
			List<Entity> entities = fallers.get(i).getNearbyEntities(1, 1, 1);
			Material mat = fallers.get(i).getMaterial();
			for (int e = 0; e < entities.size(); e++)
			{
				if (entities.get(e) instanceof LivingEntity &&
						!(entities.get(e) instanceof Player))
				{
					if (mat == Material.STONE)
						((Damageable)entities.get(e)).damage(8);
					else if (mat == Material.COBBLESTONE)
						((Damageable)entities.get(e)).damage(4);
					else
						((Damageable)entities.get(e)).damage(2);
					fallers.get(i).remove();
					fallers.remove(i);
					i--;
					break;
				}
			}
		}
	}

	public static boolean progress(Player p) {
		if (instances.get(p) != null)
			return instances.get(p).progress();
		return false;
	}
}
