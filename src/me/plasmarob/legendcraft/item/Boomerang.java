package me.plasmarob.legendcraft.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.util.Tools;
import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.NBTTagCompound;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

//TODO: stun ends when damage is taken
//TODO: stun immune after stun, for 1.5 seconds
public class Boomerang {

	public static HashMap<Player, Boomerang> instances = new HashMap<Player, Boomerang>();
	public static ConcurrentHashMap<LivingEntity,Integer> stunned = new ConcurrentHashMap<LivingEntity,Integer>();
	
	private static int boomerangCounter = 0;
	private int boomerangNumber = 0;
	Player player;
	
	ItemStack is;
	Item boomItem;
	int age = 0;
	int middleAge = 5000;
	
	int yawdiff = 0;
	Vector velocity;
	Location loc;
	Entity ent;
	double scale;
	float yaw;
	float initYaw;
	Location start;
	
	Location target;
	boolean returning = false;
	
	float towardPlayerAngle;
	List<Entity> playerArea;
	
	
	boolean isSounding = false;
	
	public Boomerang(Player player)
	{
		if (instances.containsKey(player))
			return;
		this.player = player;
		instances.put(player, this);
		is = new ItemStack(Material.CLAY_BALL);
		boomItem = player.getWorld().dropItem(player.getEyeLocation(), is);
		boomItem.setPickupDelay(32767);
		
		
		Location playerLoc = player.getEyeLocation().clone();
		playerLoc.setYaw(playerLoc.getYaw()+180);
		towardPlayerAngle = playerLoc.getYaw();
		
		start = player.getEyeLocation().clone();
		loc = start.clone();
		yaw = loc.getYaw()+30;
		initYaw = yaw;
		loc.setYaw(initYaw);
		
		velocity = loc.getDirection().normalize();
		
		boomItem.setVelocity(velocity);
		
		BlockIterator bit = new BlockIterator(player,15);
		@SuppressWarnings("unused")
		Block next;
		next = bit.next();
		while (bit.hasNext())
		{
			next = bit.next();
			// if ()
			// entity or block is found, do stuff - damage, etc.
		}
		
		playerArea = player.getNearbyEntities(20, 20, 20);
		for (Iterator<Entity> it = playerArea.iterator(); it.hasNext(); )
		{
	        Entity e = it.next();
			if (!(e instanceof LivingEntity) || e instanceof Player)
	            it.remove();
		}
		boomerangNumber = boomerangCounter++;
	}
	

	public boolean progress() {	
	
		//float diff = initYaw - yaw;
		age++;
		
		if (loc == null || boomItem == null)
		{
			//player.sendMessage("null!");
			instances.remove(player);
			if (boomItem != null) boomItem.remove();
			player.getInventory().addItem(new ItemStack(Material.CLAY_BRICK));
			return false;
		}
		if (age > 45 || age > middleAge*2-2)
		{
			//player.sendMessage("death!");
			instances.remove(player);
			boomItem.remove();
			player.getInventory().addItem(new ItemStack(Material.CLAY_BRICK));
			return false;
		}
		
		
		
		if (age > 3)
		{
			Location temploc = boomItem.getLocation().clone();
			Vector tmp = Tools.getDirection(temploc, start);
			temploc.setDirection(tmp);
			
			float convert = temploc.getYaw();
			if (convert > 180)
				convert -= 360;
			convert -= 180;
			temploc.setYaw(convert);
			
			float startYaw = start.getYaw();
			
			if (startYaw >= 0) startYaw -= 360; 
			if (startYaw > -90 && convert < -270) convert += 360; 

			//player.sendMessage(Float.toString(startYaw) + "   " + Float.toString(convert));
						
			if (!returning && startYaw > convert)
			{
				loc.setPitch(-loc.getPitch()-0.4f);
				yaw = convert + 180+30;
				
				middleAge = age;
				returning = true;
			}
			else
				loc.setPitch(loc.getPitch()-0.4f);
			velocity = loc.getDirection().normalize();
			boomItem.setVelocity(velocity);
		}
		else {
			loc.setPitch(loc.getPitch()-0.4f);
			velocity = loc.getDirection().normalize();
			boomItem.setVelocity(velocity);
		}
		
		if (!isSounding)
			callSound();
		
		
		for (Iterator<Entity> it = playerArea.iterator(); it.hasNext(); ) {
			LivingEntity e = (LivingEntity)it.next();
			if (Tools.getDirection(e.getLocation(),boomItem.getLocation()).length() < 2.5)
			{
				//add or update the stunned status.
				if (!stunned.containsKey(e) || stunned.get(e) != boomerangNumber)
				{
					e.damage(0, player);
					if (boomItem.getFireTicks() > 0)
					{
						e.setFireTicks(100);
					}
					//try to stun entity
					/*
					LegendCraft.plugin.getServer().dispatchCommand(LegendCraft.plugin.getServer().getConsoleSender(), 
							"entitydata "+e.getUniqueId()+" {NoAI:1,Silent:1}"); 
					*/
					
					noAI(e, 1);
					
					stunned.remove((LivingEntity)e);
					stunned.put((LivingEntity)e,boomerangNumber);
					LegendCraft.plugin.getServer().getScheduler().scheduleSyncDelayedTask(LegendCraft.plugin, 
							new BoomerangStunDelayThread((LivingEntity)e, boomerangNumber), Math.round(2.5*20));	// 20 ticks in a second
				}
				
			}
		}
	
		/**
		 *  9 = 5 blocks
		 *  5 = 10 blocks
		 *  3.5 = 15 blocks
		 */
		//yaw = (float)(yaw-20*Math.abs(Math.cos(Math.toRadians(age))));
		yaw = yaw - 5f;
		loc.setYaw(yaw);
		
		
		/*
		if (diff > 180)
		{
			yaw = initYaw;
		}
		*/
		
		//player.sendMessage(Float.toString(yaw));
		
		
	
		
		Block present = boomItem.getLocation().getBlock();
		if (boomItem.getFireTicks() > 0) {
			Block t;
			for (int x = -1; x < 2; x++)
			{
				for (int y = -1; y < 2; y++)
				{
					for (int z = -1; z < 2; z++)
					{
						t = present.getRelative(x, y, z);
						if (t.getType() == Material.WEB)
							t.setType(Material.FIRE);
					}
				}
			}
		}
		
		Tools.toggleButton(present, true);
		return false;
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

	public static void unStun(LivingEntity le, int i) {
		//find and remove entities associated with this # boomerang
		
		// they may not be equal if another boomerang has hit more recently.
		// if that's the case, that next boomerang will do the job.
		if (stunned.get(le) == i)
		{
			stunned.remove(le);
			
			//try to un-stun entity
			noAI((Entity)le, 0);
			
			/*
			LegendCraft.plugin.getServer().dispatchCommand(LegendCraft.plugin.getServer().getConsoleSender(), 
					"entitydata "+le.getUniqueId()+" {NoAI:0, Silent:0}");
			*/ 
		}
	}
	
	static void noAI(Entity bukkitEntity, int ai) {
	    
		//New code
		net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();

        NBTTagCompound tag = new NBTTagCompound();
        
        nmsEntity.c(tag);
        tag.setBoolean("NoAI", ai==1);
        tag.setInt("Silent", ai);
        EntityLiving el = (EntityLiving) nmsEntity;
        el.a(tag);
		
		
        // Old code
        
		/*
		net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
	    NBTTagCompound tag = nmsEntity.getNBTTag();
	    if (tag == null) {
	        tag = new NBTTagCompound();
	    }
	    nmsEntity.c(tag);
	    tag.setInt("NoAI", ai);
	    tag.setInt("Silent", ai);
	    nmsEntity.f(tag);
	    */
	}
	
	public static void repeatSound(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).callSound();
		}
		
	} 
	public void callSound() {
		isSounding = true;
		LegendCraft.plugin.getServer().getScheduler().scheduleSyncDelayedTask(LegendCraft.plugin, 
				new BoomerangSwooshDelayThread(player, boomItem), 3);	// 20 ticks in a second
	}
	
	
}
