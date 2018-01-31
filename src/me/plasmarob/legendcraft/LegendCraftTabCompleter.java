package me.plasmarob.legendcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class LegendCraftTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			List<String> autoCompleteList = new ArrayList<String>();
			//alias for /lc add|edit
			if (commandLabel.equals("lca") || commandLabel.equals("lce")) {
				String[] argsOld = args.clone();
				args = new String[argsOld.length+1];
				args[0] = (commandLabel.equals("lca")) ? "add" : "edit";
				for (int i=0; i < argsOld.length; i++) {
					args[i+1] = argsOld[i];
				}
			}
			
			// core commands
			if (args.length==1) {
				final String[] COMMANDS = { "addworld", "removeworld", "add","edit","info","create","delete","deleteDungeon", "disable", 
						                  "enable", "disable",  "expand", "insertMob", "link", "list", "listMobs", "mobinfo", 
						                  "playsound", "save", "saveMob", "select", "setWorld", "show", "spawn", "unlink" };
				autoCompleteList = new ArrayList<String>(COMMANDS.length);
				StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), autoCompleteList);
			}
			// block add/edit
			if (args[0].equals("add") || args[0].equals("edit")) {
				final String[] BLOCKS = { "Chest","Detector","Door","Music","RSDetector","Spawner","Storage", "Timer", "Torch", "TorchBlock" };
				autoCompleteList = new ArrayList<String>(BLOCKS.length);
				StringUtil.copyPartialMatches(args[1], Arrays.asList(BLOCKS), autoCompleteList);
			}
			// dungeon names
			if (args[0].equals("select")) {
				Set<String> DUNGEONS = Dungeon.getDungeons().keySet();
				autoCompleteList = new ArrayList<String>(DUNGEONS.size());
				StringUtil.copyPartialMatches(args[1], DUNGEONS, autoCompleteList);
			}
			/*
			 * TODO:
			 * - blockname
			 * - mob name
			 * - world
			 */
			
			Collections.sort(autoCompleteList);
			return autoCompleteList;
		}
		return null;
	}

}
