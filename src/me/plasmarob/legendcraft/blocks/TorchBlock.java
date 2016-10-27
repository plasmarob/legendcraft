package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.plasmarob.legendcraft.util.Tools;

public class TorchBlock implements Sender,Receiver  {
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	private int delay = 0;
	
	private int timeout = -1;
	private boolean defaultTorchLit = false;
	private String torchType = "stick";
	private int torchesLit;
	
	List<Block> torchList = new ArrayList<Block>();

	Block mainBlock;
	
	HashMap<Receiver, String> receivers = new HashMap<Receiver, String>();
	HashMap<Receiver, String> messageTypes = new HashMap<Receiver, String>();

	public TorchBlock(Block block, String name) {
		mainBlock = block;
		this.name = name;
	}
	
	@SuppressWarnings("deprecation")
	public TorchBlock(List<Block> blockList, Block block, String name) {
		mainBlock = block;
		this.name = name;
		
		for (Block b : blockList)
		{
			b.setType(Material.COBBLE_WALL);
			if (torchType == "wood")
				b.setData((byte)1);
			torchList.add(b);
		}
	}

	public List<Block> getTorches() {
		return torchList;
	}
	public Block getMainBlock() {
		return mainBlock;
	}
	
	@Override
	public HashMap<Receiver, String> getTargets(){
		return receivers;
	}
	@Override
	public HashMap<Receiver, String> getMessageTypes(){
		return messageTypes;
	}
	
	@Override
	public void setTarget(Receiver target, String linkType) {
		if (receivers.containsKey(target))
			receivers.remove(target);
		receivers.put(target, target.type());
		messageTypes.put(target, linkType);
	}

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean bool) {
		enabled = bool;
		if (enabled) {
			mainBlock.setType(Material.AIR);
			if (defaultTorchLit) {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.TORCH);
				}
			} else {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.AIR);
				}
			}
			
		} else {
			mainBlock.setType(Material.IRON_ORE);
		}
		calculateTorches();
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
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public String getTorchType() {
		return torchType;
	}
	@SuppressWarnings("deprecation")
	public void setTorchType(String t)
	{
		byte type = (byte)0;	//otherwise assume "stick"
		if (t.equals("wood")) {
			type = (byte)1;
			torchType = t;
		}
		else
			torchType = "stick";
		for (Block b : torchList)
			b.setData(type);
	}
	public boolean isDefaultLit() {
		return defaultTorchLit;
	}
	public void setDefaultLit(boolean lit) {
		this.defaultTorchLit = lit;
	}
	
	@Override
	public void trigger() { 
		if (enabled && isOn) {
			if (!inverted) {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.TORCH);
				}
			} else {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.AIR);
				}
			}
			// run();
		}
	}
	@Override
	public void set() {
		if (enabled && isOn) {
			if (!inverted) {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.TORCH);
				}
			} else {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.AIR);
				}
			}
		}
	}
	@Override
	public void reset() {
		if (enabled && isOn) {
			if (inverted) {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.TORCH);
				}
			} else {
				for (Block b : torchList) {
					b.getRelative(BlockFace.UP).setType(Material.AIR);
				}
			}
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
	public void run()
	{
		for (Receiver r : receivers.keySet()) {
			if (messageTypes.get(r).equals("trigger"))
				r.trigger();
			else if (messageTypes.get(r).equals("set"))
				r.set();
			else if (messageTypes.get(r).equals("reset"))
				r.reset();
			else if (messageTypes.get(r).equals("on"))
				r.on();
			else if (messageTypes.get(r).equals("off"))
				r.off();
		}
	}
	
	
	public void displayTargets(Player p) {
		for (Receiver r : receivers.keySet()) {
			p.sendMessage("  Links to " + receivers.get(r) + "("+messageTypes.get(r)+")");
		}
	}

	public boolean removeLink(Receiver block) {
		if (receivers.containsKey(block)) {
			receivers.remove(block);
			messageTypes.remove(block);
			return true;
		}
		return false;
	}

	public void destroy() {
		for (Block b : torchList)
		{
			b.getRelative(BlockFace.UP).setType(Material.AIR);
			b.setType(Material.AIR);
		}
	}
	
	public void clearLinks() {
		receivers.clear();
		messageTypes.clear();
	}

	@SuppressWarnings("deprecation")
	public void addTorch(Player player) {
		Block b = player.getLocation().getBlock();
		b.setType(Material.COBBLE_WALL);
		if (torchType == "wood")
			b.setData((byte)1);
		torchList.add(b);	
	}
	
	
	

	@Override
	public String type() {
		return "torchblock";
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
		
		p.sendMessage(r + "  Type"+prp+": " + torchType);
		
		String lit = "ON";
		if (!defaultTorchLit) lit = "OFF";
		p.sendMessage(prp + "  Torches initially "+r+"lit"+prp+": " + lit);
		
		String time = "none";
		if (timeout > 0) time = "" + timeout + " seconds";
		p.sendMessage(prp + "  Time "+r+"limit"+prp+": " + time);
		
		p.sendMessage(prp + "  Main Block: " + mainBlock.getX() + " " + mainBlock.getY() + " " + mainBlock.getZ());
		
		for (Block b : torchList) {
			p.sendMessage(prp + "" + b.getX() + " " + b.getY() + " " + b.getZ());
		}
		
		for (Receiver r : receivers.keySet()) {
			p.sendMessage(prp + "  Links to " + receivers.get(r) + "("+messageTypes.get(r)+")");
			Tools.showLine(mainBlock.getWorld(), this, r);
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
		} else if (key.toLowerCase().equals("type")) {
			setTorchType(value);
			p.sendMessage(prp + "  Torches set to " + getTorchType() + ".");	
		} else if (key.toLowerCase().equals("limit")) {
			//TODO: add a time limit
		} else if (key.toLowerCase().equals("lit")) {
			if (value.toLowerCase().equals("on"))
				defaultTorchLit = true;
			else
				defaultTorchLit = Boolean.parseBoolean(value); 
			p.sendMessage(prp + "  Default set to " + defaultTorchLit + ".");
		}
	}

	@SuppressWarnings("deprecation")
	public void testPlayers(List<Player> players) {
		for (Block t : torchList) {
			for (Player p : players) {
				
				/*
				double x,y,z;
				double X,Y,Z;
				x = p.getLocation().getX();
				y = p.getLocation().getY();
				z = p.getLocation().getZ();
				X = t.getLocation().getX();
				Y = t.getLocation().getY();
				Z = t.getLocation().getZ();
				p.sendMessage("" + x + " " + y + " " + z + " " + X + " " + Y + " " + Z);
				*/
				
				//p.sendMessage("" + p.getLocation().clone().add(-0.5, 0, -0.5).distance(t.getLocation()));
				
				if (t.getRelative(BlockFace.UP).getType() == Material.TORCH &&
					p.getLocation().clone().add(-0.5, 0, -0.5).distance(t.getLocation()) < 0.6 
					&& p.getItemInHand().getType() == Material.STICK) 
				{
					p.setItemInHand(new ItemStack(Material.TORCH, 1, (short)0, (byte)0));
				}
				else if (t.getRelative(BlockFace.UP).getType() == Material.AIR &&
						p.getLocation().clone().add(-0.5, 0, -0.5).distance(t.getLocation()) < 0.6  
						&& p.getItemInHand().getType() == Material.TORCH) 
				{
					t.getRelative(BlockFace.UP).setType(Material.TORCH);
					calculateTorches();
				}
			}
		}
	}	
	
	private void calculateTorches() {
		int prev = torchesLit;
		int count = 0;
		for (Block b : torchList) {
			if (b.getRelative(BlockFace.UP).getType() == Material.TORCH)
				count++;
		}
		torchesLit = count;
		if (!inverted && torchesLit == torchList.size() && prev < torchesLit)
			run();
		else if (inverted && torchesLit == 0 && prev > torchesLit)
			run();
	}
	
	@Override
	public int getX() {
		return mainBlock.getX();
	}
	@Override
	public int getY() {
		return mainBlock.getY();
	}
	@Override
	public int getZ() {
		return mainBlock.getZ();
	}
	
	@Override
	public boolean hasBlock(Block b) {
		return b.equals(mainBlock) || torchList.contains(b);
	}
}
