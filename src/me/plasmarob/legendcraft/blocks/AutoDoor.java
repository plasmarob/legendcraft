package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

import me.plasmarob.legendcraft.LegendCraft;
import net.md_5.bungee.api.ChatColor;

public class AutoDoor implements Receiver {

	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	List<Material> matList = new ArrayList<Material>();
	List<Byte> datList = new ArrayList<Byte>();
	
	private Vector min;
	private Vector max;
	private Material keyMat;
	private byte keyDat;
	private World world;
	private BlockFace openDirection = BlockFace.UP;
	private boolean isRunning = false;
	
	@SuppressWarnings("deprecation")
	public AutoDoor(Player player, String name) {
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
	
	public AutoDoor(World world, FileConfiguration doorConfig, String name) {
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
	
	public void setConfig(FileConfiguration adConfig) {
		adConfig.set("keymaterial", keyMat.toString());
		adConfig.set("keydata", (int)keyDat);
		adConfig.set("world", world.getName());
		
		adConfig.set("min", Arrays.asList(min.getX(),min.getY(),min.getZ()));
		adConfig.set("max", Arrays.asList(max.getX(),max.getY(),max.getZ()));
		for (int i = 0; i < matList.size(); i++) {
			adConfig.set("m"+Integer.toString(i+1),matList.get(i).name());
			adConfig.set("d"+Integer.toString(i+1),Integer.valueOf(datList.get(i)));
		}
	}
	
	public void update() {
		if (!isRunning)
			return;
		
		if (openDirection == BlockFace.UP) {
			// iterate the thing up
			for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
	        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
	        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
	        			//tmpB = player.getWorld().getBlockAt(x, y, z);
	        			//matList.add(tmpB.getType());
	        			//datList.add(tmpB.getData());
	                }
	            }
	        }
		}
	}
	
	@Override
	public void trigger() {
		// TODO Auto-generated method stub

	}

	@Override
	public void set() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void on() {
		// TODO Auto-generated method stub

	}

	@Override
	public void off() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void setEnabled(boolean bool) {
		enabled = bool;
		if (!enabled)
			reset();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public String type() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void show(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasBlock(Block b) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void edit(Player p, String key, String value) {
		// TODO Auto-generated method stub
		
	}

}
