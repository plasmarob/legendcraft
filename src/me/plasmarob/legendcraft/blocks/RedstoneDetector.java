package me.plasmarob.legendcraft.blocks;

import java.util.HashMap;

import me.plasmarob.util.Tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class RedstoneDetector implements Sender, Receiver {
	
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	private double delay = 0;	//TODO: add a delay
	
	private int maxTimes = -1; //max times it can be triggered (-1 infinite)
	private int timesRun = 0; //how many times triggered
	
	
	Block mainBlock;
	
	HashMap<Receiver, String> receivers = new HashMap<Receiver, String>();
	HashMap<Receiver, String> messageTypes = new HashMap<Receiver, String>();

	public RedstoneDetector(Player player, Block mainBlock, String name) {
		this.name = name;
		this.mainBlock = mainBlock;
	}
	
	public RedstoneDetector(Block mainBlock, String name) {	
		this.name = name;
		this.mainBlock = mainBlock;
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
		} else {
			mainBlock.setType(Material.REDSTONE_ORE);
		}
		timesRun = 0;
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
	public double getDelay() {
		return delay;
	}
	
	public int getMaxTimes() {
		return maxTimes;
	}
	public void setMaxTimes(int maxTimes) {
		this.maxTimes = maxTimes;
	}
	public int getTimesTriggered() {
		return timesRun;
	}

	public void displayTargets(Player p) {
		for (Receiver r : receivers.keySet()) {
			p.sendMessage("  Links to " + receivers.get(r) + "("+messageTypes.get(r)+")");
		}
	}
	
	public void testForRedstone(Block block) {
		if (mainBlock.equals(block)) {
			trigger();
			return;
		}
				
		for (BlockFace bf : Tools.faces())
		{
			if (mainBlock.equals(block.getRelative(bf))) {
				trigger();
				return;
			}
		}
	}
	
	@Override
	public void trigger() {
		if (enabled && isOn && (maxTimes == -1 || timesRun < maxTimes)) {
				run();
		}
	}
	@Override
	public void set() {
		// Does nothing for now.
	}
	@Override
	public void reset() {
		// Does nothing for now.
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
		timesRun++;
		
		//when i'm triggered, trigger my targets
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

	public boolean removeLink(Receiver block) {
		if (receivers.containsKey(block)) {
			receivers.remove(block);
			messageTypes.remove(block);
			return true;
		}
		return false;
	}

	public void clearLinks() {
		receivers.clear();
		messageTypes.clear();
	}

	@Override
	public String type() {
		return "rsDetector";
	}

	@Override
	public String name() {
		return name;
	}

	static String prp = "" + ChatColor.LIGHT_PURPLE;
	static String r = "" + ChatColor.RESET;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Redstone Detector \"" + name + "\":");

		String enable = "enabled";
		if (!enabled) enable = "disabled";
		p.sendMessage(prp + "  Currently " + enable + ".");

		String def = "ON";
		if (!defaultOnOff) def = "OFF";
		String on = "ON";
		if (!isOn) on = "OFF";
		p.sendMessage(prp + "  Is " + on + ","+r+" default"+prp+"s to " + def + ".");
		
		p.sendMessage(r + "  Inverted"+prp+"?: " + inverted);
		
		String maxTimeStr = "unlimited";
		if (maxTimes != -1) maxTimeStr = "" + maxTimes;
		p.sendMessage(r + "  Max"+prp+" runs: " + maxTimeStr);
		p.sendMessage(prp + "  Times run: " + timesRun);
		p.sendMessage(prp + "  Main block: " + mainBlock.getX() + " " + mainBlock.getY() + " " + mainBlock.getZ());
		
		for (Receiver r : receivers.keySet()) {
			p.sendMessage(prp + "  Links to " + r.name() + " ("+messageTypes.get(r)+")");
		}
	}

	

	public void destroy() {
		mainBlock.setType(Material.AIR);
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
		} else if (key.toLowerCase().equals("delay")) {
			double count = 0;
			try { 
		        count = Double.parseDouble(value); 
		    } catch(NumberFormatException e) { 
		    	p.sendMessage(red + "invalid number \"" + value + "\".");
		        return; 
		    } catch(NullPointerException e) {
		    	p.sendMessage(red + "invalid number \"" + value + "\".");
		        return;
		    }
			delay = count;
			p.sendMessage(prp + "  Delay set to " + delay + " seconds (" + Math.round(delay*20) + " ticks)");
		}
		else if (key.toLowerCase().equals("max")) {
			int count = 0;
			try { 
		        count = Integer.parseInt(value); 
		    } catch(NumberFormatException e) { 
		    	p.sendMessage(red + "invalid number \"" + value + "\".");
		        return; 
		    } catch(NullPointerException e) {
		    	p.sendMessage(red + "invalid number \"" + value + "\".");
		        return;
		    }
			maxTimes = count;
			p.sendMessage(prp + "  Max set to " + count + " triggers.");
		}		
	}


}
