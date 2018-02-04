package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

import me.plasmarob.legendcraft.Dungeon;
import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.database.DatabaseInserter;
import me.plasmarob.legendcraft.database.DatabaseMethods;
import me.plasmarob.legendcraft.util.Tools;

public class Storage implements Receiver {

	private String name;
	private int id = -1;
	public int getID() { return this.id; }
	public void setID(int id) { this.id = id; }
	
	
	private Dungeon dungeon;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	private int currentFrameIndex=0;
	
	List<Frame> frames = new ArrayList<Frame>();
	
	Block mainBlock;
	Vector min;
    Vector max;
    
    //-------
    String mode = "ONCE";
	
	public Storage(Player player, Block block, String name, Dungeon dungeon) {
		this.dungeon = dungeon;
		this.mainBlock = block;
		this.name = name;
		Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		min = sel.getNativeMinimumPoint();
        max = sel.getNativeMaximumPoint();
        
        List<BlockState> bsList = new ArrayList<BlockState>();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
        			bsList.add(player.getWorld().getBlockAt(x, y, z).getState());	
                }
            }
        }
        
        frames.add(new Frame(bsList,1,20));
        currentFrameIndex = 0;
        // get a middleish block
        mainBlock = player.getWorld().getBlockAt(max.getBlockX() - (int)Math.floor(sel.getWidth()/2), 
        		                                 max.getBlockY() - (int)Math.floor(sel.getHeight()/2),
        		                                 max.getBlockZ() - (int)Math.floor(sel.getLength()/2));
        dbInsert();
        Tools.saySuccess(player, "Storage created!");
	}
	
	@SuppressWarnings("deprecation")
	public Storage(Map<String,Object> block, List<Map<String,Object>> frameList, Dungeon dungeon) {
		this.id = (Integer) block.get("id");
		this.name = (String) block.get("name");
		this.dungeon = dungeon;
		this.mainBlock = Tools.blockFromXYZ((String) block.get("location"), dungeon.getWorld());
		this.defaultOnOff = Boolean.parseBoolean((String) block.get("default"));
		this.inverted = Boolean.parseBoolean((String) block.get("inverted"));
		this.min = Tools.weVectorFromString((String) block.get("min"));
		this.max = Tools.weVectorFromString((String) block.get("max"));
		String datajson = (String) block.get("data");
		// DECODE
		JSONParser parser = new JSONParser(); 
		try {
			JSONObject json = (JSONObject) parser.parse(datajson);
			this.mode = (String) json.get("mode");
		} catch (ParseException e) { e.printStackTrace(); }
		
		
		for (Map<String,Object> dbf : frameList) {
			List<BlockState> blocks = new ArrayList<BlockState>();
			String blockdata = (String) dbf.get("blocks");
			String[] blockA = blockdata.split(";"); // stored in xyz order, must be parsed front to back
			for (int i = 0; i < blockA.length; i++) {
				String[] blockInfo = blockA[i].split(":");
				BlockState bs = Tools.blockFromXYZ(blockInfo[2], dungeon.getWorld()).getState();
				bs.setType(Material.getMaterial(blockInfo[0]));
				bs.getData().setData((byte)Integer.parseInt(blockInfo[1]));
				blocks.add(bs);
			}
			
			int id = (Integer) dbf.get("frame_id");
			int time = (Integer) dbf.get("time");
			Frame f = new Frame(blocks,id,time);
			String json = (String) dbf.get("data");
			f.setDataByJSON(json);
			frames.add(f);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void dbInsert() {
		String minstr = Tools.locationAsString((int)min.getX(), (int)min.getY(), (int)min.getZ());
		String maxstr = Tools.locationAsString((int)max.getX(), (int)max.getY(), (int)max.getZ());
		
		JSONObject data = new JSONObject();
		data.put("mode", mode);
		String datastr = data.toJSONString();
		
		id = new DatabaseInserter("block")
						.dungeon_id(dungeon.getDungeonId())
						.type_id("STORAGE")
						.name(name)
						.location(mainBlock.getX(), mainBlock.getY(), mainBlock.getZ())
						.add("default", defaultOnOff)
						.add("inverted", inverted)
						.add("min", minstr)
						.add("max", maxstr)
						.add("data", datastr)
						.execute();
		
		Frame f = frames.get(0);
		new DatabaseInserter("storageFrame")
				.add("block_id", id)
				.add("frame_id", 1)
				.add("time", 20)
				.add("blocks", f.getBlockTypes())
				.add("data", f.getDataJSON())
				.execute();
		//TODO enhancement: add into column any special block data as JSON
	}
	
	
	
	public void addFrame() {
		addFrame(20);
	}
	public void addFrame(int time) {
		addFrame(time,-1);
	}
    public void addFrame(int ticks, int at_frame) {
    	if (ticks < 1) ticks = 1;
    	if (ticks > 600) ticks = 600; // 30 sec
    	
    	if (at_frame < 1 || at_frame > frames.size()) {
    		at_frame = frames.size() + 1;
    	}
    	
    	//Grab and create new frame
    	List<BlockState> bsList = new ArrayList<BlockState>();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
        			bsList.add(dungeon.getWorld().getBlockAt(x, y, z).getState());	
        }}}
        //DB insert
        Frame f = new Frame(bsList);
        DatabaseMethods.pushStorageFrames(id, at_frame); // slide any higher ids
		new DatabaseInserter("storageFrame")
				.add("block_id", id)
				.add("frame_id", at_frame)
				.add("time", ticks)
				.add("blocks", f.getBlockTypes())
				.add("data", f.getDataJSON())
				.execute();
        frames.add(f);
        f.setId(frames.size());
	}
	
	@Override
	public void trigger()  {
		if (!enabled || !isOn)
			return;
		// kick off an animation sequence
		if(mode.equals("ONCE")) {
			currentFrameIndex = 0; //start before 0 to kick off index 0
			animate();
			//LegendCraft.plugin.getServer().getScheduler()
			//   .scheduleSyncDelayedTask(LegendCraft.plugin, new FrameThread(this), frames.get(0).getTime());
		}
	}
	
	@SuppressWarnings("deprecation")
	private void animate() {
		// stop after end
		if (mode.equals("ONCE")) 
			if (currentFrameIndex >= frames.size()) return;	
		
		List<BlockState> blocks = frames.get(currentFrameIndex).getBlocks();
		
		int minx = min.getBlockX();
		int miny = min.getBlockY();
		int minz = min.getBlockZ();
		int maxx = max.getBlockX();
		int maxy = max.getBlockY();
		int maxz = max.getBlockZ();
        World w = dungeon.getWorld();

       
		int i = 0;
		for (int x = minx; x <= maxx; x++)  {
        	for (int y = miny; y <= maxy; y++)  {
        		for (int z = minz; z <= maxz; z++) {
        			w.getBlockAt(x, y, z).setTypeIdAndData(blocks.get(i).getTypeId(), blocks.get(i).getData().getData(), false);
        			i++;
        }}}
            
		currentFrameIndex++;
		
		if (mode.equals("ONCE") && currentFrameIndex < frames.size()-1) {
			LegendCraft.plugin.getServer().getScheduler()
			   .scheduleSyncDelayedTask(LegendCraft.plugin, new FrameThread(this), frames.get(currentFrameIndex).getTime());
		}
		
		
	}

	@Override
	public void set() {
		trigger();
	}
	@Override
	public void reset() {
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean bool) {
		enabled = bool;
	}

	@Override
	public void run() {
		if (isOn && enabled)
			trigger();
	}

	@Override
	public String type() {
		return "storage";
	}

	@Override
	public String name() {
		return name;
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
		
		p.sendMessage(prp + "  Block: " + mainBlock.getX() + " " + mainBlock.getY() + " " + mainBlock.getZ());
		
		p.sendMessage(prp + "  Min XYZ: " + min.getX() + " / " + min.getY() + " / " + min.getZ() + "");
		p.sendMessage(prp + "  Max XYZ: " + max.getX() + " / " + max.getY() + " / " + max.getZ() + "");
	}

	@Override
	public void edit(Player p, String key, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasBlock(Block b) {
		if (b.equals(mainBlock))
			return true;
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
        			if (b.equals(mainBlock.getWorld().getBlockAt(x, y, z)))
        				return true;
                }
            }
        }
		return false;
	}
	
	@SuppressWarnings("unused")
	private class Frame {	
		private List<BlockState> blocks;
		private List<BlockState> getBlocks() { return blocks; }
		private void setBlocks(List<BlockState> blocks) { this.blocks = blocks; }
		
		private int ticks = 20;
		public int getTime() { return ticks; }
		public void setTime(int t) { ticks=t; }
		
		private int fid = -1;
		
		public int getId() { return fid; }
		public void setId(int id) { this.fid=id; }
		
		Frame(List<BlockState> blocks, int id, int time) {
			this.blocks = blocks;
			this.fid = id;
			this.ticks = time;
		}
		
		Frame(List<BlockState> blocks) {
			this.blocks = blocks;
			this.fid = -1;
		}
		
		@SuppressWarnings("deprecation")
		public String getBlockTypes() {
			StringJoiner out = new StringJoiner(";");
			String t;
			for (BlockState bs : blocks) {
				// TYPE:ID,x,y,z
				t = bs.getType() + ":" + bs.getData().getData() + ":" + Tools.locationAsString(bs.getX(), bs.getY(), bs.getZ());
				out.add(t);
			}
			return out.toString();
		}
		
        public String getDataJSON() {
			return "";
		}
        
        public void setDataByJSON(String json) {
			// code for special blockstate banner/bed/etc data
		}
	}
	
	/**
	 * Animation delay thread, calls for next frame
	 */
	class FrameThread implements Runnable {	
		Storage storage;
		FrameThread(Storage storage) { this.storage = storage; }
		public void update() { storage.animate(); }
		public void run() {
		    try { update(); } catch (Exception e) { e.printStackTrace(); }
		}	
	}
}
