package me.plasmarob.legendcraft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.plasmarob.legendcraft.Dungeon;
import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.blocks.Receiver;
import me.plasmarob.legendcraft.blocks.Sender;
import me.plasmarob.legendcraft.item.ButtonDelayThread;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.util.Vector;

public class Tools {

	public static void say2(String o) {
		Bukkit.getConsoleSender().sendMessage((String)o);
	}
	public static void say(Object o)
	{
		if (o instanceof String)
			Bukkit.getConsoleSender().sendMessage((String)o);
		else if (o instanceof Integer)
			Bukkit.getConsoleSender().sendMessage(Integer.toString((Integer)o));
		else if (o instanceof Double)
			Bukkit.getConsoleSender().sendMessage(Double.toString((Double)o));
		else if (o instanceof Float)
			Bukkit.getConsoleSender().sendMessage(Float.toString((Float)o));
		else 
			Bukkit.getConsoleSender().sendMessage(o.toString());
	}
	
	public static void saySuccess(Player p, String s) {
		p.sendMessage(ChatColor.LIGHT_PURPLE + s);
	}
	
	
	public static String standardEnchantName(String bukkitName)
	{
		switch (bukkitName.toUpperCase())
	      {
	         case "PROTECTION_ENVIRONMENTAL":
	            return "Protection";
	         case "PROTECTION_FIRE":
	            return "Fire Protection";
	         case "PROTECTION_FALL":
	            return "Feather Falling";
	         case "PROTECTION_EXPLOSIONS":
	            return "Blast Protection";
	         case "PROTECTION_PROJECTILE":
	            return "Projectile Protection";
	         case "OXYGEN":
	            return "Respiration";   
	         case "WATER_WORKER":
	            return "Aqua Affinity"; 
	         case "THORNS":
	            return "Thorns";
	         case "DEPTH_STRIDER":
	            return "Depth Strider";
	            
	         case "DAMAGE_ALL":
	            return "Sharpness";
	         case "DAMAGE_UNDEAD":
	            return "Smite";
	         case "DAMAGE_ARTHROPODS":
	            return "Bane of Arthropods";
	         case "KNOCKBACK":
	            return "Knockback";
	         case "FIRE_ASPECT":
	            return "Fire Aspect";
	         case "LOOT_BONUS_MOBS":
	            return "Looting";
	            
	         case "DIG_SPEED":
	            return "Efficiency";
	         case "SILK_TOUCH":
	            return "Silk Touch";
	         case "DURABILITY":
	            return "Unbreaking"; 
	         case "LOOT_BONUS_BLOCKS":
	            return "Fortune"; 
	         
	         case "ARROW_DAMAGE":
	            return "Power";
	         case "ARROW_KNOCKBACK":
	            return "Punch";
	         case "ARROW_FIRE":
	            return "flame";
	         case "ARROW_INFINITE":
	            return "Infinity";
	         
	         case "LUCK":
	            return "Luck of the Sea";
	         case "LURE":
	            return "Lure";
	            
	         default:
	            return bukkitName;
	      }
	}
	
	
	public static LivingEntity getClosestMob(Player player)
	{
		List<Entity> entities = player.getNearbyEntities(5, 5, 5);
		LivingEntity closestEntity = null;
		double distance = Double.MAX_VALUE;
		for (int i = 0; i < entities.size(); i++)
		{
			if (! (entities.get(i) instanceof LivingEntity) || 
					entities.get(i) instanceof HumanEntity ||
					entities.get(i) instanceof Player)
			{
				entities.remove(i);
				i--;
				continue;
			}
			
			if (closestEntity == null ||
				distance > closestEntity.getLocation().distance(player.getLocation()))
			{
				closestEntity = (LivingEntity) entities.get(i);
				distance = closestEntity.getLocation().distance(player.getLocation());
			}		
		}
		return closestEntity;
	}
	
	public static void createMainFolder (File folder, String success)
	{
	    if (!folder.exists()) {
		    Bukkit.getConsoleSender().sendMessage("creating directory: " + folder.toString());
			boolean result = false;
			try{
				folder.mkdir();
			    result = true;
			} catch(SecurityException se) { }
			if(result)  
				 Bukkit.getConsoleSender().sendMessage(success);  
		}	
	}
	
	public static File findFile(String dungeonName, String fileName)
	{
		File[] listOfFiles = Dungeon.dungeonFolder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
    		if (!listOfFiles[i].isDirectory() || !listOfFiles[i].getName().equals(dungeonName)) 
    			continue;
    		File[] inner_list = listOfFiles[i].listFiles();
    		for (int j = 0; j < inner_list.length; j++) 
    		{
    			if (!inner_list[j].isFile()) 
    				continue;
    			if (inner_list[j].getName().equals(fileName + ".yml")) {
    				return inner_list[j];
    			}
    		}
    	}		
		return null;
	}
	
	private static List<BlockFace> faces;
	public static List<BlockFace> faces()
	{
		if (faces == null) {
			faces = new ArrayList<BlockFace>();
			faces.add(BlockFace.UP);
			faces.add(BlockFace.DOWN);
			faces.add(BlockFace.NORTH);
			faces.add(BlockFace.SOUTH);
			faces.add(BlockFace.EAST);
			faces.add(BlockFace.WEST);
		}
		return faces;
	}
	
	public static BlockFace playerCompassFace(Player p) {
		
		float angle = p.getEyeLocation().getYaw();
		if (angle > 180) {
			angle -= 360;
		} else if (angle < -180) {
			angle += 360;
		}
		
		say(angle);
		if (angle >= -45 && angle < 45)
			return BlockFace.SOUTH;
		else if (angle < -135 || angle >= 135)
			return BlockFace.NORTH;
		else if (angle >= 45 && angle < 135)
			return BlockFace.WEST;
		else //if (angle < -45 && angle >= -135)
			return BlockFace.EAST;
	}
	
	private static List<Material> clearblocks;
	public static boolean canSeeThrough(Material mat) {
		if (clearblocks == null) {
			clearblocks = new ArrayList<Material>();
			clearblocks.add(Material.TORCH);
			clearblocks.add(Material.AIR);
			clearblocks.add(Material.REDSTONE);
			clearblocks.add(Material.REDSTONE_WIRE);
			clearblocks.add(Material.CARPET);
			clearblocks.add(Material.REDSTONE_TORCH_ON);
			clearblocks.add(Material.REDSTONE_TORCH_OFF);
			clearblocks.add(Material.LONG_GRASS);
			clearblocks.add(Material.DOUBLE_PLANT);
			clearblocks.add(Material.DOUBLE_PLANT);
			clearblocks.add(Material.FENCE);
			clearblocks.add(Material.FENCE_GATE);
			clearblocks.add(Material.SPRUCE_FENCE);
			clearblocks.add(Material.SPRUCE_FENCE_GATE);
			clearblocks.add(Material.BIRCH_FENCE);
			clearblocks.add(Material.BIRCH_FENCE_GATE);
			clearblocks.add(Material.JUNGLE_FENCE);
			clearblocks.add(Material.JUNGLE_FENCE_GATE);
			clearblocks.add(Material.ACACIA_FENCE);
			clearblocks.add(Material.ACACIA_FENCE_GATE);
			clearblocks.add(Material.DARK_OAK_FENCE);
			clearblocks.add(Material.DARK_OAK_FENCE_GATE);
			clearblocks.add(Material.BREWING_STAND);
			clearblocks.add(Material.BANNER);
			clearblocks.add(Material.WALL_BANNER);
			clearblocks.add(Material.SIGN);
			clearblocks.add(Material.SIGN_POST);
			clearblocks.add(Material.WALL_SIGN);
			clearblocks.add(Material.STONE_BUTTON);
			clearblocks.add(Material.WOOD_BUTTON);
			clearblocks.add(Material.TRIPWIRE);
			clearblocks.add(Material.TRIPWIRE_HOOK);
			clearblocks.add(Material.WEB);
		}
		return clearblocks.contains(mat);
	}
	
	private static final float[] notes = {0.5f, 0.53f, 0.56f, 0.6f, 0.63f, 
		0.67f, 0.7f, 0.75f, 0.8f, 0.85f, 
		0.9f, 0.95f, 1.0f, 1.05f, 1.1f, 
		1.2f, 1.25f, 1.32f, 1.4f, 1.5f, 
		1.6f, 1.7f, 1.8f, 1.9f, 2.0f};
	public static float getPitch(int note)
	{
		if (note > 24 || note < 0) note = 0;
		return notes[note];
	}
	
	// for getting a vector pointing toward another location
	public static Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;
		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();
		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();
		return new Vector(x1 - x0, y1 - y0, z1 - z0);
	}
	
	
	
	public static List<Entity> getEntitiesAroundPoint(Location location, double radius) {
		return getEntitiesAroundPoint(location, radius, 0);
	}
	public static List<Entity> getEntitiesAroundPoint(Location location,
			double radius, double minRadius) {
		List<Entity> entities = location.getWorld().getEntities();
		List<Entity> list = location.getWorld().getEntities();
		for (Entity entity : entities) {
			if (entity.getWorld() != location.getWorld()) {
				list.remove(entity);
			} else if (entity.getLocation().distance(location) > radius || 
					   entity.getLocation().distance(location) < minRadius) {
				list.remove(entity);
			}
		}
		return list;
	}
	public static List<Entity> getEntitiesAroundFromList(Location location,
			double radius, List<Entity> entities) {
		List<Entity> list = new ArrayList<Entity>();
		list.addAll(entities);
		for (Entity entity : entities) {
			if (entity.getWorld() != location.getWorld())
				list.remove(entity);
			else if (entity.getLocation().distance(location) > radius)
				list.remove(entity);
		}
		return list;
	}
	
	public static List<Player> getPlayersAroundPoint(Location location, double radius) {
		return getPlayersAroundPoint(location, radius, 0);
	}
	public static List<Player> getPlayersAroundPoint(Location location,
			double radius, double minRadius) {
		List<Entity> entities = location.getWorld().getEntities();
		List<Player> list = new ArrayList<Player>();
		for (Entity entity : entities) {
			if (entity.getWorld() == location.getWorld() &&
					entity instanceof Player &&
					(entity.getLocation().distance(location) <= radius && 
					   entity.getLocation().distance(location) >= minRadius)) {
				list.add((Player)entity);
			}
		}
		return list;
	}
	
	public static void toggleButton(Block buttonBlock, boolean power)
	{
		if (buttonBlock.getType() == Material.STONE_BUTTON ||
				buttonBlock.getType() == Material.WOOD_BUTTON)
		{
			BlockState state = buttonBlock.getState();
			Button button = (Button)state.getData();
			
			if (power && !button.isPowered())
			{
				//on sound
				buttonBlock.getWorld().playSound(buttonBlock.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1f, 1f);
				
				int delay = 20;
				if (buttonBlock.getType() == Material.WOOD_BUTTON)
					delay = 30;
				
				LegendCraft.plugin.getServer().getScheduler().scheduleSyncDelayedTask(LegendCraft.plugin, 
						new ButtonDelayThread(buttonBlock), delay);
			}
			if (!power && button.isPowered())
			{
				//off sound
				buttonBlock.getWorld().playSound(buttonBlock.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1f, 1f);
			}
			
			button.setPowered(power);
			state.update();
			
			// Workaround thanks to Gabriel Risterucci
			// https://bukkit.atlassian.net/browse/BUKKIT-1858
			Block supportBlock = buttonBlock.getRelative(button.getAttachedFace());
			BlockState initialSupportState = supportBlock.getState();
			BlockState supportState = supportBlock.getState();
			supportState.setType(Material.AIR);
			supportState.update(true, false);
			initialSupportState.update(true);
		}
	}
	
	
	public static void toggleLever(Block leverBlock)
	{
		if (leverBlock.getType() == Material.LEVER)
		{
			BlockState state = leverBlock.getState();
			Lever lever = (Lever)state.getData();
			
			if (!lever.isPowered())
			{
				//on sound
				leverBlock.getWorld().playSound(leverBlock.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
			}
			if (lever.isPowered())
			{
				//off sound
				leverBlock.getWorld().playSound(leverBlock.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
			}
			
			lever.setPowered(!lever.isPowered());
			state.update();
			
			// Workaround thanks to Gabriel Risterucci
			// https://bukkit.atlassian.net/browse/BUKKIT-1858
			Block supportBlock = leverBlock.getRelative(lever.getAttachedFace());
			BlockState initialSupportState = supportBlock.getState();
			BlockState supportState = supportBlock.getState();
			supportState.setType(Material.AIR);
			supportState.update(true, false);
			initialSupportState.update(true);
		}
	}
	
	
	
	public static void showLine(World world, Sender send, Receiver rec) {
		new LineConnectEffect(LegendCraft.getEffectManager(), 
				new Location(world, send.getX(), send.getY(), send.getZ()), 
				new Location(world, rec.getX(), rec.getY(), rec.getZ()), 
				Color.fromRGB(0, 255, 0), Color.fromRGB(0, 0, 255)).start();
	}
	public static void showLine(World world, Block send, Block rec) {
		new LineConnectEffect(LegendCraft.getEffectManager(), 
				new Location(world, send.getX(), send.getY(), send.getZ()), 
				new Location(world, rec.getX(), rec.getY(), rec.getZ()), 
				Color.fromRGB(0, 255, 0), Color.fromRGB(0, 0, 255)).start();
	}
	public static void showLine(World world, Block send, Block rec, int r, int g, int b) {
		new LineConnectEffect(LegendCraft.getEffectManager(), 
				new Location(world, send.getX(), send.getY(), send.getZ()), 
				new Location(world, rec.getX(), rec.getY(), rec.getZ()), 
				Color.fromRGB(r, g, b), Color.fromRGB(r, g, b)).start();
	}
	
	
	public static void saveObject(Object o, File f) {
		try {
			if (!f.exists())
				f.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(o);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Object loadObject(File f) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			Object result = ois.readObject();
			ois.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	final static Pattern lastIntPattern = Pattern.compile("(.+_)([0-9]+)$");
	public static String incrementEndInt(String input) {
		Matcher matcher = lastIntPattern.matcher(input);
		if (matcher.find()) {
		    String someNumberStr = matcher.group(2);
		    int lastNumberInt = Integer.parseInt(someNumberStr);
		    return matcher.group(1) + (lastNumberInt+1);
		} else
			return input + "_" + 1;
	}
	
	
	
	@SuppressWarnings("deprecation")
	public static void scale(com.sk89q.worldedit.Vector min, com.sk89q.worldedit.Vector max, final Block anchor, final int scale) {
		int offx = (scale-1)/2;
		int offy = (scale-1)/2;
		int offz = (scale-1)/2;
		int offX = (scale-1)/2;
		int offY = (scale-1)/2;
		int offZ = (scale-1)/2;
		
		if (anchor.getX() == min.getX()) {
			offx = 0;
			offX = scale-1;
		} else if (anchor.getX() == max.getX()) {
			offx = scale-1;
			offX = 0;
		}
		if (anchor.getY() == min.getY()) {
			offy = 0;
			offY = scale-1;
		} else if (anchor.getY() == max.getY()) {
			offy = scale-1;
			offY = 0;
		}
		if (anchor.getZ() == min.getZ()) {
			offz = 0;
			offZ = scale-1;
		} else if (anchor.getZ() == max.getZ()) {
			offz = scale-1;
			offZ = 0;
		}
		
		anchor.setType(Material.AIR);	// don't want this getting in the way
		
		
		int minX = (int)min.getX();
		int minY = (int)min.getY();
		int minZ = (int)min.getZ();
		int maxX = (int)max.getX();
		int maxY = (int)max.getY();
		int maxZ = (int)max.getZ();
		
		//initialize all of it outside the loops for performance
		Block current;
		World world = anchor.getWorld();
		int aX = anchor.getX();
		int aY = anchor.getY();
		int aZ = anchor.getZ();
		int dx = 0;
    	int dy = 0;
    	int dz = 0;
    	int xx,yy,zz;
    	Material mat;
    	byte dat;
    	Block tmp;
		for (int x = minX; x <= maxX; x++) {
    	for (int y = minY; y <= maxY; y++) {
		for (int z = minZ; z <= maxZ; z++) {
        	if (x == aX && y == aY && z == aZ)
        		continue;
        	
        	dx = x-aX;
        	dy = y-aY;
        	dz = z-aZ;
        	current = anchor.getRelative(dx,dy,dz);
        	mat = current.getType();
        	dat = current.getData();
        	
        	if (mat == Material.AIR)
        		continue;
        	
        	if (current.getX() == minX || current.getRelative(BlockFace.WEST).getType() == Material.AIR) {
        		xx = aX+dx*scale-offx;
        		for (yy = aY+dy*scale-offy; yy <= aY+dy*scale+offY; yy++) {
    			for (zz = aZ+dz*scale-offz; zz <= aZ+dz*scale+offZ; zz++) {
    				tmp = world.getBlockAt(xx, yy, zz);
    				tmp.setType(mat);
    				tmp.setData(dat);
            	}
            	}
        	}
        	if (current.getX() == maxX || current.getRelative(BlockFace.EAST).getType() == Material.AIR) {
        		xx = aX+dx*scale+offX;
        		for (yy = aY+dy*scale-offy; yy <= aY+dy*scale+offY; yy++) {
    			for (zz = aZ+dz*scale-offz; zz <= aZ+dz*scale+offZ; zz++) {
    				tmp = world.getBlockAt(xx, yy, zz);
    				tmp.setType(mat);
    				tmp.setData(dat);
            	}
            	}
        	}
        	
        	if (current.getY() == minY || current.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
        		yy = aY+dy*scale-offy;
        		for (xx = aX+dx*scale-offx; xx <= aX+dx*scale+offX; xx++) {
    			for (zz = aZ+dz*scale-offz; zz <= aZ+dz*scale+offZ; zz++) {
    				tmp = world.getBlockAt(xx, yy, zz);
    				tmp.setType(mat);
    				tmp.setData(dat);
            	}
            	}
        	}
        	if (current.getY() == maxY || current.getRelative(BlockFace.UP).getType() == Material.AIR) {
        		yy = aY+dy*scale+offY;
        		for (xx = aX+dx*scale-offx; xx <= aX+dx*scale+offX; xx++) {
    			for (zz = aZ+dz*scale-offz; zz <= aZ+dz*scale+offZ; zz++) {
    				tmp = world.getBlockAt(xx, yy, zz);
    				tmp.setType(mat);
    				tmp.setData(dat);
            	}
            	}
        	}
        	
        	if (current.getZ() == minZ || current.getRelative(BlockFace.NORTH).getType() == Material.AIR) {
        		zz = aZ+dz*scale-offz;
        		for (xx = aX+dx*scale-offx; xx <= aX+dx*scale+offX; xx++) {
    			for (yy = aY+dy*scale-offy; yy <= aY+dy*scale+offY; yy++) {
    				tmp = world.getBlockAt(xx, yy, zz);
    				tmp.setType(mat);
    				tmp.setData(dat);
            	}
            	}
        	}
        	if (current.getZ() == maxZ || current.getRelative(BlockFace.SOUTH).getType() == Material.AIR) {
        		zz = aZ+dz*scale+offZ;
        		for (xx = aX+dx*scale-offx; xx <= aX+dx*scale+offX; xx++) {
    			for (yy = aY+dy*scale-offy; yy <= aY+dy*scale+offY; yy++) {
    				tmp = world.getBlockAt(xx, yy, zz);
    				tmp.setType(mat);
    				tmp.setData(dat);
            	}
            	}
        	}
		}
    	}
		}
        
        
		anchor.setType(Material.BEDROCK);
	}
	
	public static String locationAsString(int x, int y, int z) {
		return Integer.toString(x) + "," + Integer.toString(y) + "," + Integer.toString(z);
	}	
	public static com.sk89q.worldedit.Vector weVectorFromString(String string) {
		String[] xyz = string.split(",");
		return new com.sk89q.worldedit.Vector(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));
	}	
	public static String[] xyzFromString(String string) {
		String[] xyz = string.split(",");
		return xyz;
	}	
	public static Block blockFromString(String string, World world) {
		String[] xyz = xyzFromString(string);
		return world.getBlockAt(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
	}
	
}
