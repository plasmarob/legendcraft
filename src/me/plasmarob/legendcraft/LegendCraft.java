package me.plasmarob.legendcraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.plasmarob.legendcraft.blocks.MobTemplate;
import me.plasmarob.legendcraft.util.Tools;

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
	public final MainListener listener = new MainListener(this); // Player Listener 
	// Effects Library
    private static EffectManager effectManager; 
	// YAML file config objects
	File mainConfigFile;
	public static FileConfiguration mainConfig;
	// WorldEdit 
	public static WorldEditPlugin worldEditPlugin = null;	
	// Globals (bare minimum)
	Location spawn;
	
	/**
	 * onEnable()
	 * * Loads the Plugin
	 * * sets up objects, thread, effects manager, listener, and YAML
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable()
	{
		//----------------------
		// Primary object setups
		plugin = this;
		
		// Create the thread that updates every tick
		manager = new ThreadManager();
		getServer().getScheduler().scheduleSyncRepeatingTask(this, manager, 0, 1);	
		
		// Register the main listener
		Bukkit.getServer().getPluginManager().registerEvents(listener, this);
				
		// Create EffectManager for special effects library
		EffectLib lib = EffectLib.instance();
		effectManager = new EffectManager(lib);
				
		//----------------------
		// Setup the YAML 
		
		// Create files in memory
		try {
	        firstRun();	// Create files on disk if necessary
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		// Create the YAML Config in memory
		mainConfig = new YamlConfiguration();
		
		
		//create dungeon directory if needed.
	    Dungeon.dungeonFolder = new File(getDataFolder(), "dungeons");
	    
	    Tools.createMainFolder(Dungeon.dungeonFolder, "dungeon dir created");
	    //create mobs directory if needed.
	    MobTemplate.mobsFolder = new File(getDataFolder(), "mobs");
	    Tools.createMainFolder(MobTemplate.mobsFolder, "mobs dir created");
		
		loadYamls();	// Load the config from the files
		Bukkit.getConsoleSender().sendMessage("All dungeon ymls loaded!");  
		
		//----------------------
		// Bring in WorldEdit
		worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	    if(worldEditPlugin == null){
	        Bukkit.getConsoleSender().sendMessage("Error starting LegendCraft! WorldEdit is null."); 
	        Bukkit.getConsoleSender().sendMessage("Do not attempt to use LegendCraft in this state.");  
	    }
	    //change once set in config file
	    spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
	    
	    //----------------------
	  	// Send off valid commands
		this.getCommand("lc").setExecutor(new LegendCraftCommandExecutor(this));
		this.getCommand("legendcraft").setExecutor(new LegendCraftCommandExecutor(this));
	
		Bukkit.getConsoleSender().sendMessage("LegendCraft Loaded!");
	}
	
	/**
	 * onDisable()
	 * * Unloads the Plugin
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

	// Save YAMLs to file
	public void saveYamls() {
	    try {
	        mainConfig.save(mainConfigFile);
	    } catch (IOException e) {e.printStackTrace();}
	}
	
	// Load YAMLs from file
	public void loadYamls() {
		//MUST load before dungeons, so mobs are in memory for spawners
		MobTemplate.loadMobs();
		//Load Dungeons, its blocks, and their data
		Dungeon.loadDungeons();
	    try {	
	        mainConfig.load(mainConfigFile);
	    } catch (Exception e) {e.printStackTrace();}
	}
	
	// Copy to file
	public void copyYamlsToFile(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        if (in != null)
		        while((len=in.read(buf))>0){
		            out.write(buf,0,len);
		        }
	        out.close();
	        if (in != null)
	        	in.close();
	    } catch (Exception e) {e.printStackTrace();}
	}
	
	/**
	 * firstRun()
	 * Create the files if they don't exist
	 */
	private void firstRun() throws Exception {
		mainConfigFile = new File(getDataFolder(), "config.yml");
	    if(!mainConfigFile.exists()){
	    	mainConfigFile.getParentFile().mkdirs();
	    	copyYamlsToFile(getResource("config.yml"), mainConfigFile);
	    }
	}

	public static EffectManager getEffectManager() {
		return effectManager;
	}
}

