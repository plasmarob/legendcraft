package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.util.Tools;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class Door implements Receiver {
	
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	List<Material> matList = new ArrayList<Material>();
	List<Byte> datList = new ArrayList<Byte>();
	
	Vector min;
    Vector max;
	Material keyMat;
	byte keyDat;
	World world;
	
	@SuppressWarnings("deprecation")
	public Door(Player player, String name) {
		this.name = name;
		
		Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		min = sel.getNativeMinimumPoint();
        max = sel.getNativeMaximumPoint();
        this.world = player.getWorld();
        keyMat = player.getItemInHand().getType();
        keyDat = player.getItemInHand().getData().getData();
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
		player.sendMessage(ChatColor.LIGHT_PURPLE + "Door created!");
	}
	
	public Door(World world, FileConfiguration doorConfig, String name) {
		this.world = world;
		this.name = name;
		keyMat = Material.getMaterial(doorConfig.getString("keymaterial"));
		keyDat = (byte) doorConfig.getInt("keydata");
		
		//main trigger block
		List<Integer> minXYZ = doorConfig.getIntegerList("min");
		List<Integer> maxXYZ = doorConfig.getIntegerList("max");
		min = new Vector(minXYZ.get(0), minXYZ.get(1), minXYZ.get(2));
		max = new Vector(maxXYZ.get(0), maxXYZ.get(1), maxXYZ.get(2));
		int i = 1;
		while (true) {
			Material mat = Material.getMaterial(doorConfig.getString("m" + i));
			Byte data = (byte)doorConfig.getInt("d" + i);
			if (mat != null && data != null) {
				matList.add(mat);
				datList.add(data);
			}
			else
				break;
			i++;
		}
	}
	
	public void setConfig(FileConfiguration doorConfig) {
		doorConfig.set("keymaterial", keyMat.toString());
		doorConfig.set("keydata", (int)keyDat);
		doorConfig.set("world", world.getName());
		
		doorConfig.set("min", Arrays.asList(min.getX(),min.getY(),min.getZ()));
		doorConfig.set("max", Arrays.asList(max.getX(),max.getY(),max.getZ()));
		for (int i = 0; i < matList.size(); i++) {
			doorConfig.set("m"+Integer.toString(i+1),matList.get(i).name());
			doorConfig.set("d"+Integer.toString(i+1),Integer.valueOf(datList.get(i)));
		}
	}

	public boolean containsBlock(Block b)
	{
		//Bukkit.getConsoleSender().sendMessage("Is trying!");
		Block tmpB;
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
        			tmpB = world.getBlockAt(x, y, z);
        			if (tmpB.getX() == b.getX() && tmpB.getY() == b.getY() &&
        			tmpB.getZ() == b.getZ() && tmpB.getWorld().equals(b.getWorld()))
        				return true;
        }}}
		return false;
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
	
	public Material getKeyMat() {
		return keyMat;
	}
	public byte getKeyDat() {
		return keyDat;
	}
	
	
	@Override
	public void trigger() {
		if (!enabled || !isOn)
			return;
		if (inverted)
			reset();
		else
			set();
	}
	@SuppressWarnings("deprecation")
	@Override
	public void set() {
		if (!enabled || !isOn)
			return;
		
		Block tmpB;
		int i = 0;
		if (inverted) {
			for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
	        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
	        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
	        			tmpB = world.getBlockAt(x, y, z);
	        			tmpB.setType(matList.get(i));
	        			tmpB.setData(datList.get(i));
	        			i++;
	        }}}
		} else {
			for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
	        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
	        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
	        			tmpB = world.getBlockAt(x, y, z);
	        			tmpB.setType(Material.AIR);
	        			tmpB.setData((byte)0);
	        			i++;
	        }}}
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	public void reset() {
		if (!enabled || !isOn)
			return;
		Block tmpB;
		int i = 0;
		if (!inverted) {
			for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
	        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
	        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
	        			tmpB = world.getBlockAt(x, y, z);
	        			tmpB.setType(matList.get(i));
	        			tmpB.setData(datList.get(i));
	        			i++;
	        }}}
		} else {
			for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
	        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
	        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
	        			tmpB = world.getBlockAt(x, y, z);
	        			tmpB.setType(Material.AIR);
	        			tmpB.setData((byte)0);
	        			i++;
	        }}}
		}
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
		// Does nothing in this case - set and reset handle it.
	}

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean bool) {
		Tools.say("Here!");
		enabled = bool;
		if (!enabled)
			reset();
	}
	public boolean isOn() {
		return isOn;
	}
	
	
	@Override
	public String type() {
		return "door";
	}
	@Override
	public String name() {
		return name;
	}

	static String prp = "" + ChatColor.LIGHT_PURPLE;
	static String r = "" + ChatColor.RESET;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Door \"" + name + "\":");

		String enable = "enabled";
		if (!enabled) enable = "disabled";
		p.sendMessage(prp + "  Currently " + enable + ".");
		
		String def = "ON";
		if (!defaultOnOff) def = "OFF";
		String on = "ON";
		if (!isOn) on = "OFF";
		p.sendMessage(prp + "  Is " + on + ","+r+" default"+prp+"s to " + def + ".");
		
		p.sendMessage(r + "  Inverted"+prp+"?: " + inverted);
			
		p.sendMessage(prp + "  Min XYZ: " + min.getX() + " / " + min.getY() + " / " + min.getZ() + "");
		p.sendMessage(prp + "  Max XYZ: " + max.getX() + " / " + max.getY() + " / " + max.getZ() + "");
		
		p.sendMessage(prp + "  Key: [" + keyMat.toString() + "][" + keyDat + "]");
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
	
	
	@Override
	public int getX() {
		return (min.getBlockX() + max.getBlockX())/2;
	}
	@Override
	public int getY() {
		return (min.getBlockY() + max.getBlockY())/2;
	}
	@Override
	public int getZ() {
		return (min.getBlockZ() + max.getBlockZ())/2;
	}
}
