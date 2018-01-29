package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import me.plasmarob.legendcraft.Dungeon;
import me.plasmarob.legendcraft.database.DatabaseInserter;
import me.plasmarob.legendcraft.util.Tools;

public class Detector implements Sender, Receiver {
	private Dungeon dungeon;
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	private double delay = 0;	// TODO: add a delay
	
	List<Block> blockList = new ArrayList<Block>();
	Block mainBlock;
	boolean isWaiting = false;
	
	HashMap<Receiver, String> receivers = new HashMap<Receiver, String>();
	HashMap<Receiver, String> messageTypes = new HashMap<Receiver, String>();

	private int maxTimes = -1; //max times it can be triggered (-1 infinite)
	private int timesRun = 0; //how many times triggered
	private boolean isTriggering = false;
	
	public Detector(Player player, Block mainBlock, String name, Dungeon dungeon) {
		
		this.dungeon = dungeon;
		this.mainBlock = mainBlock;
		this.name = name;
		blockList.add(mainBlock);
		boolean foundMore = true;
		Block tmpB;
		while (blockList.size() < 100 && foundMore) {
			foundMore = false;
			List<Block> tempList = new ArrayList<Block>();
			for (Block b : blockList)
			{
				for (BlockFace bf : Tools.faces())
				{
					tmpB = b.getRelative(bf);
					if (tmpB.getType() == Material.COAL_ORE && !blockList.contains(tmpB))
					{
						tempList.add(tmpB);
						foundMore = true;
					}
				}
			}
			blockList.addAll(tempList);
		}
		if (blockList.size() >= 100)
			player.sendMessage("Too many potential detector blocks found. Truncating to first 100 found.");
		
		dbInsert();
	}
	
	public Detector(Map<String,Object> data, Dungeon dungeon) {
		this.name = (String) data.get("name");
		this.dungeon = dungeon;
		mainBlock = Tools.blockFromString((String) data.get("location"), dungeon.getWorld());
		defaultOnOff = Boolean.parseBoolean((String) data.get("default"));
		inverted = Boolean.parseBoolean((String) data.get("inverted"));
		maxTimes = (int) data.get("times");
		
		String blockString = (String) data.get("blocks");
		String[] blocks = blockString.split(";");
		for (String blk : blocks) {
			blockList.add(Tools.blockFromString(blk, dungeon.getWorld()));
		}
	}
	
	public void dbInsert() {
		// Insert into DB
		StringJoiner blocks = new StringJoiner(";");
		for (Block b : blockList) {
			blocks.add(Tools.locationAsString(b.getX(), b.getY(), b.getZ()));
		}
		
		new DatabaseInserter("block")
				.dungeon_id(dungeon.getDungeonId())
				.type_id("PLAYER_DETECTOR")
				.name(name)
				.location(mainBlock.getX(), mainBlock.getY(), mainBlock.getZ())
				.add("default", defaultOnOff)
				.add("inverted", inverted)
				.add("times", maxTimes)
				.add("blocks", blocks.toString())
				.execute();
	}
	
	@Deprecated
	public Detector(List<Block> blocks, Block mainBlock, String name) {
		blockList.addAll(blocks);	
		this.mainBlock = mainBlock;
		this.name = name;
	}
	
	public List<Block> getBlocks() {
		return blockList;
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
			for (Block b : blockList)
				b.setType(Material.AIR);
		} else {
			for (Block b : blockList)
				b.setType(Material.COAL_ORE);
		}
		timesRun = 0;
		isTriggering = false;
		isOn = defaultOnOff;
	}
	
	public boolean isDefaultOnOff() {
		return defaultOnOff;
	}
	public void setDefaultOnOff(boolean defaultOnOff) {
		this.defaultOnOff = defaultOnOff;
		dbInsert();
	}
	public boolean isInverted() {
		return inverted;
	}
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
		dbInsert();
	}
	public void setDelay(int delay) {
		this.delay = delay;
		dbInsert();
	}
	public double getDelay() {
		return delay;
	}
	
	public int getMaxTimes() {
		return maxTimes;
	}
	public void setMaxTimes(int maxTimes) {
		this.maxTimes = maxTimes;
		dbInsert();
	}
	public int getTimesTriggered() {
		return timesRun;
	}

	public void testPlayers(List<Player> players) {
		
		if (enabled && isOn && !isWaiting && (maxTimes == -1 || timesRun < maxTimes)) {
			for (Player p : players) {
				if (blockList.contains(p.getLocation().getBlock()) || 
						blockList.contains(p.getEyeLocation().getBlock())) {
					if (!isTriggering) {
						isTriggering = true;
						run();
					}
					return;
				}
			}	
			isTriggering = false;
		}
	}

	@Override
	public void trigger() {
		//if it's on, and 
		if (enabled && isOn && (maxTimes == -1 || timesRun < maxTimes)) {
				run();
		}
	}
	public void set() {
		if (!enabled || !isOn)
			return;
		isOn = !defaultOnOff;
		
		if (!inverted) {
			run();
		} else {
			timesRun = 0;
		}
	}
	public void reset() {
		if (!enabled || !isOn)
			return;
		isOn = defaultOnOff;
		timesRun = 0;
		
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
	public void run()
	{
		isWaiting = false;
		timesRun++;
		//send the message to the receivers
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

	public boolean removeLink(Receiver receiver) {
		if (receivers.containsKey(receiver)) {
			receivers.remove(receiver);
			messageTypes.remove(receiver);
			return true;
		}
		return false;
	}

	public void destroy() {
		for (Block b : blockList)
			b.setType(Material.AIR);
	}
		
	public void clearLinks() {
		receivers.clear();
		messageTypes.clear();
	}

	@Override
	public String type() {
		return "detector";
	}

	@Override
	public String name() {
		return name;
	}
	
	static String prp = "" + ChatColor.LIGHT_PURPLE;
	static String r = "" + ChatColor.RESET;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Detector \"" + name + "\":");

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
		
		dbInsert();
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
		return blockList.contains(b);
	}
	
}
