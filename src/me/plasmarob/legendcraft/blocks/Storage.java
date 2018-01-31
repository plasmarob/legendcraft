package me.plasmarob.legendcraft.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.bukkit.Material;
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
        
        // get a middleish block
        mainBlock = player.getWorld().getBlockAt(max.getBlockX() - (int)Math.floor(sel.getWidth()/2), 
        		                                 max.getBlockY() - (int)Math.floor(sel.getHeight()/2),
        		                                 max.getBlockZ() - (int)Math.floor(sel.getLength()/2));
        dbInsert();
        Tools.saySuccess(player, "Storage created!");
	}
	
	public Storage(Map<String,Object> data, Dungeon dungeon) {
		this.block_id = (Integer) data.get("id");
		this.name = (String) data.get("name");
		this.dungeon = dungeon;
		this.mainBlock = Tools.blockFromString((String) data.get("location"), dungeon.getWorld());
		this.defaultOnOff = Boolean.parseBoolean((String) data.get("default"));
		this.inverted = Boolean.parseBoolean((String) data.get("inverted"));
		this.min = Tools.weVectorFromString((String) data.get("min"));
		this.max = Tools.weVectorFromString((String) data.get("max"));
		String datajson = (String) data.get("data");
		
		JSONParser parser = new JSONParser(); 
		try {
			JSONObject json = (JSONObject) parser.parse(datajson);
			this.mode = (String) json.get("mode");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void dbInsert() {
		// Insert into DB
		String minstr = Tools.locationAsString((int)min.getX(), (int)min.getY(), (int)min.getZ());
		String maxstr = Tools.locationAsString((int)max.getX(), (int)max.getY(), (int)max.getZ());
		
		JSONObject data = new JSONObject();
		data.put("mode", mode);
		String datastr = data.toJSONString();
		
		
		/* HOW TO DECODE
		 * 
		JSONParser parser = new JSONParser(); 
		try {
			JSONObject json = (JSONObject) parser.parse("");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        */
		
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
				.add("storage_id", block_id)
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
                }
            }
        }
        //DB insert
        Frame f = new Frame(bsList);
		new DatabaseInserter("storageFrame")
				.add("storage_id", block_id)
				.add("frame_id", 1)
				.add("time", 20)
				.add("blocks", f.getBlockTypes())
				.add("data", f.getDataJSON())
				.execute();
        frames.add(f);
        f.setId(frames.size());
	}
	
	
	//@SuppressWarnings("deprecation")
	@Override
	public void trigger() 
	{
		if (!enabled || !isOn)
			return;
		
		Block tmpB;
		int i = 0;
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++)  {
        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++)  {
        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
        			tmpB = mainBlock.getWorld().getBlockAt(x, y, z);
        			//tmpB.setType(matList.get(i));
        			//tmpB.setData(datList.get(i));
        			i++;
                }
            }
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

	private class Frame {
		
		private List<BlockState> blocks;
		
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

}
