package me.plasmarob.legendcraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import me.plasmarob.legendcraft.blocks.MobTemplate;
import me.plasmarob.legendcraft.database.Database;
import me.plasmarob.legendcraft.database.DatabaseMethods;
import me.plasmarob.legendcraft.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;

// TODO: make long grass in dungeons respawn
// TODO: make pot block model, implement pots
// TODO: make heart container and heart container piece
// TODO: ice arrow
// TODO: add dungeon reset when no players present
public class LegendCraft extends JavaPlugin {
	
	// Primary objects 
	public static LegendCraft plugin;
	public static ThreadManager manager;
	public final MainListener listener = new MainListener(); // Player Listener 
	public final LegendCraftTabCompleter tabCompleter = new LegendCraftTabCompleter();
	// Effects Library
    private static EffectManager effectManager; 
    public static EffectManager getEffectManager() { return effectManager; }
	// YAML file config objects
	File mainConfigFile;
	public static FileConfiguration mainConfig;
	// WorldEdit dependency
	public static WorldEditPlugin worldEditPlugin = null;	
	// Globals (bare minimum)
	@Deprecated
	Location spawn;
	// Database
	private Database db;
	public Database getDatabase() { return db; }
	
	/**
	 * onEnable()
	 * * sets up objects, thread, effects manager, listener, and YAML
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable()
	{
		// Create files & DB
		firstRun();
		
		//----------------------
		// Primary object setups
		plugin = this;
		manager = new ThreadManager(); // Create the thread that updates every tick
		getServer().getScheduler().scheduleSyncRepeatingTask(this, manager, 0, 1);
		// Register the main listener
		Bukkit.getServer().getPluginManager().registerEvents(listener, this);
		// Create EffectManager for special effects library
		effectManager = new EffectManager(EffectLib.instance());
		// Create the YAML Config in memory
		mainConfig = new YamlConfiguration(); 
		//---------------
		// Database Setup
		this.db = new SQLite(this);
        this.db.load();
        DatabaseMethods.getInstance(this);	// init method singleton
        Dungeon.loadDungeons(); // 
        
		loadYamls();	// Load the config from the files
		getLogger().info("All dungeon ymls loaded!");  
		
		//-------------------
		// Bring in WorldEdit
		worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	    if(worldEditPlugin == null){
	        getLogger().severe("Error starting LegendCraft! WorldEdit isn't found."); 
	        //TODO: TEST & DELETEME Bukkit.getConsoleSender().sendMessage("Do not attempt to use LegendCraft in this state.");  
	        Bukkit.getPluginManager().disablePlugin(this);
	    }
	    //change once set in config file
	    spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
	    
	    //------------------------
	  	// Send off valid commands
	    List<String> commands = Arrays.asList("lca","lce","lc","legendcraft");
	    for (String cmd : commands) {
	    	this.getCommand(cmd).setExecutor(new LegendCraftCommandExecutor(this));
	    	this.getCommand(cmd).setTabCompleter(tabCompleter);
	    }
		
	    // Success
		getLogger().info("Loaded!");
	}
	
	/**
	 * onDisable()
	 * * puts it all to YAML
	 */
	@Override
	public void onDisable() {
		effectManager.dispose();	// Dump the effects manager
		Dungeon.disableDungeons();
		//Dungeon.saveDungeons();
		MobTemplate.saveMobs();
		saveYamls();
	}
	
	/**
	 * firstRun()
	 * Create files if they don't exist
	 */
	private void firstRun() {
		if(!getDataFolder().exists())
			getDataFolder().mkdir();	
		mainConfigFile = new File(getDataFolder(), "config.yml");
	    if(!mainConfigFile.exists())
	    	copyYamlsToFile(getResource("config.yml"), mainConfigFile);
	    
	    File dungeonFolder = new File(getDataFolder(), "dungeons");
	    if(!dungeonFolder.exists()) {
	    	try{ 
		    	dungeonFolder.mkdir(); 
		    	getLogger().info("dungeons folder created.");
		    } catch(Exception e) { e.printStackTrace(); }
	    }
	    Dungeon.setDungeonFolder(dungeonFolder);
	    
	    File mobsFolder = new File(getDataFolder(), "mobs");
	    if(!mobsFolder.exists()) {
	    	try{ 
	    		mobsFolder.mkdir(); 		    	
		    	getLogger().info("mobs folder created.");
		    } catch(Exception e) { e.printStackTrace(); }
	    }
	    MobTemplate.setMobFolder(mobsFolder);
	}
	
	//------------------------------------------------
    // YAML methods
	
	// Load YAMLs from file
	public void loadYamls() {
		//MUST load before dungeons, so mobs are in memory for spawners
		//TODO: they will be in the DB soon
		MobTemplate.loadMobs();
		//Load Dungeons, its blocks, and their data
		//*** Dungeon.loadDungeons();
	    try {	
	        mainConfig.load(mainConfigFile);
	    } catch (Exception e) {e.printStackTrace();}
	}

	// Save YAMLs to file
	public void saveYamls() {
	    try {
	        mainConfig.save(mainConfigFile);
	    } catch (IOException e) {e.printStackTrace();}
	}
		
	// Copy to file
	public void copyYamlsToFile(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        if (in != null) {
	        	int len;
	        	byte[] buf = new byte[1024];
		        while((len=in.read(buf))>0){
		            out.write(buf,0,len);
		        }
		        in.close();
	        }
	        out.close();
	    } catch (Exception e) {e.printStackTrace();}
	}
}

