package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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
import me.plasmarob.legendcraft.util.Tools;

public class Storage implements Receiver {

	private String name;
	private Dungeon dungeon;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	private int block_id;
	
	private int currentFrameIndex=0;
	
	//List<Material> matList = new ArrayList<Material>();
	//List<Byte> datList = new ArrayList<Byte>();
	//List<BlockState> bsList = new ArrayList<BlockState>();
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
	
	public Storage(Map<String,Object> block, List<Map<String,Object>> frames, Dungeon dungeon) {
		this.block_id = (Integer) block.get("id");
		this.name = (String) block.get("name");
		this.dungeon = dungeon;
		this.mainBlock = Tools.blockFromString((String) block.get("location"), dungeon.getWorld());
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
		
		//TODO get storage frames into a storage, parsing their data (or add constructor using map)
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void dbInsert() {
		String minstr = Tools.locationAsString((int)min.getX(), (int)min.getY(), (int)min.getZ());
		String maxstr = Tools.locationAsString((int)max.getX(), (int)max.getY(), (int)max.getZ());
		
		JSONObject data = new JSONObject();
		data.put("mode", mode);
		String datastr = data.toJSONString();
		
		block_id = new DatabaseInserter("block")
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
				.add("block_id", block_id)
				.add("frame_id", 1)
				.add("time", 20)
				.add("blocks", f.getBlockTypes())
				.add("data", f.getDataJSON())
				.execute();
		
		//TODO enhancement: add into column any special block data as JSON
	}
	
	public void addFrame() {
		addFrame(20,-1);
	}
	public void addFrame(int time) {
		addFrame(time,-1);
	}
    public void addFrame(int ticks, int after) {
    	//Grab and create new frame
    	List<BlockState> bsList = new ArrayList<BlockState>();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
        			bsList.add(dungeon.getWorld().getBlockAt(x, y, z).getState());	
        }}}
        //DB insert
        Frame f = new Frame(bsList);
		new DatabaseInserter("storageFrame")
				.add("block_id", block_id)
				.add("frame_id", 1)
				.add("time", 20)
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
			currentFrameIndex = -1; //start before 0 to kick off index 0
			LegendCraft.plugin.getServer().getScheduler()
			   .scheduleSyncDelayedTask(LegendCraft.plugin, new FrameThread(this), frames.get(0).getTime());
		}
	}
	
	@SuppressWarnings("deprecation")
	public void next() {
		// stop after end
		if (mode.equals("ONCE")) {
			if (currentFrameIndex >= frames.size()) {
				return;
			}
			currentFrameIndex++;
		}
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
            
		if (mode.equals("ONCE") && currentFrameIndex < frames.size()-1) {
			LegendCraft.plugin.getServer().getScheduler()
			   .scheduleSyncDelayedTask(LegendCraft.plugin, new FrameThread(this), frames.get(currentFrameIndex+1).getTime());
		}
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEnabled(boolean bool) {
		// TODO Auto-generated method stub
		
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
	public void edit(Player p, String key, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasBlock(Block b) {
		// TODO Auto-generated method stub
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
		
		private int id = -1;
		
		public int getId() { return id; }
		public void setId(int id) { this.id=id; }
		
		Frame(List<BlockState> blocks, int id, int time) {
			this.blocks = blocks;
			this.id = id;
			this.ticks = time;
		}
		
		Frame(List<BlockState> blocks) {
			this.blocks = blocks;
			this.id = -1;
		}
		
		@SuppressWarnings("deprecation")
		public String getBlockTypes() {
			StringJoiner out = new StringJoiner(";");
			String t;
			for (BlockState bs : blocks) {
				// TYPE:ID,x,y,z
				t = bs.getType() + ":" + bs.getData().getData() + "," + Tools.locationAsString(bs.getX(), bs.getY(), bs.getZ());
				out.add(t);
			}
			return out.toString();
		}
		
        public String getDataJSON() {
			return "";
		}
	}
	
	/**
	 * Animation delay thread, calls for next frame
	 */
	class FrameThread implements Runnable {	
		Storage storage;
		FrameThread(Storage storage) { this.storage = storage; }
		public void update() { storage.next(); }
		public void run() {
		    try { update(); } catch (Exception e) { e.printStackTrace(); }
		}	
	}
}
