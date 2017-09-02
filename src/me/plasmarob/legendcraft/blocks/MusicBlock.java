package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class MusicBlock implements Receiver {
	
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	private int radius = 16;
	private float volume = 1;
	
	String tune;
	Block block;
	Player player;
	
	
	public MusicBlock(Player player, Block block, String name) {
		this.name = name;
		if (!Tune.tuneStrings.containsKey(player)) {
			player.sendMessage(ChatColor.RED + "You must play a valid tune first!");
			return;
		}
		
		tune = Tune.tuneStrings.get(player);
		this.player = player;
		this.block = block;
		player.sendMessage(ChatColor.LIGHT_PURPLE + 
				"music block created with previously played tune.");
	}

	public MusicBlock(Block mainBlock, String name) {
		this.name = name;
		this.block = mainBlock;
	}
	
	public void write() {
		
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
	
	@Override
	public void trigger() {
		if (enabled && isOn)
			new Tune(block, tune, radius, volume);
	}
	@Override
	public void set() {
		if (enabled && isOn)
			new Tune(block, tune, radius, volume);
	}
	@Override
	public void reset() {
		// does nothing
	}
	@Override
	public void on() {
		isOn = true;
	}
	@Override
	public void off() {
		isOn = false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean bool) {
		enabled = bool;
		if (enabled) 
			block.setType(Material.AIR);
		else
			block.setType(Material.DIAMOND_ORE);
	}

	public List<Integer> getXYZ() {
		List<Integer> xyz = new ArrayList<Integer>();
		xyz.add(block.getX());
		xyz.add(block.getY());
		xyz.add(block.getZ());
		return xyz;
	}

	public String getTuneString() {
		return tune;
	}
	public void setTuneString(String tune) {
		this.tune = tune;
	}

	@Override
	public String type() {
		return "musicblock";
	}

	@Override
	public String name() {
		return name;
	}

	static String prp = "" + ChatColor.LIGHT_PURPLE;
	static String r = "" + ChatColor.RESET;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Music Block \"" + name + "\":");

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
		
		p.sendMessage(prp + "  Tune: " + tune);
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
