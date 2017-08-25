package me.plasmarob.legendcraft;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.blocks.MobTemplate;
import me.plasmarob.legendcraft.blocks.Tune;
import me.plasmarob.legendcraft.database.Database;
import me.plasmarob.legendcraft.database.DatabaseMethods;
import me.plasmarob.legendcraft.util.Tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

@SuppressWarnings("unused")
public class LegendCraftCommandExecutor implements CommandExecutor {

	//TODO: show dungeon XYZ range

	//TODO: Delete Mob
	//TODO: remove/reset mobs from spawner
		
	//TODO: show location of block{s} via effect
	
	//TODO: spawner as a sender
	//TODO: teleporter
	//TODO: store player hearts
	
	//TODO: animation and monster block designs
	
	//TODO: edit all blocks
	
	LegendCraft plugin;
	public static ConcurrentHashMap<String, MobTemplate> mobs = MobTemplate.mobs;
	public static ConcurrentHashMap<String, Dungeon> dungeons = Dungeon.dungeons;
	public static ConcurrentHashMap<Player, String> selectedDungeons = Dungeon.selectedDungeons;
	String red = "" + ChatColor.RED;
	String purp = "" + ChatColor.LIGHT_PURPLE;
	Player player;
	
	public LegendCraftCommandExecutor(LegendCraft plugin)  {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		
		//alias /lca for /lc add 
		if (commandLabel.equals("lca")) {
			String[] argsOld = args.clone();
			args = new String[argsOld.length+1];
			args[0] = "add";
			for (int i=0; i < argsOld.length; i++) {
				args[i+1] = argsOld[i];
			}
		}
		
		//alias /lce to /lc edit 
		if (commandLabel.equals("lce")) {
			String[] argsOld = args.clone();
			args = new String[argsOld.length+1];
			args[0] = "edit";
			for (int i=0; i < argsOld.length; i++) {
				args[i+1] = argsOld[i];
			}
		}
		
		int last = args.length - 1;
		
		if (sender instanceof Player) {
           player = (Player) sender;
        } else {
           sender.sendMessage("You must be a player!");
           return false;
        }
		
		if (!player.hasPermission("legendcraft.commands") && !player.isOp()) {	
			say(red + "You do not have permission to perform that command.");
			return false;
		}
		
		//List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		//if (!strings.contains(player.getWorld().getName()))
		//Tools.say(DatabaseMethods.containsWorld(player.getWorld().getUID()));
		//Tools.say(plugin.getDatabase().containsWorld(player.getWorld().getUID()));
		if (!DatabaseMethods.containsWorld(player.getWorld().getUID()))
		{
			if ( !(last >= 0 && (args[0].toLowerCase().equals("addworld") || args[0].toLowerCase().equals("removeworld"))))
			{
				say(red + "You cannot perform that command in this world.");
				say(red + "try using '/lc addworld' first.");
				return false;
			}
		}
		
		
		/**
		 * HELP
		 * - Lists available commands
		 * --- /lc list
		 */
		if (args == null || args.length == 0 || (args.length >= 1 && args[0].toLowerCase().equals("help")))
		{
			int page = 1;
			if (args.length >= 2)
			{
				try {
					page = Integer.parseInt(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				if (page > 4)
					page = 4;
			}
			
			String title = ChatColor.GREEN + "---------";
			title += ChatColor.RESET + " LegendCraft Help ("+Integer.toString(page)+"/4) ";
			title += ChatColor.GREEN + "------------------------------------------";
			title = title.substring(0,54);
			say(title);
			
			String c = ChatColor.DARK_GREEN + "/lc ";
			String r = ChatColor.RESET + "";
			
			String[] helptext = new String[36];
			helptext[0]=ChatColor.GRAY+"Use /lc help [n] to get page n of help.";
			helptext[1]=c+"add Chest <name> : "+r+"New chest";
			helptext[2]=c+"add Detector <name> : "+r+"New detector";
			helptext[3]=c+"add Door <name> : "+r+"New door";
			helptext[4]=c+"add Music <name> : "+r+"Music block from last played tune";
			helptext[5]=c+"add RSDetector <name> : "+r+"New Redstone Detector";
			helptext[6]=c+"add Spawner <name> : "+r+"Create new spawner";
			helptext[7]=c+"add Storage <name> : "+r+"Create new storage block";
			helptext[8]=c+"add Timer <name> <delay> : "+r+"New Timer";
			helptext[9]=c+"add Torch <name> <TorchBlock> : "+r+"New Torch to TorchBlock";
			helptext[10]=c+"add TorchBlock <name> : "+r+"New TorchBlock";
			helptext[11]=c+"create <dungeon> : "+r+"Create new dungeon";
			helptext[12]=c+"delete <block> : "+r+"Deletes block";
			helptext[13]=c+"deleteDungeon <dungeon> :"+r+"Deletes dungeon";
			helptext[14]=c+"disable [dungeon] : "+r+"Disable dungeon";
			helptext[15]=c+"edit <block> <key> <value> : "+r+"Edit a block";
			helptext[16]=c+"enable [dungeon] : "+r+"Enable dungeon";
			helptext[17]=c+"expand: "+r+"expand dungeon to WorldEdit selection";
			helptext[18]=c+"insertMob <spawner> <mob> [count] : "+r+"Put in spawner";
			helptext[19]=c+"link <from> <to> [TRIGGER|set|reset|on|off] : "+r+"Link blocks";
			helptext[20]=c+"list: "+r+"List existing dungeons";
			helptext[21]=c+"listMobs: "+r+"List available mobs";
			helptext[22]=c+"m <tune> : "+r+"see playSound";
			helptext[23]=c+"mobInfo: "+r+"Show info about mob";
			helptext[24]=c+"playsound <tune> : "+r+"plays a written tune.";
			helptext[25]=c+"ps <tune> : "+r+"see playSound";
			helptext[26]=c+"saveMob <name> : "+r+"Save closest mob as <name>";
			helptext[27]=c+"save : "+r+"Save current dungeon";
			helptext[28]=c+"save: "+r+"Save the current dungeon";
			helptext[29]=c+"select <dungeon> : "+r+"Select dungeon to edit";
			helptext[30]=c+"setWorld: "+r+"adds/removes current world to/from plugin";
			helptext[31]=c+"show [block] : "+r+"Details dungeon or block";
			helptext[32]=c+"spawn: "+r+"Spawn mob";
			helptext[33]=c+"unlink <block>: "+r+"remove links to and from a block";
			helptext[34]=c+"unlink <from> <to>: "+r+"remove a specific link";
			helptext[35]="";
						
			page = (page < 1 || page > 4) ? 1 : page;
			int firsthelp = 9*(page-1);
			for (int i = 0; i < 9; i++) {
				say(helptext[firsthelp+i]);
			}
			return true;
		}
		
		
		if (last >= 2 && args[0].toLowerCase().equals("add")) {
		if (!selectedDungeons.containsKey(player))
			say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
		else {
			String addType = args[1].toLowerCase();
			String dungeonStr = selectedDungeons.get(player);
			/**
			 * Add Chest
			 * - attempts to create a new chest
			 */
			if (addType.equals("chest") || addType.equals("che")) {
				if (!dungeons.get(dungeonStr).tryAddChest(player, args[2]))
					say(red + "Chest creation failed.");	
			/**
			 * Add Detector
			 * - attempts to create a trigger via looked-at coal_ore
			 */
			} else if (addType.equals("detector") || addType.equals("det")) {
				if (!dungeons.get(dungeonStr).tryAddDetector(player, args[2]))
					say(red + "Detector creation failed.");
			/**
			 * Add Door
			 * - attempts to create a new door, 
			 * -	a selection that disappears when hit by currently held item 
			 */
			} else if (addType.equals("door") || addType.equals("doo") || addType.equals("dor")) {
				if (!dungeons.get(dungeonStr).tryAddDoor(player, args[2]))
					say(red + "Door creation failed.");
			/**
			 * Add Music
			 * - attempts to create a music block via looked-at diamond_ore
			 */
			} else if (addType.equals("music") || addType.equals("mus")) {
				if (!dungeons.get(dungeonStr).tryAddMusic(player, args[2]))
					say(red + "Music block creation failed.");
			/**
			 * Add Redstone Detector
			 * - attempts to create a RS detector via looked-at redstone ore
			 */
			} else if (addType.equals("rsdetector") || addType.equals("rsdet") || addType.equals("rs")) {
				if (!dungeons.get(dungeonStr).tryAddRSDetector(player, args[2]))
					say(red + "RS Detector creation failed.");
			/**
			 * Add Spawner
			 * - attempts to create a spawner via looked-at gold_ore
			 */
			} else if (addType.equals("spawner") || addType.equals("spawn") || addType.equals("spa")) {
				if (!dungeons.get(dungeonStr).tryAddSpawner(player, args[2]))
					say(red + "Spawner creation failed.");
			/**
			 * Add Storage
			 * - attempts to create a storage block via looked-at emerald_ore
			 */
			} else if (addType.equals("storageblock") || addType.equals("storage") || addType.equals("sto")) {
				if (!dungeons.get(dungeonStr).tryAddStorageBlock(player, args[2]))
					say(red + "Storage block creation failed.");
			/**
			 * Add Timer
			 * - attempts to create a timer via looked-at IDC
			 */
				//TODO: catch the lack of args[3]
			} else if (addType.equals("timer") || addType.equals("time") || addType.equals("tim")) {
				if (args.length < 4)
					say(red + "Invalid arguments. Usage: /lc add timer <name> <delay>");
				else if (!dungeons.get(dungeonStr).tryAddTimer(player, args[2], args[3]))
					say(red + "Timer creation failed.");
			/**
			 * Add Torch
			 * - attempts to create a torch and add it to a torchblock
			 */
			} else if (addType.equals("torch") || addType.equals("tor")) {
				if (!dungeons.get(dungeonStr).tryAddTorch(player, args[2]))
					say(red + "Torch addition failed.");
			/**
			 * Add TorchBlock
			 * - attempts to create a torch via looked-at iron_ore
			 */
			} else if (addType.equals("torchblock") || addType.equals("torblk")) {
				if (!dungeons.get(dungeonStr).tryAddTorchBlock(player, args[2]))
					say(red + "Torch block creation failed.");
			}
		}} else if (args[0] != null && args[0].toLowerCase().equals("add")) {
			say(red+ "Invalid arguments. Please provide a name.");
		}

	
		/*
		if(args[0].toLowerCase().equals("testsave"))
		{
			HashMap<Integer,Selection> map = new HashMap<Integer,Selection>();
			Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
			map.put(0, sel);
			if (sel instanceof CuboidSelection) {
				Tools.saveObject(map, new File(plugin.getDataFolder(), "temp.dat"));
			}
		}
		if(args[0].toLowerCase().equals("testload"))
		{
			HashMap<Integer,Selection> map = (HashMap<Integer,Selection>)Tools.loadObject(new File(plugin.getDataFolder(), "temp.dat"));
			if (map != null) {
				Selection sel = map.get(0);
				if (sel instanceof CuboidSelection) {
			        sel.getMaximumPoint().getBlock().setType(Material.PACKED_ICE);
			        sel.getMinimumPoint().getBlock().setType(Material.PACKED_ICE);
				}
			}
		}
		*/    
		        		
		

		
		/**
		 * Create Dungeon
		 * - Creates and selects* a new blank dungeon from selection.
		 * --- /lc create <dungeon>
		 */
		if(last >= 1 && (args[0].toLowerCase().equals("create") || args[0].toLowerCase().equals("cd")))
		{
			if (!dungeons.containsKey(args[1])) {
				Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
				if (sel instanceof CuboidSelection) {
			        Vector min = sel.getNativeMinimumPoint();
			        Vector max = sel.getNativeMaximumPoint();
			        dungeons.put(args[1], new Dungeon(sel.getWorld(), min, max));
			        say("Dungeon created and selected!");
			        selectedDungeons.put(player, args[1]);
			    } else {
			        say(red + "Invalid Selection!");
			    }
			}
			else
				say("Dungeon with this name already exists.");
		}
		else if (args[0].toLowerCase().equals("create"))
			say(red + "Usage: /lc create <dungeon>");
		
		
		/**
		 * Delete Block
		 * - deletes a trigger/spawner/storage from a dungeon
		 */
		if (last >= 1 && args[0].toLowerCase().equals("delete"))
		{
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				dungeons.get(selectedDungeons.get(player)).tryDelete(player, args[1], 
						Tools.findFile(selectedDungeons.get(player), args[1]));
			}
		}
		else if (args[0].toLowerCase().equals("delblock"))
			say(red + "Usage: /lc delete <name>");
		
		/**
		 * Delete Dungeon
		 * - Deletes named dungeon.
		 */
		if (last >= 1 && args[0].toLowerCase().equals("deletedungeon"))
		{	
			if (dungeons.containsKey(args[1]))
			{
				Dungeon deletee = dungeons.get(args[1]);
				
				if (deletee.isEnabled()) {
					say(red + "Can't delete enabled dungeon.");
					return false;
				}
				
				if (selectedDungeons.containsKey(player) && selectedDungeons.get(player).equals(args[1])) {
					selectedDungeons.remove(player);
				}
				
				boolean attempt = deletee.tryDeleteDungeon(player, args[1], Dungeon.findDungeonDir(args[1]));
				if (attempt) {
					dungeons.remove(args[1]);
				}
				else
					say("Error deleting dungeon. If this persists, remove yml file while server is not running.");
			}
			else
				say(red + "Dungeon with this name not found.");
		}
		else if (args[0].toLowerCase().equals("deletedungeon"))
			say(red + "Usage: /lc deleteDungeon <dungeon>");
		
		/**
		 * Dungeon Enable&Disable
		 * - Changes whether dungeon is enabled
		 * - sets or unsets code Blocks, resetting them.
		 * --- /lc enable <dungeon>
		 * --- /lc disable <dungeon>
		 */
		if (last >= 1 && args[0].toLowerCase().equals("enable")) {
			if (dungeons.containsKey(args[1])) {
				dungeons.get(args[1]).setEnabled(true);
				say(purp + "Dungeon " + args[1] + " enabled.");
			}
			else
				say(red + "Dungeon with this name not found.");
		} else if (last >= 1 && args[0].toLowerCase().equals("disable")) {
			if (dungeons.containsKey(args[1])) {
				dungeons.get(args[1]).setEnabled(false);
				say(purp + "Dungeon " + args[1] + " disabled.");
			}
			else
				say(red + "Dungeon with this name not found.");
		}
		else if (args[0].toLowerCase().equals("enable")) {
			if (selectedDungeons.containsKey(player)) {
				dungeons.get(selectedDungeons.get(player)).setEnabled(true);
				say(purp + "Dungeon " + selectedDungeons.get(player) + " enabled.");
			} else {
				say(red + "No dungeon selected.");
				say(red + "Usage: /lc enable [dungeon]");
			}
		}
		else if (args[0].toLowerCase().equals("disable")) {
			if (selectedDungeons.containsKey(player)) {
				dungeons.get(selectedDungeons.get(player)).setEnabled(false);
				say(purp + "Dungeon " + selectedDungeons.get(player) + " disabled.");
			} else {
				say(red + "No dungeon selected.");
				say(red + "Usage: /lc disable [dungeon]");
			}
		}
		
		/**
		 * Edit (universal)
		 * - attempts to edit a block
		 * --- /lc edit <block> <key> <value>
		 */
		if (last >=3 && args[0].toLowerCase().equals("edit")) {
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				String dungeonStr = selectedDungeons.get(player);
				dungeons.get(dungeonStr).tryEdit(player, args[1], args[2], args[3]);
			}
		}
		else if (args[0].toLowerCase().equals("edit"))
			say(red + "Usage: /lc edit <block> <key> <value>\nTo see valid options, use /lc show <block>");
		
		
		/**
		 * Expand Dungeon
		 * - Expands dungeon selected.
		 * --- /lc expand
		 */
		if(args[0].toLowerCase().equals("expand"))
		{
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				dungeons.get(selectedDungeons.get(player)).resize(player);
			}
		}


		/**
		 * Insert Mob 
		 * - puts saved mob into a spawner
		 * --- /lc insertmob <spawner> <mob> [count]
		 */
		//TODO: add a way to remove them
		if ( last >= 2 && args[0].toLowerCase().equals("insertmob")) {
			int count = 1;
			if (last >= 3) {
				try { 
			        count = Integer.parseInt(args[3]); 
			    } catch(NumberFormatException e) { 
			    	say(red + "invalid number \"" + args[3] + "\".");
			        return false; 
			    } catch(NullPointerException e) {
			    	say(red + "invalid number \"" + args[3] + "\".");
			        return false;
			    }
			}
			if (count > 10 | count < 1) {
				say(red + "count (" + count + ") must be between 1 and 10");
		        return false;
			}
			
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else if (!mobs.containsKey(args[2]))
				say(red + "Mob with this name not found."); 
			else {
				String dungeonStr = selectedDungeons.get(player);
				if (!dungeons.get(dungeonStr).tryInsertMob(player, args[1], args[2], mobs.get(args[2]), count))
					say(red + "Mob insertion failed.");
			}
		}
		else if (args[0].toLowerCase().equals("insertmob"))
			say(red + "Usage: /lc insertMob <spawner> <mob> [count]");
		
		
		/**
		 * Link Blocks
		 * - attempts to link blocks, where A triggers B
		 * --- /lc link <block_from> <block_to> [TRIGGER|set|reset|on|off]
		 */
		if (last >= 2 && args[0].toLowerCase().equals("link"))
		{
			String type = "trigger";
			if (last >= 3 && 
					(args[3].toLowerCase().equals("set") || 
					 args[3].toLowerCase().equals("reset") ||
					 args[3].toLowerCase().equals("on") || 
					 args[3].toLowerCase().equals("off")
					))
				type = args[3].toLowerCase();
	
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				String dungeonStr = selectedDungeons.get(player);
				if (!dungeons.get(dungeonStr).tryLink(player, args[1], args[2], type))
					say(red + "Linking failed.");
			}
		}
		else if (args[0].toLowerCase().equals("link"))
			say(red + "Usage: /lc link <sender> <receiver> [trigger|set|reset|on|off]");
		
		
		/**
		 * List
		 * - Lists available dungeons for selection
		 * --- /lc list
		 */
		if(last >= 0 && args[0].toLowerCase().equals("list")) {
			say("Legendcraft Dungeons:");
			for (String s : dungeons.keySet())
				say(" " + s);
		}
		
		
		/**
		 * List Mobs
		 * - Lists available mobs
		 * --- /lc listmobs
		 */
		if(last >= 0 && args[0].toLowerCase().equals("listmobs")) {
			say(purp + "Available mobs:");
			String mList = "";
			for (String s : mobs.keySet())
				mList = mList + " " + s;
			say(purp + mList);
		}
		
		if(last >= 0 && args[0].toLowerCase().equals("compass")) 
			Tools.playerCompassFace(player);
		
		/**
		 * Mob Info
		 * - Lists all* data about nearest mob
		 * * CustomName will be added later
		 * --- /lc mobinfo
		 */
		//TODO: Add CustomName
		//TODO: Add some color to the mobinfo command, and test string converts
		if (last >= 0 && args[0].toLowerCase().equals("mobinfo"))
		{
			LivingEntity closestEntity = Tools.getClosestMob(player);
			if (closestEntity != null)
			{
				say(purp + closestEntity.getType().toString());
				//TODO: custom name =   say(closestEntity.getName());
				//  ( this would need to be read&written in the YAML )
				say("Health: " + Double.toString(closestEntity.getHealth()));
				//Potions:
				Collection<PotionEffect> potioneffects = closestEntity.getActivePotionEffects();
				say(purp + "PotionEffects:"); 
				for (PotionEffect pe : potioneffects){
					say(pe.getType().getName() + 
							": t=" + Integer.toString(pe.getDuration()) + 
							", lvl=" + Integer.toString(pe.getAmplifier()+1));
				}
				//Armor+Enchants:
				say(purp + "Armor:"); 
				EntityEquipment armor = closestEntity.getEquipment();
				ItemStack[] armorList = armor.getArmorContents();
				for (ItemStack item : armorList) {
					if (item.getType() != Material.AIR) {
						say(purp + " " + item.getType().toString().toLowerCase() + ":");
						Map<Enchantment, Integer> helmEnch = item.getEnchantments(); 
						for (Enchantment e : helmEnch.keySet())
							say(purp + 
									"   " + Tools.standardEnchantName(e.getName()) + ": " + helmEnch.get(e).toString()); 
					}
				}
			}
			else
				say(red + "No nearby mob found.");
		}
	
		/**
		 * Play sound
		 * - play note block sound
		 * --- /lc ps <string>
		 */
		if (last >= 1 && (args[0].toLowerCase().equals("ps") || args[0].toLowerCase().equals("m") || 
							args[0].toLowerCase().equals("playsound")))
		{
			new Tune(player, args[1]);
		}
		else if (args[0].toLowerCase().equals("ps"))
			say(red + "Usage: /lc ps <tune>");
		

		/**
		 * Save
		 * - Saves the dungeon to a file
		 * --- /lc save
		 */
		//TODO: after CustomName is added, add <Custom> was saved as <Name>.
		if (args[0].toLowerCase().equals("save"))
		{
			plugin.saveYamls();
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				String dungeonStr = selectedDungeons.get(player);
				if (!dungeons.get(dungeonStr).saveDungeon(dungeonStr))
					say(red + "Saving Dungeon " + dungeonStr + " failed.");
				else
					say(ChatColor.GREEN + "Saved Dungeon " + dungeonStr + " !");
			}
		} //TODO - allow naming a dungeon to save
		else if (args[0].toLowerCase().equals("save"))
			say(red + "Usage: /lc save");
		
		
		/**
		 * Save Mob
		 * - Saves the nearest mob to a file, for spawner use
		 * --- /lc savemob <name>
		 */
		//TODO: after CustomName is added, add <Custom> was saved as <Name>.
		if (last >= 1 && args[0].toLowerCase().equals("savemob"))
		{
			if (!mobs.containsKey(args[1])) {
				LivingEntity closestEntity = Tools.getClosestMob(player);
				if (closestEntity != null) {
					mobs.put(args[1], new MobTemplate(closestEntity,args[1]));
					say(purp + "Saved " + closestEntity.getType().toString().toLowerCase() + " " + args[1] + ".");
				}
				else
					say(red + "No mob found.");		
			}else{
		        say(red + "Mob with this name already exists.");
		    }
		}
		else if (args[0].toLowerCase().equals("savemob"))
			say(red + "Usage: /lc savemob <name>");
		
		
		/**
		 * Scale
		 * - scale a selection
		 * --- /lc scale <factor>
		 */
		if(last >= 1 && args[0].toLowerCase().equals("scale")) {
			Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
			if (sel instanceof CuboidSelection) {
		        Vector min = sel.getNativeMinimumPoint();
		        Vector max = sel.getNativeMaximumPoint();
		        int minX = (int)min.getX();
		        int minY = (int)min.getY();
		        int minZ = (int)min.getZ();
		        int maxX = (int)max.getX();
		        int maxY = (int)max.getY();
		        int maxZ = (int)max.getZ();
		        int limit1 = 0;
		        Block anchor = player.getLocation().getBlock();	//dummy initialization
		        int scale = 0;
		        try {
		        	scale = Integer.parseInt(args[1]);
				} catch (Exception e) {
					say(red + "Invalid scale!");
					return false;
				}
		        
		        if (scale < 2)
		        {
		        	say(red + "Invalid scale!");
					return false;
		        }
		        
		        if (scale*(maxX-minX+1) > 750)
		        {
		        	say(red + "X scale must be less than 750.");
					return false;
		        }
		        if (scale*(maxY-minY+1) > 750)
		        {
		        	say(red + "Y scale must be less than 750.");
					return false;
		        }
		        if (scale*(maxZ-minZ+1) > 750)
		        {
		        	say(red + "Z scale must be less than 750.");
					return false;
		        }
		        
		        outerBreak:
		        for (int x = minX; x <= maxX; x++) {
	        	for (int y = minY; y <= maxY; y++) {
        		for (int z = minZ; z <= maxZ; z++) {
		        	if (player.getWorld().getBlockAt(x, y, z).getType() == Material.BEDROCK) {
		        		limit1++;
		        		if (limit1 > 1)
	        				break outerBreak;
		        		anchor = player.getWorld().getBlockAt(x, y, z);
		        		//anchor.setType(Material.GLASS);
		        	}
        		}
		        }	
		        }
		        
		        if (limit1 == 1)
		        {
		        	Tools.scale(min,max,anchor,scale);
		        }
		        else
		        {
		        	say(red + "Have exactly 1 bedrock as an anchor!");
		        }
		        
		    } else {
		        say(red + "Invalid Selection!");
		    }
		}
		else if (args[0].toLowerCase().equals("scale"))
			say(red + "Usage: /lc scale <factor>");
		
		
		
		/**
		 * Select
		 * - selects available dungeon
		 * --- /lc select <dungeon>
		 */
		if(last >= 1 && args[0].toLowerCase().equals("select")) {
			
			if (!dungeons.containsKey(args[1])) {
				say(red + "Dungeon with this name not found.");
				say(purp + "Existing Dungeons:");
				String dList = "";
				for (String s : dungeons.keySet())
					dList = dList + "  " + s;
				say(purp + dList);
			}
			else {
				selectedDungeons.put(player, args[1]);
				say(purp + args[1] + " selected.");
			}
		}
		else if (args[0].toLowerCase().equals("select"))
			say(red + "Usage: /lc select <dungeon>");
		
		
		/**
		 * Add & Remove World
		 * - sets active worlds
		 * --- /lc addworld
		 * --- /lc removeworld
		 */
		if(last >= 0 && args[0].toLowerCase().equals("addworld")) {
			
			String worldName;
			UUID worldUUID;
			World world;
			if (last >= 1) {
				if (Bukkit.getWorld(args[1]) != null) {
					world = Bukkit.getWorld(args[1]);
				} else {
					say("world not found with this name.");
					return false;
				}
			} else {
				world = player.getWorld();
			}	
			
			DatabaseMethods.addWorld(world.getUID(), world.getName());
			say("plugin enabled in world " + world.getName() + ".");
			return true;
			
			/*
			List<String> wStrings = LegendCraft.mainConfig.getStringList("worlds");
			String wname;
			if (last >= 1) {
				if (Bukkit.getWorld(args[1]) != null) {
					wname = args[1];
				} else {
					say("world not found with this name.");
					return false;
				}
			} else {
				wname = player.getWorld().getName();
			}	
			
			if (wStrings.contains(wname)) {
				wStrings.remove(wname);
				LegendCraft.mainConfig.set("worlds", wStrings);
				say("plugin disabled in world " + wname + ".");
			} else {
				wStrings.add(wname);
				LegendCraft.mainConfig.set("worlds", wStrings);
				say("plugin enabled in world " + wname + ".");
			}
			*/
		}
		if(last >= 0 && args[0].toLowerCase().equals("removeworld")) {
			String worldName;
			UUID worldUUID;
			World world;
			if (last >= 1) {
				if (Bukkit.getWorld(args[1]) != null) {
					world = Bukkit.getWorld(args[1]);
				} else {
					say("world not found with this name.");
					return false;
				}
			} else {
				world = player.getWorld();
			}	
			
			DatabaseMethods.removeWorld(world.getUID());
			say("plugin disabled in world " + world.getName() + ".");
			return true;
		}
		
		/**
		 * Show
		 * - shows information about a block
		 * --- /lc show <name>
		 */
		if(args[0].toLowerCase().equals("show") || args[0].toLowerCase().equals("info")) {
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				if (last >= 1) {
					String dungeonStr = selectedDungeons.get(player);
					dungeons.get(dungeonStr).show(player, args[1]);
				}
				else {
					dungeons.get(selectedDungeons.get(player)).show(player);
					say(ChatColor.GRAY + "/lc show <block> for a specific block.");
				}
			}
		}
		
		/**
		 * Spawn Mob
		 * - Spawns a mob from a file
		 * --- /lc spawn <mob>
		 */
		//TODO: after CustomName is added, add <Type> named <Custom> spawned.
		if(last >= 1 && args[0].toLowerCase().equals("spawn"))
		{
			if (mobs.containsKey(args[1])) {
				mobs.get(args[1]).spawn(player.getLocation());	
				say(purp + "Spawned " + args[1] + ".");
			}else{
		        say(red + "There isn't a mob by this name.");
		    }
		}
		else if (args[0].toLowerCase().equals("spawn"))
			say(red + "Usage: /lc spawn <mob>");
		
	

		/**
		 * Unlink Block
		 * - removes links from a block
		 */
		if (last >= 1 && args[0].toLowerCase().equals("unlink"))
		{
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				if (last >= 2)
					dungeons.get(selectedDungeons.get(player)).unlink(player, args[1], args[2]);
				else
					dungeons.get(selectedDungeons.get(player)).unlink(player, args[1]);
			}
		}
		else if (args[0].toLowerCase().equals("unlink"))
		{
			say(red + "Usage: /lc unlink <name>");
			say(red + "Usage: /lc unlink <from> <to>");
		}
		
		
		//-----------------------------------------------------------
		//-----------------------------------------------------------
		
		
		/**
		 * Heart count changer
		 * - Changes the player's health
		 * --- /lc myhealth <heart #>
		 */
		if(last >= 1 && args[0].toLowerCase().equals("myhealth"))
		{
			double health = Integer.parseInt(args[1]) * 2;
			if (health < 2 || health > 100)
				say(red + "Health must be between 1 and 50.");
			else {
				//player.setMaxHealth(health);
				player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
				player.setHealth(health);
			}
		}
		else if (args[0].toLowerCase().equals("myhealth"))
			say(red + "Usage: /lc myhealth <heart #>");
		
		
		return false;
	}
	
	

	//method abbr.
	void say(String s) {
		player.sendMessage(s);
	}
}
