package me.plasmarob.legendcraft.blocks;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public interface Receiver {
	public int getID();
	public void setID(int id);
	
	// 0 is start by default, 0 and 1 are switched if it is inverted.
	// 0,1 -> 1
	public void trigger();
	// turns a block on/off from its default.
	// 0 -> 1
	public void set();
	// 0,1 -> 0
	public void reset();
	public void on();
	public void off();
	
	public boolean isEnabled();
	public void setEnabled(boolean bool);
	
	//TODO: alter this to standardize the interface. 
	// allows running via external delay
	public void run();
	
	public String type();
	public String name();
	public int getX();
	public int getY();
	public int getZ();
	
	public void show(Player p);
	public void edit(Player p, String key, String value);
	public boolean hasBlock(Block b);
	
	public void dbInsert();
}
