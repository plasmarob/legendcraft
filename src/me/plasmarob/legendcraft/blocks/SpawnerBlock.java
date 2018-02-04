package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.util.Tools;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class SpawnerBlock implements Receiver, Sender{

	private int id = -1;
	public int getID() { return this.id; }
	public void setID(int id) { this.id= id; }
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	private int delay = 0;
	
	Vector min;
	Vector max;
	World world;
	Block block;
	Map<MobTemplate,Integer> mobList = new HashMap<MobTemplate,Integer>();
	List<Entity> entityList = new ArrayList<Entity>();
	
	HashMap<Receiver, Link> links = new HashMap<Receiver, Link>();
	
	public SpawnerBlock(Player player, Block block, String name) {
		this.block = block;
		this.name = name;
		Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		min = sel.getNativeMinimumPoint();
		max = sel.getNativeMaximumPoint();
		world = block.getWorld();
       
		Tools.saySuccess(player, "Spawner block created!");
	}
	
	public SpawnerBlock(World world, FileConfiguration config, String name) {
		
		this.world = world;
		this.name = name;
		
		List<Integer> blkXYZ = config.getIntegerList("block");
		block = world.getBlockAt(blkXYZ.get(0), blkXYZ.get(1), blkXYZ.get(2));
		List<Integer> minXYZ = config.getIntegerList("min");
		min = new Vector(minXYZ.get(0), minXYZ.get(1), minXYZ.get(2));
		List<Integer> maxXYZ = config.getIntegerList("max");
		max = new Vector(maxXYZ.get(0), maxXYZ.get(1), maxXYZ.get(2));
		int i = 1;
		while (true)
		{
			String mobName = config.getString("m" + i + ".name");
			int count = config.getInt("m" + i + ".count");
			if (mobName != null) {
				MobTemplate mob = MobTemplate.getMobTemplate(mobName);
				mobList.put(mob, count);
			}
			else
				break;
			i++;
		}
	}
	
	public void dbInsert() {
		
	}


	public boolean isDefaultOnOff() {
		return defaultOnOff;
	}
	public void setDefaultOnOff(boolean defaultOnOff) {
		this.defaultOnOff = defaultOnOff;
	}
	public boolean isInverted() {
		return inverted;
	}
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public int getDelay() {
		return delay;
	}
	
	
	//@SuppressWarnings("unused")
	@Override
	public void trigger() {
		if (enabled)
		{
			Location location;
			for (MobTemplate mob : mobList.keySet())
			{
				// do it count times
				for (int i = 0; i < mobList.get(mob); i++)
				{
					// 10 attempts for each mob
					for (int r = 0; r < 10; r++)
					{
						double x = min.getX()+Math.random()*(max.getX()-min.getX());
						double y = min.getY()+Math.random()*(max.getY()-min.getY());
						double z = min.getZ()+Math.random()*(max.getZ()-min.getZ());
						location = new Location(world,x,y,z);
						if (location.getBlock().getType() == Material.AIR) {
							entityList.add(mob.spawn(location));
							break;
						}
					}
				}
			}
			//enabled = false;
		}
	}
	@Override
	public void set() {
		trigger();
	}
	@Override
	public void reset() {
		for (Entity e : entityList)
			e.remove();
		entityList.clear();
	}
	@Override
	public void on() {
		if (enabled)
			isOn = true;
	}
	@Override
	public void off() {
		if (enabled) {
			if (isOn) {
				for (Entity e : entityList)
					e.remove();
				entityList.clear();
			}
			isOn = false;
		}
	}
	
	@Override
	public void run() {
		//send the message to the receivers
		for (Receiver r : links.keySet()) {
			links.get(r).call(r);
		}
	}
	
	public void insert(MobTemplate mob, String name, int count) {
		if (mobList.containsKey(mob)) {
			count += mobList.get(mob);
		}
		mobList.put(mob, count);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean bool) {
		enabled = bool;
		if (enabled)
			block.setType(Material.AIR);
		else {
			block.setType(Material.GOLD_ORE);
			for (Entity e : entityList)
				e.remove();
			entityList.clear();
		}
	}

	public void setConfig(FileConfiguration config) {
		config.set("block", Arrays.asList(block.getX(),block.getY(),block.getZ()));
		config.set("min", Arrays.asList(min.getX(),min.getY(),min.getZ()));
		config.set("max", Arrays.asList(max.getX(),max.getY(),max.getZ()));
		int i = 1;
		for (MobTemplate mob : mobList.keySet())
		{
			config.set("m"+i+".name", mob.getMyName());
			config.set("m"+i+".count", mobList.get(mob));
			i++;
		}
	}

	@Override
	public void clearLinks() {
		links.clear();
	}
	
	@Override
	public String type() {
		return "spawner";
	}


	@Override
	public HashMap<Receiver, Link> getLinks(){
		return links;
	}

	@Override
	public boolean setTarget(Receiver target, Link linkType) {
		try {
			if (links.containsKey(target)) links.remove(target);
			links.put(target, linkType);
			return true;
		} catch (Exception e) { return false; }
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public boolean removeLink(Receiver block) {
		if (links.containsKey(block)) {
			links.remove(block);
			return true;
		}
		return false;
	}

	static String prp = "" + ChatColor.LIGHT_PURPLE;
	static String r = "" + ChatColor.RESET;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Spawner \"" + name + "\":");

		String enable = "enabled";
		if (!enabled) enable = "disabled";
		p.sendMessage(prp + "  Currently " + enable + ".");
		
		String def = "ON";
		if (!defaultOnOff) def = "OFF";
		String on = "ON";
		if (!isOn) on = "OFF";
		p.sendMessage(prp + "  Is " + on + ","+r+" default"+prp+"s to " + def + ".");
		
		p.sendMessage(r + "  Inverted"+prp+"?: " + inverted);
		
		p.sendMessage(prp + "  Block: " + block.getX() + " " + block.getY() + " " + block.getZ());
		
		p.sendMessage(prp + "  Min XYZ: " + min.getX() + " / " + min.getY() + " / " + min.getZ() + "");
		p.sendMessage(prp + "  Max XYZ: " + max.getX() + " / " + max.getY() + " / " + max.getZ() + "");
		
		for (MobTemplate mt : mobList.keySet()) {
			p.sendMessage(prp + "  " + mt.getMyName() + " x " + mobList.get(mt));
		}
		
		for (Receiver r : links.keySet()) {
			p.sendMessage(prp + "  Links to " + r.name() + " ("+links.get(r).NAME+")");
			Tools.showLine(world, this, r);
		}
	}
	
	
	String red = "" + ChatColor.RED;
	public void edit(Player p, String key, String value) {
		if (key.toLowerCase().equals("default")) {
			if (value.toLowerCase().equals("on"))
				defaultOnOff = true;
			else
				defaultOnOff = Boolean.parseBoolean(value); 
			p.sendMessage(prp + "  Default set to " + defaultOnOff + ".");
		} else if (key.toLowerCase().equals("inverted")) {
			inverted = Boolean.parseBoolean(value); 
			p.sendMessage(prp + "  Inverted set to " + inverted + ".");	
		} else {
			
			MobTemplate mob = MobTemplate.getMobTemplate(key);
			if (mob == null) {
				p.sendMessage(r+value+" not found.");
				return;
			}
			int count = -1;
			try { 
		        count = Integer.parseInt(value); 
		    } catch(NumberFormatException e) { 
		    	p.sendMessage(red + "invalid number \"" + value + "\".");
		        return; 
		    } catch(NullPointerException e) {
		    	p.sendMessage(red + "invalid number \"" + value + "\".");
		        return;
		    }
			if (count >= 0)
				mobList.remove(mob);
			if (count > 0)
				mobList.put(mob, count);
			p.sendMessage(prp + " Mob " + key + " set to " + count + " copies.");
		}
	}

	public void updateMobs() {
		if (entityList.size() == 0)
			return;

		for (int i = 0; i < entityList.size(); i++) {
			if (entityList.get(i).isDead()) {
				entityList.remove(i);
				i--;
			}
		}
		
		if (!enabled || !isOn)
			return;
		//should only happen the first time it shrinks to 0.
		if (entityList.size() == 0) {
			run();
		}
			
	}
	
	
	@Override
	public int getX() {
		return block.getX();
	}
	@Override
	public int getY() {
		return block.getY();
	}
	@Override
	public int getZ() {
		return block.getZ();
	}
	
	@Override
	public boolean hasBlock(Block b) {
		return b.equals(block);
	}
}
