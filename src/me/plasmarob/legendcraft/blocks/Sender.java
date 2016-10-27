package me.plasmarob.legendcraft.blocks;

import java.util.HashMap;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public interface Sender {
	public HashMap<Receiver, String> getTargets();
	public HashMap<Receiver, String> getMessageTypes();
	public void clearLinks();
	public boolean removeLink(Receiver receiver);
	
	public void setTarget(Receiver receiver, String type);
	
	public String type();
	public String name();
	
	public boolean isEnabled();
	public void setEnabled(boolean bool);
	
	void run();
	void trigger();
	public void on();
	public void off();
	
	public void show(Player p);
	public boolean hasBlock(Block b);
	public int getX();
	public int getY();
	public int getZ();
}
