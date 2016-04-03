package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.util.Tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class StorageBlock implements Receiver {
	
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	List<Material> matList = new ArrayList<Material>();
	List<Byte> datList = new ArrayList<Byte>();
	
	Block block;
	Vector min;
    Vector max;
    
    /**
     * Creates StorageBlock - called by player command
     * @param player
     * @param block
     */
	@SuppressWarnings("deprecation")
	public StorageBlock(Player player, Block block, String name) {
		this.block = block;
		this.name = name;
		Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		min = sel.getNativeMinimumPoint();
        max = sel.getNativeMaximumPoint();
		Block tmpB;
		
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
        			tmpB = player.getWorld().getBlockAt(x, y, z);
        			matList.add(tmpB.getType());
        			datList.add(tmpB.getData());
                }
            }
        }
		Tools.saySuccess(player, "Storage block created!");
	}
	
	/**
	 * Loads StorageBlock from fileConfig
	 * @param world
	 * @param storageConfig
	 */
	public StorageBlock(World world, FileConfiguration storageConfig, String name) {
		
		this.name = name;
		
		List<Integer> blkCoord = storageConfig.getIntegerList("block");
		block = world.getBlockAt(blkCoord.get(0),blkCoord.get(1),blkCoord.get(2));
		List<Double> minCoord = storageConfig.getDoubleList("min");
		min = new Vector(minCoord.get(0),minCoord.get(1),minCoord.get(2));
		List<Double> maxCoord = storageConfig.getDoubleList("max");
		max = new Vector(maxCoord.get(0),maxCoord.get(1),maxCoord.get(2));
		int i = 1;
		while (true) {
			Material mat = Material.getMaterial(storageConfig.getString("m" + Integer.toString(i)));
			Byte data = (byte)storageConfig.getInt("d" + Integer.toString(i));
			if (mat != null && data != null) {
				matList.add(mat);
				datList.add(data);
			}
			else
				break;
			i++;
		}
	}

	/**
	 * Save storage block to config
	 * @param storageConfig
	 */
	public void setConfig(FileConfiguration storageConfig) {
		storageConfig.set("block", Arrays.asList(block.getX(),block.getY(),block.getZ()));
		storageConfig.set("min", Arrays.asList(min.getX(),min.getY(),min.getZ()));
		storageConfig.set("max", Arrays.asList(max.getX(),max.getY(),max.getZ()));

		for (int i = 0; i < matList.size(); i++) {
			storageConfig.set("m"+Integer.toString(i+1),matList.get(i).name());
			storageConfig.set("d"+Integer.toString(i+1),Integer.valueOf(datList.get(i)));
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void trigger() 
	{
		if (!enabled || !isOn)
			return;
		
		Block tmpB;
		int i = 0;
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
        {
        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++)
            {
        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
                {
        			tmpB = block.getWorld().getBlockAt(x, y, z);
        			tmpB.setType(matList.get(i));
        			tmpB.setData(datList.get(i));
        			i++;
                }
            }
        }
	}
	
	
	@Override
	public void set() {
		trigger();
	}
	@Override
	public void reset() {
	}
	@Override
	public void on() {
		if (enabled)
			isOn = true;
	}
	@Override
	public void off() {
		if (enabled)
			isOn = false;
	}
	
	@Override
	public void run() {
		if (isOn && enabled)
			trigger();
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
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean bool) {
		enabled = bool;
		if (enabled) 
			block.setType(Material.AIR);
		else
			block.setType(Material.EMERALD_ORE);
	}
	
	@Override
	public String type() {
		return "storage";
	}

	@Override
	public String name() {
		return name;
	}
	
	
	static String prp = "" + ChatColor.LIGHT_PURPLE;
	static String r = "" + ChatColor.RESET;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Storage Block \"" + name + "\":");

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
		}	
	}
}