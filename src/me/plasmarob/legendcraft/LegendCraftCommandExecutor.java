package me.plasmarob.legendcraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.blocks.Link;
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
import org.bukkit.block.BlockState;
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
	private static ConcurrentHashMap<String, MobTemplate> mobs = MobTemplate.mobs;
	private static ConcurrentHashMap<String, Dungeon> dungeons = Dungeon.getDungeons();
	private static ConcurrentHashMap<Player, String> selectedDungeons = Dungeon.getSelectedDungeons();
	String red = "" + ChatColor.RED;
	String purp = "" + ChatColor.LIGHT_PURPLE;
	String b = "" + ChatColor.DARK_BLUE;
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
		
		int last_index = args.length - 1;
		
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
		if (!DatabaseMethods.containsWorld(player.getWorld().getUID())) {
			if ( !(last_index >= 0 && (args[0].toLowerCase().equals("addworld") || args[0].toLowerCase().equals("removeworld")))) {
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
		if (args == null || args.length == 0 || (args.length >= 1 && args[0].toLowerCase().equals("help"))) {
			int page = 1;
			if (args.length >= 2) {
				try { page = Integer.parseInt(args[1]); } catch (Exception e) { page = 1; }
				if (page > 4) page = 4;
			}
			
			String title = ChatColor.GREEN + "---------";
			title += ChatColor.RESET + " LegendCraft Help ("+Integer.toString(page)+"/4) ";
			title += ChatColor.GREEN + "------------------------------------------";
			title = title.substring(0,54);
			say(title);
			
			String c = ChatColor.DARK_GREEN + "/lc ";
			String r = ChatColor.RESET + "";
			
			List<String> ht = new ArrayList<String>();

			ht.add(ChatColor.GRAY+"Use /lc ? to get a tutorial.");
			ht.add(c+"add Chest <name> : "+r+"New chest");
			ht.add(c+"add Detector <name> : "+r+"New detector");
			ht.add(c+"add Door <name> : "+r+"New door");
			ht.add(c+"add Music <name> : "+r+"Music block from last played tune");
			ht.add(c+"add RSDetector <name> : "+r+"New Redstone Detector");
			ht.add(c+"add Spawner <name> : "+r+"New spawner");
			ht.add(c+"add Storage <name> : "+r+"New storage block");
			ht.add(c+"add Timer <name> <delay> : "+r+"New Timer");
			ht.add(c+"add Torch <name> <TorchBlock> : "+r+"New Torch to TorchBlock");
			ht.add(c+"add TorchBlock <name> : "+r+"New TorchBlock");
			ht.add(c+"create <dungeon> : "+r+"New dungeon");
			ht.add(c+"delete <block> : "+r+"Deletes block");
			ht.add(c+"deleteDungeon <dungeon> :"+r+"Deletes dungeon");
			ht.add(c+"disable [dungeon] : "+r+"Disable dungeon");
			ht.add(c+"edit <block> <key> <value> : "+r+"Edit a block");
			ht.add(c+"enable [dungeon] : "+r+"Enable dungeon");
			ht.add(c+"expand : "+r+"expand dungeon to WorldEdit selection");
			ht.add(c+"insertMob <spawner> <mob> [count] : "+r+"Put in spawner");
			ht.add(c+"link <from> <to> [TRIGGER|set|reset|on|off] : "+r+"Link blocks");
			ht.add(c+"list: "+r+"List existing dungeons");
			ht.add(c+"listMobs: "+r+"List available mobs");
			ht.add(c+"m <tune> : "+r+"see playSound");
			ht.add(c+"mobInfo: "+r+"Show info about mob");
			ht.add(c+"playsound <tune> : "+r+"plays a written tune.");
			ht.add(c+"ps <tune> : "+r+"see playSound");
			ht.add(c+"savemob <name> : "+r+"Save closest mob as <name>");
			ht.add(c+"save : "+r+"Save current dungeon");
			ht.add(c+"select <dungeon> : "+r+"Select dungeon to edit");
			ht.add(c+"setworld : "+r+"adds/remove current world to/from plugin");
			ht.add(c+"show [block] : "+r+"Details dungeon or block");
			ht.add(c+"spawn: "+r+"Spawn mob");
			ht.add(c+"unlink <block>: "+r+"remove links to and from a block");
			ht.add(c+"unlink <from> <to>: "+r+"remove a specific link");
						
			page = (page < 1 || page > 4) ? 1 : page;
			int firsthelp = 9*(page-1);
			for (int i = 0; i < 9 && i < ht.size(); i++) {
				say(ht.get(firsthelp+i));
			}
			return true;
		}
		
		if (last_index >= 1 && args[0].equals("?")) {
			int tutpage=1;
			final int SIZE=4;
			if (args.length >= 2) {
				try { tutpage = Integer.parseInt(args[1]); } catch (Exception e) { tutpage = 1; }
				tutpage = (tutpage < 1 || tutpage > SIZE) ? 1 : tutpage;
			}
			
			String title = ChatColor.GREEN + "-------";
			title += ChatColor.RESET + " LegendCraft Tutorial ("+tutpage+"/"+SIZE+") ";
			title += ChatColor.GREEN + "----------------------------------------";
			title = title.substring(0,54);
			say(title);
			
			//TODO: finish tutorial
			/*
			 * detector
			 * lc enable/disable
			 */
			switch (tutpage) {
			case 2:
				say(b + "Getting Started:");
				say(b + "dis/enable the plugin for worlds:");
				say(b + "  /lc addworld");
				say(b + "  /lc removeworld");
				say(b + "Use WorldEdit wand to select a big 3D area.");
				say(b + "When ready, create the dunegeon:");
				say(b + "  /lc create <name>");
			    break;
			case 3:
				say(b + "See & Choose:");
				say(b + "To see existing dungeons:");
				say(b + "  /lc list");
				say(b + "To select a dungeon to edit:");
				say(b + "  /lc select <dungeon>");
				say(b + "To see info about a dungeon:");
				say(b + "  /lc info");
			    break;     
			case 1:
		    default:
		    	say(b + "Welcome to LegendCraft!");
		    	say(b + "LegendCraft lets you create dungeons.");
		    	say(b + "Make your server into a Zelda map!");
		    	say(b + "Make challenges for friends!");
		    	say(b + "Set traps and build puzzles!");
		    	say(b + "Create spawners and bosses!");
		    	say(b + "Use \"/lc ? 2\" to go to learn how.");
		    	break;
			}
		}
		
		if (last_index >= 0 && args[0].toLowerCase().equals("test")) {
			player.sendMessage("hi");
		}
		
		if (last_index >= 2 && args[0].toLowerCase().equals("add")) {
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
				if (!dungeons.get(dungeonStr).tryAddStorage(player, args[2]))
					say(red + "Storage block creation failed.");
			} else if (addType.equals("storageframe") || addType.equals("frame") || addType.equals("sf")) {
				if (last_index < 2)
					say(red + "Invalid arguments. Usage: /lc add storageframe <storage_name> [ticks] [before_frame]");
				else if (last_index == 2) {
					if (!dungeons.get(dungeonStr).tryAddStorageFrame(player, args[2]))
						say(red + "Storage frame addition failed.");
				}
				else if (last_index == 3) {
					int ticks = -1;
					try { 
						ticks = Integer.parseInt(args[3]); 
				    } catch (Exception e) { 
				    	say(red + args[3] +" is not a valid number of ticks.");
				    }
					if (!dungeons.get(dungeonStr).tryAddStorageFrame(player, args[2], ticks))
						say(red + "Storage frame addition failed.");
				}
				else if (last_index == 4) {
					int ticks = -1;
					int beforeFrame = -1;
					try { 
						ticks = Integer.parseInt(args[3]); 
						beforeFrame = Integer.parseInt(args[4]); 
				    } catch (Exception e) { 
				    	say(red + "Storage frame addition failed.");
				    }
					if (!dungeons.get(dungeonStr).tryAddStorageFrame(player, args[2], ticks, beforeFrame))
						say(red + "Storage frame addition failed.");
				}
				else
					say(red + "Invalid arguments. Usage: /lc add storageframe <storage_name> [ticks] [before_frame]");
			/**
			 * Add Timer
			 * - attempts to create a timer via looked-at IDC
			 */
				//TODO: catch the lack of args[3]
			} else if (addType.equals("timer") || addType.equals("time") || addType.equals("tim")) {
				if (last_index < 3)
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
			
			String c = ChatColor.DARK_GRAY + "/lc ";
			String r = ChatColor.RESET + "";
			say(red+ "No block specified.");
			/*
			 * DELETEME - autocomplete is fine
			say(c+"add Chest <name> : "+r+"New chest");
			say(c+"add Detector <name> : "+r+"New detector");
			say(c+"add Door <name> : "+r+"New door");
			say(c+"add Music <name> : "+r+"Music block from last played tune");
			say(c+"add RSDetector <name> : "+r+"New Redstone Detector");
			say(c+"add Spawner <name> : "+r+"New spawner");
			say(c+"add Storage <name> : "+r+"New storage block");
			say(c+"add Timer <name> <delay> : "+r+"New Timer");
			say(c+"add Torch <name> <TorchBlock> : "+r+"New Torch to TorchBlock");
			say(c+"add TorchBlock <name> : "+r+"New TorchBlock");
			*/
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
		if(last_index >= 1 && (args[0].toLowerCase().equals("create") || args[0].toLowerCase().equals("cd"))) {
			if (!dungeons.containsKey(args[1])) {
				Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
				if (sel instanceof CuboidSelection) {
			        Vector min = sel.getNativeMinimumPoint();
			        Vector max = sel.getNativeMaximumPoint();
			        
			        new Dungeon(args[1], sel.getWorld(), min, max);
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
		if (last_index >= 1 && args[0].toLowerCase().equals("delete")) {
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
		if (last_index >= 1 && args[0].toLowerCase().equals("deletedungeon")) {	
			if (dungeons.containsKey(args[1])) {
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
				} else
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
		if (last_index >= 1 && args[0].toLowerCase().equals("enable")) {
			if (dungeons.containsKey(args[1])) {
				dungeons.get(args[1]).setEnabled(true);
				say(purp + "Dungeon " + args[1] + " enabled.");
			} else
				say(red + "Dungeon with this name not found.");
		} else if (last_index >= 1 && args[0].toLowerCase().equals("disable")) {
			if (dungeons.containsKey(args[1])) {
				dungeons.get(args[1]).setEnabled(false);
				say(purp + "Dungeon " + args[1] + " disabled.");
			} else
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
		if (last_index >=3 && args[0].toLowerCase().equals("edit")) {
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
		if(args[0].toLowerCase().equals("expand")) {
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
		if ( last_index >= 2 && args[0].toLowerCase().equals("insertmob")) {
			int count = 1;
			if (last_index >= 3) {
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
		if (last_index >= 2 && args[0].toLowerCase().equals("link")) {
			String type = "trigger";
			if (last_index >= 3 && 
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
				if (!Link.valid(type) || !dungeons.get(dungeonStr).tryLink(player, args[1], args[2], Link.get(type.toUpperCase())))
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
		if(last_index >= 0 && args[0].toLowerCase().equals("list")) {
			say("Legendcraft Dungeons:");
			for (String s : dungeons.keySet())
				say(" " + s);
		}
		
		
		/**
		 * List Mobs
		 * - Lists available mobs
		 * --- /lc listmobs
		 */
		if(last_index >= 0 && args[0].toLowerCase().equals("listmobs")) {
			say(purp + "Available mobs:");
			String mList = "";
			for (String s : mobs.keySet())
				mList = mList + " " + s;
			say(purp + mList);
		}
		
		if(last_index >= 0 && args[0].toLowerCase().equals("compass")) 
			Tools.playerCompassFace(player);
		
		/**
		 * Mob Info
		 * - Lists all* data about nearest mob
		 * * CustomName will be added later
		 * --- /lc mobinfo
		 */
		//TODO: Add CustomName
		//TODO: Add some color to the mobinfo command, and test string converts
		if (last_index >= 0 && args[0].toLowerCase().equals("mobinfo"))
		{
			LivingEntity closestEntity = Tools.getClosestMob(player);
			if (closestEntity != null) {
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
							say(purp + "   " + Tools.standardEnchantName(e.getName()) + ": " + helmEnch.get(e).toString()); 
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
		if (last_index >= 1 && (args[0].toLowerCase().equals("ps") || args[0].toLowerCase().equals("m") || 
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
		if (last_index >= 1 && args[0].toLowerCase().equals("savemob"))
		{
			if (!mobs.containsKey(args[1])) {
				LivingEntity closestEntity = Tools.getClosestMob(player);
				if (closestEntity != null) {
					mobs.put(args[1], new MobTemplate(closestEntity,args[1]));
					say(purp + "Saved " + closestEntity.getType().toString().toLowerCase() + " " + args[1] + ".");
				} else
					say(red + "No mob found.");		
			} else {
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
		if(last_index >= 1 && args[0].toLowerCase().equals("scale")) {
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
		        
		        if (scale < 2) {
		        	say(red + "Invalid scale!");
					return false;
		        }
		        
		        if (scale*(maxX-minX+1) > 750) {
		        	say(red + "X scale must be less than 750.");
					return false;
		        }
		        if (scale*(maxY-minY+1) > 750) {
		        	say(red + "Y scale must be less than 750.");
					return false;
		        }
		        if (scale*(maxZ-minZ+1) > 750) {
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
		        
		        if (limit1 == 1) {
		        	Tools.scale(min,max,anchor,scale);
		        } else {
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
		if(last_index >= 1 && args[0].toLowerCase().equals("select")) {
			
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
		if(last_index >= 0 && args[0].toLowerCase().equals("addworld")) {
			
			String worldName;
			UUID worldUUID;
			World world;
			if (last_index >= 1) {
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
		if(last_index >= 0 && args[0].toLowerCase().equals("removeworld")) {
			String worldName;
			UUID worldUUID;
			World world;
			if (last_index >= 1) {
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
				if (last_index >= 1) {
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
		if(last_index >= 1 && args[0].toLowerCase().equals("spawn"))
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
		if (last_index >= 1 && args[0].toLowerCase().equals("unlink"))
		{
			if (!selectedDungeons.containsKey(player))
				say(red + "No dungeon selected. Select one using\n /lc select <dungeon>");
			else {
				if (last_index >= 2)
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
		if(last_index >= 1 && args[0].toLowerCase().equals("myhealth"))
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
