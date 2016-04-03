package me.plasmarob.legendcraft.blocks;

import org.bukkit.entity.Player;

public interface Receiver {
	
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
	
	public void show(Player p);
}