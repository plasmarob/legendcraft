package me.plasmarob.legendcraft.blocks;

import java.util.HashMap;

import me.plasmarob.legendcraft.Dungeon;
import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.database.DatabaseInserter;
import me.plasmarob.legendcraft.util.Tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Timer implements Sender, Receiver {
	private Dungeon dungeon;
	private int id = -1;
	public int getID() { return this.id; }
	public void setID(int id) { this.id= id; }
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	Block mainBlock;
	HashMap<Receiver, Link> messageTypes = new HashMap<Receiver, Link>();
	
	private int maxTimes = -1; //max times it can be triggered (-1 infinite)
	private int timesRun = 0; //how many times triggered
	private boolean isWaiting = false;
	private double delay = -1; //delay (-1 immediate)
	
	public Timer(Block mainBlock, String name, double delay, Dungeon dungeon) {
		this.dungeon = dungeon;
		this.mainBlock = mainBlock;
		this.name = name;
		this.delay = delay;
		
		dbInsert();
	}
	
	public void dbInsert() {
		// Insert into DB
		new DatabaseInserter("block")
				.dungeon_id(dungeon.getDungeonId())
				.type_id("TIMER")
				.name(name)
				.location(mainBlock.getX(), mainBlock.getY(), mainBlock.getZ())
				.add("default", defaultOnOff)
				.add("inverted", inverted)
				.add("times", maxTimes)
				.execute();
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
	public Block getMainBlock() {
		return mainBlock;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public double getDelay() {
		return delay;
	}

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean bool) {
		enabled = bool;
		isOn = defaultOnOff;
		if (enabled) {
			mainBlock.setType(Material.AIR);
		} else {
			mainBlock.setType(Material.QUARTZ_ORE);
		}
		timesRun = 0;
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
	
	@Override
	public void trigger() {
		if (enabled && isOn && !isWaiting && (maxTimes == -1 || timesRun < maxTimes)) {
			if (delay <= 0) {
				run();
			} else {
				LegendCraft.plugin.getServer().getScheduler().scheduleSyncDelayedTask(LegendCraft.plugin, 
						new SenderDelayThread(this), Math.round(delay*20));	// 20 ticks in a second
				isWaiting = true;
			}
		}
	}
	@Override
	public void set() {
		if (!enabled || !isOn)
			return;
		
		if (!inverted) {
			run();
		} else {
			timesRun = 0;
		}
	}
	@Override
	public void reset() {
		if (!enabled || !isOn)
			return;
		
		if (!inverted) {
			timesRun = 0;
		} else {
			run();
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
		isWaiting = false;
		timesRun++;
		//send the message to the receivers
		for (Receiver r : messageTypes.keySet()) {
			messageTypes.get(r).call(r);
		}
	}

	@Override
	public boolean setTarget(Receiver target, Link linkType) {
		try {
			if (messageTypes.containsKey(target)) messageTypes.remove(target);
			messageTypes.put(target, linkType);
			return true;
		} catch (Exception e) { return false; }
	}
	@Override
	public HashMap<Receiver, Link> getLinks() {
		return messageTypes;
	}
	// No setMessageType necessary - setTarget overrides anyway

	@Override
	public boolean removeLink(Receiver receiver) {
		if (messageTypes.containsKey(receiver)) {
			messageTypes.remove(receiver);
			return true;
		}
		return false;
	}
	@Override
	public void clearLinks() {
		messageTypes.clear();
	}
	
	@Override
	public String name() {
		return name;
	}
	@Override
	public String type() {
		return "timer";
	}
	
	static String prp = "" + ChatColor.LIGHT_PURPLE;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Timer \"" + name + "\":");

		String enable = "enabled";
		if (!enabled) enable = "disabled";
		p.sendMessage(prp + "  Currently " + enable + ".");
		
		String def = "ON";
		if (!defaultOnOff) def = "OFF";
		String on = "ON";
		if (!isOn) def = "OFF";
		p.sendMessage(prp + "  Is " + on + ", defaults to " + def + ".");
		p.sendMessage(prp + "  Delay: " + delay + " seconds (" + Math.round(delay*20) + " ticks)");
		p.sendMessage(prp + "  Inverted?: " + inverted);
		
		String maxTimeStr = "unlimited";
		if (maxTimes != -1) maxTimeStr = "" + maxTimes;
		p.sendMessage(prp + "  Max triggers: " + maxTimeStr);
		p.sendMessage(prp + "  Times triggered: " + timesRun);
		p.sendMessage(prp + "  Main block: " + mainBlock.getX() + " " + mainBlock.getY() + " " + mainBlock.getZ());
		
		for (Receiver r : messageTypes.keySet()) {
			p.sendMessage(prp + "  Links to " + r.name() + " ("+messageTypes.get(r).NAME+")");
			Tools.showLine(mainBlock.getWorld(), this, r);
		}
		//TODO: show inbound links
	}

	String red = "" + ChatColor.RED;
	public void edit(Player p, String key, String value) {
		
		if (key.toLowerCase().equals("delay")) {
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
		return b.equals(mainBlock);
	}
}
