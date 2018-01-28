package me.plasmarob.legendcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
			
			if (args[0].equals("add") || args[0].equals("edit")) {
				final String[] BLOCKS = { "Chest","Detector","Door","Music","RSDetector","Spawner","Storage", "Timer", "Torch", "TorchBlock" };
				autoCompleteList = new ArrayList<String>(BLOCKS.length);
				StringUtil.copyPartialMatches(args[1], Arrays.asList(BLOCKS), autoCompleteList);
			}
			
			Collections.sort(autoCompleteList);
			return autoCompleteList;
		}
		return null;
	}

}
