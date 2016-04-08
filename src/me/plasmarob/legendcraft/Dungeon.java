package me.plasmarob.legendcraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.blocks.ChestBlock;
import me.plasmarob.legendcraft.blocks.Detector;
import me.plasmarob.legendcraft.blocks.Door;
import me.plasmarob.legendcraft.blocks.MobTemplate;
import me.plasmarob.legendcraft.blocks.MusicBlock;
import me.plasmarob.legendcraft.blocks.Receiver;
import me.plasmarob.legendcraft.blocks.RedstoneDetector;
import me.plasmarob.legendcraft.blocks.Sender;
import me.plasmarob.legendcraft.blocks.SpawnerBlock;
import me.plasmarob.legendcraft.blocks.StorageBlock;
import me.plasmarob.legendcraft.blocks.Timer;
import me.plasmarob.legendcraft.blocks.TorchBlock;
import me.plasmarob.legendcraft.blocks.Tune;
import me.plasmarob.util.Tools;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

// TODO: check for storage and detectors in same world and inside dungeon boundaries
// 
// TODO: add and read musicblocks from file
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

public class Dungeon {

	// Global static data ///////////////////////
	public static File dungeonFolder;
	public static ConcurrentHashMap<String, Dungeon> dungeons = new ConcurrentHashMap<String, Dungeon>();
	public static ConcurrentHashMap<String, FileConfiguration> dungeonConfigs = new ConcurrentHashMap<String, FileConfiguration>();
	public static ConcurrentHashMap<Player, String> selectedDungeons = new ConcurrentHashMap<Player, String>();
	///////////////////////

	private boolean enabled = false;
	
	private World world;
	private Vector min;
	private Vector max;
	
	public ConcurrentHashMap<String, Detector> detectors = new ConcurrentHashMap<String, Detector>();
	private ConcurrentHashMap<String, StorageBlock> storages = new ConcurrentHashMap<String, StorageBlock>();
	private ConcurrentHashMap<String, SpawnerBlock> spawners = new ConcurrentHashMap<String, SpawnerBlock>();
	private ConcurrentHashMap<String, MusicBlock> musics = new ConcurrentHashMap<String, MusicBlock>();
	private ConcurrentHashMap<String, Door> doors = new ConcurrentHashMap<String, Door>();
	private ConcurrentHashMap<String, RedstoneDetector> rsDetectors = new ConcurrentHashMap<String, RedstoneDetector>();
	private ConcurrentHashMap<String, TorchBlock> torchBlocks = new ConcurrentHashMap<String, TorchBlock>();
	private ConcurrentHashMap<String, Timer> timerBlocks = new ConcurrentHashMap<String, Timer>();
	private ConcurrentHashMap<String, ChestBlock> chestBlocks = new ConcurrentHashMap<String, ChestBlock>();
	
	private List<Player> players = new ArrayList<Player>();
	
	public Dungeon(World world, Vector min, Vector max)
	{
		this.world = world;
		this.min = min;
		this.max = max;
	}
	
	
	public Dungeon(File folder, FileConfiguration config)
	{	
		this.world = Bukkit.getWorld(config.getString("world"));
		List<Double> min = config.getDoubleList("min");
		List<Double> max = config.getDoubleList("max");
		this.min = new Vector(min.get(0),min.get(1),min.get(2));
		this.max = new Vector(max.get(0),max.get(1),max.get(2));
	
		//add chests
		int i = 0;
		int j = 1;
		while (true) {
			i++;
			String name = config.getString("chests.c" + i + ".name");	// name
			if (name == null) {
				break;
			} else {
				
				//mainBlock
				List<Integer> xyzMain = config.getIntegerList("chests.c" + i + ".block");
				
				//List<Map<?, ?>> store = new ArrayList<Map<?, ?>>();
				
				List<Map<?, ?>> map = config.getMapList("chests.c" + i + ".itemList");
				@SuppressWarnings("unchecked")
				List<Integer> locs = (List<Integer>) config.getList("chests.c" + i + ".slots");
				
				Block mainBlock = world.getBlockAt(xyzMain.get(0), xyzMain.get(1), xyzMain.get(2));
				// creation & insertion
				ChestBlock newChe = new ChestBlock(map, locs, mainBlock, name);
				newChe.setDefaultOnOff(config.getBoolean("chests.c" + i + ".default")); // default
				newChe.setInverted(config.getBoolean("chests.c" + i + ".inverted")); // inverted
				
				newChe.setFace(BlockFace.valueOf(config.getString("chests.c" + i + ".face"))); // face
				
				chestBlocks.put(name, newChe);
			}
		}	
				
		//add detectors
		i = 0;
		j = 1;
		while (true) {
			i++;
			String name = config.getString("detectors.d" + i + ".name");	// name
			if (name == null) {
				break;
			} else {
				List<Block> blockList = new ArrayList<Block>(); // blockList
				j = 1;
				while (true) {
					List<Integer> xyz = config.getIntegerList("detectors.d" + i + "." + j);
					if (xyz.size() > 0) {
						blockList.add(world.getBlockAt(xyz.get(0), xyz.get(1), xyz.get(2)));
					} else
						break;
					j++;
				}
				
				//mainBlock
				List<Integer> xyzMain = config.getIntegerList("detectors.d" + i + ".mainBlock");
				Block mainBlock = world.getBlockAt(xyzMain.get(0), xyzMain.get(1), xyzMain.get(2));
				// creation & insertion
				Detector newDet = new Detector(blockList, mainBlock, name);
				newDet.setDefaultOnOff(config.getBoolean("detectors.d" + i + ".default")); // default
				newDet.setInverted(config.getBoolean("detectors.d" + i + ".inverted")); // inverted
				newDet.setMaxTimes(config.getInt("detectors.d" + i + ".times")); // max_trig_times
				detectors.put(name, newDet);
			}
		}	
		
		//add doors
		i = 0;
		while (true) {
			i++;
			String name = config.getString("doors.d" + i + ".name");	// name
			if (name == null)
				break;
			else {
				File doorFile = new File(folder, name + ".yml");
				FileConfiguration doorConfig = new YamlConfiguration();
				try {
					doorConfig.load(doorFile);
				} catch (Exception e) {e.printStackTrace();}	
				Door door = new Door(world, doorConfig, name);
				door.setDefaultOnOff(config.getBoolean("doors.d" + i + ".default")); // default
				door.setInverted(config.getBoolean("doors.d" + i + ".inverted")); // inverted
				doors.put(name, door);
			}
		}	
		
		//add music
		i = 0;
		while (true) {
			i++;
			String name = config.getString("musics.m" + i + ".name");	// name
			if (name == null) {
				break;
			} else {
				//mainBlock
				List<Integer> xyzMain = config.getIntegerList("musics.m" + i + ".location");
				Block mainBlock = world.getBlockAt(xyzMain.get(0), xyzMain.get(1), xyzMain.get(2));
				MusicBlock music = new MusicBlock(mainBlock, name);
				music.setDefaultOnOff(config.getBoolean("musics.m" + i + ".default")); // default
				music.setInverted(config.getBoolean("musics.m" + i + ".inverted")); // inverted
				music.setTuneString(config.getString("musics.m" + i + ".tune")); // inverted
				musics.put(name, music);
			}
		}	
		
		
		//add redstone detectors
		i = 0;
		while (true)
		{
			i++;
			String name = config.getString("redstones.r" + i + ".name");	// name
			if (name == null) {
				break;
			} else {
				//mainBlock
				List<Integer> xyzMain = config.getIntegerList("redstones.r" + i + ".mainBlock");
				Block mainBlock = world.getBlockAt(xyzMain.get(0), xyzMain.get(1), xyzMain.get(2));
				// creationn & insertion
				RedstoneDetector newRSDet = new RedstoneDetector(mainBlock, name);
				newRSDet.setDefaultOnOff(config.getBoolean("redstones.r" + i + ".default")); // default
				newRSDet.setInverted(config.getBoolean("redstones.r" + i + ".inverted")); // inverted
				newRSDet.setMaxTimes(config.getInt("redstones.r" + i + ".times")); // max_trig_times
				rsDetectors.put(name, newRSDet);
			}
		}
		
		//add spawners
		i = 0;
		while (true)
		{
			i++;
			String name = config.getString("spawners.s" + i + ".name");	// name
			if (name == null) {
				break;
			} else {
				File spawnerFile = new File(folder, name + ".yml");
				FileConfiguration spawnerConfig = new YamlConfiguration();
				try {
					spawnerConfig.load(spawnerFile);
				} catch (Exception e) {e.printStackTrace();}	
				SpawnerBlock sp = new SpawnerBlock(world, spawnerConfig, name);
				sp.setDefaultOnOff(config.getBoolean("spawners.s" + i + ".default")); // default
				sp.setInverted(config.getBoolean("spawners.s" + i + ".inverted")); // inverted
				spawners.put(name, sp);
			}
		}	
		
		//add storage
		i = 0;
		while (true)
		{
			i++;
			String name = config.getString("storages.s" + i + ".name");

			if (name == null)
				break;
			else {
				File storageFile = new File(folder, name + ".yml");
				FileConfiguration storageConfig = new YamlConfiguration();
				try {
					storageConfig.load(storageFile);
				} catch (Exception e) {e.printStackTrace();}	
				
				StorageBlock st = new StorageBlock(world, storageConfig, name);
				st.setDefaultOnOff(config.getBoolean("storages.s" + i + ".default"));
				st.setInverted(config.getBoolean("storages.s" + i + ".inverted"));
				storages.put(name, st);
			}
		}	
		
		//add timers
		i = 0;
		while (true) {
			i++;
			String name = config.getString("timers.t" + i + ".name");	// name
			double delay = config.getDouble("timers.t" + i + ".delay");	// double
			if (name == null) {
				break;
			} else {
				//mainBlock
				List<Integer> xyzMain = config.getIntegerList("timers.t" + i + ".mainBlock");
				Block mainBlock = world.getBlockAt(xyzMain.get(0), xyzMain.get(1), xyzMain.get(2));
				
				// creation & insertion
				Timer newTime = new Timer(mainBlock, name, delay);
				newTime.setDefaultOnOff(config.getBoolean("timers.t" + i + ".default")); // default
				newTime.setInverted(config.getBoolean("timers.t" + i + ".inverted")); // inverted
				newTime.setMaxTimes(config.getInt("timers.t" + i + ".times")); // max_trig_times
				timerBlocks.put(name, newTime);
			}
		}	
	
		//add torchblock
		i = 0;
		while (true)
		{
			i++;
			String name = config.getString("torchblocks.t" + i + ".name");
			if (name == null) {
				break;
			} else {
				//main trigger block
				List<Integer> xyzMain = config.getIntegerList("torchblocks.t" + i + ".mainBlock");
				Block mainBlock = world.getBlockAt(xyzMain.get(0), xyzMain.get(1), xyzMain.get(2));
				
				List<Block> blockList = new ArrayList<Block>(); // blockList
				j = 1;
				while (true) {
					List<Integer> xyz = config.getIntegerList("torchblocks.t" + i + "." + j);
					if (xyz.size() > 0) {
						blockList.add(world.getBlockAt(xyz.get(0), xyz.get(1), xyz.get(2)));
					} else
						break;
					j++;
				}
				
				// creation & insertion
				TorchBlock newTorchBlk = new TorchBlock(blockList, mainBlock, name);
				
				newTorchBlk.setDefaultOnOff(config.getBoolean("torchblocks.t" + i + ".default")); // default
				newTorchBlk.setInverted(config.getBoolean("torchblocks.t" + i + ".inverted")); // inverted
				newTorchBlk.setTorchType(config.getString("torchblocks.t" + i + ".type")); // torchType
				newTorchBlk.setTimeout(config.getInt("torchblocks.t" + i + ".timeout")); // timeout
				newTorchBlk.setDefaultLit(config.getBoolean("torchblocks.t" + i + ".lit")); // lit?
				torchBlocks.put(name, newTorchBlk);
			}
		}	
		
		//------------------------------------------
		// LINK BLOCKS (SENDER -> RECEIVER)
		//
		// all must be loaded (above) before we can get them to point to each other.
		//------------------------------------------
		int k = 1;	
		for (String name : detectors.keySet()) {
			Detector det = detectors.get(name);
			i = 1; //find detector in file
			String inName = config.getString("detectors.d" + i + ".name");
			while (inName != null) {
				// when match is found
				if (inName.equals(name)) {
					k = 1; //current triggerable in detector
					String targetType = config.getString("detectors.d" + i + ".receivers." + k + ".type"); // [detector|spawner|storage]
					String trigType = config.getString("detectors.d" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
					if (trigType == null) trigType = "trigger";	
					while (targetType != null) {
						String target = config.getString("detectors.d" + i + ".receivers." 
								+ k + ".name"); //shouldn't be null if type isn't	
						Receiver rec = getReceiver(target);
						if (rec != null) det.setTarget(rec,trigType); 
						k++;
						targetType = config.getString("detectors.d" + i + ".receivers." + k + ".type"); // [detector|spawner|storage]
						trigType = config.getString("detectors.d" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
						if (trigType == null) trigType = "trigger";	
					}
				}
				i++;
				inName = config.getString("detectors.d" + i + ".name");
			}	
		}
		
		k = 1;	
		for (String name : rsDetectors.keySet()) {
			RedstoneDetector rsDet = rsDetectors.get(name);
			i = 1; //find in file
			String inName = config.getString("redstones.r" + i + ".name");
			while (inName != null) {
				// when match is found
				if (inName.equals(name)) {
					k = 1; //current receiver in detector
					String targetType = config.getString("redstones.r" + i + ".receivers." + k + ".type"); // [receiver type]
					String trigType = config.getString("redstones.r" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
					if (trigType == null) trigType = "trigger";	
					while (targetType != null) {
						String target = config.getString("redstones.r" + i + ".receivers." + k + ".name");
						Receiver rec = getReceiver(target);
						if (rec != null) rsDet.setTarget(rec,trigType); 
						k++;
						targetType = config.getString("redstones.r" + i + ".receivers." + k + ".type"); // [receiver type]
						trigType = config.getString("redstones.r" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
						if (trigType == null) trigType = "trigger";	
					}
				}
				i++;
				inName = config.getString("redstones.r" + i + ".name");
			}	
		}
		
		k = 1;	
		for (String name : spawners.keySet()) {
			SpawnerBlock spawner = spawners.get(name);
			i = 1; //find in file
			String inName = config.getString("spawners.s" + i + ".name");
			while (inName != null) {
				// when match is found
				if (inName.equals(name)) {
					k = 1; //current receiver in detector
					String targetType = config.getString("spawners.s" + i + ".receivers." + k + ".type"); // [receiver type]
					String trigType = config.getString("spawners.s" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
					if (trigType == null) trigType = "trigger";	
					while (targetType != null) {
						String target = config.getString("spawners.s" + i + ".receivers." + k + ".name");
						Receiver rec = getReceiver(target);
						if (rec != null) spawner.setTarget(rec,trigType); 
						k++;
						targetType = config.getString("spawners.s" + i + ".receivers." + k + ".type"); // [receiver type]
						trigType = config.getString("spawners.s" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
						if (trigType == null) trigType = "trigger";	
					}
				}
				i++;
				inName = config.getString("spawners.s" + i + ".name");
			}	
		}
		
		k = 1;	
		for (String name : timerBlocks.keySet()) {
			Timer timer = timerBlocks.get(name);
			i = 1; //find in file
			String inName = config.getString("timers.t" + i + ".name");
			while (inName != null) {
				// when match is found
				if (inName.equals(name)) {
					k = 1; //current receiver in detector
					String targetType = config.getString("timers.t" + i + ".receivers." + k + ".type"); // [receiver type]
					String trigType = config.getString("timers.t" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
					if (trigType == null) trigType = "trigger";	
					while (targetType != null) {
						String target = config.getString("timers.t" + i + ".receivers." + k + ".name");
						Receiver rec = getReceiver(target);
						if (rec != null) timer.setTarget(rec,trigType); 
						k++;
						targetType = config.getString("timers.t" + i + ".receivers." + k + ".type"); // [receiver type]
						trigType = config.getString("timers.t" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
						if (trigType == null) trigType = "trigger";	
					}
				}
				i++;
				inName = config.getString("timers.t" + i + ".name");
			}	
		}
		
		k = 1;	
		for (String name : torchBlocks.keySet()) {
			TorchBlock torchBlk = torchBlocks.get(name);
			i = 1; //find in file
			String inName = config.getString("torchblocks.t" + i + ".name");
			while (inName != null) {
				// when match is found
				if (inName.equals(name)) {
					k = 1; //current receiver in detector
					String targetType = config.getString("torchblocks.t" + i + ".receivers." + k + ".type"); // [receiver type]
					String trigType = config.getString("torchblocks.t" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
					if (trigType == null) trigType = "trigger";	
					while (targetType != null) {
						String target = config.getString("torchblocks.t" + i + ".receivers." + k + ".name");
						Receiver rec = getReceiver(target);
						if (rec != null) torchBlk.setTarget(rec,trigType); 
						k++;
						targetType = config.getString("torchblocks.t" + i + ".receivers." + k + ".type"); // [receiver type]
						trigType = config.getString("torchblocks.t" + i + ".receivers." + k + ".trigtype"); // [trigger|set|reset]
						if (trigType == null) trigType = "trigger";	
					}
				}
				i++;
				inName = config.getString("torchblocks.t" + i + ".name");
			}	
		}
	}
	
	
	
	
	//universal careful file saver for setConfig
	public boolean saveObject(File folder, String name, Receiver obj) {
		
		if (!(obj instanceof Door) && 
			!(obj instanceof SpawnerBlock) &&
			!(obj instanceof StorageBlock)) 
			return false;
		
		// Do a trial run
		File objFile = new File(folder, name + "-temp.yml");
		try {
			LegendCraft.plugin.copyYamlsToFile(LegendCraft.plugin.getResource(name + "-temp.yml"), objFile);
			FileConfiguration objConfig = new YamlConfiguration();
			
			if (obj instanceof Door)
				((Door)obj).setConfig(objConfig); // file w/ {key mat&dat, min, max, blockList mat&dat}
			if (obj instanceof SpawnerBlock)
				((SpawnerBlock)obj).setConfig(objConfig); // file w/ {mainBlock, min, max, mobList name&count}
			if (obj instanceof StorageBlock)
				((StorageBlock)obj).setConfig(objConfig);
				objConfig.save(objFile);
		} catch (Exception e) {
			e.printStackTrace();
			objFile.delete();
			return false;
		}
		try {
			objFile.delete();
		} catch (Exception e){ e.printStackTrace(); }
		objFile = null;
		
		// Actually create file if we got this far
		objFile = new File(folder, name + ".yml");
		try {
			LegendCraft.plugin.copyYamlsToFile(LegendCraft.plugin.getResource(name + ".yml"), objFile);
			FileConfiguration objConfig = new YamlConfiguration();
			
			if (obj instanceof Door)
				((Door)obj).setConfig(objConfig); // file w/ {key mat&dat, min, max, blockList mat&dat}
			if (obj instanceof SpawnerBlock)
				((SpawnerBlock)obj).setConfig(objConfig); // file w/ {mainBlock, min, max, mobList name&count}
			if (obj instanceof StorageBlock)
				((StorageBlock)obj).setConfig(objConfig);
				objConfig.save(objFile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	
	
	public void setConfig(File folder, FileConfiguration config) {
		
		int i = 1; // block #
		int j = 1; // receiver #
		
		i = 1;
		for (String name : chestBlocks.keySet())
		{
			ChestBlock chest = chestBlocks.get(name);
			config.set("chests.c" + i + ".name", name);	// name	
			config.set("chests.c" + i + ".default", chest.isDefaultOnOff()); // default
			config.set("chests.c" + i + ".inverted", chest.isInverted()); // inverted
			
			Block mb = chest.getMainBlock(); // mainBlock
			List<Integer> mainXYZ = Arrays.asList(mb.getX(), mb.getY(), mb.getZ());
			config.set("chests.c" + i + ".block", mainXYZ);
			
			List<Map<?, ?>> itemList = chest.getItems();
			config.set("chests.c" + i + ".itemList", itemList);
			List<Integer> locList = chest.getItemLocations();
			config.set("chests.c" + i + ".slots", locList);
			
			config.set("chests.c" + i + ".face", chest.getFace().name());
			
			i++;
		}
		
		i = 1;
		for (String name : detectors.keySet())
		{
			Detector det = detectors.get(name);
			config.set("detectors.d" + i + ".name", name);	// name	
			config.set("detectors.d" + i + ".default", det.isDefaultOnOff()); // default
			config.set("detectors.d" + i + ".inverted", det.isInverted()); // inverted
			config.set("detectors.d" + i + ".times", det.getMaxTimes());	// maxTimes
			
			Block mb = det.getMainBlock(); // mainBlock
			List<Integer> mainXYZ = Arrays.asList(mb.getX(), mb.getY(), mb.getZ());
			config.set("detectors.d" + i + ".mainBlock", mainXYZ);
			
			List<Block> blocks = det.getBlocks(); // blockList
			j = 1;
			for (Block b : blocks) {
				List<Integer> xyz = Arrays.asList(b.getX(), b.getY(), b.getZ());
				config.set("detectors.d" + i + "." + j, xyz);
				j++;
			}
			
			HashMap<Receiver, String> targets = det.getTargets();	//receivers
			HashMap<Receiver, String> messageTypes = det.getMessageTypes();
			j = 1;
			for (Receiver receiver : targets.keySet()) {
				config.set("detectors.d" + i + ".receivers." + j + ".type", receiver.type());
				config.set("detectors.d" + i + ".receivers." + j + ".name", receiver.name());
				config.set("detectors.d" + i + ".receivers." + j + ".trigtype", messageTypes.get(receiver));
				j++;
			}
			
			i++;
		}
		
		i = 1;
		for (String name : doors.keySet())
		{
			Door door = doors.get(name);
			config.set("doors.d" + i + ".name", name);	// name
			config.set("doors.d" + i + ".default", door.isDefaultOnOff());	// default			
			config.set("doors.d" + i + ".inverted", door.isInverted()); // inverted
			
			saveObject(folder, name, door);
			i++;
		}
		
		i = 1;
		for (String name : musics.keySet())
		{
			MusicBlock mb = musics.get(name);
			config.set("musics.m" + i + ".name", name);	// name
			config.set("musics.m" + i + ".default", mb.isDefaultOnOff());	// default			
			config.set("musics.m" + i + ".inverted", mb.isInverted()); // inverted
			
			List<Integer> xyz = mb.getXYZ();	// block
			config.set("musics.m" + i + ".location", xyz);
			String tuneString = mb.getTuneString();	// tune string
			config.set("musics.m" + i + ".tune", tuneString);
			
			i++;
		}
		
		i = 1;
		for (String name : rsDetectors.keySet())
		{
			RedstoneDetector rsDet = rsDetectors.get(name);
			config.set("redstones.r" + i + ".name", name);	// name
			config.set("redstones.r" + i + ".default", rsDet.isDefaultOnOff());	// default			
			config.set("redstones.r" + i + ".inverted", rsDet.isInverted()); // inverted
			config.set("redstones.r" + i + ".times", rsDet.getMaxTimes());	// maxtimes
			
			Block mb = rsDet.getMainBlock();	// mainBlock
			List<Integer> mainXYZ = Arrays.asList(mb.getX(), mb.getY(), mb.getZ());
			config.set("redstones.r" + i + ".mainBlock", mainXYZ);
			
			HashMap<Receiver, String> targets = rsDet.getTargets();	// receivers
			HashMap<Receiver, String> messageTypes = rsDet.getMessageTypes();
			j = 1;
			for (Receiver receiver : targets.keySet()) {
				config.set("redstones.r" + i + ".receivers." + j + ".type", receiver.type());
				config.set("redstones.r" + i + ".receivers." + j + ".name", receiver.name());
				config.set("redstones.r" + i + ".receivers." + j + ".trigtype", messageTypes.get(receiver));
				j++;
			}
			
			i++;
		}
		
		i = 1;
		for (String name : spawners.keySet())
		{
			SpawnerBlock sp = spawners.get(name);
			config.set("spawners.s" + i + ".name", name);	// name
			config.set("spawners.s" + i + ".default", sp.isDefaultOnOff());	// default			
			config.set("spawners.s" + i + ".inverted", sp.isInverted()); // inverted
		
			HashMap<Receiver, String> targets = sp.getTargets();	// receivers
			HashMap<Receiver, String> messageTypes = sp.getMessageTypes();
			j = 1;
			for (Receiver receiver : targets.keySet()) {
				config.set("spawners.s" + i + ".receivers." + j + ".type", receiver.type());
				config.set("spawners.s" + i + ".receivers." + j + ".name", receiver.name());
				config.set("spawners.s" + i + ".receivers." + j + ".trigtype", messageTypes.get(receiver));
				j++;
			}
			
			saveObject(folder, name, sp);
			i++;
		}
		
		i = 1;
		for (String name : storages.keySet()) {
			StorageBlock sb = storages.get(name);
			config.set("storages.s" + i + ".name", name);	// name
			config.set("storages.s" + i + ".default", sb.isDefaultOnOff());	// default			
			config.set("storages.s" + i + ".inverted", sb.isInverted()); // inverted
		
			saveObject(folder, name, sb);
			i++;
		}
		
		i = 1;
		for (String name : timerBlocks.keySet()) {
			Timer timer = timerBlocks.get(name);
			config.set("timers.t" + i + ".name", name);	//name
			config.set("timers.t" + i + ".default", timer.isDefaultOnOff());	// default
			config.set("timers.t" + i + ".inverted", timer.isInverted()); // inverted
			config.set("timers.t" + i + ".delay", timer.getDelay()); // delay
			config.set("timers.t" + i + ".times", timer.getMaxTimes());	// maxtimes
			
			Block mb = timer.getMainBlock();	// mainBlock
			List<Integer> mainXYZ = Arrays.asList(mb.getX(), mb.getY(), mb.getZ());
			config.set("timers.t" + i + ".mainBlock", mainXYZ);
			
			HashMap<Receiver, String> targets = timer.getTargets();	// receivers
			HashMap<Receiver, String> messageTypes = timer.getMessageTypes();
			j = 1;
			for (Receiver receiver : targets.keySet()) {
				config.set("timers.t" + i + ".receivers." + j + ".type", receiver.type());
				config.set("timers.t" + i + ".receivers." + j + ".name", receiver.name());
				config.set("timers.t" + i + ".receivers." + j + ".trigtype", messageTypes.get(receiver));
				j++;
			}
			
			i++;
		}
		
		i = 1;
		for (String name : torchBlocks.keySet()) {
			TorchBlock torchBlk = torchBlocks.get(name);
			config.set("torchblocks.t" + i + ".name", name);	//name
			config.set("torchblocks.t" + i + ".default", torchBlk.isDefaultOnOff());	// default
			config.set("torchblocks.t" + i + ".inverted", torchBlk.isInverted()); // inverted
			config.set("torchblocks.t" + i + ".lit", torchBlk.isDefaultLit()); // lit?
			config.set("torchblocks.t" + i + ".type", torchBlk.getTorchType()); // torch type
			config.set("torchblocks.t" + i + ".timeout", torchBlk.getTimeout()); // timeout
			
			Block mb = torchBlk.getMainBlock();	// mainBlock
			List<Integer> mainXYZ = Arrays.asList(mb.getX(), mb.getY(), mb.getZ());
			config.set("torchblocks.t" + i + ".mainBlock", mainXYZ);
			
			j = 1;
			List<Block> blocks = torchBlk.getTorches();	// torchList
			for (Block b : blocks) {
				List<Integer> xyz = Arrays.asList(b.getX(), b.getY(), b.getZ());
				config.set("torchblocks.t" + i + "." + j, xyz);
				j++;
			}
			
			HashMap<Receiver, String> targets = torchBlk.getTargets();	// receivers
			HashMap<Receiver, String> messageTypes = torchBlk.getMessageTypes();
			j = 1;
			for (Receiver receiver : targets.keySet()) {
				config.set("torchblocks.t" + i + ".receivers." + j + ".type", receiver.type());
				config.set("torchblocks.t" + i + ".receivers." + j + ".name", receiver.name());
				config.set("torchblocks.t" + i + ".receivers." + j + ".trigtype", messageTypes.get(receiver));
				j++;
			}
			
			i++;
		}
		
		
	}
	
	
	
	public World getWorld() {
		return world;
	}
	
	public double[] getCorners()
	{
		double[] corners = {min.getX(),min.getY(),min.getZ(),max.getX(),max.getY(),max.getZ()};
		return corners;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	public void setEnabled(boolean bool)
	{
		enabled = bool;	
		for (String s : chestBlocks.keySet()) {
			chestBlocks.get(s).setEnabled(bool);
		}
		for (String s : detectors.keySet()) {
			detectors.get(s).setEnabled(bool);
		}
		for (String s : rsDetectors.keySet()) {
			rsDetectors.get(s).setEnabled(bool);
		}
		for (String s : storages.keySet()) {
			storages.get(s).setEnabled(bool);
		}
		for (String s : spawners.keySet()) {
			spawners.get(s).setEnabled(bool);
		}
		for (String s : doors.keySet()) {
			doors.get(s).setEnabled(bool);
		}
		for (String s : musics.keySet()) {
			musics.get(s).setEnabled(bool);
		}
		for (String s : timerBlocks.keySet()) {
			timerBlocks.get(s).setEnabled(bool);
		}
		for (String s : torchBlocks.keySet()) {
			torchBlocks.get(s).setEnabled(bool);
		}
	}
	
	public boolean tryAddDetector(Player player, String name)
	{
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		while (bit.hasNext()) {
			next = bit.next();
			if (next.getType() == Material.COAL_ORE) {
				if (next.getX() < min.getX() || next.getX() > max.getX() || 
					next.getY() < min.getY() || next.getY() > max.getY() || 
					next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
					next.getWorld() != world) 
				{
					player.sendMessage(ChatColor.RED + "This block is not within dungeon boundaries.");
					return false;
				} else {
					detectors.put(name, new Detector(player, next, name));
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Detector " + name + " created!");
					return true;
				}
			} else if (!Tools.canSeeThrough(next.getType())) {
				player.sendMessage(ChatColor.RED + "Coal not found. Enter this command while facing " + 
									Material.COAL_ORE.toString());
				return false;
			}
		}
		return false;
	}
	
	public boolean tryAddTimer(Player player, String name, String value) {
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		double delay = 0;
		try { 
			delay = Double.parseDouble(value); 
	    } catch(NumberFormatException e) { 
	    	player.sendMessage(ChatColor.RED + "invalid number \"" + value + "\".");
	        return false; 
	    } catch(NullPointerException e) {
	    	player.sendMessage(ChatColor.RED + "invalid number \"" + value + "\".");
	        return false;
	    }
		player.sendMessage(prp + "  Delay set to " + delay + " seconds (" + Math.round(delay*20) + " ticks)");
		
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		while (bit.hasNext()) {
			next = bit.next();
			
			if (next.getType() == Material.QUARTZ_ORE) {
				if (next.getX() < min.getX() || next.getX() > max.getX() || 
					next.getY() < min.getY() || next.getY() > max.getY() || 
					next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
					next.getWorld() != world) 
				{
					player.sendMessage(ChatColor.RED + "This block is not within dungeon boundaries.");
					return false;
				} else {
					timerBlocks.put(name, new Timer(next, name, delay));
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Timer block " + name + " created!");
					return true;
				}
			} else if (!Tools.canSeeThrough(next.getType())) {
				player.sendMessage(ChatColor.RED + "Quartz not found. Enter this command while facing " + 
									Material.QUARTZ_ORE.toString());
				return false;
			}
		}
		return false;
	}
	
	public boolean tryAddTorchBlock(Player player, String name) {
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		
		while (bit.hasNext())
		{
			next = bit.next();
			
			if (next.getType() == Material.IRON_ORE)
			{
				if (next.getX() < min.getX() || next.getX() > max.getX() || 
					next.getY() < min.getY() || next.getY() > max.getY() || 
					next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
					next.getWorld() != world) 
				{
					player.sendMessage(ChatColor.RED + "This block is not within dungeon boundaries.");
					return false;
				} else {
					torchBlocks.put(name, new TorchBlock(next, name));
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Torch block " + name + " created!");
					return true;
				}
			}
			else if (!Tools.canSeeThrough(next.getType()))
			{
				player.sendMessage(ChatColor.RED + "Iron not found. Enter this command while facing " + 
									Material.IRON_ORE.toString());
				return false;
			}
		}
		return false;
	}
	public boolean tryAddTorch(Player player, String tBlkName) {
		if (!torchBlocks.containsKey(tBlkName))
			return false;
		else {
			torchBlocks.get(tBlkName).addTorch(player);
		}	
		return true;
	}
	
	
	public boolean tryAddRSDetector(Player player, String name)
	{
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		
		while (bit.hasNext())
		{
			next = bit.next();
			
			if (next.getType() == Material.REDSTONE_ORE || 
					next.getType() == Material.GLOWING_REDSTONE_ORE)
			{
				if (next.getX() < min.getX() || next.getX() > max.getX() || 
					next.getY() < min.getY() || next.getY() > max.getY() || 
					next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
					next.getWorld() != world) 
				{
					player.sendMessage(ChatColor.RED + "This block is not within dungeon boundaries.");
					return false;
				} else {
					rsDetectors.put(name, new RedstoneDetector(player, next, name));
					player.sendMessage(ChatColor.LIGHT_PURPLE + "RS Detector " + name + " created!");
					return true;
				}
			}
			else if (!Tools.canSeeThrough(next.getType()))
			{
				player.sendMessage(ChatColor.RED + "redstone not found. Enter this command while facing " + 
									Material.REDSTONE_ORE.toString());
				return false;
			}
		}
		return false;
	}

	
	public boolean tryAddChest(Player player, String name) {
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		
		while (bit.hasNext())
		{
			next = bit.next();
			
			if (next.getType() == Material.CHEST)
			{
				if (next.getX() < min.getX() || next.getX() > max.getX() || 
					next.getY() < min.getY() || next.getY() > max.getY() || 
					next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
					next.getWorld() != world) 
				{
					player.sendMessage(ChatColor.RED + "This chest is not within dungeon boundaries.");
					return false;
				} else {
					chestBlocks.put(name, new ChestBlock(player, next, name));
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Chest " + name + " created!");
					return true;
				}
			}
			else if (!Tools.canSeeThrough(next.getType()))
			{
				player.sendMessage(ChatColor.RED + "chest not found. Enter this command while facing " + 
									Material.CHEST.toString());
				return false;
			}
		}
		return false;
	}
	
	
	
	public boolean tryAddStorageBlock(Player player, String name)
	{
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		
		while (bit.hasNext())
		{
			next = bit.next();
			
			Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		    if (sel instanceof CuboidSelection && 
		    		sel.getHeight()*sel.getLength()*sel.getWidth() < 2000 ) 
		    {
				if (next.getType() == Material.EMERALD_ORE)
				{
					if (next.getX() < min.getX() || next.getX() > max.getX() || 
						next.getY() < min.getY() || next.getY() > max.getY() || 
						next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
						next.getWorld() != world) 
					{
						player.sendMessage(ChatColor.RED + "This block is not within dungeon boundaries.");
						return false;
					} else {
						storages.put(name, new StorageBlock(player, next, name));
						return true;
					}
					
				}
		    }
		    else
		    	player.sendMessage(ChatColor.RED + "Invalid WorldEdit selection.");
		    
		    if (!Tools.canSeeThrough(next.getType()))
			{
				player.sendMessage(ChatColor.RED + "Storage Block not found. Enter this command while facing " + 
									Material.EMERALD_ORE.toString());
				return false;
			}
		}
		player.sendMessage(ChatColor.RED + "Storage Block not found. Enter this command while facing " + 
							Material.EMERALD_ORE.toString());
		return false;
	}
	
	
	
	
	public boolean tryAddDoor(Player player, String name)
	{
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
	    if (sel instanceof CuboidSelection && 
	    		sel.getHeight()*sel.getLength()*sel.getWidth() < 2000 ) 
	    {
			Vector dMin = sel.getNativeMinimumPoint();
			Vector dMax = sel.getNativeMaximumPoint();
	    	
			if (dMin.getX() < min.getX() || dMax.getX() > max.getX() || 
					dMin.getY() < min.getY() || dMax.getY() > max.getY() || 
					dMin.getZ() < min.getZ() || dMax.getZ() > max.getZ() ||
				sel.getWorld() != world) 
			{
				player.sendMessage(ChatColor.RED + "Selection is not within dungeon boundaries.");
				return false;
			} else {
				doors.put(name, new Door(player, name));
				return true;
			}
	    }
	    else
	    	player.sendMessage(ChatColor.RED + "Invalid WorldEdit selection.");
		return false;
	}
	
	
	public boolean tryAddMusic(Player player, String name) {
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		
		while (bit.hasNext())
		{
			next = bit.next();
			
			if (next.getType() == Material.DIAMOND_ORE)
			{
				if (next.getX() < min.getX() || next.getX() > max.getX() || 
					next.getY() < min.getY() || next.getY() > max.getY() || 
					next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
					next.getWorld() != world) 
				{
					player.sendMessage(ChatColor.RED + "This block is not within dungeon boundaries.");
					return false;
				} else if (!Tune.tuneStrings.containsKey(player)) {
					player.sendMessage(ChatColor.RED + "Use '/lc ps <tune>' to create a tune first.");
					return false;
				} else {
					musics.put(name, new MusicBlock(player, next, name));
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Musicblock " + name + " created!");
					return true;
				}
				
			}
			else if (!Tools.canSeeThrough(next.getType()))
			{
				player.sendMessage(ChatColor.RED + "Musicblock not found. Enter this command while facing " + 
									Material.DIAMOND_ORE.toString());
				return false;
			}
		}
		return false;
	}

	
	
	public boolean tryAddSpawner(Player player, String name) {
		
		if (nameUsed(name)) {
			player.sendMessage(ChatColor.RED + "A dungeon block with this name already exists.");
			return false;
		}
		
		BlockIterator bit = new BlockIterator(player, 20);
		Block next;
		
		while (bit.hasNext())
		{
			next = bit.next();
			Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		    if (sel instanceof CuboidSelection && 
		    		sel.getHeight()*sel.getLength()*sel.getWidth() < 2000 ) 
		    {
				if (next.getType() == Material.GOLD_ORE) {
					if (next.getX() < min.getX() || next.getX() > max.getX() || 
						next.getY() < min.getY() || next.getY() > max.getY() || 
						next.getZ() < min.getZ() || next.getZ() > max.getZ() ||
						next.getWorld() != world) 
					{
						player.sendMessage(ChatColor.RED + "This block is not within dungeon boundaries.");
						return false;
					} else {
						spawners.put(name, new SpawnerBlock(player, next, name));
						return true;
					}
				}
		    }
		    else
		    	player.sendMessage(ChatColor.RED + "Invalid WorldEdit selection.");
		    
		    if (next.getType() != Material.AIR) {
				player.sendMessage(ChatColor.RED + "Spawner not found. Enter this command while facing " + Material.GOLD_ORE.toString());
				return false;
			}
		}
		player.sendMessage(ChatColor.RED + "Spawner not found. Enter this command while facing " + Material.GOLD_ORE.toString());
		return false;
	}

	
	public boolean tryLink(Player player, String b1, String b2, String type) {
		
		Sender sender = getSender(b1);
		Receiver receiver = getReceiver(b2);
		if (sender != null && receiver != null) {
			sender.setTarget(receiver, type);
			player.sendMessage(ChatColor.GREEN + type + " link successful.");
			return true;
		} else if (receiver == null)
			player.sendMessage(ChatColor.RED + b2 + " does not exist.");
		else
			player.sendMessage(ChatColor.RED + b1 + " does not exist.");
		return false;
	}
	
	
	
	public boolean nameUsed(String name)
	{
		return (chestBlocks.containsKey(name) ||
				detectors.containsKey(name) || 
				storages.containsKey(name) || 
				spawners.containsKey(name) ||
				musics.containsKey(name) ||
				doors.containsKey(name) ||
				rsDetectors.containsKey(name) ||
				timerBlocks.containsKey(name) ||
				torchBlocks.containsKey(name)
				);
	}
	
	public Sender getSender (String s)
	{
		if (detectors.containsKey(s))
			return detectors.get(s);
		else if (spawners.containsKey(s))
			return spawners.get(s);	
		else if (rsDetectors.containsKey(s))
			return rsDetectors.get(s);
		else if (timerBlocks.containsKey(s))
			return timerBlocks.get(s);
		else if (torchBlocks.containsKey(s))
			return torchBlocks.get(s);
		return null;			
	}
	
	public Receiver getReceiver (String r)
	{
		if (spawners.containsKey(r))
			return spawners.get(r);
		else if (detectors.containsKey(r))
			return detectors.get(r);	
		else if (storages.containsKey(r))
			return storages.get(r);		
		else if (musics.containsKey(r))
			return musics.get(r);		
		else if (doors.containsKey(r))
			return doors.get(r);	
		else if (timerBlocks.containsKey(r))
			return timerBlocks.get(r);
		else if (torchBlocks.containsKey(r))
			return torchBlocks.get(r);
		else if (chestBlocks.containsKey(r))
			return chestBlocks.get(r);
		return null;			
	}
	
	public boolean isHeldSender (String s)
	{
		return (detectors.containsKey(s) || 
				spawners.containsKey(s) ||
				rsDetectors.containsKey(s) ||
				torchBlocks.containsKey(s));
	}
	
	public boolean isHeldReceiver (String s)
	{
		return (detectors.containsKey(s) ||
				storages.containsKey(s) ||
				musics.containsKey(s) ||
				doors.containsKey(s) ||
				spawners.containsKey(s) ||
				chestBlocks.containsKey(s) ||
				torchBlocks.containsKey(s));
	}
	
	
	
	

	public boolean tryInsertMob(Player player, String spawner, String name, MobTemplate mob, int count) {
		if (spawners.containsKey(spawner))
		{
			spawners.get(spawner).insert(mob, name, count);
			player.sendMessage(ChatColor.LIGHT_PURPLE + Integer.toString(count) + 
					" of " + name + " inserted into " + spawner + ".");
			return true;
		}
		else
			player.sendMessage(ChatColor.RED + "Spawner with this name does not exist.");
		return false;
	}

	
	
	
	public void updateIfEnabled() {
		if(enabled)
			update();
	}
	//Actually check for players around.
	public void update() {
		
		players.clear();
		players = world.getPlayers();
		Player p;
		for (int i = 0; i < players.size(); i++)
		{
			p = players.get(i);
			if (p.getLocation().getX() < min.getBlockX() ||
				p.getLocation().getY() < min.getBlockY() ||
				p.getLocation().getZ() < min.getBlockZ() ||
				p.getLocation().getX() > max.getBlockX() ||
				p.getLocation().getY() > max.getBlockY() ||
				p.getLocation().getZ() > max.getBlockZ())
			{
				players.remove(p);
				i--;
			}
		}
		
		
		for (String d : detectors.keySet()) {
			detectors.get(d).testPlayers(players);
		}
		for (String t : torchBlocks.keySet()) {
			torchBlocks.get(t).testPlayers(players);
		}
		
		for (String s : spawners.keySet()) {
			spawners.get(s).updateMobs();
		}
	}


	static String prp = "" + ChatColor.LIGHT_PURPLE;
	public void show(Player player, String name) {
		if (detectors.containsKey(name)) {
			detectors.get(name).show(player);
			showLinksFrom(player, detectors.get(name));
		} else if (rsDetectors.containsKey(name)) {
			rsDetectors.get(name).show(player);
			showLinksFrom(player, rsDetectors.get(name));
		} else if (spawners.containsKey(name)) {
			SpawnerBlock sp = spawners.get(name);
			sp.show(player);
			showLinksFrom(player, sp);
		} else if (storages.containsKey(name)) {
			StorageBlock st = storages.get(name);
			st.show(player);
			showLinksFrom(player, st);
		} else if (doors.containsKey(name)) {
			Door door = doors.get(name);
			door.show(player);
			showLinksFrom(player, door);
		} else if (torchBlocks.containsKey(name)) {
			TorchBlock tb = torchBlocks.get(name);
			tb.show(player);
			showLinksFrom(player, tb);
		} else if (musics.containsKey(name)) {
			MusicBlock mb = musics.get(name);
			mb.show(player);
			showLinksFrom(player, mb);
		} else if (timerBlocks.containsKey(name)) {
			Timer time = timerBlocks.get(name);
			time.show(player);
			showLinksFrom(player, time);
		} else if (chestBlocks.containsKey(name)) {
			ChestBlock chest = chestBlocks.get(name);
			chest.show(player);
			showLinksFrom(player, chest);
		}
	}

	public void showLinksFrom(Player player, Receiver rec)
	{
		for (String s : detectors.keySet()) {
			if (detectors.get(s).getTargets().containsKey(rec)) 
				player.sendMessage(prp + "  Link from " + s + " ("+ detectors.get(s).getMessageTypes().get(rec) +")" );
		}
		for (String s : rsDetectors.keySet()) {
			if (rsDetectors.get(s).getTargets().containsKey(rec)) 
				player.sendMessage(prp + "  Link from " + s + " ("+ rsDetectors.get(s).getMessageTypes().get(rec) +")" );
		}
		for (String s : spawners.keySet()) {
			if (spawners.get(s).getTargets().containsKey(rec)) 
				player.sendMessage(prp + "  Link from " + s + " ("+ spawners.get(s).getMessageTypes().get(rec) +")" );
		}
		for (String s : torchBlocks.keySet()) {
			if (torchBlocks.get(s).getTargets().containsKey(rec)) 
				player.sendMessage(prp + "  Link from " + s + " ("+ torchBlocks.get(s).getMessageTypes().get(rec) +")" );
		}
		for (String s : timerBlocks.keySet()) {
			if (timerBlocks.get(s).getTargets().containsKey(rec)) 
				player.sendMessage(prp + "  Link from " + s + " ("+ timerBlocks.get(s).getMessageTypes().get(rec) +")" );
		}
	}
	
	
	public void testForRedstone(Block block) {
		for (String key : rsDetectors.keySet()) {
			rsDetectors.get(key).testForRedstone(block);
		}
	}

	public void unlink(Player player, String name)
	{
		Sender sender = getSender(name);
		if (sender != null) sender.clearLinks();
		
		Receiver rec = getReceiver(name);
		if (rec != null) {
			for (String s : detectors.keySet()) 
				detectors.get(s).removeLink(rec);
			for (String s : rsDetectors.keySet()) 
				rsDetectors.get(s).removeLink(rec);
			for (String s : spawners.keySet()) 
				spawners.get(s).removeLink(rec);
			for (String s : torchBlocks.keySet()) 
				torchBlocks.get(s).removeLink(rec);
			for (String s : timerBlocks.keySet()) 
				timerBlocks.get(s).removeLink(rec);
		}
		if (sender != null || rec != null)
			player.sendMessage(ChatColor.LIGHT_PURPLE + "All links to and from this block deleted.");
	}
	

	public void unlink(Player player, String sender, String target) {
		Sender send = getSender(sender);
		Receiver rec = getReceiver(target);
		if (rec != null && send != null) {
			send.removeLink(rec);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "This link removed.");
		}
	}

	
	public boolean tryDeleteDungeon(Player player, String myName, File file) {
		
		// Delete independent files
		for (String s : detectors.keySet()) {
			tryDelete(player, s, Tools.findFile(myName, s));
		}
		for (String s : spawners.keySet()) {
			tryDelete(player, s, Tools.findFile(myName, s));
		}
		for (String s : storages.keySet()) {
			tryDelete(player, s, Tools.findFile(myName, s));
		}
		
		if (file != null && file.exists())
		{
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Dungeon deleted.");
			return true;
		}
		return true;
	}
	
	
	public void tryDelete(Player player, String name, File file) {
		unlink(player, name);
		if (chestBlocks.containsKey(name)) {
			chestBlocks.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Chest " + name + " and its links deleted.");
		} else if (detectors.containsKey(name)) {
			detectors.get(name).destroy();
			detectors.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Detector " + name + " and its links deleted.");
		} else if (rsDetectors.containsKey(name)) {
			rsDetectors.get(name).destroy();
			rsDetectors.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "RS detector " + name + " and its links deleted.");
		} else if (torchBlocks.containsKey(name)) {
			torchBlocks.get(name).destroy();
			torchBlocks.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Torch block " + name + " and its links deleted.");
		} else if (timerBlocks.containsKey(name)) {
			timerBlocks.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Timer block " + name + " and its links deleted.");
		} else if (spawners.containsKey(name)) {			
			spawners.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Spawner " + name + " and its links deleted.");
			if (file != null && file.exists()) {
				file.delete();
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Spawner file deleted.");
			}
		} else if (storages.containsKey(name)) {
			storages.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Storage block " + name + " and its links deleted.");
			if (file != null && file.exists()) {
				file.delete();
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Storage file deleted.");
			}
		} else if (musics.containsKey(name)) {
			musics.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Music Block " + name + " and its links deleted.");
		} else if (doors.containsKey(name)) {
			doors.remove(name);
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Door " + name + " and its links deleted.");
			if (file != null && file.exists()) {
				file.delete();
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Door file deleted.");
			}
		}
		
	}

	public void listBlocks(Player player) {
		if (detectors.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Detectors:");
			String names = "";
			for (String name : detectors.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (rsDetectors.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "RS Detectors:");
			String names = "";
			for (String name : rsDetectors.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (spawners.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Spawners:");
			String names = "";
			for (String name : spawners.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (storages.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Storage blocks:");
			String names = "";
			for (String name : storages.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (musics.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Music Blocks:");
			String names = "";
			for (String name : musics.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (doors.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Doors:");
			String names = "";
			for (String name : doors.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (torchBlocks.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Torch Blocks:");
			String names = "";
			for (String name : torchBlocks.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (timerBlocks.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Timer Blocks:");
			String names = "";
			for (String name : timerBlocks.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
		if (chestBlocks.size() > 0) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Chests:");
			String names = "";
			for (String name : chestBlocks.keySet()) {
				names += name + " ";
			}
			player.sendMessage("  " + names);
		}
	}

	public ConcurrentHashMap<String, Door> getDoors() {
		 return doors;
	}
	
	
	// Static Dungeon load & save
	// -----------------------
	public static void loadDungeons()
	{
		try {
	    	File[] listOfFiles = dungeonFolder.listFiles();
	    	//Bukkit.getConsoleSender().sendMessage("dfLen: " + Integer.toString(listOfFiles.length)); 
	    	for (int i = 0; i < listOfFiles.length; i++) 
	    	{
	    		if (listOfFiles[i].isDirectory()) 
	    		{
	    	        //Bukkit.getConsoleSender().sendMessage("Directory " + listOfFiles[i].getName());
	    	        File[] inner_list = listOfFiles[i].listFiles();
	    	        Bukkit.getConsoleSender().sendMessage("ifLen" + Integer.toString(i) + ": " + Integer.toString(inner_list.length)); 
	    	        for (int j = 0; j < inner_list.length; j++) 
	    	        {
	    	    		if (inner_list[j].isFile()) 
	    	    		{
	    	    			if (inner_list[j].getName().equals(listOfFiles[i].getName() + ".yml"))
	    	    			{
	    	    				//Bukkit.getConsoleSender().sendMessage("File " + inner_list[j].getName());
	    	    				FileConfiguration newDungeon = new YamlConfiguration();
	    	    				newDungeon.load(inner_list[j]);
	    	    				dungeonConfigs.put(listOfFiles[i].getName(), newDungeon); 
	    	    				dungeons.put(listOfFiles[i].getName(), new Dungeon(listOfFiles[i], newDungeon));
	    	    			}
	    	    		}
	    	    	}
	    		}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	public static void disableDungeons()
	{
		for (String name : dungeons.keySet())
		{
			Dungeon dungeon = dungeons.get(name);
			dungeon.setEnabled(false);
		}
	}
	
	
	
	// Static Dungeon load & save
	// --------------------------
	public static void saveDungeons()
	{
		for (String name : dungeons.keySet())
		{
			Dungeon dungeon = dungeons.get(name);
			File currentFolder = new File(dungeonFolder, name); 
			if (!currentFolder.exists())
			{
				try{
					currentFolder.mkdir();
				} catch(SecurityException se) { }
			}
			
			File dungeonFile = new File(currentFolder, name + ".yml");
			LegendCraft.plugin.copyYamlsToFile(LegendCraft.plugin.getResource(name + ".yml"), dungeonFile);
			 	
			FileConfiguration dungeonConfig = new YamlConfiguration();
			dungeonConfig.set("world", dungeon.getWorld().getName());
			double[] corners = dungeon.getCorners();
			List<Double> min = new ArrayList<Double>();
			List<Double> max = new ArrayList<Double>();
			min.add(corners[0]);
			min.add(corners[1]);
			min.add(corners[2]);
			max.add(corners[3]);
			max.add(corners[4]);
			max.add(corners[5]);
			dungeonConfig.set("min", min);
			dungeonConfig.set("max", max);
			
			dungeon.setConfig(currentFolder, dungeonConfig);
			
			try {
				dungeonConfig.save(dungeonFile);
			} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	
	// Save single dungeon
	public boolean saveDungeon(String name)
	{
		File currentFolder = new File(dungeonFolder, name); 
		if (!currentFolder.exists()) {
			try{
				currentFolder.mkdir();
			} catch(SecurityException se) { }
		}
		
		//see if we can save successfully.
		boolean canSave = tryDungeonSave(currentFolder, name + "-temp", true);
		if (canSave)
			tryDungeonSave(currentFolder, name);
		
		return true;
	}
	
	// Attempt to save to a file
	public boolean tryDungeonSave(File currentFolder, String name) {
		return tryDungeonSave(currentFolder, name, false);
	}
	public boolean tryDungeonSave(File currentFolder, String name, boolean deleteFile)
	{
		File dungeonFile = new File(currentFolder, name + ".yml");
		try {
			LegendCraft.plugin.copyYamlsToFile(LegendCraft.plugin.getResource(name + ".yml"), dungeonFile);
			 	
			FileConfiguration dungeonConfig = new YamlConfiguration();
			dungeonConfig.set("world", getWorld().getName());
			double[] corners = getCorners();
			List<Double> min = new ArrayList<Double>();
			List<Double> max = new ArrayList<Double>();
			min.add(corners[0]);
			min.add(corners[1]);
			min.add(corners[2]);
			max.add(corners[3]);
			max.add(corners[4]);
			max.add(corners[5]);
			dungeonConfig.set("min", min);
			dungeonConfig.set("max", max);
			
			setConfig(currentFolder, dungeonConfig);
		
			dungeonConfig.save(dungeonFile);
		} catch (Exception e) {
			e.printStackTrace();
			dungeonFile.delete();
			return false;
		}	
		if (deleteFile)
			dungeonFile.delete();
		return true;
	}
	
	
	
	public static File findDungeonDir(String dungeonName)
	{
		File[] listOfFiles = dungeonFolder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
    		if (listOfFiles[i].isDirectory() && listOfFiles[i].getName().equals(dungeonName)) 
    			return listOfFiles[i];
    	}		
		return null;
	}


	public void resize(Player player) {
		Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		if (sel instanceof CuboidSelection && sel.getWorld() == world) {
	        Vector min2 = sel.getNativeMinimumPoint();
	        Vector max2 = sel.getNativeMaximumPoint();
	        if (	min.getX() >= min2.getX() &&
		        	min.getY() >= min2.getY() &&
		        	min.getZ() >= min2.getZ() &&
		        	max.getX() <= max2.getX() &&
		        	max.getY() <= max2.getY() &&
		        	max.getZ() <= max2.getZ()) 
	        {
	        	min = min2;
	        	max = max2;
	        	player.sendMessage(ChatColor.LIGHT_PURPLE + "Dungeon Expanded.");	
	        } else {
		        player.sendMessage(ChatColor.RED + "Invalid Selection! New dungeon region must contain the old one.");
		    }
	    } else {
	        player.sendMessage(ChatColor.RED + "Invalid Selection! New dungeon region must contain the old one.");
	    }
	}


	public void tryEdit(Player player, String name, String key, String value) {
		if (nameUsed(name))
		{
			if (detectors.containsKey(name))
				detectors.get(name).edit(player, key, value);
			else if (doors.containsKey(name))
				doors.get(name).edit(player, key, value);
			else if (musics.containsKey(name))
				musics.get(name).edit(player, key, value);
			else if (rsDetectors.containsKey(name))
				rsDetectors.get(name).edit(player, key, value);
			else if (spawners.containsKey(name))
				spawners.get(name).edit(player, key, value);
			else if (storages.containsKey(name))
				storages.get(name).edit(player, key, value);
			else if (timerBlocks.containsKey(name))
				timerBlocks.get(name).edit(player, key, value);
			else if (torchBlocks.containsKey(name))
				torchBlocks.get(name).edit(player, key, value);
			else if (chestBlocks.containsKey(name))
				chestBlocks.get(name).edit(player, key, value);
		} else {
			//TODO: show generic properties
		}
	}


	public static void checkChests(Block b)
	{
		for (String s : Dungeon.dungeons.keySet())
		{
			for (String c : Dungeon.dungeons.get(s).chestBlocks.keySet()) {
				dungeons.get(s).chestBlocks.get(c).check(b);
			}
		}		
	}



	


	


	
}
